package de.richardvierhaus.nlq_gc.enums;

import de.richardvierhaus.nlq_gc.llm.LanguageModel;
import de.richardvierhaus.nlq_gc.llm.QWen;

public enum ModelLiterals {
    QWEN3_1_7_B {
        @Override
        public LanguageModel getLLM() {
            return new QWen("Qwen/Qwen3-1.7B");
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
        return QWEN3_1_7_B.getLLM();
    }
}