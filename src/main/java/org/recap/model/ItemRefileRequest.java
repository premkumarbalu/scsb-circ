package org.recap.model;

import java.util.List;

/**
 * Created by sudhishk on 15/12/16.
 */
public class ItemRefileRequest {
    private List<String> itemBarcodes;
    private List<Integer> requestIds;

    public List<String> getItemBarcodes() {
        return itemBarcodes;
    }

    public void setItemBarcodes(List<String> itemBarcodes) {
        this.itemBarcodes = itemBarcodes;
    }

    public List<Integer> getRequestIds() {
        return requestIds;
    }

    public void setRequestIds(List<Integer> requestIds) {
        this.requestIds = requestIds;
    }
}
