package de.richardvierhaus.nlq_gc.llm;

public interface LanguageModel {

    /**
     * Takes a given prompt, hands it over to the LLM and returns a transactionId.
     *
     * @param prompt
     *         The prompt to be executed.
     * @return A transactionId.
     */
    String handlePrompt(final String prompt);

    /**
     * Checks whether the execution of the prompt with the given transactionId is finished and gives the LLMs response.
     *
     * @param transactionId
     *         The transactions id.
     * @return The LLMs response in case it is available. Otherwise <code>null</code>.
     */
    String getResponse(final String transactionId);

}