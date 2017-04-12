package org.recap.model;

import java.io.Serializable;

/**
 * Created by angelind on 29/7/16.
 */
public class RequestInstitutionBibPK implements Serializable {
    private Integer itemId;
    private Integer owningInstitutionId;

    /**
     * Instantiates a new Request institution bib pk.
     */
    public RequestInstitutionBibPK(){
        //Do nothing
    }

    /**
     * Instantiates a new Request institution bib pk.
     *
     * @param itemId              the item id
     * @param owningInstitutionId the owning institution id
     */
    public RequestInstitutionBibPK(Integer itemId, Integer owningInstitutionId) {
        this.itemId = itemId;
        this.owningInstitutionId = owningInstitutionId;
    }

    /**
     * Gets item id.
     *
     * @return the item id
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * Sets item id.
     *
     * @param itemId the item id
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        RequestInstitutionBibPK requestInstitutionBibPK = (RequestInstitutionBibPK) o;

        if (itemId != null ? !itemId.equals(requestInstitutionBibPK.itemId) : requestInstitutionBibPK.itemId != null)
            return false;
        return owningInstitutionId != null ? owningInstitutionId.equals(requestInstitutionBibPK.owningInstitutionId) : requestInstitutionBibPK.owningInstitutionId == null;

    }

    @Override
    public int hashCode() {
        int result = itemId != null ? itemId.hashCode() : 0;
        result = 31 * result + (owningInstitutionId != null ? owningInstitutionId.hashCode() : 0);
        return result;
    }

}
