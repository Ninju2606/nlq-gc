package de.richardvierhaus.nlq_gc.nlq;

import de.richardvierhaus.nlq_gc.GraphCode;
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
        // TODO verification, parsing & call
        throw new UnsupportedOperationException("Not implemented yet");
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
        // TODO verification
        return service.getGraphCode(transactionId);
    }

}