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

    @PutMapping("/fill")
    public void setEncoding(@RequestParam final String mappings) {
        service.clear();
        List<EncodingMapping> mappingsParsed = List.of(gson.fromJson(mappings, EncodingMapping[].class));
        mappingsParsed.forEach(service::addMapping);
    }

    @PutMapping("/add")
    public void putEncoding(@RequestParam final String mapping) {
        EncodingMapping mappingParsed = gson.fromJson(mapping, EncodingMapping.class);
        service.addMapping(mappingParsed);
    }

    @DeleteMapping("/remove")
    public void removeEncoding(@RequestParam final int id) {
        service.removeMapping(id);
    }

    @DeleteMapping("/clear")
    public void clear() {
        service.clear();
    }

}