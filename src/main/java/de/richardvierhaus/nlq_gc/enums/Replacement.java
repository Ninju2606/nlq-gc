package de.richardvierhaus.nlq_gc.enums;

public enum Replacement {
    // TODO
    QUERY {
        @Override
        public String getName() {
            return "[QUERY]";
        }
    },
    USER {
        @Override
        public String getName() {
            return "[USER]";
        }
    },
    ENCODING {
        @Override
        public String getName() {
            return "[ENCODING]";
        }
    },
    KEYWORDS {
        @Override
        public String getName() {
            return "[KEYWORDS]";
        }
    };

    /**
     * Gives the name of the replacement how it is placed inside a prompt.
     *
     * @return The replacements name.
     */
    public abstract String getName();

}