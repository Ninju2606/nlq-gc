package de.richardvierhaus.nlq_gc.enums;

import java.util.List;

public enum PromptKeyword implements Prompt {
    // TODO
    TEST {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of();
        }

        @Override
        public String getResource() {
            return "prompts/keywordExtraction/abc.txt";
        }
    };

    /**
     * Provides the default {@link PromptKeyword} in case no one was specified.
     *
     * @return The default {@link PromptKeyword}.
     */
    public static PromptKeyword getDefault() {
        return TEST;
    }
}