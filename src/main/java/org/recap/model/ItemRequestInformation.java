package org.recap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Created by hemalathas on 1/11/16.
 */
public class ItemRequestInformation {

    private List<String> itemBarcodes;
    private String titleIdentifier;
    private String itemOwningInstitution = ""; // PUL, CUL, NYPL
    private String patronBarcode = "";
    private String emailAddress = "";
    private String requestingInstitution = ""; // PUL, CUL, NYPL
    private String requestType = ""; // Retrieval,EDD, Hold, Recall, Borrow Direct
    private String deliveryLocation = "";
    private String customerCode = "";
    private String requestNotes = "";
    private String trackingId; // NYPL - trackingId
    private String author; // NYPL - author
    private String callNumber; // NYPL - callNumber

    /**
     * EDD Request
     */
    private String startPage;
    private String endPage;
    private String chapterTitle = "";
    private String expirationDate;
    private String bibId = "";
    private String username;

    public String getBibId() {
        return bibId;
    }

    public void setBibId(String bibId) {
        this.bibId = bibId;
    }

    public String getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getTitleIdentifier() {
        return titleIdentifier;
    }

    public void setTitleIdentifier(String titleIdentifier) {
        this.titleIdentifier = titleIdentifier;
    }

    public String getRequestNotes() {
        return requestNotes;
    }

    public void setRequestNotes(String requestNotes) {
        this.requestNotes = requestNotes;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    public String getPatronBarcode() {
        return patronBarcode;
    }

    public void setPatronBarcode(String patronBarcode) {
        this.patronBarcode = patronBarcode;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getRequestingInstitution() {
        return requestingInstitution;
    }

    public void setRequestingInstitution(String requestingInstitution) {
        this.requestingInstitution = requestingInstitution;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public List<String> getItemBarcodes() {
        return itemBarcodes;
    }

    public void setItemBarcodes(List<String> itemBarcodes) {
        this.itemBarcodes = itemBarcodes;
    }

    public String getStartPage() {
        return startPage;
    }

    public void setStartPage(String startPage) {
        this.startPage = startPage;
    }

    public String getEndPage() {
        return endPage;
    }

    public void setEndPage(String endPage) {
        this.endPage = endPage;
    }

    public String getChapterTitle() {
        return chapterTitle;
    }

    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
    }

    public void setItemOwningInstitution(String itemOwningInstitution) {
        this.itemOwningInstitution = itemOwningInstitution;
    }

    public String getItemOwningInstitution() {
        return this.itemOwningInstitution;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @JsonIgnore
    public boolean isOwningInstitutionItem() {
        boolean bSuccess;
        if (itemOwningInstitution.equalsIgnoreCase(requestingInstitution)) {
            bSuccess = true;
        } else {
            bSuccess = false;
        }
        return bSuccess;
    }
}