package de.richardvierhaus.nlq_gc.evaluation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import de.richardvierhaus.nlq_gc.encoding.EncodingMapping;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class ResponseChecker {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

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

    protected ResponseChecker check() {
        try {
            ResponseDTO responseParsed = GSON.fromJson(responseDTO.responsePlain, ResponseDTO.class);
            responseDTO.copyValues(responseParsed);
        } catch (JsonSyntaxException e) {
            responseDTO.state = ExecutionState.NON_PARSABLE_JSON;
            return this;
        }

        if (execution.expectErrors()) {
            if (!checkErrors()) // check whether expected errors occurred
                responseDTO.state = ExecutionState.EXPECTED_ERROR;
        } else if (!checkFalseErrors()) {
            responseDTO.state = ExecutionState.EXPECTED_NO_ERROR;
        } else if (!checkKeywords()) { // check keywords matching
            responseDTO.state = ExecutionState.WRONG_KEYWORDS;
        } else if (!execution.isKeywordPrompt()) { // GC prompts need to check correct matrix as well
            if (!checkMatrixSize())
                responseDTO.state = ExecutionState.NON_MATCHING_MATRIX;
            else if (!checkMatrixValues())
                responseDTO.state = ExecutionState.NON_EXISTENT_MATRIX_ENTRIES;
            else if (!checkMatrixMatching())
                responseDTO.state = ExecutionState.WRONG_MATRIX_ENTRY;
            else if (responseDTO.matrixSimilarity < 1) // if no error due to wrong matrix entries but similarity not 100% set warning
                responseDTO.state = ExecutionState.WARNING_MATRIX;
        }
        return this;
    }

    protected String getAsString() {
        return GSON.toJson(responseDTO);
    }

    private boolean checkErrors() {
        if (execution.getNlq().getKeywords() == null) // expect an error while keyword extraction
            return StringUtils.hasText(responseDTO.error) && (responseDTO.dictionary == null || responseDTO.dictionary.isEmpty());
        else if (execution.getNlq().getMatrix() == null && !execution.isKeywordPrompt()) // expect an error while gc generation
            return StringUtils.hasText(responseDTO.error) && (responseDTO.matrix == null || responseDTO.matrix.length == 0);

        return true;
    }

    private boolean checkFalseErrors() {
        if (execution.getNlq().getKeywords() != null && StringUtils.hasText(responseDTO.error)) return false;
        return execution.isKeywordPrompt() || execution.getNlq().getMatrix() == null || !StringUtils.hasText(responseDTO.error);
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

        boolean falseEntry = false;
        int matchingEntries = 0;

        // check for all entries whether the same keyword combination has the same matrix value
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix.length; j++) {
                String keyword1 = responseDTO.dictionary.get(i).toLowerCase();
                String keyword2 = responseDTO.dictionary.get(j).toLowerCase();
                int expectedValue = getValueByKeywords(keyword1, keyword2);

                if (matrix[i][j] == expectedValue) matchingEntries++;
                else if (matrix[i][j] != 0 && expectedValue != 0) falseEntry = true;
            }
        }

        responseDTO.matrixSimilarity = (double) matchingEntries / (matrix.length * matrix.length);

        return !falseEntry && responseDTO.matrixSimilarity > 0.85;
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
        public double matrixSimilarity;
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