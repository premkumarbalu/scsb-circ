package org.recap.ils.model.response;

/**
 * Created by sudhishk on 16/12/16.
 */
public class ItemHoldResponse extends AbstractResponseItem {

    private boolean available;
    private String transactionDate;
    private String institutionID;
    private String patronIdentifier;
    private String titleIdentifier;
    private String expirationDate;
    private String pickupLocation;
    private String queuePosition;
    private String bibId;
    private String ISBN;
    private String LCCN;
    private String trackingId;
    private String jobId;
    private String updatedDate;
    private String createdDate;


    /**
     * Gets available.
     *
     * @return the available
     */
    public boolean getAvailable() {
        return available;
    }

    /**
     * Sets available.
     *
     * @param available the available
     */
    public void setAvailable(boolean available) {
        this.available = available;
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
     * Gets institution id.
     *
     * @return the institution id
     */
    public String getInstitutionID() {
        return institutionID;
    }

    /**
     * Sets institution id.
     *
     * @param institutionID the institution id
     */
    public void setInstitutionID(String institutionID) {
        this.institutionID = institutionID;
    }

    /**
     * Gets patron identifier.
     *
     * @return the patron identifier
     */
    public String getPatronIdentifier() {
        return patronIdentifier;
    }

    /**
     * Sets patron identifier.
     *
     * @param patronIdentifier the patron identifier
     */
    public void setPatronIdentifier(String patronIdentifier) {
        this.patronIdentifier = patronIdentifier;
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
     * Gets pickup location.
     *
     * @return the pickup location
     */
    public String getPickupLocation() {
        return pickupLocation;
    }

    /**
     * Sets pickup location.
     *
     * @param pickupLocation the pickup location
     */
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    /**
     * Gets queue position.
     *
     * @return the queue position
     */
    public String getQueuePosition() {
        return queuePosition;
    }

    /**
     * Sets queue position.
     *
     * @param queuePosition the queue position
     */
    public void setQueuePosition(String queuePosition) {
        this.queuePosition = queuePosition;
    }

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
     * Gets job id.
     *
     * @return the job id
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Sets job id.
     *
     * @param jobId the job id
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
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
}
