package de.richardvierhaus.nlq_gc.llm;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class QWenTest {

    private QWen qwen;

    @BeforeEach
    public void init() {
        qwen = Mockito.spy(QWen.class);
    }

    @Test
    public void testHandlePrompt() throws IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        InputStream inputStream = new ByteArrayInputStream("{\"transaction_id\":\"RESPONSE\"}".getBytes());
        OutputStream os = mock(OutputStream.class);
        doReturn(os).when(conn).getOutputStream();
        doReturn(conn).when(qwen).getHttpURLConnection(any());
        doReturn(inputStream).when(conn).getInputStream();
        doReturn(200).when(conn).getResponseCode();

        assertThat(qwen.handlePrompt("PROMPT")).isEqualTo("RESPONSE");

        verify(qwen).getHttpURLConnection("http://127.0.0.1:8000/handle");
        verify(conn).setRequestMethod("POST");
        verify(conn).setRequestProperty("Content-Type", "application/json");
        verify(conn).setDoOutput(true);
        verify(os).write("{\"prompt\":\"PROMPT\"}".getBytes());

        // error
        doReturn(400).when(conn).getResponseCode();
        try {
            qwen.handlePrompt("PROMPT");
            Assertions.fail("Expected exception did not throw");
        } catch (Exception e) {
            assertThat(e).hasCauseExactlyInstanceOf(IOException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("HTTP POST failed with code 400");
        }
    }

    @Test
    public void testGetResponse() throws IOException {
        HttpURLConnection conn = mock(HttpURLConnection.class);
        InputStream inputStream = new ByteArrayInputStream("{\"response\":\"RESPONSE\"}".getBytes());
        doReturn(conn).when(qwen).getHttpURLConnection(any());
        doReturn(200).when(conn).getResponseCode();
        doReturn(inputStream).when(conn).getInputStream();

        assertThat(qwen.getResponse("TRANSACTION")).isEqualTo("RESPONSE");
        verify(qwen).getHttpURLConnection("http://127.0.0.1:8000/response?transaction_id=TRANSACTION");
        verify(conn).setRequestMethod("GET");
        verify(conn).setRequestProperty("Accept", "application/json");

        // Response null
        inputStream = new ByteArrayInputStream("{\"response\":null}".getBytes());
        doReturn(inputStream).when(conn).getInputStream();
        assertThat(qwen.getResponse("TRANSACTION")).isNull();

        // error
        doReturn(400).when(conn).getResponseCode();
        try {
            qwen.getResponse("TRANSACTION");
            Assertions.fail("Expected exception did not throw");
        } catch (Exception e) {
            assertThat(e).hasCauseExactlyInstanceOf(IOException.class);
            assertThat(e.getCause().getMessage()).isEqualTo("HTTP GET failed with code 400");
        }
    }

}