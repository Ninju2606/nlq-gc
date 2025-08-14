package de.richardvierhaus.nlq_gc.evaluation;

import com.google.gson.Gson;
import de.richardvierhaus.nlq_gc.encoding.EncodingMapping;
import de.richardvierhaus.nlq_gc.enums.Prompt;
import de.richardvierhaus.nlq_gc.enums.PromptGraphCode;
import de.richardvierhaus.nlq_gc.enums.PromptKeyword;
import de.richardvierhaus.nlq_gc.enums.Replacement;
import de.richardvierhaus.nlq_gc.nlq.PromptBuilder;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class Execution {

    private static final Gson GSON = new Gson();
    private static final String USER = "Maria";
    private static final List<EncodingMapping> ENCODINGS = createEncodings();

    private final Prompt prompt;
    private final Query nlq;
    private ExecutionState state = null;

    private String fullPrompt;

    public Execution(final Prompt prompt, final Query nlq) {
        this.prompt = prompt;
        this.nlq = nlq;
    }

    private Execution(final Prompt prompt, final Query nlq, final String fullPrompt) {
        this.prompt = prompt;
        this.nlq = nlq;
        this.fullPrompt = fullPrompt;
    }

    public String getPath() {
        String goal = isKeywordPrompt() ? "keywords" : "gc";
        return String.format("evaluation/%s/%s/%s/", goal, prompt.toString(), nlq.name());
    }

    public String getFileName() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH:mm:ss");
        return String.format("%s%s_%s.json", getPath(), state.name(), LocalDateTime.now().format(formatter));
    }

    public boolean isKeywordPrompt() {
        if (prompt instanceof PromptKeyword) return true;
        if (prompt instanceof PromptGraphCode) return false;
        throw new IllegalArgumentException("Prompt Class unknown");
    }

    public String getFullPrompt() {
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

    public void setState(final ExecutionState state) {
        this.state = state;
    }

    public ExecutionState getState() {
        return state;
    }

    public Query getNlq() {
        return nlq;
    }

    public Prompt getPrompt() {
        return prompt;
    }

    private static List<EncodingMapping> createEncodings() {
        return List.of(
                new EncodingMapping(1, "", true),
                new EncodingMapping(2, "", true),
                new EncodingMapping(3, "", true),
                new EncodingMapping(4, "", true),
                new EncodingMapping(5, "", true)
        );
    }

    protected static List<EncodingMapping> getEncodings() {
        return ENCODINGS;
    }

    public Execution copy() {
        return new Execution(prompt, nlq, getFullPrompt());
    }

}