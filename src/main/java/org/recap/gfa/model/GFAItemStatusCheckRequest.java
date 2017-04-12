package org.recap.gfa.model;

import java.util.List;

/**
 * Created by sudhishk on 27/1/17.
 */
public class GFAItemStatusCheckRequest {

    private List<GFAItemStatus> itemStatus;

    /**
     * Gets item status.
     *
     * @return the item status
     */
    public List<GFAItemStatus> getItemStatus() {
        return itemStatus;
    }

    /**
     * Sets item status.
     *
     * @param itemStatus the item status
     */
    public void setItemStatus(List<GFAItemStatus> itemStatus) {
        this.itemStatus = itemStatus;
    }
}
