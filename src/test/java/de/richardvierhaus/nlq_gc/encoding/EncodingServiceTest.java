package de.richardvierhaus.nlq_gc.encoding;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EncodingServiceTest {

    @Autowired
    private EncodingService service;

    @Autowired
    private EncodingMappingRepository repository;

    @Test
    public void testTransactions() {
        EncodingMapping mapping1 = new EncodingMapping(1, "ABC", false);
        EncodingMapping mapping2 = new EncodingMapping(2, "TEST", true);
        EncodingMapping mapping3 = new EncodingMapping();
        mapping3.setId(1);
        mapping3.setAttribute("ABC2");
        mapping3.setIdentityMapping(false);

        service.addMapping(mapping1);
        service.addMapping(mapping2);
        assertThat(service.getEncodingMappings()).hasSize(2)
                .anyMatch(mapping -> mapping.getId() == 1 && mapping.getAttribute().equals("ABC") && !mapping.isIdentityMapping())
                .anyMatch(mapping -> mapping.getId() == 2 && mapping.getAttribute().equals("TEST") && mapping.isIdentityMapping());
        assertThat(service.getEncodingMappingsAsString()).isEqualTo("[{\"id\":1,\"attribute\":\"ABC\",\"identityMapping\":false},{\"id\":2,\"attribute\":\"TEST\",\"identityMapping\":true}]");

        service.addMapping(mapping3);

        service.removeMapping(2);
        assertThat(service.getEncodingMappings()).hasSize(1)
                .allMatch(mapping -> mapping.getId() == 1 && mapping.getAttribute().equals("TEST2") && !mapping.isIdentityMapping());

        service.clear();
        assertThat(service.getEncodingMappings()).isEmpty();
    }

    @Test
    public void testGetInstance() {
        assertThat(EncodingService.getInstance()).isNotNull();
    }

}