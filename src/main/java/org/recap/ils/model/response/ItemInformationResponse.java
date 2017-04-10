package org.recap.ils.model.response;

import java.util.List;

/**
 * Created by sudhishk on 15/12/16.
 */
public class ItemInformationResponse extends AbstractResponseItem {

    private Integer requestId;
    private String expirationDate;
    private String titleIdentifier;
    private String dueDate;
    private String circulationStatus;
    private String securityMarker;
    private String feeType;
    private String transactionDate;
    private String holdQueueLength = "0";
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
    private String callNumber;
    private String itemType;
    private List<String> bibIds;
    private String source;
    private String createdDate;
    private String updatedDate;
    private String deletedDate;
    private boolean isDeleted;
    private String patronBarcode = "";
    private String requestingInstitution = "";
    private String emailAddress = "";
    private String requestType = ""; // Retrieval,EDD, Hold, Recall, Borrow Direct
    private String deliveryLocation = "";
    private String requestNotes = "";
    private Integer itemId;
    private String username;

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

    /**
     * Gets circulation status.
     *
     * @return the circulation status
     */
    public String getCirculationStatus() {
        return circulationStatus;
    }

    /**
     * Sets circulation status.
     *
     * @param circulationStatus the circulation status
     */
    public void setCirculationStatus(String circulationStatus) {
        this.circulationStatus = circulationStatus;
    }

    /**
     * Gets security marker.
     *
     * @return the security marker
     */
    public String getSecurityMarker() {
        return securityMarker;
    }

    /**
     * Sets security marker.
     *
     * @param securityMarker the security marker
     */
    public void setSecurityMarker(String securityMarker) {
        this.securityMarker = securityMarker;
    }

    /**
     * Gets fee type.
     *
     * @return the fee type
     */
    public String getFeeType() {
        return feeType;
    }

    /**
     * Sets fee type.
     *
     * @param feeType the fee type
     */
    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    /**
     * Gets transaction date.
     *
     * @return the transaction date
     */
    public String getTransactionDate() {
        return transactionDate;
    }

    /**
     * Sets transaction date.
     *
     * @param transactionDate the transaction date
     */
    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    /**
     * Gets hold queue length.
     *
     * @return the hold queue length
     */
    public String getHoldQueueLength() {
        return holdQueueLength;
    }

    /**
     * Sets hold queue length.
     *
     * @param holdQueueLength the hold queue length
     */
    public void setHoldQueueLength(String holdQueueLength) {
        this.holdQueueLength = holdQueueLength;
    }

    /**
     * Gets recall date.
     *
     * @return the recall date
     */
    public String getRecallDate() {
        return recallDate;
    }

    /**
     * Sets recall date.
     *
     * @param recallDate the recall date
     */
    public void setRecallDate(String recallDate) {
        this.recallDate = recallDate;
    }

    /**
     * Gets owner.
     *
     * @return the owner
     */
    public String getOwner() {
        return Owner;
    }

    /**
     * Sets owner.
     *
     * @param owner the owner
     */
    public void setOwner(String owner) {
        Owner = owner;
    }

    /**
     * Gets media type.
     *
     * @return the media type
     */
    public String getMediaType() {
        return mediaType;
    }

    /**
     * Sets media type.
     *
     * @param mediaType the media type
     */
    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    /**
     * Gets permanent location.
     *
     * @return the permanent location
     */
    public String getPermanentLocation() {
        return permanentLocation;
    }

    /**
     * Sets permanent location.
     *
     * @param permanentLocation the permanent location
     */
    public void setPermanentLocation(String permanentLocation) {
        this.permanentLocation = permanentLocation;
    }

    /**
     * Gets current location.
     *
     * @return the current location
     */
    public String getCurrentLocation() {
        return currentLocation;
    }

    /**
     * Sets current location.
     *
     * @param currentLocation the current location
     */
    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    /**
     * Gets bib id.
     *
     * @return the bib id
     */
    public String getBibID() {
        return bibID;
    }

    /**
     * Sets bib id.
     *
     * @param bibID the bib id
     */
    public void setBibID(String bibID) {
        this.bibID = bibID;
    }

    /**
     * Gets isbn.
     *
     * @return the isbn
     */
    public String getISBN() {
        return ISBN;
    }

    /**
     * Sets isbn.
     *
     * @param ISBN the isbn
     */
    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
    }

    /**
     * Gets lccn.
     *
     * @return the lccn
     */
    public String getLCCN() {
        return LCCN;
    }

    /**
     * Sets lccn.
     *
     * @param LCCN the lccn
     */
    public void setLCCN(String LCCN) {
        this.LCCN = LCCN;
    }

    /**
     * Gets hold pickup date.
     *
     * @return the hold pickup date
     */
    public String getHoldPickupDate() {
        return holdPickupDate;
    }

    /**
     * Sets hold pickup date.
     *
     * @param holdPickupDate the hold pickup date
     */
    public void setHoldPickupDate(String holdPickupDate) {
        this.holdPickupDate = holdPickupDate;
    }

    /**
     * Gets currency type.
     *
     * @return the currency type
     */
    public String getCurrencyType() {
        return currencyType;
    }

    /**
     * Sets currency type.
     *
     * @param currencyType the currency type
     */
    public void setCurrencyType(String currencyType) {
        this.currencyType = currencyType;
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
     * Gets item type.
     *
     * @return the item type
     */
    public String getItemType() {
        return itemType;
    }

    /**
     * Sets item type.
     *
     * @param itemType the item type
     */
    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    /**
     * Gets bib ids.
     *
     * @return the bib ids
     */
    public List<String> getBibIds() {
        return bibIds;
    }

    /**
     * Sets bib ids.
     *
     * @param bibIds the bib ids
     */
    public void setBibIds(List<String> bibIds) {
        this.bibIds = bibIds;
    }

    /**
     * Gets source.
     *
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * Sets source.
     *
     * @param source the source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Gets created date.
     *
     * @return the created date
     */
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets created date.
     *
     * @param createdDate the created date
     */
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Gets updated date.
     *
     * @return the updated date
     */
    public String getUpdatedDate() {
        return updatedDate;
    }

    /**
     * Sets updated date.
     *
     * @param updatedDate the updated date
     */
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * Gets deleted date.
     *
     * @return the deleted date
     */
    public String getDeletedDate() {
        return deletedDate;
    }

    /**
     * Sets deleted date.
     *
     * @param deletedDate the deleted date
     */
    public void setDeletedDate(String deletedDate) {
        this.deletedDate = deletedDate;
    }

    /**
     * Is deleted boolean.
     *
     * @return the boolean
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Sets deleted.
     *
     * @param deleted the deleted
     */
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
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
     * Gets item id.
     *
     * @return the item id
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * Sets item id.
     *
     * @param itemId the item id
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
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
}
