package de.richardvierhaus.nlq_gc.encoding;

import com.google.gson.Gson;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EncodingService {

    private static EncodingService INSTANCE;

    private final Gson gson;
    private final EncodingMappingRepository repository;

    public EncodingService(final EncodingMappingRepository repository) {
        gson = new Gson();
        this.repository = repository;
        if (INSTANCE == null) INSTANCE = this;
    }

    /**
     * Provides a list of all stored {@link EncodingMapping}s.
     *
     * @return A {@link List} of {@link EncodingMapping}.
     */
    public List<EncodingMapping> getEncodingMappings() {
        return repository.findAll();
    }

    /**
     * Deletes all {@link EncodingMapping}s.
     */
    protected void clear() {
        repository.deleteAll();
    }

    /**
     * Adds the given {@link EncodingMapping}.
     *
     * @param mapping
     *         An {@link EncodingMapping}.
     */
    protected void addMapping(final EncodingMapping mapping) {
        repository.save(mapping);
    }

    /**
     * Deletes a {@link EncodingMapping} by the given id.
     *
     * @param id
     *         The {@link EncodingMapping}s id.
     */
    protected void removeMapping(final int id) {
        repository.deleteById(id);
    }

    /**
     * Provides a JSON string of the stored {@link EncodingMapping}s.
     *
     * @return JSON String with {@link EncodingMapping}s.
     */
    public String getEncodingMappingsAsString() {
        return gson.toJson(getEncodingMappings());
    }

    public static EncodingService getInstance() {
        return INSTANCE;
    }

}