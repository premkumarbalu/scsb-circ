package org.recap.model;

import java.io.Serializable;

/**
 * Created by angelind on 29/7/16.
 */
public class ItemPK implements Serializable {
    private Integer owningInstitutionId;
    private String owningInstitutionItemId;


    /**
     * Instantiates a new Item pk.
     */
    public ItemPK(){
        //Do nothing
    }

    /**
     * Instantiates a new Item pk.
     *
     * @param owningInstitutionId     the owning institution id
     * @param owningInstitutionItemId the owning institution item id
     */
    public ItemPK(Integer owningInstitutionId, String owningInstitutionItemId) {
        this.owningInstitutionId = owningInstitutionId;
        this.owningInstitutionItemId = owningInstitutionItemId;
    }

    /**
     * Gets owning institution id.
     *
     * @return the owning institution id
     */
    public Integer getOwningInstitutionId() {
        return owningInstitutionId;
    }

    /**
     * Sets owning institution id.
     *
     * @param owningInstitutionId the owning institution id
     */
    public void setOwningInstitutionId(Integer owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }

    /**
     * Gets owning institution item id.
     *
     * @return the owning institution item id
     */
    public String getOwningInstitutionItemId() {
        return owningInstitutionItemId;
    }

    /**
     * Sets owning institution item id.
     *
     * @param owningInstitutionItemId the owning institution item id
     */
    public void setOwningInstitutionItemId(String owningInstitutionItemId) {
        this.owningInstitutionItemId = owningInstitutionItemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ItemPK itemPK = (ItemPK) o;

        if (owningInstitutionId != null ? !owningInstitutionId.equals(itemPK.owningInstitutionId) : itemPK.owningInstitutionId != null)
            return false;
        return owningInstitutionItemId != null ? owningInstitutionItemId.equals(itemPK.owningInstitutionItemId) : itemPK.owningInstitutionItemId == null;

    }

    @Override
    public int hashCode() {
        int result = owningInstitutionId != null ? owningInstitutionId.hashCode() : 0;
        result = 31 * result + (owningInstitutionItemId != null ? owningInstitutionItemId.hashCode() : 0);
        return result;
    }

}
