package de.richardvierhaus.nlq_gc.evaluation;

import java.util.List;

public enum Query {
    Q1 {
        @Override
        protected String getQuery() {
            return "Ich spiele Basketball";
        }

        @Override
        protected List<String> getKeywords() {
            return List.of("maria", "play", "basketball");
        }

        @Override
        protected int[][] getMatrix() {
            return new int[][]{{1, 8, 0}, {0, 4, 9}, {0, 0, 1}};
        }
    },
    Q2 {
        @Override
        protected String getQuery() {
            return "Tim rennt";
        }

        @Override
        protected List<String> getKeywords() {
            return List.of("tim", "run");
        }

        @Override
        protected int[][] getMatrix() {
            return new int[][]{{1, 8}, {0, 4}};
        }
    },
    Q3 {
        @Override
        protected String getQuery() {
            return "Ich schwimme in einem Pool, der mir gehört";
        }

        @Override
        protected List<String> getKeywords() {
            return List.of("maria", "swim", "own", "pool");
        }

        @Override
        protected int[][] getMatrix() {
            return new int[][]{{1, 8, 8, 0}, {0, 4, 0, 9}, {0, 0, 4, 9}, {0, 0, 0, 1}};
        }
    },
    Q4 {
        @Override
        protected String getQuery() {
            return "Ich schwimme in meinem Pool";
        }

        @Override
        protected List<String> getKeywords() {
            return List.of("maria", "swim", "own", "pool");
        }

        @Override
        protected int[][] getMatrix() {
            return new int[][]{{1, 8, 8, 0}, {0, 4, 0, 9}, {0, 0, 4, 9}, {0, 0, 0, 1}};
        }
    },
    Q5 {
        @Override
        protected String getQuery() {
            return "Zeig mir Situationen, in denen ich einen gelben Hut trage";
        }

        @Override
        protected List<String> getKeywords() {
            return List.of("maria", "wear", "hat", "yellow");
        }

        @Override
        protected int[][] getMatrix() {
            return new int[][]{{1, 8, 0, 0}, {0, 4, 8, 0}, {0, 0, 1, 0}, {0, 0, 14, 2}};
        }
    },
    Q6 {
        @Override
        protected String getQuery() {
            return "Wir müssen bald einen Termin ausmachen.";
        }

        @Override
        protected List<String> getKeywords() {
            return List.of("maria", "make", "appointment");
        }

        @Override
        protected int[][] getMatrix() {
            return null; // no relation for "bald" (soon)
        }
    },
    Q7 {
        @Override
        protected String getQuery() {
            return "Häuser um Fragen zu malen";
        }

        @Override
        protected List<String> getKeywords() {
            return List.of();
        }

        @Override
        protected int[][] getMatrix() {
            return null;
        }
    },
    Q8 {
        @Override
        protected String getQuery() {
            return "Meine Tochter und ich fahren Boot auf einem See";
        }

        @Override
        protected List<String> getKeywords() {
            return List.of("maria", "person", "drive", "boat", "lake");
        }

        @Override
        protected int[][] getMatrix() {
            return new int[][]{{1, 10, 8, 0, 0}, {0, 1, 8, 0, 0}, {0, 0, 4, 9, 0}, {0, 0, 0, 1, 7}, {0, 0, 0, 0, 1}};
        }
    };

    protected abstract String getQuery();

    protected abstract List<String> getKeywords();

    protected abstract int[][] getMatrix();
}