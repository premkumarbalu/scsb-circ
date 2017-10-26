package org.recap.model.submitcollection;

import org.recap.model.BibliographicEntity;

import java.util.List;

/**
 * Created by premkb on 22/10/17.
 */
public class NonBoundWithBibliographicEntityObject {

    private String owningInstitutionBibId;

    private List<BibliographicEntity> bibliographicEntityList;

    public String getOwningInstitutionBibId() {
        return owningInstitutionBibId;
    }

    public void setOwningInstitutionBibId(String owningInstitutionBibId) {
        this.owningInstitutionBibId = owningInstitutionBibId;
    }

    public List<BibliographicEntity> getBibliographicEntityList() {
        return bibliographicEntityList;
    }

    public void setBibliographicEntityList(List<BibliographicEntity> bibliographicEntityList) {
        this.bibliographicEntityList = bibliographicEntityList;
    }
}
