package de.richardvierhaus.nlq_gc.enums;

import de.richardvierhaus.nlq_gc.llm.LanguageModel;
import de.richardvierhaus.nlq_gc.llm.QWen;

public enum ModelLiterals {
    QWEN {
        @Override
        LanguageModel getLLM() {
            // TODO
            return new QWen("1234");
        }
    };

    /**
     * Gives the instance of the corresponding {@link LanguageModel}.
     *
     * @return A {@link LanguageModel} instance.
     */
    abstract LanguageModel getLLM();
}