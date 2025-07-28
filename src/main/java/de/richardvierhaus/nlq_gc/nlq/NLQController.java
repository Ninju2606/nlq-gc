package de.richardvierhaus.nlq_gc.nlq;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.richardvierhaus.nlq_gc.GraphCode;
import de.richardvierhaus.nlq_gc.enums.ModelLiterals;
import de.richardvierhaus.nlq_gc.enums.PromptGraphCode;
import de.richardvierhaus.nlq_gc.enums.PromptKeyword;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("")
public class NLQController {

    private final NLQService service;
    private final Gson gson;

    public NLQController(final NLQService nlqService) {
        this.service = nlqService;
        this.gson = new Gson();
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
                            @RequestParam final Optional<String> promptKeyword,
                            @RequestParam final Optional<String> promptGC,
                            @RequestParam final Optional<String> model) {
        if (!StringUtils.hasText(query))
            throw new UnsupportedOperationException("No query has been provided.");
        if (!StringUtils.hasText(user))
            throw new UnsupportedOperationException("No user identification has been provided.");

        // Preparation of the graph code prompt
        PromptGraphCode gcPrompt;
        try {
            gcPrompt = PromptGraphCode.valueOf(promptGC.orElse(null));
        } catch (IllegalArgumentException | NullPointerException e) {
            gcPrompt = PromptGraphCode.getDefault();
        }

        // Preparation of the keyword prompt
        PromptKeyword keywordPrompt = null;
        if (gcPrompt.requiresKeywords()) {
            try {
                keywordPrompt = PromptKeyword.valueOf(promptKeyword.orElse(null));
            } catch (IllegalArgumentException | NullPointerException e) {
                keywordPrompt = PromptKeyword.getDefault();
            }
        }

        // Preparation of the llm
        ModelLiterals modelLiteral;
        try {
            modelLiteral = ModelLiterals.valueOf(model.orElse(null));
        } catch (IllegalArgumentException | NullPointerException e) {
            modelLiteral = ModelLiterals.getDefault();
        }

        final String transactionId = service.handleNLQ(query, user, keywordPrompt, gcPrompt, modelLiteral);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("transactionId", transactionId);
        return gson.toJson(jsonObject);
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