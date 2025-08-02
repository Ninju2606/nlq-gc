package de.richardvierhaus.nlq_gc;

import de.richardvierhaus.nlq_gc.enums.ModelLiterals;
import de.richardvierhaus.nlq_gc.enums.State;
import org.springframework.core.style.ToStringCreator;

import java.util.List;

public class GraphCode {

    private State state;
    private final ModelLiterals model;
    private List<String> dictionary;
    private int[][] matrix;
    private String error;
    private String description;
    private final long start;

    private static final GraphCode NOT_AVAILABLE = new GraphCode(State.NOT_AVAILABLE, null);
    protected static final String ERROR_DEFAULT = "An error occurred while parsing the query. Please try again.";

    /**
     * Creates an empty graph code of state PENDING.
     *
     * @return A new {@link GraphCode} instance.
     */
    public static GraphCode getPendingGC(final ModelLiterals model) {
        return new GraphCode(State.PENDING, model);
    }

    /**
     * Gives a graph code of state NOT_AVAILABLE
     *
     * @return A {@link GraphCode} instance representing non available GCs.
     */
    public static GraphCode getNotAvailable() {
        return NOT_AVAILABLE;
    }

    private GraphCode(final State state, final ModelLiterals model) {
        this.state = state;
        this.model = model;
        this.start = System.currentTimeMillis();
    }

    /**
     * Updates the GCs state to ERROR and stores an error text.
     *
     * @param error
     *         The errors description.
     */
    public void error(final String error, final String description) {
        checkStateUpdate();
        this.state = State.ERROR;
        this.error = error;
        this.description = description;
    }

    /**
     * Stores the GC values in case dictionary and matrix are valid and sets the state to FINISHED. Otherwise, the state
     * is set to ERROR.
     *
     * @param dictionary
     *         A {@link List} of dictionary terms.
     * @param matrix
     *         A two-dimensional array of integer values.
     * @param description
     *         The new description of the graph code.
     */
    public void finished(final List<String> dictionary, final int[][] matrix, final String description) {
        checkStateUpdate();
        if (verifyDictionaryAndMatrix(dictionary, matrix)) {
            this.state = State.FINISHED;
            this.dictionary = dictionary;
            this.matrix = matrix;
            this.description = description;
        } else {
            this.state = State.ERROR;
            this.error = ERROR_DEFAULT;
        }
    }

    /**
     * Performs null-Checks on the parsed values, checks whether the matrix is quadratic and whether the sizes of
     * dictionary and matrix match.
     *
     * @param dictionary
     *         The {@link List} of dictionary terms to be checked.
     * @param matrix
     *         The two-dimensional array of integer values to be checked.
     * @return <code>true</code> if the values are valid. Otherwise <code>false</code>.
     */
    private boolean verifyDictionaryAndMatrix(final List<String> dictionary, final int[][] matrix) {
        // Null-Check
        if (dictionary == null || matrix == null) return false;

        // Matrix quadratic check
        for (int[] row : matrix) {
            if (row.length != matrix.length) return false;
        }

        // Same size as dictionary check
        return dictionary.size() == matrix.length;
    }

    /**
     * It is only allowed to update the state of PENDING GCs. Performing updates on other states is prevented by this
     * method.
     *
     * @throws UnsupportedOperationException
     *         in case a state update is not allowed.
     */
    private void checkStateUpdate() {
        if (getState() != State.PENDING)
            throw new UnsupportedOperationException(String.format("This GraphCode of state %s cannot be modified.", this.state));
    }

    // Getter

    public State getState() {
        return state;
    }

    public List<String> getDictionary() {
        return dictionary;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public String getError() {
        return error;
    }

    public String getDescription() {
        return description;
    }

    public long getStart() {
        return start;
    }

    public ModelLiterals getModel() {
        return model;
    }

    @Override
    public String toString() {
        ToStringCreator creator = new ToStringCreator(this);
        creator.append("state", state)
                .append("start", start)
                .append("model", model.name())
                .append("dictionary", dictionary)
                .append("matrix", matrix)
                .append("description", description)
                .append("error", error);
        return creator.toString();
    }

}