package org.recap.gfa.model;

import java.util.List;

/**
 * Created by sudhishk on 27/1/17.
 */
public class GFAItemStatusCheckRequest {

    private List<GFAItemStatus> itemStatus;

    public List<GFAItemStatus> getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(List<GFAItemStatus> itemStatus) {
        this.itemStatus = itemStatus;
    }
}
