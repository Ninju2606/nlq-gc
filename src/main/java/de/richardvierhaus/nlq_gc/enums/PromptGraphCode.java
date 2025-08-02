package de.richardvierhaus.nlq_gc.enums;

import java.util.List;

public enum PromptGraphCode implements Prompt {
    NO_KEYWORDS_SIMPLE {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of(Replacement.ENCODING, Replacement.QUERY);
        }

        @Override
        public String getResource() {
            return "prompts/graphCode/PromptGraphCode_NoKeywords_Simple.txt";
        }
    },
    NO_KEYWORDS_01_FS {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of(Replacement.USER, Replacement.ENCODING, Replacement.QUERY);
        }

        @Override
        public String getResource() {
            return "prompts/graphCode/PromptGraphCode_NoKeywords_01_FS.txt";
        }
    },
    NO_KEYWORDS_01_ZS {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of(Replacement.USER, Replacement.ENCODING, Replacement.QUERY);
        }

        @Override
        public String getResource() {
            return "prompts/graphCode/PromptGraphCode_NoKeywords_01_ZS.txt";
        }
    },

    WITH_KEYWORDS_01_FS {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of(Replacement.USER, Replacement.ENCODING, Replacement.QUERY, Replacement.KEYWORDS);
        }

        @Override
        public String getResource() {
            return "prompts/graphCode/PromptGraphCode_WithKeywords_01_FS.txt";
        }
    },
    WITH_KEYWORDS_01_ZS {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of(Replacement.USER, Replacement.ENCODING, Replacement.QUERY, Replacement.KEYWORDS);
        }

        @Override
        public String getResource() {
            return "prompts/graphCode/PromptGraphCode_WithKeywords_01_ZS.txt";
        }
    },
    NO_KEYWORDS_02_COT_FS {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of(Replacement.USER, Replacement.ENCODING, Replacement.QUERY);
        }

        @Override
        public String getResource() {
            return "prompts/graphCode/PromptGraphCode_NoKeywords_02_CoT_FS.txt";
        }
    },
    NO_KEYWORDS_02_COT_ZS {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of(Replacement.USER, Replacement.ENCODING, Replacement.QUERY);
        }

        @Override
        public String getResource() {
            return "prompts/graphCode/PromptGraphCode_NoKeywords_02_CoT_ZS.txt";
        }
    },

    WITH_KEYWORDS_02_COT_FS {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of(Replacement.USER, Replacement.ENCODING, Replacement.QUERY, Replacement.KEYWORDS);
        }

        @Override
        public String getResource() {
            return "prompts/graphCode/PromptGraphCode_WithKeywords_02_CoT_FS.txt";
        }
    },
    WITH_KEYWORDS_02_COT_ZS {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of(Replacement.USER, Replacement.ENCODING, Replacement.QUERY, Replacement.KEYWORDS);
        }

        @Override
        public String getResource() {
            return "prompts/graphCode/PromptGraphCode_WithKeywords_02_CoT_ZS.txt";
        }
    };

    /**
     * Determines whether an additional {@link PromptKeyword} is required.
     *
     * @return boolean value specifying the necessity of a {@link PromptKeyword}.
     */
    public boolean requiresKeywords() {
        return getRequiredReplacements().contains(Replacement.KEYWORDS);
    }

    /**
     * Provides the default {@link PromptGraphCode} in case no one was specified.
     *
     * @return The default {@link PromptGraphCode}.
     */
    public static PromptGraphCode getDefault() {
        return NO_KEYWORDS_01_FS;
    }
}