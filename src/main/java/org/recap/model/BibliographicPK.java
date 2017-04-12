package org.recap.model;

import java.io.Serializable;

/**
 * Created by pvsubrah on 7/7/16.
 */
public class BibliographicPK implements Serializable {
    private Integer owningInstitutionId;
    private String owningInstitutionBibId;

    /**
     * Instantiates a new Bibliographic pk.
     */
    public BibliographicPK() {
        //Do nothing
    }

    /**
     * Instantiates a new Bibliographic pk.
     *
     * @param owningInstitutionId    the owning institution id
     * @param owningInstitutionBibId the owning institution bib id
     */
    public BibliographicPK(Integer owningInstitutionId, String owningInstitutionBibId) {
        this.owningInstitutionId = owningInstitutionId;
        this.owningInstitutionBibId = owningInstitutionBibId;
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
     * Gets owning institution bib id.
     *
     * @return the owning institution bib id
     */
    public String getOwningInstitutionBibId() {
        return owningInstitutionBibId;
    }

    /**
     * Sets owning institution bib id.
     *
     * @param owningInstitutionBibId the owning institution bib id
     */
    public void setOwningInstitutionBibId(String owningInstitutionBibId) {
        this.owningInstitutionBibId = owningInstitutionBibId;
    }



    @Override
    public int hashCode() {
        return Integer.valueOf(owningInstitutionId.toString()+owningInstitutionBibId);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BibliographicPK bibliographicPK  = (BibliographicPK) obj;
        if(bibliographicPK.getOwningInstitutionId().equals(owningInstitutionId) && bibliographicPK.getOwningInstitutionBibId().equals(owningInstitutionBibId)){
            return true;
        }

        return false;
    }
}
