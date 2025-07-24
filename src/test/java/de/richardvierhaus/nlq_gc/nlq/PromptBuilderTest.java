package de.richardvierhaus.nlq_gc.nlq;

import de.richardvierhaus.nlq_gc.enums.Prompt;
import de.richardvierhaus.nlq_gc.enums.Replacement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.MissingFormatArgumentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class PromptBuilderTest {

    @Test
    public void testFileAvailable() {
        PromptBuilder builder = new PromptBuilder(TestPrompt.TEST);
        assertThat(builder.getLeftoverReplacements()).containsExactlyInAnyOrder(Replacement.USER, Replacement.QUERY);

        // Exceptions
        assertThatExceptionOfType(MissingFormatArgumentException.class).isThrownBy(builder::toString);
        assertThatExceptionOfType(UnsupportedOperationException.class).isThrownBy(() -> builder.replace(Replacement.KEYWORDS, "ABC"));

        // Replacements
        assertThat(builder.replaceIfRequired(Replacement.KEYWORDS, "TEST")).isEqualTo(builder);
        assertThat(builder.getLeftoverReplacements()).containsExactlyInAnyOrder(Replacement.USER, Replacement.QUERY);
        assertThat(builder.replace(Replacement.USER, "Test User")).isEqualTo(builder);
        assertThat(builder.getLeftoverReplacements()).containsExactly(Replacement.QUERY);

        builder.replace(Replacement.QUERY, "Hello World");
        assertThat(builder.getLeftoverReplacements()).isEmpty();

        // toString()
        assertThat(builder.toString()).isEqualTo("Test User asked following query:\nHello World");
    }

    @Test
    public void testFileNotAvailable() {
        try {
            new PromptBuilder(TestPrompt.NO_FILE);
            Assertions.fail("Expected exception did not throw");
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("The file with path prompts/abc.txt could not be loaded.");
        }
    }

    private enum TestPrompt implements Prompt {
        TEST {
            @Override
            public List<Replacement> getRequiredReplacements() {
                return List.of(Replacement.USER, Replacement.QUERY);
            }

            @Override
            public String getResource() {
                return "prompts/test.txt";
            }
        },
        NO_FILE {
            @Override
            public List<Replacement> getRequiredReplacements() {
                return List.of();
            }

            @Override
            public String getResource() {
                return "prompts/abc.txt";
            }
        }
    }

}