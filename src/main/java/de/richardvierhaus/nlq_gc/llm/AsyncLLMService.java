package de.richardvierhaus.nlq_gc.llm;

import com.google.gson.Gson;
import de.richardvierhaus.nlq_gc.GraphCode;
import de.richardvierhaus.nlq_gc.KeywordResponse;
import de.richardvierhaus.nlq_gc.enums.Replacement;
import de.richardvierhaus.nlq_gc.nlq.PromptBuilder;
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
            } catch (InterruptedException e) {
                scheduler.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * The scheduled task to process timeouts and check for results.
     */
    private void processTransactions() {
        if (!running) return;

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
     * @param llm
     *         The {@link LanguageModel} to be used.
     * @param preparedGCPrompt
     *         A {@link PromptBuilder} instance that is only missing keywords to be replaced.
     * @return A transactionId to poll the resulting graph code.
     */
    public String addKeywordPrompt(final String prompt, final LanguageModel llm,
                                   final PromptBuilder preparedGCPrompt) {
        if (preparedGCPrompt.getLeftoverReplacements().size() != 1 || !preparedGCPrompt.getLeftoverReplacements().contains(Replacement.KEYWORDS))
            throw new UnsupportedOperationException("The given PromptBuilder does not contain exactly the replacement KEYWORDS");

        final String transactionId = UUID.randomUUID().toString();
        String llmTransaction = llm.handlePrompt(prompt);

        transactionMapping.put(transactionId, llmTransaction);
        pendingKeywordTransactions.put(transactionId, GraphCode.getPendingGC(llm));
        preparedGCPrompts.put(transactionId, preparedGCPrompt);

        return transactionId;
    }

    /**
     * Starts the execution of the given prompt for graph code generation.
     *
     * @param prompt
     *         The prompt to be executed.
     * @param llm
     *         The {@link LanguageModel} to be used.
     * @return A transactionId to poll the resulting graph code.
     */
    public String addGCPrompt(final String prompt, final LanguageModel llm) {
        return addGCPrompt(prompt, GraphCode.getPendingGC(llm), UUID.randomUUID().toString());
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
        String llmTransaction = graphCode.getLLM().handlePrompt(prompt);
        transactionMapping.put(transactionId, llmTransaction);
        //        pendingGraphCodeTransactions.put(transactionId, graphCode);

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
        if (result != null) return result;

        result = pendingKeywordTransactions.get(transactionId);
        if (result != null) return result;

        result = checkPendingGraphCodeTransaction(transactionId);
        if (result != null) return result;

        return GraphCode.getNotAvailable();
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
            if (entry.getValue().getStart() + TIMEOUT > currentTime) {
                String transactionId = entry.getKey();
                iterator.remove();
                transactionMapping.remove(transactionId);
                preparedGCPrompts.remove(transactionId);
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

            String response = graphCode.getLLM().getResponse(transactionMapping.get(transactionId));
            if (!StringUtils.hasText(response)) continue;

            try {
                transactionMapping.remove(transactionId);
                KeywordResponse responseParsed = gson.fromJson(response, KeywordResponse.class);

                if (StringUtils.hasText(responseParsed.getError())) {
                    graphCode.error(responseParsed.getError());
                    preparedGCPrompts.remove(transactionId);
                    finishedGraphCodes.put(transactionId, graphCode);
                } else {
                    PromptBuilder builder = preparedGCPrompts.remove(transactionId);
                    builder.replace(Replacement.KEYWORDS, gson.toJson(responseParsed.getDictionary()));
                    addGCPrompt(builder.toString(), graphCode, transactionId);
                }
            } finally {
                iterator.remove();
            }
        }
    }

    private GraphCode checkPendingGraphCodeTransaction(final String transactionId) {
        GraphCode graphCode = pendingGraphCodeTransactions.get(transactionId);
        if (graphCode == null) return null;

        String response = graphCode.getLLM().getResponse(transactionMapping.get(transactionId));
        if (!StringUtils.hasText(response)) return null;

        try {
            GraphCode responseParsed = gson.fromJson(response, GraphCode.class);
            if (StringUtils.hasText(responseParsed.getError())) {
                graphCode.error(responseParsed.getError());
            } else {
                graphCode.finished(responseParsed.getDictionary(), responseParsed.getMatrix(), responseParsed.getDescription());
            }
        } finally {
            transactionMapping.remove(transactionId);
        }
        return graphCode;
    }

}