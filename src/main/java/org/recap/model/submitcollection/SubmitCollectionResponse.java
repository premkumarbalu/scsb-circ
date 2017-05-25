package org.recap.model.submitcollection;

/**
 * Created by premkb on 24/5/17.
 */
public class SubmitCollectionResponse {

    private String itemBarcode;

    private String message;

    public String getItemBarcode() {
        return itemBarcode;
    }

    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
