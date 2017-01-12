package org.recap.ils.model;

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

    public String getItemBarcode() {
        return itemBarcode;
    }

    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    public String getItemOwningInstitution() {
        return itemOwningInstitution;
    }

    public void setItemOwningInstitution(String itemOwningInstitution) {
        this.itemOwningInstitution = itemOwningInstitution;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getScreenMessage() {
        return screenMessage;
    }

    public void setScreenMessage(String screenMessage) {
        this.screenMessage = screenMessage;
    }

    public String getEsipDataIn() {
        return esipDataIn;
    }

    public void setEsipDataIn(String esipDataIn) {
        this.esipDataIn = esipDataIn;
    }

    public String getEsipDataOut() {
        return esipDataOut;
    }

    public void setEsipDataOut(String esipDataOut) {
        this.esipDataOut = esipDataOut;
    }

}
