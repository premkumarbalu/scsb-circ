package org.recap.model.deaccession;

/**
 * Created by rajeshbabuk on 21/2/17.
 */
public class DeAccessionItem {

    private String itemBarcode;
    private String deliveryLocation;

    /**
     * Gets item barcode.
     *
     * @return the item barcode
     */
    public String getItemBarcode() {
        return itemBarcode;
    }

    /**
     * Sets item barcode.
     *
     * @param itemBarcode the item barcode
     */
    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    /**
     * Gets delivery location.
     *
     * @return the delivery location
     */
    public String getDeliveryLocation() {
        return null != deliveryLocation ? deliveryLocation.toUpperCase() : null;
    }

    /**
     * Sets delivery location.
     *
     * @param deliveryLocation the delivery location
     */
    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = null != deliveryLocation ? deliveryLocation.toUpperCase() : null;
    }
}
