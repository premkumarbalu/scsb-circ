package org.recap.model.submitcollection;

import org.recap.model.BibliographicEntity;

/**
 * Created by premkb on 14/10/17.
 */
public class BarcodeBibliographicEntityObject {

    private String barcode;

    private String owningInstitutionBibId;

    private BibliographicEntity bibliographicEntity;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getOwningInstitutionBibId() {
        return owningInstitutionBibId;
    }

    public void setOwningInstitutionBibId(String owningInstitutionBibId) {
        this.owningInstitutionBibId = owningInstitutionBibId;
    }

    public BibliographicEntity getBibliographicEntity() {
        return bibliographicEntity;
    }

    public void setBibliographicEntity(BibliographicEntity bibliographicEntity) {
        this.bibliographicEntity = bibliographicEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BarcodeBibliographicEntityObject that = (BarcodeBibliographicEntityObject) o;

        if (barcode != null ? !barcode.equals(that.barcode) : that.barcode != null) return false;
        if (owningInstitutionBibId != null ? !owningInstitutionBibId.equals(that.owningInstitutionBibId) : that.owningInstitutionBibId != null)
            return false;
        return bibliographicEntity != null ? bibliographicEntity.equals(that.bibliographicEntity) : that.bibliographicEntity == null;
    }

    @Override
    public int hashCode() {
        int result = barcode != null ? barcode.hashCode() : 0;
        result = 31 * result + (owningInstitutionBibId != null ? owningInstitutionBibId.hashCode() : 0);
        result = 31 * result + (bibliographicEntity != null ? bibliographicEntity.hashCode() : 0);
        return result;
    }

}
