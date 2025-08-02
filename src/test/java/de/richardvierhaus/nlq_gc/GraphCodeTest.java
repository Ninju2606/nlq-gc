package de.richardvierhaus.nlq_gc;

import de.richardvierhaus.nlq_gc.enums.ModelLiterals;
import de.richardvierhaus.nlq_gc.enums.State;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class GraphCodeTest {

    @Mock
    ModelLiterals model;

    @Test
    public void testNotAvailable() {
        final GraphCode gc = GraphCode.getNotAvailable();
        checkGCValues(gc, State.NOT_AVAILABLE, null, null, null, null);
        assertThat(gc.getModel()).isNull();

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> gc.error("ABC", "TEST"));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> gc.finished(List.of(), null, "ABC"));
    }

    @Test
    public void testError() {
        long timeBefore = System.currentTimeMillis();
        final GraphCode gc = GraphCode.getPendingGC(model);
        assertThat(gc.getStart()).isBetween(timeBefore, System.currentTimeMillis());
        assertThat(gc.getModel()).isEqualTo(model);
        checkGCValues(gc, State.PENDING, null, null, null, null);

        gc.error("ABC", "TEST");
        checkGCValues(gc, State.ERROR, null, null, "ABC", "TEST");

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> gc.error("ABC", "TEST"));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> gc.finished(List.of(), null, "ABC"));
    }

    @Test
    public void testFinishedValid() {
        final GraphCode gc = GraphCode.getPendingGC(model);
        checkGCValues(gc, State.PENDING, null, null, null, null);

        final List<String> dictionary = List.of("A", "B");
        final int[][] matrix = {{0, 0}, {0, 0}};
        gc.finished(dictionary, matrix, "ABC");
        checkGCValues(gc, State.FINISHED, dictionary, matrix, null, "ABC");

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> gc.error("ABC", "TEST"));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> gc.finished(List.of(), null, "ABC"));
    }

    @Test
    public void testFinishedNotValid() {
        List<String> dictionary = List.of("A", "B");
        int[][] matrixNotQuadratic = {{0}, {0, 0}};
        int[][] matrix = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};

        // Null values
        GraphCode gc = GraphCode.getPendingGC(model);
        gc.finished(null, matrix, null);
        checkGCValues(gc, State.ERROR, null, null, GraphCode.ERROR_DEFAULT, null);

        gc = GraphCode.getPendingGC(model);
        gc.finished(dictionary, null, null);
        checkGCValues(gc, State.ERROR, null, null, GraphCode.ERROR_DEFAULT, null);

        // Not quadratic matrix
        gc = GraphCode.getPendingGC(model);
        gc.finished(dictionary, matrixNotQuadratic, null);
        checkGCValues(gc, State.ERROR, null, null, GraphCode.ERROR_DEFAULT, null);

        // Not same size
        gc = GraphCode.getPendingGC(model);
        gc.finished(dictionary, matrix, null);
        checkGCValues(gc, State.ERROR, null, null, GraphCode.ERROR_DEFAULT, null);
    }

    private void checkGCValues(final GraphCode gc, final State state, final List<String> dictionary,
                               final int[][] matrix, final String error, final String description) {
        assertThat(gc.getState()).isEqualTo(state);
        assertThat(gc.getDescription()).isEqualTo(description);
        assertThat(gc.getDictionary()).isEqualTo(dictionary);
        assertThat(gc.getError()).isEqualTo(error);
        assertThat(gc.getMatrix()).isEqualTo(matrix);
    }

}