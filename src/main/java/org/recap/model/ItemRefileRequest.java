package org.recap.model;

import java.util.List;

/**
 * Created by sudhishk on 15/12/16.
 */
public class ItemRefileRequest {
    private List<String> itemBarcodes;
    private List<Integer> requestIds;

    /**
     * Gets item barcodes.
     *
     * @return the item barcodes
     */
    public List<String> getItemBarcodes() {
        return itemBarcodes;
    }

    /**
     * Sets item barcodes.
     *
     * @param itemBarcodes the item barcodes
     */
    public void setItemBarcodes(List<String> itemBarcodes) {
        this.itemBarcodes = itemBarcodes;
    }

    /**
     * Gets request ids.
     *
     * @return the request ids
     */
    public List<Integer> getRequestIds() {
        return requestIds;
    }

    /**
     * Sets request ids.
     *
     * @param requestIds the request ids
     */
    public void setRequestIds(List<Integer> requestIds) {
        this.requestIds = requestIds;
    }
}
