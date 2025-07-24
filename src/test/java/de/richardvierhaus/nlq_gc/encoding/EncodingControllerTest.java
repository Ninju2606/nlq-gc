package de.richardvierhaus.nlq_gc.encoding;

import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest
public class EncodingControllerTest {

    private EncodingController controller;
    private EncodingService service;
    private Gson gson;

    @BeforeEach
    public void init() {
        service = Mockito.mock(EncodingService.class);
        controller = Mockito.spy(new EncodingController(service));
        gson = new Gson();
    }

    @Test
    public void testSetEncoding() {
        // Prepare Data
        EncodingMapping mapping1 = new EncodingMapping(1, "ABC", false);
        EncodingMapping mapping2 = new EncodingMapping(2, "TEST", true);
        String json = gson.toJson(List.of(mapping1, mapping2));

        // Call
        controller.setEncoding(json);

        // Verification
        ArgumentCaptor<EncodingMapping> captor = ArgumentCaptor.forClass(EncodingMapping.class);
        verify(service).clear();
        verify(service, times(2)).addMapping(captor.capture());

        assertThat(captor.getAllValues()).hasSize(2)
                .anyMatch(mapping -> mapping.getId() == 1 && mapping.getAttribute().equals("ABC") && !mapping.isIdentityMapping())
                .anyMatch(mapping -> mapping.getId() == 2 && mapping.getAttribute().equals("TEST") && mapping.isIdentityMapping());
    }

    @Test
    public void testPutEncoding() {
        // Prepare Data
        EncodingMapping mapping = new EncodingMapping(1, "ABC", false);
        String json = gson.toJson(mapping);

        // Call
        controller.putEncoding(json);

        // Verification
        ArgumentCaptor<EncodingMapping> captor = ArgumentCaptor.forClass(EncodingMapping.class);
        verify(service).addMapping(captor.capture());
        assertThat(captor.getValue()).matches(m -> m.getId() == 1 && m.getAttribute().equals("ABC") && !m.isIdentityMapping());
    }

    @Test
    public void testRemoveEncoding() {
        controller.removeEncoding(123);
        verify(service).removeMapping(123);
    }

    @Test
    public void testClear() {
        controller.clear();
        verify(service).clear();
    }

}