package de.richardvierhaus.nlq_gc.evaluation;

public enum ExecutionState {

    SUCCESS,
    NON_PARSABLE_JSON,
    EXPECTED_ERROR,
    EXPECTED_NO_ERROR,
    WRONG_KEYWORDS,
    NON_MATCHING_MATRIX,
    NON_EXISTENT_MATRIX_ENTRIES,
    WRONG_MATRIX_ENTRY,
    WARNING_MATRIX

}