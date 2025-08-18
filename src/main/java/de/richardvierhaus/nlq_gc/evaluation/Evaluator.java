package de.richardvierhaus.nlq_gc.evaluation;

import de.richardvierhaus.nlq_gc.enums.PromptGraphCode;
import de.richardvierhaus.nlq_gc.enums.PromptKeyword;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Evaluator {

    private static final int RUNS_PER_EXECUTION = 10;

    private final Logger LOGGER = LoggerFactory.getLogger(Evaluator.class);
    private final List<Execution> executions = new ArrayList<>();
    private final Map<Execution, Integer> completedExecutions = new HashMap<>();

    public static void main(final String[] args) {
        Evaluator evaluator = new Evaluator();
        evaluator.print();
        evaluator.run();
    }

    private Evaluator() {
        // Fill completed executions
        for (PromptGraphCode prompt : PromptGraphCode.values()) {
            for (Query nlq : Query.values()) {
                Execution execution = new Execution(prompt, nlq);
                if (execution.isPossible())
                    completedExecutions.put(execution, getNumberOfFiles(execution.getPath()));
            }
        }
        for (PromptKeyword prompt : PromptKeyword.values()) {
            for (Query nlq : Query.values()) {
                Execution execution = new Execution(prompt, nlq);
                completedExecutions.put(execution, getNumberOfFiles(execution.getPath()));
            }
        }

        // Add leftover executions
        for (Map.Entry<Execution, Integer> entry : completedExecutions.entrySet()) {
            for (int i = 0; i < RUNS_PER_EXECUTION - entry.getValue(); i++) {
                executions.add(entry.getKey().copy());
            }
        }
    }

    private void run() {
        LOGGER.info(String.valueOf(executions.size()));
        executions.forEach(this::runExecution);
    }

    private void runExecution(final Execution execution) {
        try {
            LOGGER.info("Starting execution of prompt [{}] with query [{}]", execution.getPrompt(), execution.getNlq());
            String transaction = Execution.getModel().getLLM().handlePrompt(execution.getFullPrompt());
            String response = pollResponse(transaction);
            LOGGER.debug("Response: {}", response);
            if (!StringUtils.hasText(response)) return;

            final ResponseChecker checker = new ResponseChecker(response, execution);
            String checkedResult = checker.check().getAsString();

            writeFile(execution.getFileName(checker.getState()), checkedResult);

            LOGGER.info("Finished execution of prompt [{}] with query [{}]. State: {}", execution.getPrompt(), execution.getNlq(), checker.getState());
        } catch (Exception e) {
            LOGGER.error("Error while running an execution", e);
        }
    }

    private String pollResponse(final String transactionID) {
        int maxAttempts = 60; // 60 seconds
        int attempts = 0;
        String response = null;

        while (attempts < maxAttempts) {
            try {
                response = Execution.getModel().getLLM().getResponse(transactionID);
                if (StringUtils.hasText(response)) break;

                Thread.sleep(1000);
                attempts++;
            } catch (Exception e) {
                LOGGER.error("Error while polling a response", e);
            }
        }

        return response;
    }

    private void print() {
        for (Map.Entry<Execution, Integer> entry : completedExecutions.entrySet()) {
            String prompt = entry.getKey().getPrompt().toString();
            String query = entry.getKey().getNlq().name();
            int amount = entry.getValue();

            LOGGER.info(String.format("Prompt: %-30s Query: %-10s Amount: %02d/%02d",
                    prompt, query, amount, RUNS_PER_EXECUTION));
        }
    }

    private int getNumberOfFiles(final String path) {
        File file = new File(path);
        if (!file.exists() || !file.isDirectory()) {
            file.mkdirs();
            return 0;
        }
        File[] files = file.listFiles();

        return files == null ? 0 : (int) Arrays.stream(files).filter(File::isFile).count();
    }

    private void writeFile(final String path, final String content) {
        Path filePath = Paths.get(path);
        try {
            Files.write(filePath, content.getBytes());
            LOGGER.debug("Successfully created file: {}", path);
        } catch (IOException e) {
            LOGGER.error("Error while creating file", e);
        }
    }

}