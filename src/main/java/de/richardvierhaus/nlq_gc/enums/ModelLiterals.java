package de.richardvierhaus.nlq_gc.enums;

import de.richardvierhaus.nlq_gc.llm.LanguageModel;
import de.richardvierhaus.nlq_gc.llm.QWen;
import de.richardvierhaus.nlq_gc.llm.QWen3Coder;
import de.richardvierhaus.nlq_gc.llm.QWen3_235bFree;

public enum ModelLiterals {
    QWEN3_1_7_B {
        @Override
        public LanguageModel getLLM() {
            return QWen.getInstance();
        }
    },
    QWEN3_CODER {
        @Override
        public LanguageModel getLLM() {
            return QWen3Coder.getInstance();
        }
    },
    QWEN3_235B_FREE {
        @Override
        public LanguageModel getLLM() {
            return QWen3_235bFree.getInstance();
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
    public static ModelLiterals getDefault() {
        return QWEN3_1_7_B;
    }
}