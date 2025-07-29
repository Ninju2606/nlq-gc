package de.richardvierhaus.nlq_gc.encoding;

import com.google.gson.Gson;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/encoding")
public class EncodingController {

    private final EncodingService service;
    private final Gson gson;

    public EncodingController(final EncodingService service) {
        this.service = service;
        this.gson = new Gson();
    }

    /**
     * Fills the stored {@link EncodingMapping}s with the given {@link List}.
     *
     * @param mappings
     *         A JSON string of {@link EncodingMapping}s.
     */
    @PutMapping("/fill")
    public void setEncoding(@RequestBody final String mappings) {
        service.clear();
        List<EncodingMapping> mappingsParsed = List.of(gson.fromJson(mappings, EncodingMapping[].class));
        mappingsParsed.forEach(service::addMapping);
    }

    /**
     * Adds the given {@link EncodingMapping}.
     *
     * @param mapping
     *         An {@link EncodingMapping}.
     */
    @PutMapping("/add")
    public void putEncoding(@RequestBody final String mapping) {
        EncodingMapping mappingParsed = gson.fromJson(mapping, EncodingMapping.class);
        service.addMapping(mappingParsed);
    }

    /**
     * Deletes an {@link EncodingMapping} by the given id.
     *
     * @param id
     *         The ID of the {@link EncodingMapping} to delete.
     */
    @DeleteMapping("/remove")
    public void removeEncoding(@RequestParam final int id) {
        service.removeMapping(id);
    }

    /**
     * Deletes all {@link EncodingMapping}s.
     */
    @DeleteMapping("/clear")
    public void clear() {
        service.clear();
    }

    /**
     * Provides all stored {@link EncodingMapping}s.
     *
     * @return A {@link List} of {@link EncodingMapping}s.
     */
    @GetMapping("/")
    public List<EncodingMapping> getEncodingMappings() {
        return service.getEncodingMappings();
    }

}