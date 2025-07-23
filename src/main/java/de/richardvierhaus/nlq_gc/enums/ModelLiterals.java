package de.richardvierhaus.nlq_gc.enums;

import de.richardvierhaus.nlq_gc.llm.LanguageModel;
import de.richardvierhaus.nlq_gc.llm.QWen;

public enum ModelLiterals {
    QWEN {
        @Override
        public LanguageModel getLLM() {
            // TODO
            return new QWen("1234");
        }
    };

    /**
     * Gives the instance of the corresponding {@link LanguageModel}.
     *
     * @return A {@link LanguageModel} instance.
     */
    public abstract LanguageModel getLLM();

    /**
     * Provides the default {@link LanguageModel} instance in case no one was specified.
     *
     * @return The default {@link LanguageModel} instance.
     */
    public static LanguageModel getDefault() {
        return QWEN.getLLM();
    }
}