package de.richardvierhaus.nlq_gc.enums;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class EnumControllerTest {

    @Autowired
    private EnumController controller;

    @Test
    public void testGetPromptsKeyword() {
        // TODO uncomment when enum filled and files created
        //        assertThat(controller.getPromptsKeyword()).hasSize(PromptKeyword.values().length);
    }

    @Test
    public void testGetPromptsGC() {
        // TODO uncomment when enum filled and files created
        //        assertThat(controller.getPromptsGC()).hasSize(PromptGraphCode.values().length);
    }

    @Test
    public void testGetModels() {
        assertThat(controller.getModels()).hasSize(ModelLiterals.values().length);
    }

}