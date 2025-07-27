package de.richardvierhaus.nlq_gc.enums;

import java.util.List;

public enum PromptGraphCode implements Prompt {
    // TODO
    NO_KEYWORDS_01_FS {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of(Replacement.USER, Replacement.ENCODING, Replacement.QUERY);
        }

        @Override
        public String getResource() {
            return "prompts/graphCode/PromptGraphCode_NoKeywords_01_FS.txt";
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