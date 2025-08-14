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
            return List.of("Maria", "play", "basketball");
        }

        @Override
        protected int[][] getMatrix() {
            return new int[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};
        }
    };

    protected abstract String getQuery();

    protected abstract List<String> getKeywords();

    protected abstract int[][] getMatrix();
}