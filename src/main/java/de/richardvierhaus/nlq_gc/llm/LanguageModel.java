package de.richardvierhaus.nlq_gc.llm;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public abstract class LanguageModel {

    private Properties llmProperties;

    /**
     * Takes a given prompt, hands it over to the LLM and returns a transactionId.
     *
     * @param prompt
     *         The prompt to be executed.
     * @return A transactionId.
     */
    abstract String handlePrompt(final String prompt);

    /**
     * Checks whether the execution of the prompt with the given transactionId is finished and gives the LLMs response.
     *
     * @param transactionId
     *         The transactions' id.
     * @return The LLMs response in case it is available. Otherwise <code>null</code>.
     */
    abstract String getResponse(final String transactionId);

    /**
     * Provides an {@link Properties} instance of the llm.properties file. Can be used to store models parameters like
     * URLs.
     *
     * @return The {@link Properties}.
     */
    protected Properties getLLMProperties() {
        if (llmProperties == null) {
            llmProperties = new Properties();
            try (InputStream input = getClass().getClassLoader().getResourceAsStream("llm.properties")) {
                if (input == null) {
                    throw new FileNotFoundException("Properties file not found");
                }
                llmProperties.load(input);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return llmProperties;
    }

}