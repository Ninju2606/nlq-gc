package de.richardvierhaus.nlq_gc.enums;

import java.util.List;

public enum PromptKeyword implements Prompt {
    KEYWORDS_01_FS {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of(Replacement.USER, Replacement.QUERY);
        }

        @Override
        public String getResource() {
            return "prompts/keywordExtraction/PromptKeyword_01_FS.txt";
        }
    },
    KEYWORDS_01_ZS {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of(Replacement.USER, Replacement.QUERY);
        }

        @Override
        public String getResource() {
            return "prompts/keywordExtraction/PromptKeyword_01_ZS.txt";
        }
    };

    /**
     * Provides the default {@link PromptKeyword} in case no one was specified.
     *
     * @return The default {@link PromptKeyword}.
     */
    public static PromptKeyword getDefault() {
        return KEYWORDS_01_FS;
    }
}