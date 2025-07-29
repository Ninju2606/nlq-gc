package de.richardvierhaus.nlq_gc.llm;

import com.google.gson.Gson;
import de.richardvierhaus.nlq_gc.GraphCode;
import de.richardvierhaus.nlq_gc.KeywordResponse;
import de.richardvierhaus.nlq_gc.enums.ModelLiterals;
import de.richardvierhaus.nlq_gc.enums.Replacement;
import de.richardvierhaus.nlq_gc.nlq.PromptBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AsyncLLMService {

    private static volatile AsyncLLMService INSTANCE;
    private static final int INTERVAL_SECONDS = 3; // 3 seconds between polling
    private static final int TIMEOUT = 300000;  // 5 minutes before deletion
    private static final Logger LOGGER = LoggerFactory.getLogger(AsyncLLMService.class);

    private final Map<String, GraphCode> pendingKeywordTransactions;
    private final Map<String, GraphCode> pendingGraphCodeTransactions;
    private final Map<String, GraphCode> finishedGraphCodes;
    private final Map<String, PromptBuilder> preparedGCPrompts;
    private final Map<String, String> transactionMapping;

    private final Gson gson;
    private final ScheduledExecutorService scheduler;
    private volatile boolean running;

    private AsyncLLMService() {
        pendingKeywordTransactions = new ConcurrentHashMap<>();
        pendingGraphCodeTransactions = new ConcurrentHashMap<>();
        finishedGraphCodes = new ConcurrentHashMap<>();
        preparedGCPrompts = new ConcurrentHashMap<>();
        transactionMapping = new ConcurrentHashMap<>();

        gson = new Gson();
        scheduler = Executors.newSingleThreadScheduledExecutor();
        running = false;
    }

    public static AsyncLLMService getInstance() {
        if (INSTANCE == null) {
            synchronized (AsyncLLMService.class) {
                if (INSTANCE == null) {
                    INSTANCE = new AsyncLLMService();
                    LOGGER.info("Created AsyncLLMService instance");
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Starts the scheduler.
     */
    public void init() {
        if (!running) {
            running = true;
            scheduler.scheduleWithFixedDelay(this::processTransactions, 0, INTERVAL_SECONDS, TimeUnit.SECONDS);
            LOGGER.info("Started AsyncLLMService scheduler");
        }
    }

    /**
     * Stops the scheduler.
     */
    public void shutdown() {
        if (running) {
            running = false;
            scheduler.shutdown();
            try {
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
                LOGGER.info("Terminated AsyncLLMService scheduler");
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
                LOGGER.info("Terminated AsyncLLMService scheduler", e);
            }
        }
    }

    /**
     * The scheduled task to process timeouts and check for results.
     */
    private void processTransactions() {
        if (!running) return;

        LOGGER.trace("Routine scheduler run");

        long currentTime = System.currentTimeMillis();
        removeTimeouts(finishedGraphCodes, currentTime);
        removeTimeouts(pendingKeywordTransactions, currentTime);
        removeTimeouts(pendingGraphCodeTransactions, currentTime);

        checkKeywordTransactions();
    }

    /**
     * Starts the execution of the given prompt for keyword extraction.
     *
     * @param prompt
     *         The prompt to be executed.
     * @param model
     *         The {@link ModelLiterals} instance to be used.
     * @param preparedGCPrompt
     *         A {@link PromptBuilder} instance that is only missing keywords to be replaced.
     * @return A transactionId to poll the resulting graph code.
     */
    public String addKeywordPrompt(final String prompt, final ModelLiterals model,
                                   final PromptBuilder preparedGCPrompt) {
        if (preparedGCPrompt.getLeftoverReplacements().size() != 1 || !preparedGCPrompt.getLeftoverReplacements().contains(Replacement.KEYWORDS))
            throw new UnsupportedOperationException("The given PromptBuilder does not contain exactly the replacement KEYWORDS");

        final String transactionId = UUID.randomUUID().toString();
        String llmTransaction = model.getLLM().handlePrompt(prompt);
        LOGGER.debug("Started keyword transaction [{}]", transactionId);
        LOGGER.trace("Executing keyword transaction [{}] with following prompt: {}", transactionId, prompt);

        transactionMapping.put(transactionId, llmTransaction);
        pendingKeywordTransactions.put(transactionId, GraphCode.getPendingGC(model));
        preparedGCPrompts.put(transactionId, preparedGCPrompt);

        return transactionId;
    }

    /**
     * Starts the execution of the given prompt for graph code generation.
     *
     * @param prompt
     *         The prompt to be executed.
     * @param model
     *         The {@link ModelLiterals} instance to be used.
     * @return A transactionId to poll the resulting graph code.
     */
    public String addGCPrompt(final String prompt, final ModelLiterals model) {
        return addGCPrompt(prompt, GraphCode.getPendingGC(model), UUID.randomUUID().toString());
    }

    /**
     * Starts the execution of the given prompt.
     *
     * @param prompt
     *         The prompt to be executed.
     * @param graphCode
     *         The {@link GraphCode} instance containing necessary information.
     * @param transactionId
     *         The already prepared transactionId.
     * @return A transactionId to poll the resulting graph code.
     */
    private String addGCPrompt(final String prompt, final GraphCode graphCode, final String transactionId) {
        String llmTransaction = graphCode.getModel().getLLM().handlePrompt(prompt);
        LOGGER.debug("Started graph code transaction [{}]", transactionId);
        LOGGER.trace("Executing graph code transaction [{}] with following prompt: {}", transactionId, prompt);

        transactionMapping.put(transactionId, llmTransaction);
        pendingGraphCodeTransactions.put(transactionId, graphCode);

        return transactionId;
    }

    /**
     * Looks up the {@link GraphCode} corresponding to the given transactionId.
     *
     * @param transactionId
     *         The transactions' id.
     * @return The {@link GraphCode} behind the transactionId.
     */
    public GraphCode getGraphCode(final String transactionId) {
        GraphCode result = finishedGraphCodes.remove(transactionId);
        if (result == null) {
            result = pendingKeywordTransactions.get(transactionId);
            if (result == null) {
                result = checkPendingGraphCodeTransaction(transactionId);
                if (result == null) result = GraphCode.getNotAvailable();
            }
        }
        LOGGER.trace("Transaction [{}] found graph code {}", transactionId, result);
        return result;
    }

    /**
     * Checks all entries in the given mapping for their processing time exceeding the timeout time and removes them in
     * case they do.
     *
     * @param mapping
     *         The {@link Map} of transactionId and {@link GraphCode}.
     * @param currentTime
     *         The current time milliseconds.
     */
    private void removeTimeouts(final Map<String, GraphCode> mapping, final long currentTime) {
        Iterator<Map.Entry<String, GraphCode>> iterator = mapping.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, GraphCode> entry = iterator.next();
            if (entry.getValue().getStart() + TIMEOUT < currentTime) {
                String transactionId = entry.getKey();
                iterator.remove();
                transactionMapping.remove(transactionId);
                preparedGCPrompts.remove(transactionId);
                LOGGER.debug("Removed transaction [{}] due to timeout", transactionId);
            }
        }

    }

    /**
     * Iterates through all pending keyword transactions and checks whether they have been handled yet. In case a
     * transaction is finished the following GC prompt is send to the LLM.
     */
    private void checkKeywordTransactions() {
        Iterator<Map.Entry<String, GraphCode>> iterator = pendingKeywordTransactions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, GraphCode> entry = iterator.next();
            String transactionId = entry.getKey();
            GraphCode graphCode = entry.getValue();

            String response = graphCode.getModel().getLLM().getResponse(transactionMapping.get(transactionId));
            if (!StringUtils.hasText(response)) continue;

            LOGGER.trace("Found response for keyword transaction [{}]: {}", transactionId, response);

            try {
                KeywordResponse responseParsed = gson.fromJson(response, KeywordResponse.class);

                if (StringUtils.hasText(responseParsed.getError())) {
                    graphCode.error(responseParsed.getError());
                    preparedGCPrompts.remove(transactionId);
                    finishedGraphCodes.put(transactionId, graphCode);
                    LOGGER.debug("Found errors during keyword extraction [{}]: {}", transactionId, graphCode);
                } else {
                    LOGGER.debug("Keyword extraction [{}] found keywords: {}", transactionId, responseParsed.getDictionary());
                    PromptBuilder builder = preparedGCPrompts.remove(transactionId);
                    builder.replace(Replacement.KEYWORDS, gson.toJson(responseParsed.getDictionary()));
                    addGCPrompt(builder.toString(), graphCode, transactionId);
                }
            } finally {
                transactionMapping.remove(transactionId);
                iterator.remove();
            }
        }
    }

    private GraphCode checkPendingGraphCodeTransaction(final String transactionId) {
        GraphCode graphCode = pendingGraphCodeTransactions.get(transactionId);
        if (graphCode == null) return null;

        String response = graphCode.getModel().getLLM().getResponse(transactionMapping.get(transactionId));
        if (!StringUtils.hasText(response)) return graphCode;

        LOGGER.trace("Found response for graph code transaction [{}]: {}", transactionId, response);

        try {
            GraphCode responseParsed = gson.fromJson(response, GraphCode.class);
            if (StringUtils.hasText(responseParsed.getError())) {
                graphCode.error(responseParsed.getError());
                LOGGER.debug("Found errors during graph code generation [{}]: {}", transactionId, graphCode);
            } else {
                graphCode.finished(responseParsed.getDictionary(), responseParsed.getMatrix(), responseParsed.getDescription());
                LOGGER.debug("Graph code generation [{}] finished: {}", transactionId, graphCode);
            }
        } finally {
            transactionMapping.remove(transactionId);
            pendingGraphCodeTransactions.remove(transactionId);
        }
        return graphCode;
    }

}