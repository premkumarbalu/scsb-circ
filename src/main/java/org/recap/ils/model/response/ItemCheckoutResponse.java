package org.recap.ils.model.response;

/**
 * Created by sudhishk on 16/12/16.
 */
public class ItemCheckoutResponse extends AbstractResponseItem {

    private boolean renewal;
    private boolean magneticMedia;
    private boolean desensitize;
    private String transactionDate;
    private String institutionID;
    private String patronIdentifier;
    private String titleIdentifier;
    private String dueDate;
    private String feeType ;
    private String securityInhibit;
    private String currencyType;
    private String feeAmount;
    private String mediaType;
    private String bibId;
    private String ISBN;
    private String LCCN;
    private String jobId;
    private boolean processed;
    private String updatedDate;
    private String createdDate;

    /**
     * Gets renewal.
     *
     * @return the renewal
     */
    public boolean getRenewal() {
        return renewal;
    }

    /**
     * Sets renewal.
     *
     * @param renewal the renewal
     */
    public void setRenewal(boolean renewal) {
        this.renewal = renewal;
    }

    /**
     * Gets magnetic media.
     *
     * @return the magnetic media
     */
    public boolean getMagneticMedia() {
        return magneticMedia;
    }

    /**
     * Sets magnetic media.
     *
     * @param magneticMedia the magnetic media
     */
    public void setMagneticMedia(boolean magneticMedia) {
        this.magneticMedia = magneticMedia;
    }

    /**
     * Gets desensitize.
     *
     * @return the desensitize
     */
    public boolean getDesensitize() {
        return desensitize;
    }

    /**
     * Sets desensitize.
     *
     * @param desensitize the desensitize
     */
    public void setDesensitize(boolean desensitize) {
        this.desensitize = desensitize;
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
     * Gets security inhibit.
     *
     * @return the security inhibit
     */
    public String getSecurityInhibit() {
        return securityInhibit;
    }

    /**
     * Sets security inhibit.
     *
     * @param securityInhibit the security inhibit
     */
    public void setSecurityInhibit(String securityInhibit) {
        this.securityInhibit = securityInhibit;
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
     * Gets fee amount.
     *
     * @return the fee amount
     */
    public String getFeeAmount() {
        return feeAmount;
    }

    /**
     * Sets fee amount.
     *
     * @param feeAmount the fee amount
     */
    public void setFeeAmount(String feeAmount) {
        this.feeAmount = feeAmount;
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
     * Is processed boolean.
     *
     * @return the boolean
     */
    public boolean isProcessed() {
        return processed;
    }

    /**
     * Sets processed.
     *
     * @param processed the processed
     */
    public void setProcessed(boolean processed) {
        this.processed = processed;
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
