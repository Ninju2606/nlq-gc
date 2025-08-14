package de.richardvierhaus.nlq_gc.evaluation;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import de.richardvierhaus.nlq_gc.encoding.EncodingMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ResponseChecker {

    private static final Gson GSON = new Gson();

    private final Execution execution;
    private final ResponseDTO responseDTO;

    protected ResponseChecker(final String response, final Execution execution) {
        this.execution = execution;
        responseDTO = new ResponseDTO();
        responseDTO.responsePlain = response;
        responseDTO.state = ExecutionState.SUCCESS;
    }

    protected ExecutionState getState() {
        return responseDTO.state;
    }

    protected String check() {
        try {
            ResponseDTO responseParsed = GSON.fromJson(responseDTO.responsePlain, ResponseDTO.class);
            responseDTO.copyValues(responseParsed);
        } catch (JsonSyntaxException e) {
            responseDTO.state = ExecutionState.NON_PARSABLE_JSON;
            return GSON.toJson(responseDTO);
        }

        if (!checkKeywords()) {
            responseDTO.state = ExecutionState.WRONG_KEYWORDS;
        }
        if (!execution.isKeywordPrompt()) {
            if (!checkMatrixSize()) {
                responseDTO.state = ExecutionState.NON_MATCHING_MATRIX;
            } else if (!checkMatrixValues()) {
                responseDTO.state = ExecutionState.NON_EXISTENT_MATRIX_ENTRIES;
            } else if (!checkMatrixMatching()) {
                responseDTO.state = ExecutionState.WRONG_MATRIX_ENTRY;
            }
        }
        return GSON.toJson(responseDTO);
    }

    private boolean checkKeywords() {
        final List<String> keywordsExpected = execution.getNlq().getKeywords();
        if (responseDTO.dictionary.size() != keywordsExpected.size()) // compare dictionary size
            return false;
        for (String entry : responseDTO.dictionary) { // verify dictionary contains all keywords
            if (keywordsExpected.stream().noneMatch(keyword -> keyword.equalsIgnoreCase(entry)))
                return false;
        }
        return true;
    }

    private boolean checkMatrixSize() {
        final int[][] matrix = responseDTO.matrix;
        // check matrix size matching dictionary
        if (responseDTO.dictionary.size() != matrix.length) return false;

        // check matrix quadratic
        for (int[] row : matrix) {
            if (row.length != matrix.length) return false;
        }
        return true;
    }

    private boolean checkMatrixValues() {
        Set<Integer> diagonalValues = new HashSet<>();
        Set<Integer> otherValues = new HashSet<>();
        final int[][] matrix = responseDTO.matrix;

        // collect all used values
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                if (i == j) diagonalValues.add(matrix[i][j]);
                else otherValues.add(matrix[i][j]);
            }
        }

        List<EncodingMapping> encodingMappings = Execution.getEncodings();

        // check all diagonal values are identity mappings
        if (!diagonalValues.stream().allMatch(value ->
                encodingMappings.stream().filter(EncodingMapping::isIdentityMapping).anyMatch(mapping -> mapping.getId() == value)))
            return false;

        // check all non-diagonal values are either 0 or not identity mappings
        return otherValues.stream().allMatch(value -> value == 0 ||
                encodingMappings.stream().filter(Predicate.not(EncodingMapping::isIdentityMapping)).anyMatch(mapping -> mapping.getId() == value));
    }

    private boolean checkMatrixMatching() {
        final int[][] matrix = responseDTO.matrix;

        // check for all entries whether the same keyword combination has the same matrix value
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                String keyword1 = responseDTO.dictionary.get(i).toLowerCase();
                String keyword2 = responseDTO.dictionary.get(j).toLowerCase();
                if (matrix[i][j] != getValueByKeywords(keyword1, keyword2))
                    return false;
            }
        }
        return true;
    }

    private int getValueByKeywords(final String keyword1, final String keyword2) {
        final Query query = execution.getNlq();
        int index1 = query.getKeywords().indexOf(keyword1);
        int index2 = query.getKeywords().indexOf(keyword2);
        return query.getMatrix()[index1][index2];
    }

    protected static class ResponseDTO {

        public ExecutionState state;
        public List<String> dictionary;
        public int[][] matrix;
        public String description;
        public String error;
        public String responsePlain;

        protected void copyValues(final ResponseDTO other) {
            this.dictionary = other.dictionary;
            this.matrix = other.matrix;
            this.description = other.description;
            this.error = other.error;
        }

    }

}