package org.recap.gfa.model;

/**
 * Created by sudhishk on 27/1/17.
 */
public class Ttitem {
    private String itemBarcode;
    private String itemStatus;
    private String customerCode;
    private String destination;
    private String deliveryMethod;

    private String requestDate;
    private String requestTime;
    private String errorCode;
    private String errorNote;
    private Integer requestId;
    private String requestor;

    private String requestorFirstName;
    private String requestorLastName;
    private String requestorMiddleName;
    private String requestorEmail;
    private String requestorOther;
    private String biblioTitle;
    private String biblioLocation;
    private String biblioAuthor;
    private String biblioVolume;
    private String biblioCode;
    private String articleTitle;
    private String articleAuthor;
    private String articleVolume;
    private String articleIssue;
    private String articleDate;
    private String startPage;
    private String endPage;
    private String pages;
    private String other;
    private String priority;
    private String notes;

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
     * Gets item status.
     *
     * @return the item status
     */
    public String getItemStatus() {
        return itemStatus;
    }

    /**
     * Sets item status.
     *
     * @param itemStatus the item status
     */
    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
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
     * Gets destination.
     *
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets destination.
     *
     * @param destination the destination
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Gets delivery method.
     *
     * @return the delivery method
     */
    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    /**
     * Sets delivery method.
     *
     * @param deliveryMethod the delivery method
     */
    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    /**
     * Gets request date.
     *
     * @return the request date
     */
    public String getRequestDate() {
        return requestDate;
    }

    /**
     * Sets request date.
     *
     * @param requestDate the request date
     */
    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    /**
     * Gets request time.
     *
     * @return the request time
     */
    public String getRequestTime() {
        return requestTime;
    }

    /**
     * Sets request time.
     *
     * @param requestTime the request time
     */
    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    /**
     * Gets error code.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets error code.
     *
     * @param errorCode the error code
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Gets error note.
     *
     * @return the error note
     */
    public String getErrorNote() {
        return errorNote;
    }

    /**
     * Sets error note.
     *
     * @param errorNote the error note
     */
    public void setErrorNote(String errorNote) {
        this.errorNote = errorNote;
    }

    /**
     * Gets request id.
     *
     * @return the request id
     */
    public Integer getRequestId() {
        return requestId;
    }

    /**
     * Sets request id.
     *
     * @param requestId the request id
     */
    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRequestor() {
        return requestor;
    }

    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

    public String getRequestorFirstName() {
        return requestorFirstName;
    }

    public void setRequestorFirstName(String requestorFirstName) {
        this.requestorFirstName = requestorFirstName;
    }

    public String getRequestorLastName() {
        return requestorLastName;
    }

    public void setRequestorLastName(String requestorLastName) {
        this.requestorLastName = requestorLastName;
    }

    public String getRequestorMiddleName() {
        return requestorMiddleName;
    }

    public void setRequestorMiddleName(String requestorMiddleName) {
        this.requestorMiddleName = requestorMiddleName;
    }

    public String getRequestorEmail() {
        return requestorEmail;
    }

    public void setRequestorEmail(String requestorEmail) {
        this.requestorEmail = requestorEmail;
    }

    public String getRequestorOther() {
        return requestorOther;
    }

    public void setRequestorOther(String requestorOther) {
        this.requestorOther = requestorOther;
    }

    public String getBiblioTitle() {
        return biblioTitle;
    }

    public void setBiblioTitle(String biblioTitle) {
        this.biblioTitle = biblioTitle;
    }

    public String getBiblioLocation() {
        return biblioLocation;
    }

    public void setBiblioLocation(String biblioLocation) {
        this.biblioLocation = biblioLocation;
    }

    public String getBiblioAuthor() {
        return biblioAuthor;
    }

    public void setBiblioAuthor(String biblioAuthor) {
        this.biblioAuthor = biblioAuthor;
    }

    public String getBiblioVolume() {
        return biblioVolume;
    }

    public void setBiblioVolume(String biblioVolume) {
        this.biblioVolume = biblioVolume;
    }

    public String getBiblioCode() {
        return biblioCode;
    }

    public void setBiblioCode(String biblioCode) {
        this.biblioCode = biblioCode;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getArticleAuthor() {
        return articleAuthor;
    }

    public void setArticleAuthor(String articleAuthor) {
        this.articleAuthor = articleAuthor;
    }

    public String getArticleVolume() {
        return articleVolume;
    }

    public void setArticleVolume(String articleVolume) {
        this.articleVolume = articleVolume;
    }

    public String getArticleIssue() {
        return articleIssue;
    }

    public void setArticleIssue(String articleIssue) {
        this.articleIssue = articleIssue;
    }

    public String getArticleDate() {
        return articleDate;
    }

    public void setArticleDate(String articleDate) {
        this.articleDate = articleDate;
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

    public String getPages() {
        return pages;
    }

    public void setPages(String pages) {
        this.pages = pages;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        this.other = other;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }
}
