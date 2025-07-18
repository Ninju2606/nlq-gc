package de.richardvierhaus.nlq_gc.enums;

import java.util.List;

public interface Prompt {

    /**
     * Gives a {@link List} of {@link Replacement}s required to execute this prompt.
     *
     * @return The {@link Replacement}s for this prompt.
     */
    List<Replacement> getRequiredReplacements();

    /**
     * Gives the path where the prompt can be found and loaded from.
     *
     * @return The path where the prompt is stored
     */
    String getResource();

}