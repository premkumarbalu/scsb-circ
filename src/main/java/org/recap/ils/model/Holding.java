package org.recap.ils.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * Created by pvsubrah on 6/21/16.
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Holding implements Serializable {

    @XmlElement
    private String owningInstitutionHoldingsId;

    /**
     * The Content.
     */
    @XmlElement(required = true, nillable = true)
    protected ContentType content;

    @XmlElement
    private List<Items> items;

    /**
     * Gets owning institution holdings id.
     *
     * @return the owning institution holdings id
     */
    public String getOwningInstitutionHoldingsId() {
        return owningInstitutionHoldingsId;
    }

    /**
     * Sets owning institution holdings id.
     *
     * @param owningInstitutionHoldingsId the owning institution holdings id
     */
    public void setOwningInstitutionHoldingsId(String owningInstitutionHoldingsId) {
        this.owningInstitutionHoldingsId = owningInstitutionHoldingsId;
    }

    /**
     * Gets content.
     *
     * @return the content
     */
    public ContentType getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(ContentType content) {
        this.content = content;
    }

    /**
     * Gets items.
     *
     * @return the items
     */
    public List<Items> getItems() {
        return items;
    }

    /**
     * Sets items.
     *
     * @param items the items
     */
    public void setItems(List<Items> items) {
        this.items = items;
    }
}
