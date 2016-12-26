package org.recap.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

/**
 * Created by hemalathas on 1/11/16.
 */
//@JsonIgnoreProperties({"itemBarcodes", "titleIdentifier", "patronBarcode","patronBarcode","emailAddress","requestingInstitution","requestingInstitution","requestType","deliveryLocation","requestNotes","startPage","endPage","chapterTitle"})
public class ItemRequestInformation {

    private List<String> itemBarcodes;
    private String titleIdentifier;
    private String itemOwningInstitution=""; // PUL, CUL, NYPL
    private String patronBarcode="";
    private String emailAddress="";
    private String requestingInstitution=""; // PUL, CUL, NYPL
    private String requestType=""; // Retrieval,EDD, Hold, Recall, Borrow Direct
    private String deliveryLocation="";
    private String requestNotes="";
    private String source; // NYPL - nyplSource
    private String trackingId; // NYPL - trackingId
    private String title; // NYPL - title
    private String author; // NYPL - author
    private String callNumber; // NYPL - callNumber

    /**
     * EDD Request
     */
    private Integer startPage=0;
    private Integer endPage=0;
    private String chapterTitle="";
    private String expirationDate;
    private String bibId;

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

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public Integer getStartPage() {
        return startPage;
    }

    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
    }

    public Integer getEndPage() {
        return endPage;
    }

    public void setEndPage(Integer endPage) {
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

    @JsonIgnore
    public boolean isOwningInstitutionItem(){
        boolean bSuccess=false;
        if (itemOwningInstitution.equalsIgnoreCase(requestingInstitution)){
            bSuccess=true;
        }else {
            bSuccess=false;
        }
        return bSuccess;
    }
}