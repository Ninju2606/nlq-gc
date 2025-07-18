package de.richardvierhaus.nlq_gc.llm;

public class QWen implements LanguageModel {

    private final String version;

    /**
     * Initializes a QWen instance with the given version.
     *
     * @param version
     *         The version of QWen to use.
     */
    public QWen(final String version) {
        this.version = version;
    }

    @Override
    public String handlePrompt(final String prompt) {
        // TODO
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public String getResponse(final String transactionId) {
        // TODO
        throw new UnsupportedOperationException("Not implemented yet");
    }

}