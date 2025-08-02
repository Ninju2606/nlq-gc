package de.richardvierhaus.nlq_gc;

import java.util.List;

public record KeywordResponse(List<String> dictionary, String error, String description) {

}