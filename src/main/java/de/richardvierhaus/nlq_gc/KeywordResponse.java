package de.richardvierhaus.nlq_gc;

import java.util.List;

public class KeywordResponse {

    private final List<String> dictionary;
    private final String error;

    public KeywordResponse(final List<String> dictionary, final String error) {
        this.dictionary = dictionary;
        this.error = error;
    }

    public List<String> getDictionary() {
        return dictionary;
    }

    public String getError() {
        return error;
    }

}