package org.recap.ils.model.response;

import org.recap.ils.model.response.AbstractResponseItem;

/**
 * Created by sudhishk on 16/12/16.
 */
public class ItemCreateBibResponse extends AbstractResponseItem {

    private String bibId;
    private String ItemId;

    public String getItemId() {
        return ItemId;
    }

    public void setItemId(String itemId) {
        ItemId = itemId;
    }

    public String getBibId() {
        return bibId;
    }

    public void setBibId(String bibId) {
        this.bibId = bibId;
    }


}
