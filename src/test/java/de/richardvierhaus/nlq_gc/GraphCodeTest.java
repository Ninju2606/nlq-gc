package de.richardvierhaus.nlq_gc;

import de.richardvierhaus.nlq_gc.enums.State;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
public class GraphCodeTest {

    @Test
    public void testNotAvailable() {
        final GraphCode gc = GraphCode.getNotAvailable();
        checkGCValues(gc, State.NOT_AVAILABLE, null, null, null, null);

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> gc.error(any()));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> gc.finished(any(), any(), any()));
    }

    @Test
    public void testError() {
        final GraphCode gc = GraphCode.getPendingGC();
        checkGCValues(gc, State.PENDING, null, null, null, null);

        gc.error("ABC");
        checkGCValues(gc, State.ERROR, null, null, "ABC", null);

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> gc.error(any()));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> gc.finished(any(), any(), any()));
    }

    @Test
    public void testFinishedValid() {
        final GraphCode gc = GraphCode.getPendingGC();
        checkGCValues(gc, State.PENDING, null, null, null, null);

        final List<String> dictionary = List.of("A", "B");
        final int[][] matrix = {{0, 0}, {0, 0}};
        gc.finished(dictionary, matrix, "ABC");
        checkGCValues(gc, State.FINISHED, dictionary, matrix, null, "ABC");

        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> gc.error(any()));
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> gc.finished(any(), any(), any()));
    }

    @Test
    public void testFinishedNotValid() {
        List<String> dictionary = List.of("A", "B");
        int[][] matrixNotQuadratic = {{0}, {0, 0}};
        int[][] matrix = {{0, 0, 0}, {0, 0, 0}, {0, 0, 0}};

        // Null values
        GraphCode gc = GraphCode.getPendingGC();
        gc.finished(null, matrix, null);
        checkGCValues(gc, State.ERROR, null, null, GraphCode.ERROR_DEFAULT, null);

        gc = GraphCode.getPendingGC();
        gc.finished(dictionary, null, null);
        checkGCValues(gc, State.ERROR, null, null, GraphCode.ERROR_DEFAULT, null);

        // Not quadratic matrix
        gc = GraphCode.getPendingGC();
        gc.finished(dictionary, matrixNotQuadratic, null);
        checkGCValues(gc, State.ERROR, null, null, GraphCode.ERROR_DEFAULT, null);

        // Not same size
        gc = GraphCode.getPendingGC();
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