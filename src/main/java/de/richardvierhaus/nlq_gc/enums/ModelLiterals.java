package de.richardvierhaus.nlq_gc.enums;

import de.richardvierhaus.nlq_gc.llm.LanguageModel;
import de.richardvierhaus.nlq_gc.llm.OpenRouterLLM;
import de.richardvierhaus.nlq_gc.llm.QWen;

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
            return OpenRouterLLM.getInstance("qwen/qwen3-coder");
        }
    },
    QWEN3_235B_FREE {
        @Override
        public LanguageModel getLLM() {
            return OpenRouterLLM.getInstance("qwen/qwen3-235b-a22b:free");
        }
    },
    QWEN2_5_72B_INSTRUCT {
        @Override
        public LanguageModel getLLM() {
            return OpenRouterLLM.getInstance("qwen/qwen-2.5-72b-instruct");
        }
    },
    QWEN3_14B {
        @Override
        public LanguageModel getLLM() {
            return OpenRouterLLM.getInstance("qwen/qwen3-14b");
        }
    },
    QWEN_TURBO {
        @Override
        public LanguageModel getLLM() {
            return OpenRouterLLM.getInstance("qwen/qwen-turbo");
        }
    },
    QWEN3_235B {
        @Override
        public LanguageModel getLLM() {
            return OpenRouterLLM.getInstance("qwen/qwen3-235b-a22b-2507");
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