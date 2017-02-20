package org.recap.model.deAccession;

import java.util.List;

/**
 * Created by chenchulakshmig on 11/10/16.
 */
public class DeAccessionRequest {
    private List<String> itemBarcodes;

    public List<String> getItemBarcodes() {
        return itemBarcodes;
    }

    public void setItemBarcodes(List<String> itemBarcodes) {
        this.itemBarcodes = itemBarcodes;
    }
}
