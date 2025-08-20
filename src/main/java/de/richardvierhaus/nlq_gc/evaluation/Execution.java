package de.richardvierhaus.nlq_gc.evaluation;

import com.google.gson.Gson;
import de.richardvierhaus.nlq_gc.encoding.EncodingMapping;
import de.richardvierhaus.nlq_gc.enums.*;
import de.richardvierhaus.nlq_gc.nlq.PromptBuilder;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Execution {

    private static final Gson GSON = new Gson();
    private static final String USER = "Maria";
    private static final List<EncodingMapping> ENCODINGS = createEncodings();
    private static final ModelLiterals MODEL = ModelLiterals.QWEN2_5_72B_INSTRUCT;

    private final Prompt prompt;
    private final Query nlq;

    private String fullPrompt;

    protected Execution(final Prompt prompt, final Query nlq) {
        this.prompt = prompt;
        this.nlq = nlq;
    }

    private Execution(final Prompt prompt, final Query nlq, final String fullPrompt) {
        this.prompt = prompt;
        this.nlq = nlq;
        this.fullPrompt = fullPrompt;
    }

    protected String getPath() {
        String goal = isKeywordPrompt() ? "keywords" : "gc";
        return String.format("evaluation/%s/%s/%s/%s/", goal, MODEL, prompt.toString(), nlq.name());
    }

    protected String getFileName(final ExecutionState state) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        return String.format("%s%s_%s.json", getPath(), state.name(), LocalDateTime.now().format(formatter));
    }

    protected boolean isKeywordPrompt() {
        if (prompt instanceof PromptKeyword) return true;
        if (prompt instanceof PromptGraphCode) return false;
        throw new IllegalArgumentException("Prompt Class unknown");
    }

    protected boolean isPossible() {
        // GC prompts that require keywords cannot be performed when the query does not provide keywords
        return isKeywordPrompt() || !((PromptGraphCode) prompt).requiresKeywords() || (getNlq().getKeywords() != null && !getNlq().getKeywords().isEmpty());
    }

    protected boolean expectErrors() {
        return nlq.getKeywords() == null || nlq.getMatrix() == null;
    }

    protected String getFullPrompt() {
        // lazy initialization
        if (!StringUtils.hasText(fullPrompt)) {
            PromptBuilder builder = new PromptBuilder(prompt)
                    .replace(Replacement.QUERY, nlq.getQuery())
                    .replaceIfRequired(Replacement.USER, USER)
                    .replaceIfRequired(Replacement.KEYWORDS, GSON.toJson(nlq.getKeywords()))
                    .replaceIfRequired(Replacement.ENCODING, GSON.toJson(ENCODINGS));
            fullPrompt = builder.toString();
        }
        return fullPrompt;
    }

    protected Query getNlq() {
        return nlq;
    }

    protected Prompt getPrompt() {
        return prompt;
    }

    private static List<EncodingMapping> createEncodings() {
        return List.of(
                new EncodingMapping(1, "object", true),
                new EncodingMapping(2, "context", true),
                new EncodingMapping(3, "synonym", true),
                new EncodingMapping(4, "activity", true),
                new EncodingMapping(5, "attached", false),
                new EncodingMapping(6, "under", false),
                new EncodingMapping(7, "above", false),
                new EncodingMapping(8, "perform", false),
                new EncodingMapping(9, "target", false),
                new EncodingMapping(10, "daughter", false),
                new EncodingMapping(14, "characteristic", false)
        );
    }

    protected static List<EncodingMapping> getEncodings() {
        return ENCODINGS;
    }

    public ModelLiterals getModel() {
        return MODEL;
    }

    protected Execution copy() {
        return new Execution(prompt, nlq, getFullPrompt());
    }

}