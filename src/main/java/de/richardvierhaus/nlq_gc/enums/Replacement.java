package de.richardvierhaus.nlq_gc.enums;

public enum Replacement {
    // TODO
    QUERY {
        @Override
        String getName() {
            return "[QUERY]";
        }
    },
    USER {
        @Override
        String getName() {
            return "[USER]";
        }
    },
    ENCODING {
        @Override
        String getName() {
            return "[ENCODING]";
        }
    };

    /**
     * Gives the name of the replacement how it is placed inside a prompt.
     *
     * @return The replacements name.
     */
    abstract String getName();

}