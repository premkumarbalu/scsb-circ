package org.recap.ils.model;

import java.util.List;

/**
 * Created by sudhishk on 15/12/16.
 */
public class ItemInformationRequest {

    private List<String> itemBarcodes;

    private String itemOwningInstitution=""; // PUL, CUL, NYPL

    public List<String> getItemBarcodes() {
        return itemBarcodes;
    }

    public void setItemBarcodes(List<String> itemBarcodes) {
        this.itemBarcodes = itemBarcodes;
    }

    public String getItemOwningInstitution() {
        return itemOwningInstitution;
    }

    public void setItemOwningInstitution(String itemOwningInstitution) {
        this.itemOwningInstitution = itemOwningInstitution;
    }

}
