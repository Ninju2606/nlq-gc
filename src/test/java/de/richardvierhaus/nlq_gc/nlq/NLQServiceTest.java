package de.richardvierhaus.nlq_gc.nlq;

import de.richardvierhaus.nlq_gc.encoding.EncodingService;
import de.richardvierhaus.nlq_gc.enums.ModelLiterals;
import de.richardvierhaus.nlq_gc.enums.PromptGraphCode;
import de.richardvierhaus.nlq_gc.enums.PromptKeyword;
import de.richardvierhaus.nlq_gc.enums.Replacement;
import de.richardvierhaus.nlq_gc.llm.AsyncLLMService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

public class NLQServiceTest {

    private MockedStatic<EncodingService> mockedStaticEncoding;
    private MockedStatic<AsyncLLMService> mockedStaticLLM;

    private NLQService service;
    private AsyncLLMService llmService;
    private EncodingService encodingService;

    @BeforeEach
    public void init() {
        mockedStaticLLM = Mockito.mockStatic(AsyncLLMService.class);
        llmService = Mockito.mock(AsyncLLMService.class);
        mockedStaticLLM.when(AsyncLLMService::getInstance).thenReturn(llmService);

        mockedStaticEncoding = Mockito.mockStatic(EncodingService.class);
        encodingService = Mockito.mock(EncodingService.class);
        mockedStaticEncoding.when(EncodingService::getInstance).thenReturn(encodingService);

        doReturn("ENCODING123").when(encodingService).getEncodingMappingsAsString();

        service = Mockito.spy(new NLQService());
    }

    @AfterEach
    void tearDown() {
        mockedStaticLLM.close();
        mockedStaticEncoding.close();
    }

    @Test
    public void testInit() {
        service.init();
        verify(llmService).init();
    }

    @Test
    public void testDestroy() {
        service.destroy();
        verify(llmService).shutdown();
    }

    @Test
    public void testHandleNLQ() {
        // only gc prompt
        service.handleNLQ("QUERY123", "USER123", null, PromptGraphCode.NO_KEYWORDS_01_ZS, ModelLiterals.getDefault());

        ArgumentCaptor<String> promptCaptor = ArgumentCaptor.forClass(String.class);
        verify(llmService).addGCPrompt(promptCaptor.capture(), eq(ModelLiterals.getDefault()));
        assertThat(promptCaptor.getValue()).contains("QUERY123").contains("USER123").contains("ENCODING123");

        // with keywords
        service.handleNLQ("QUERY123", "USER123", PromptKeyword.KEYWORDS_01_ZS, PromptGraphCode.WITH_KEYWORDS_01_ZS, ModelLiterals.getDefault());

        ArgumentCaptor<PromptBuilder> promptBuilderCaptor = ArgumentCaptor.forClass(PromptBuilder.class);
        verify(llmService).addKeywordPrompt(promptCaptor.capture(), eq(ModelLiterals.getDefault()), promptBuilderCaptor.capture());
        assertThat(promptCaptor.getValue()).contains("QUERY123").contains("USER123").doesNotContain("ENCODING123");
        assertThat(promptBuilderCaptor.getValue().getLeftoverReplacements()).containsExactly(Replacement.KEYWORDS);
    }

    @Test
    public void testGetGraphCode() {
        service.getGraphCode("TRANSACTION");
        verify(llmService).getGraphCode("TRANSACTION");
    }

}