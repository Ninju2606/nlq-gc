package de.richardvierhaus.nlq_gc.nlq;

import de.richardvierhaus.nlq_gc.GraphCode;
import de.richardvierhaus.nlq_gc.enums.PromptGraphCode;
import de.richardvierhaus.nlq_gc.enums.PromptKeyword;
import de.richardvierhaus.nlq_gc.enums.Replacement;
import de.richardvierhaus.nlq_gc.llm.AsyncLLMService;
import de.richardvierhaus.nlq_gc.llm.LanguageModel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;

@Service
public class NLQService {

    private final AsyncLLMService llmService;

    public NLQService() {
        llmService = AsyncLLMService.getInstance();
    }

    @PostConstruct
    public void init() {
        llmService.init();
    }

    @PreDestroy
    public void destroy() {
        llmService.shutdown();
    }

    /**
     * Takes a NLQ, builds the prompts and calls the LLM to execute them. A transactionId is generated, stored and
     * returned to manage the NLQs session.
     *
     * @param query
     *         The NLQ.
     * @param user
     *         The identification of the user.
     * @param promptKeyword
     *         The {@link PromptKeyword} to be used.
     * @param promptGraphCode
     *         The {@link PromptGraphCode} to be used.
     * @param llm
     *         The {@link LanguageModel} to be used.
     * @return A transactionId which can be used to poll the resulting graph code.
     */
    protected String handleNLQ(final String query, final String user, final PromptKeyword promptKeyword,
                               final PromptGraphCode promptGraphCode, final LanguageModel llm) {
        if (promptGraphCode.getRequiredReplacements().contains(Replacement.KEYWORDS) && promptKeyword == null)
            throw new UnsupportedOperationException("The given GC prompt requires an keyword prompt.");

        PromptBuilder promptBuilderGC = new PromptBuilder(promptGraphCode);
        promptBuilderGC.replaceIfRequired(Replacement.QUERY, query)
                .replaceIfRequired(Replacement.USER, user)
                .replaceIfRequired(Replacement.ENCODING, "TODO"); // TODO

        if (promptGraphCode.getRequiredReplacements().contains(Replacement.KEYWORDS)) {
            PromptBuilder promptBuilderKeyword = new PromptBuilder(promptKeyword);
            promptBuilderKeyword.replaceIfRequired(Replacement.QUERY, query)
                    .replaceIfRequired(Replacement.USER, user)
                    .replaceIfRequired(Replacement.ENCODING, "TODO"); // TODO

            return llmService.addKeywordPrompt(promptBuilderKeyword.toString(), llm, promptBuilderGC);
        }
        return llmService.addGCPrompt(promptBuilderGC.toString(), llm);
    }

    /**
     * Looks up the {@link GraphCode} corresponding to the given transactionId.
     *
     * @param transactionId
     *         The transactions id.
     * @return The {@link GraphCode} behind the transactionId.
     */
    protected GraphCode getGraphCode(final String transactionId) {
        return llmService.getGraphCode(transactionId);
    }

}