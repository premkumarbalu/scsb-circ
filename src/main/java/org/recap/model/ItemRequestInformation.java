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
    private String issue;
    private String volume;
    private String itemAuthor;
    private String itemVolume;
    private Integer requestId;


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

    /**
     * Gets expiration date.
     *
     * @return the expiration date
     */
    public String getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets expiration date.
     *
     * @param expirationDate the expiration date
     */
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Gets title identifier.
     *
     * @return the title identifier
     */
    public String getTitleIdentifier() {
        return titleIdentifier;
    }

    /**
     * Sets title identifier.
     *
     * @param titleIdentifier the title identifier
     */
    public void setTitleIdentifier(String titleIdentifier) {
        this.titleIdentifier = titleIdentifier;
    }

    /**
     * Gets request notes.
     *
     * @return the request notes
     */
    public String getRequestNotes() {
        return requestNotes;
    }

    /**
     * Sets request notes.
     *
     * @param requestNotes the request notes
     */
    public void setRequestNotes(String requestNotes) {
        this.requestNotes = requestNotes;
    }

    /**
     * Gets tracking id.
     *
     * @return the tracking id
     */
    public String getTrackingId() {
        return trackingId;
    }

    /**
     * Sets tracking id.
     *
     * @param trackingId the tracking id
     */
    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    /**
     * Gets author.
     *
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets author.
     *
     * @param author the author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets call number.
     *
     * @return the call number
     */
    public String getCallNumber() {
        return callNumber;
    }

    /**
     * Sets call number.
     *
     * @param callNumber the call number
     */
    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    /**
     * Gets patron barcode.
     *
     * @return the patron barcode
     */
    public String getPatronBarcode() {
        return patronBarcode;
    }

    /**
     * Sets patron barcode.
     *
     * @param patronBarcode the patron barcode
     */
    public void setPatronBarcode(String patronBarcode) {
        this.patronBarcode = patronBarcode;
    }

    /**
     * Gets request type.
     *
     * @return the request type
     */
    public String getRequestType() {
        return requestType;
    }

    /**
     * Sets request type.
     *
     * @param requestType the request type
     */
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    /**
     * Gets delivery location.
     *
     * @return the delivery location
     */
    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    /**
     * Sets delivery location.
     *
     * @param deliveryLocation the delivery location
     */
    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    /**
     * Gets requesting institution.
     *
     * @return the requesting institution
     */
    public String getRequestingInstitution() {
        return requestingInstitution;
    }

    /**
     * Sets requesting institution.
     *
     * @param requestingInstitution the requesting institution
     */
    public void setRequestingInstitution(String requestingInstitution) {
        this.requestingInstitution = requestingInstitution;
    }

    /**
     * Gets email address.
     *
     * @return the email address
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets email address.
     *
     * @param emailAddress the email address
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Gets item barcodes.
     *
     * @return the item barcodes
     */
    public List<String> getItemBarcodes() {
        return itemBarcodes;
    }

    /**
     * Sets item barcodes.
     *
     * @param itemBarcodes the item barcodes
     */
    public void setItemBarcodes(List<String> itemBarcodes) {
        this.itemBarcodes = itemBarcodes;
    }

    /**
     * Gets start page.
     *
     * @return the start page
     */
    public String getStartPage() {
        return startPage;
    }

    /**
     * Sets start page.
     *
     * @param startPage the start page
     */
    public void setStartPage(String startPage) {
        this.startPage = startPage;
    }

    /**
     * Gets end page.
     *
     * @return the end page
     */
    public String getEndPage() {
        return endPage;
    }

    /**
     * Sets end page.
     *
     * @param endPage the end page
     */
    public void setEndPage(String endPage) {
        this.endPage = endPage;
    }

    /**
     * Gets chapter title.
     *
     * @return the chapter title
     */
    public String getChapterTitle() {
        return chapterTitle;
    }

    /**
     * Sets chapter title.
     *
     * @param chapterTitle the chapter title
     */
    public void setChapterTitle(String chapterTitle) {
        this.chapterTitle = chapterTitle;
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
     * Gets item owning institution.
     *
     * @return the item owning institution
     */
    public String getItemOwningInstitution() {
        return this.itemOwningInstitution;
    }

    /**
     * Gets customer code.
     *
     * @return the customer code
     */
    public String getCustomerCode() {
        return customerCode;
    }

    /**
     * Sets customer code.
     *
     * @param customerCode the customer code
     */
    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets issue.
     *
     * @return the issue
     */
    public String getIssue() {
        return issue;
    }

    /**
     * Sets issue.
     *
     * @param issue the issue
     */
    public void setIssue(String issue) {
        this.issue = issue;
    }

    /**
     * Gets volume.
     *
     * @return the volume
     */
    public String getVolume() {
        return volume;
    }

    /**
     * Sets volume.
     *
     * @param volume the volume
     */
    public void setVolume(String volume) {
        this.volume = volume;
    }

    /**
     * Gets item author.
     *
     * @return the item author
     */
    public String getItemAuthor() {
        return itemAuthor;
    }

    /**
     * Sets item author.
     *
     * @param itemAuthor the item author
     */
    public void setItemAuthor(String itemAuthor) {
        this.itemAuthor = itemAuthor;
    }

    /**
     * Gets item volume.
     *
     * @return the item volume
     */
    public String getItemVolume() {
        return itemVolume;
    }

    /**
     * Sets item volume.
     *
     * @param itemVolume the item volume
     */
    public void setItemVolume(String itemVolume) {
        this.itemVolume = itemVolume;
    }

    /**
     * Sets Request Id
     *
     * @return
     */
    public Integer getRequestId() {
        return requestId;
    }

    /**
     * Gets Request Id
     *
     * @param requestId
     */
    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    /**
     * Is owning institution item boolean.
     *
     * @return the boolean
     */
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