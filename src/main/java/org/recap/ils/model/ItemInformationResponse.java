package org.recap.ils.model;

/**
 * Created by sudhishk on 15/12/16.
 */
public class ItemInformationResponse {

    private String itemBarcode;
    private String expirationDate;
    private String screenMessage;
    private boolean success;
    private String titleIdentifier;
    private String dueDate;
    private String circulationStatus;
    private String securityMarker;
    private String feeType;
    private String transactionDate;
    private String holdQueueLength;
    private String holdPickupDate;
    private String recallDate;
    private String Owner;
    private String mediaType;
    private String permanentLocation;
    private String currentLocation;
    private String bibID;
    private String ISBN;
    private String LCCN;
    private String currencyType;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getItemBarcode() {
        return itemBarcode;
    }

    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getScreenMessage() {
        return screenMessage;
    }

    public void setScreenMessage(String screenMessage) {
        this.screenMessage = screenMessage;
    }

    public String getTitleIdentifier() {
        return titleIdentifier;
    }

    public void setTitleIdentifier(String titleIdentifier) {
        this.titleIdentifier = titleIdentifier;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getCirculationStatus() {
        return circulationStatus;
    }

    public void setCirculationStatus(String circulationStatus) {
        this.circulationStatus = circulationStatus;
    }

    public String getSecurityMarker() {
        return securityMarker;
    }

    public void setSecurityMarker(String securityMarker) {
        this.securityMarker = securityMarker;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getHoldQueueLength() {
        return holdQueueLength;
    }

    public void setHoldQueueLength(String holdQueueLength) {
        this.holdQueueLength = holdQueueLength;
    }

    public String getRecallDate() {
        return recallDate;
    }

    public void setRecallDate(String recallDate) {
        this.recallDate = recallDate;
    }

    public String getOwner() {
        return Owner;
    }

    public void setOwner(String owner) {
        Owner = owner;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getPermanentLocation() {
        return permanentLocation;
    }

    public void setPermanentLocation(String permanentLocation) {
        this.permanentLocation = permanentLocation;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public String getBibID() {
        return bibID;
    }

    public void setBibID(String bibID) {
        this.bibID = bibID;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    public String getLCCN() {
        return LCCN;
    }

    public void setLCCN(String LCCN) {
        this.LCCN = LCCN;
    }

    public String getHoldPickupDate() {
        return holdPickupDate;
    }

    public void setHoldPickupDate(String holdPickupDate) {
        this.holdPickupDate = holdPickupDate;
    }

    public String getCurrencyType() {
        return currencyType;
    }

    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
    }

}
