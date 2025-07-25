package de.richardvierhaus.nlq_gc.nlq;

import de.richardvierhaus.nlq_gc.GraphCode;
import de.richardvierhaus.nlq_gc.enums.ModelLiterals;
import de.richardvierhaus.nlq_gc.enums.PromptGraphCode;
import de.richardvierhaus.nlq_gc.enums.PromptKeyword;
import de.richardvierhaus.nlq_gc.llm.LanguageModel;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("")
public class NLQController {

    private final NLQService service;

    public NLQController(final NLQService nlqService) {
        this.service = nlqService;
    }

    /**
     * Takes a NLQ, parses the parameters and passes it over to build the required prompts.
     *
     * @param query
     *         The NLQ.
     * @param user
     *         The identification of the user.
     * @param promptKeyword
     *         The key of the prompt for keyword extraction. If <code>null</code>, a default is taken.
     * @param promptGC
     *         The key of the prompt for graph code generation. If <code>null</code>, a default is taken.
     * @param model
     *         The key of the LLM. If <code>null</code>, a default is taken.
     * @return A transactionId which can be used to poll the resulting graph code.
     */
    @PostMapping("/handleNLQ")
    public String handleNLQ(@RequestParam final String query, @RequestParam final String user,
                            @RequestParam final String promptKeyword, @RequestParam final String promptGC,
                            @RequestParam final String model) {
        if (!StringUtils.hasText(query))
            throw new UnsupportedOperationException("No query has been provided.");
        if (!StringUtils.hasText(user))
            throw new UnsupportedOperationException("No user identification has been provided.");

        // Preparation of the graph code prompt
        PromptGraphCode gcPrompt;
        try {
            gcPrompt = PromptGraphCode.valueOf(promptGC);
        } catch (IllegalArgumentException e) {
            gcPrompt = PromptGraphCode.getDefault();
        }

        // Preparation of the keyword prompt
        PromptKeyword keywordPrompt = null;
        if (gcPrompt.requiresKeywords()) {
            try {
                keywordPrompt = PromptKeyword.valueOf(promptKeyword);
            } catch (IllegalArgumentException e) {
                keywordPrompt = PromptKeyword.getDefault();
            }
        }

        // Preparation of the llm
        LanguageModel llm;
        try {
            ModelLiterals modelLiteral = ModelLiterals.valueOf(model);
            llm = modelLiteral.getLLM();
        } catch (IllegalArgumentException e) {
            llm = ModelLiterals.getDefault();
        }

        return service.handleNLQ(query, user, keywordPrompt, gcPrompt, llm);
    }

    /**
     * Checks whether the execution of the prompt with the given transactionId is finished and gives the resulting graph
     * code.
     *
     * @param transactionId
     *         The transactions' id.
     * @return The graph code.
     */
    @GetMapping("/graphCode")
    public GraphCode getGraphCode(@RequestParam final String transactionId) {
        if (!StringUtils.hasText(transactionId))
            throw new UnsupportedOperationException("No transactionId has been provided.");
        return service.getGraphCode(transactionId);
    }

}