package de.richardvierhaus.nlq_gc.encoding;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class EncodingMapping {

    @Id
    private int id;
    private String attribute;
    private boolean identityMapping;

    protected EncodingMapping() {
    }

    /**
     * Creates a new {@link EncodingMapping} instance.
     *
     * @param id
     *         The id value.
     * @param attribute
     *         An attribute description.
     * @param identityMapping
     *         <code>true</code> if the id is only allowed to be on the main diagonal of the graph code value matrix.
     */
    public EncodingMapping(final int id, final String attribute, final boolean identityMapping) {
        this.id = id;
        this.attribute = attribute;
        this.identityMapping = identityMapping;
    }

    public int getId() {
        return id;
    }

    public String getAttribute() {
        return attribute;
    }

    public boolean isIdentityMapping() {
        return identityMapping;
    }

    public void setId(final int id) {
        this.id = id;
    }

    public void setAttribute(final String attribute) {
        this.attribute = attribute;
    }

    public void setIdentityMapping(final boolean identityMapping) {
        this.identityMapping = identityMapping;
    }

}