package de.richardvierhaus.nlq_gc.llm;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class QWen extends LanguageModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(QWen.class);

    private static QWen INSTANCE;

    private final String handleUrl = getLLMProperties().getProperty("QWEN_HANDLE");
    private final String responseUrl = getLLMProperties().getProperty("QWEN_RESPONSE");
    private final Gson gson = new Gson();

    private QWen() {
    }

    public static QWen getInstance() {
        if (INSTANCE == null) INSTANCE = new QWen();
        return INSTANCE;
    }

    // Not necessary in production. Only for testing purposes
    public static void setInstance(final QWen instance) {
        if (INSTANCE == null) INSTANCE = instance;
    }

    @Override
    public String handlePrompt(final String prompt) {
        try {
            LOGGER.trace("Transmitting following prompt to QWen:\n{}", prompt);

            HttpURLConnection conn = getHttpURLConnection(handleUrl);

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("prompt", prompt);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = gson.toJson(requestJson).getBytes(StandardCharsets.UTF_8);
                os.write(input);
            }

            if (conn.getResponseCode() != 200) {
                throw new IOException("HTTP POST failed with code " + conn.getResponseCode());
            }

            String response = readResponse(conn);
            LOGGER.info("Received response to /handle: {}", response);
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            return jsonResponse.get("transaction_id").getAsString();

        } catch (Exception e) {
            throw new RuntimeException("Error in handlePrompt: " + e.getMessage(), e);
        }
    }

    @Override
    public String getResponse(final String transactionId) {
        try {
            HttpURLConnection conn = getHttpURLConnection(responseUrl + "?transaction_id=" + URLEncoder.encode(transactionId, StandardCharsets.UTF_8));

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() != 200) {
                throw new IOException("HTTP GET failed with code " + conn.getResponseCode());
            }

            String response = readResponse(conn);
            LOGGER.info("Received response to /response: {}", response);
            JsonObject jsonResponse = gson.fromJson(response, JsonObject.class);
            return jsonResponse.get("response").isJsonNull() ? null : jsonResponse.get("response").getAsString();

        } catch (Exception e) {
            throw new RuntimeException("Error in getResponse: " + e.getMessage(), e);
        }
    }

    /**
     * Provides a {@link HttpURLConnection} for a given url.
     *
     * @param url
     *         The requested url route.
     * @return A {@link HttpURLConnection} for the given url.
     * @throws IOException
     *         if an I/O exception occurs during connection opening.
     */
    protected HttpURLConnection getHttpURLConnection(final String url) throws IOException {
        return (HttpURLConnection) URI.create(url).toURL().openConnection();
    }

    /**
     * Reads the response of a {@link HttpURLConnection} and returns it as String.
     *
     * @param conn
     *         The {@link HttpURLConnection}.
     * @return The extracted text.
     * @throws IOException
     *         in case the connection cannot be read.
     */
    private String readResponse(final HttpURLConnection conn) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line.trim());
            }
            return result.toString();
        }
    }

}