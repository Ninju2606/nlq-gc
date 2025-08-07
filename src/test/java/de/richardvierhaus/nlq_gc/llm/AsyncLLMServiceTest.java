package de.richardvierhaus.nlq_gc.llm;

import com.google.gson.Gson;
import de.richardvierhaus.nlq_gc.GraphCode;
import de.richardvierhaus.nlq_gc.KeywordResponse;
import de.richardvierhaus.nlq_gc.enums.ModelLiterals;
import de.richardvierhaus.nlq_gc.enums.Replacement;
import de.richardvierhaus.nlq_gc.enums.State;
import de.richardvierhaus.nlq_gc.nlq.PromptBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

public class AsyncLLMServiceTest {

    private static QWen qwen;
    private static AsyncLLMService service;

    private static final Gson gson = new Gson();

    private PromptBuilder gcBuilder;

    @BeforeAll
    public static void init() {
        service = spy(AsyncLLMService.class);
        service.init();

        qwen = mock(QWen.class);
        QWen.setInstance(qwen);
    }

    @AfterAll
    public static void shutdown() {
        service.shutdown();
    }

    @BeforeEach
    public void initRun() {
        Mockito.reset(service, qwen);

        gcBuilder = mock(PromptBuilder.class);
        doReturn("GCPROMPT").when(gcBuilder).toString();
        doReturn(List.of(Replacement.KEYWORDS)).when(gcBuilder).getLeftoverReplacements();

        doReturn("KEYWORD-TRANSACTION").when(qwen).handlePrompt("KEYWORDPROMPT");
        doReturn("GC-TRANSACTION").when(qwen).handlePrompt("GCPROMPT");
    }

    @Test
    public void testSuccess() throws InterruptedException {
        assertThat(service.getGraphCode("ABC")).isEqualTo(GraphCode.getNotAvailable());

        KeywordResponse keywordResponse = new KeywordResponse(List.of("KEY1", "KEY2"), null, null);

        doReturn(gson.toJson(keywordResponse)).when(qwen).getResponse("KEYWORD-TRANSACTION");
        doReturn("{\"dictionary\":[\"Key1\", \"Key2\"], \"matrix\":[[1,2],[3,4]], \"description\":\"TEST\"}").when(qwen).getResponse("GC-TRANSACTION");

        String transactionId1 = service.addKeywordPrompt("KEYWORDPROMPT", ModelLiterals.QWEN3_1_7_B, gcBuilder);
        String transactionId2 = service.addGCPrompt("GCPROMPT", ModelLiterals.QWEN3_1_7_B);

        GraphCode gc1 = service.getGraphCode(transactionId1);
        assertThat(gc1.getState()).isEqualTo(State.PENDING);
        assertThat(gc1.getModel()).isEqualTo(ModelLiterals.QWEN3_1_7_B);

        // wait at least one scheduler loop
        Thread.sleep(4000);

        gc1 = service.getGraphCode(transactionId1);
        GraphCode gc2 = service.getGraphCode(transactionId2);
        int[][] matrix = {{1, 2}, {3, 4}};
        assertThat(gc1.getState()).isEqualTo(gc2.getState()).isEqualTo(State.FINISHED);
        assertThat(gc1.getMatrix()).isEqualTo(gc2.getMatrix()).isEqualTo(matrix);
        assertThat(gc1.getDictionary()).isEqualTo(gc2.getDictionary()).containsExactly("Key1", "Key2");
        assertThat(gc1.getDescription()).isEqualTo(gc2.getDescription()).isEqualTo("TEST");

        assertThat(service.getGraphCode(transactionId1)).isSameAs(service.getGraphCode(transactionId2))
                .isSameAs(GraphCode.getNotAvailable());
    }

    @Test
    public void testFail() throws InterruptedException {
        KeywordResponse keywordResponse = new KeywordResponse(null, "ERROR1", "DESCRIPTION1");
        doReturn(gson.toJson(keywordResponse)).when(qwen).getResponse("KEYWORD-TRANSACTION");
        doReturn("{\"error\":\"ERROR2\",\"description\":\"DESCRIPTION2\"}").when(qwen).getResponse("GC-TRANSACTION");

        String transactionId1 = service.addKeywordPrompt("KEYWORDPROMPT", ModelLiterals.QWEN3_1_7_B, gcBuilder);
        String transactionId2 = service.addGCPrompt("GCPROMPT", ModelLiterals.QWEN3_1_7_B);

        // wait at least one scheduler loop
        Thread.sleep(4000);

        final GraphCode gc1 = service.getGraphCode(transactionId1);
        final GraphCode gc2 = service.getGraphCode(transactionId2);
        assertThat(gc1.getState()).isEqualTo(gc2.getState()).isEqualTo(State.ERROR);
        assertThat(gc1.getError()).isEqualTo("ERROR1");
        assertThat(gc1.getDescription()).isEqualTo("DESCRIPTION1");
        assertThat(gc2.getError()).isEqualTo("ERROR2");
        assertThat(gc2.getDescription()).isEqualTo("DESCRIPTION2");

    }

    @Test
    public void testTimeout() throws InterruptedException {
        doReturn(0).when(service).getTimeout();

        String transactionId = service.addKeywordPrompt("KEYWORDPROMPT", ModelLiterals.QWEN3_1_7_B, gcBuilder);

        // wait at least one scheduler loop
        Thread.sleep(4000);

        assertThat(service.getGraphCode(transactionId)).isSameAs(GraphCode.getNotAvailable());
    }

    @Test
    public void testException() {
        doReturn(List.of(Replacement.USER, Replacement.QUERY)).when(gcBuilder).getLeftoverReplacements();
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> service.addKeywordPrompt("", null, gcBuilder));

        doReturn(List.of(Replacement.USER)).when(gcBuilder).getLeftoverReplacements();
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> service.addKeywordPrompt("", null, gcBuilder));
    }

}