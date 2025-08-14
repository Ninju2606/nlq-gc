package de.richardvierhaus.nlq_gc.evaluation;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;

public class ResponseChecker {

    private static final Gson GSON = new Gson();

    private final Execution execution;
    private final ResponseDTO responseDTO;

    protected ResponseChecker(final String response, final Execution execution) {
        this.execution = execution;
        responseDTO = new ResponseDTO();
        responseDTO.responsePlain = response;

    }

    protected String check() {
        try {
            ResponseDTO responseParsed = GSON.fromJson(responseDTO.responsePlain, ResponseDTO.class);
            responseDTO.copyValues(responseParsed);
        } catch (JsonSyntaxException e) {
            responseDTO.state = ExecutionState.NON_PARSABLE_JSON;
            return GSON.toJson(responseDTO);
        }

        responseDTO.state = ExecutionState.SUCCESS;
        return GSON.toJson(responseDTO);
    }

    private boolean checkKeywords() {
        final List<String> keywordsExpected = execution.getNlq().getKeywords();
        if (responseDTO.dictionary.size() != keywordsExpected.size())
            return false;
        for (String entry : responseDTO.dictionary) {
            if (!keywordsExpected.contains(entry))
                return false;
        }
        return true;
    }

    protected static class ResponseDTO {

        public ExecutionState state;
        public List<String> dictionary;
        public int[][] matrix;
        public String description;
        public String error;
        public String responsePlain;

        public static ResponseDTO ofState(final ExecutionState state) {
            ResponseDTO responseDTO = new ResponseDTO();
            responseDTO.state = state;
            return responseDTO;
        }

        protected void copyValues(final ResponseDTO other) {
            this.dictionary = other.dictionary;
            this.matrix = other.matrix;
            this.description = other.description;
            this.error = other.error;
        }

    }

}