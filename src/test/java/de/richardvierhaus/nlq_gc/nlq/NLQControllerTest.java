package de.richardvierhaus.nlq_gc.nlq;

import de.richardvierhaus.nlq_gc.enums.ModelLiterals;
import de.richardvierhaus.nlq_gc.enums.PromptGraphCode;
import de.richardvierhaus.nlq_gc.enums.PromptKeyword;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class NLQControllerTest {

    private NLQController controller;
    private NLQService service;

    @BeforeEach
    public void init() {
        service = Mockito.mock(NLQService.class);
        controller = Mockito.spy(new NLQController(service));
    }

    @Test
    public void testHandleNLQ() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> controller.handleNLQ(null, null, Optional.empty(), Optional.empty(), Optional.empty()));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> controller.handleNLQ("Test", null, Optional.empty(), Optional.empty(), Optional.empty()));

        controller.handleNLQ("QUERY", "USER", Optional.empty(), Optional.empty(), Optional.empty());
        verify(service).handleNLQ("QUERY", "USER", null, PromptGraphCode.getDefault(), ModelLiterals.getDefault());

        controller.handleNLQ("QUERY", "USER", Optional.empty(), Optional.of("WITH_KEYWORDS_01_FS"), Optional.of("QWEN3_1_7_B"));
        verify(service).handleNLQ("QUERY", "USER", PromptKeyword.getDefault(), PromptGraphCode.WITH_KEYWORDS_01_FS, ModelLiterals.QWEN3_1_7_B);
    }

    @Test
    public void testGetGraphCode() {
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> controller.getGraphCode(null));

        controller.getGraphCode("TRANSACTION");
        verify(service).getGraphCode("TRANSACTION");
    }

}