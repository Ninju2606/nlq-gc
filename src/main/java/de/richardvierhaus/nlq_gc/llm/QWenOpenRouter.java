package de.richardvierhaus.nlq_gc.llm;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class QWenOpenRouter extends LanguageModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(QWenOpenRouter.class);
    private static final String API_KEY = System.getenv("OPENROUTER_API_KEY");

    private final String model;

    private final Map<String, String> transactions = new HashMap<>();

    protected QWenOpenRouter(final String model) {
        this.model = model;
    }

    @Override
    public String handlePrompt(final String prompt) {
        String jsonBody = String.format("""
                {
                  "model": "%s",
                  "messages": [
                    {
                      "role": "user",
                      "content": "%s /no_think"
                    }
                  ]
                }
                """, model, URLEncoder.encode(prompt, StandardCharsets.UTF_8));

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://openrouter.ai/api/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // run http request
            long start = System.currentTimeMillis();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            LOGGER.debug("Handling prompt took {} ms", System.currentTimeMillis() - start);

            // collect response content
            JsonElement responseJSON = JsonParser.parseString(response.body());
            String responseParsed = responseJSON.getAsJsonObject().getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();
            responseParsed = responseParsed.replace("```json", "").replace("```", "");
            LOGGER.debug(responseParsed);

            String transactionID = UUID.randomUUID().toString();
            transactions.put(transactionID, responseParsed);
            return transactionID;
        } catch (Exception e) {
            throw new RuntimeException("Error in handlePrompt: " + e.getMessage(), e);
        }
    }

    @Override
    public String getResponse(final String transactionId) {
        return transactions.remove(transactionId);
    }

}