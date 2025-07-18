package de.richardvierhaus.nlq_gc.enums;

import java.util.List;

public enum PromptGraphCode implements Prompt {
    // TODO
    TEST {
        @Override
        public List<Replacement> getRequiredReplacements() {
            return List.of();
        }

        @Override
        public String getResource() {
            return "prompts/graphCode/abc.txt";
        }
    }
}