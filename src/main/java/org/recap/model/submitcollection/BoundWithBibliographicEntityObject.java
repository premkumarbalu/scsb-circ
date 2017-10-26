package org.recap.model.submitcollection;

import org.recap.model.BibliographicEntity;

import java.util.List;

/**
 * Created by premkb on 22/10/17.
 */
public class BoundWithBibliographicEntityObject {

    private String barcode;

    private List<BibliographicEntity> bibliographicEntityList;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public List<BibliographicEntity> getBibliographicEntityList() {
        return bibliographicEntityList;
    }

    public void setBibliographicEntityList(List<BibliographicEntity> bibliographicEntityList) {
        this.bibliographicEntityList = bibliographicEntityList;
    }
}
