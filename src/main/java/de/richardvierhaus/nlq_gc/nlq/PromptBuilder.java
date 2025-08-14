package de.richardvierhaus.nlq_gc.nlq;

import de.richardvierhaus.nlq_gc.enums.Prompt;
import de.richardvierhaus.nlq_gc.enums.Replacement;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.MissingFormatArgumentException;

public class PromptBuilder {

    private final List<Replacement> leftoverReplacements;
    private String prompt;

    public PromptBuilder(final Prompt prompt) {
        leftoverReplacements = new ArrayList<>(prompt.getRequiredReplacements());
        this.prompt = readPrompt(prompt);
    }

    /**
     * Performs a replacement on the prompt in case it is left to be performed.
     *
     * @param replacement
     *         The {@link Replacement} to be performed.
     * @param replacementText
     *         The text to replace the replacement.
     * @return Current instance for the opportunity to keep building the prompt.
     * @throws UnsupportedOperationException
     *         in case the given replacement is not required (anymore).
     */
    public PromptBuilder replace(final Replacement replacement, final String replacementText) {
        if (!leftoverReplacements.remove(replacement))
            throw new UnsupportedOperationException(
                    String.format("The replacement %s either has already been replaced or is not required for the specified prompt.", replacement));
        prompt = prompt.replace(replacement.getName(), replacementText);
        return this;
    }

    /**
     * Performs a safe replacement on the prompt in case it is left to be performed.
     *
     * @param replacement
     *         The {@link Replacement} to be performed.
     * @param replacementText
     *         The text to replace the replacement.
     * @return Current instance for the opportunity to keep building the prompt.
     */
    public PromptBuilder replaceIfRequired(final Replacement replacement, final String replacementText) {
        if (leftoverReplacements.remove(replacement)) {
            prompt = prompt.replace(replacement.getName(), replacementText);
        }
        return this;
    }

    /**
     * Gives the completed prompt in case all replacements are completed.
     *
     * @return A string containing the final prompt.
     * @throws MissingFormatArgumentException
     *         in case not all necessary replacements have been performed yet.
     */
    @Override
    public String toString() {
        if (leftoverReplacements.isEmpty())
            return prompt;
        throw new MissingFormatArgumentException(String.format("Following replacements have not been handled: %s", leftoverReplacements));
    }

    /**
     * Reads the content of a file behind the given prompt.
     *
     * @param prompt
     *         The {@link Prompt} to be read.
     * @return The files content.
     */
    public static String readPrompt(final Prompt prompt) {
        InputStream inputStream = PromptBuilder.class.getClassLoader().getResourceAsStream(prompt.getResource());
        if (inputStream == null)
            throw new RuntimeException(String.format("The file with path %s could not be loaded.", prompt.getResource()));
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        return String.join("\n", reader.lines().toArray(String[]::new));
    }

    public List<Replacement> getLeftoverReplacements() {
        return List.copyOf(leftoverReplacements);
    }

}