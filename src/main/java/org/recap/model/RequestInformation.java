package org.recap.model;

import org.recap.ils.model.response.ItemInformationResponse;

/**
 * Created by sudhishk on 9/11/17.
 */
public class RequestInformation {
    private ItemRequestInformation itemRequestInfo;
    private ItemInformationResponse itemResponseInformation;

    public ItemRequestInformation getItemRequestInfo() {
        return itemRequestInfo;
    }

    public void setItemRequestInfo(ItemRequestInformation itemRequestInfo) {
        this.itemRequestInfo = itemRequestInfo;
    }

    public ItemInformationResponse getItemResponseInformation() {
        return itemResponseInformation;
    }

    public void setItemResponseInformation(ItemInformationResponse itemResponseInformation) {
        this.itemResponseInformation = itemResponseInformation;
    }
}
