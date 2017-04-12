package org.recap.ils.model.response;

/**
 * Created by sudhishk on 15/12/16.
 */
public abstract class AbstractResponseItem {

    private String itemBarcode;
    private String itemOwningInstitution=""; // PUL, CUL, NYPL
    private String screenMessage;
    private boolean success;
    private String esipDataIn;
    private String esipDataOut;

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
     * Gets item owning institution.
     *
     * @return the item owning institution
     */
    public String getItemOwningInstitution() {
        return itemOwningInstitution;
    }

    /**
     * Sets item owning institution.
     *
     * @param itemOwningInstitution the item owning institution
     */
    public void setItemOwningInstitution(String itemOwningInstitution) {
        this.itemOwningInstitution = itemOwningInstitution;
    }

    /**
     * Is success boolean.
     *
     * @return the boolean
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets success.
     *
     * @param success the success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets screen message.
     *
     * @return the screen message
     */
    public String getScreenMessage() {
        return screenMessage;
    }

    /**
     * Sets screen message.
     *
     * @param screenMessage the screen message
     */
    public void setScreenMessage(String screenMessage) {
        this.screenMessage = screenMessage;
    }

    /**
     * Gets esip data in.
     *
     * @return the esip data in
     */
    public String getEsipDataIn() {
        return esipDataIn;
    }

    /**
     * Sets esip data in.
     *
     * @param esipDataIn the esip data in
     */
    public void setEsipDataIn(String esipDataIn) {
        this.esipDataIn = esipDataIn;
    }

    /**
     * Gets esip data out.
     *
     * @return the esip data out
     */
    public String getEsipDataOut() {
        return esipDataOut;
    }

    /**
     * Sets esip data out.
     *
     * @param esipDataOut the esip data out
     */
    public void setEsipDataOut(String esipDataOut) {
        this.esipDataOut = esipDataOut;
    }

}
