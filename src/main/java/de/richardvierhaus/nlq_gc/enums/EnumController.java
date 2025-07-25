package de.richardvierhaus.nlq_gc.enums;

import de.richardvierhaus.nlq_gc.llm.LanguageModel;
import de.richardvierhaus.nlq_gc.nlq.PromptBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/enums")
public class EnumController {

    /**
     * A GET-route which returns a mapping of all possible prompts to extract keywords. The key represents the
     * identification while the value contains the prompt.
     *
     * @return A {@link Map} with information from {@link PromptKeyword}.
     */
    @GetMapping("/promptsKeyword")
    public Map<String, String> getPromptsKeyword() {
        return Arrays.stream(PromptKeyword.values()).collect(Collectors.toMap(Enum::name, PromptBuilder::readPrompt));
    }

    /**
     * A GET-route which returns a mapping of all possible prompts to generate graph codes. The key represents the
     * identification while the value contains the prompt.
     *
     * @return A {@link Map} with information from {@link PromptGraphCode}.
     */
    @GetMapping("/promptsGC")
    public Map<String, String> getPromptsGC() {
        return Arrays.stream(PromptGraphCode.values()).collect(Collectors.toMap(Enum::name, PromptBuilder::readPrompt));
    }

    /**
     * A GET-route which returns a listing of all available {@link LanguageModel}s.
     *
     * @return A {@link List} with {@link LanguageModel} instances.
     */
    @GetMapping("/models")
    public List<String> getModels() {
        return Arrays.stream(ModelLiterals.values()).map(Enum::name).toList();
    }

}