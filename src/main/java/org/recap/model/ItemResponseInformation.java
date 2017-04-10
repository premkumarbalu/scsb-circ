package org.recap.model;


/**
 * Created by hemalathas on 1/11/16.
 */
public class ItemResponseInformation {

    private String patronBarcode;
    private String itemBarcode;
    private String requestType;
    private String deliveryLocation;
    private String requestingInstitution;
    private String bibliographicId;
    private String expirationDate;
    private String screenMessage;
    private boolean success;
    private String emailAddress;
    private Integer startPage;
    private Integer endPage;
    private String titleIdentifier;
    private String bibiid;
    private String dueDate;


    /**
     * Gets bibliographic id.
     *
     * @return the bibliographic id
     */
    public String getBibliographicId() {
        return bibliographicId;
    }

    /**
     * Sets bibliographic id.
     *
     * @param bibliographicId the bibliographic id
     */
    public void setBibliographicId(String bibliographicId) {
        this.bibliographicId = bibliographicId;
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
     * Gets start page.
     *
     * @return the start page
     */
    public Integer getStartPage() {
        return startPage;
    }

    /**
     * Sets start page.
     *
     * @param startPage the start page
     */
    public void setStartPage(Integer startPage) {
        this.startPage = startPage;
    }

    /**
     * Gets end page.
     *
     * @return the end page
     */
    public Integer getEndPage() {
        return endPage;
    }

    /**
     * Sets end page.
     *
     * @param endPage the end page
     */
    public void setEndPage(Integer endPage) {
        this.endPage = endPage;
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
     * Gets bibiid.
     *
     * @return the bibiid
     */
    public String getBibiid() {
        return bibiid;
    }

    /**
     * Sets bibiid.
     *
     * @param bibiid the bibiid
     */
    public void setBibiid(String bibiid) {
        this.bibiid = bibiid;
    }

    /**
     * Gets due date.
     *
     * @return the due date
     */
    public String getDueDate() {
        return dueDate;
    }

    /**
     * Sets due date.
     *
     * @param dueDate the due date
     */
    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }




}
