package org.recap.gfa.model;

/**
 * Created by sudhishk on 8/12/16.
 */
public class GFAItemStatusCheckResponse
{
    private String itemBarcode;
    private String itemStatus;
    private String itemOwner;

    public String getItemBarcode() {
        return itemBarcode;
    }

    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    public String getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    public String getItemOwner() {
        return itemOwner;
    }

    public void setItemOwner(String itemOwner) {
        this.itemOwner = itemOwner;
    }
}
