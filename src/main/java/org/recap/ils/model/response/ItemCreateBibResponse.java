package org.recap.ils.model.response;

/**
 * Created by sudhishk on 16/12/16.
 */
public class ItemCreateBibResponse extends AbstractResponseItem {

    private String bibId;
    private String ItemId;

    /**
     * Gets item id.
     *
     * @return the item id
     */
    public String getItemId() {
        return ItemId;
    }

    /**
     * Sets item id.
     *
     * @param itemId the item id
     */
    public void setItemId(String itemId) {
        ItemId = itemId;
    }

    /**
     * Gets bib id.
     *
     * @return the bib id
     */
    public String getBibId() {
        return bibId;
    }

    /**
     * Sets bib id.
     *
     * @param bibId the bib id
     */
    public void setBibId(String bibId) {
        this.bibId = bibId;
    }


}
