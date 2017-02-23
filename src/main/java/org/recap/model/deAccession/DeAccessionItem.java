package org.recap.model.deAccession;

/**
 * Created by rajeshbabuk on 21/2/17.
 */
public class DeAccessionItem {

    private String itemBarcode;
    private String deliveryLocation;

    public String getItemBarcode() {
        return itemBarcode;
    }

    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }
}
