package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 6/1/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "patron",
        "jobId",
        "processed",
        "success",
        "updatedDate",
        "createdDate",
        "recordType",
        "record",
        "nyplSource",
        "pickupLocation",
        "neededBy",
        "numberOfCopies"
})
public class NYPLHoldData {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("patron")
    private String patron;
    @JsonProperty("jobId")
    private String jobId;
    @JsonProperty("processed")
    private Boolean processed;
    @JsonProperty("success")
    private Boolean success;
    @JsonProperty("updatedDate")
    private String updatedDate;
    @JsonProperty("createdDate")
    private String createdDate;
    @JsonProperty("recordType")
    private String recordType;
    @JsonProperty("record")
    private String record;
    @JsonProperty("nyplSource")
    private String nyplSource;
    @JsonProperty("pickupLocation")
    private String pickupLocation;
    @JsonProperty("neededBy")
    private String neededBy;
    @JsonProperty("numberOfCopies")
    private Integer numberOfCopies;

    /**
     * Gets id.
     *
     * @return the id
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets patron.
     *
     * @return the patron
     */
    @JsonProperty("patron")
    public String getPatron() {
        return patron;
    }

    /**
     * Sets patron.
     *
     * @param patron the patron
     */
    @JsonProperty("patron")
    public void setPatron(String patron) {
        this.patron = patron;
    }

    /**
     * Gets job id.
     *
     * @return the job id
     */
    @JsonProperty("jobId")
    public String getJobId() {
        return jobId;
    }

    /**
     * Sets job id.
     *
     * @param jobId the job id
     */
    @JsonProperty("jobId")
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    /**
     * Gets processed.
     *
     * @return the processed
     */
    @JsonProperty("processed")
    public Boolean getProcessed() {
        return processed;
    }

    /**
     * Sets processed.
     *
     * @param processed the processed
     */
    @JsonProperty("processed")
    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    /**
     * Gets success.
     *
     * @return the success
     */
    @JsonProperty("success")
    public Boolean getSuccess() {
        return success;
    }

    /**
     * Sets success.
     *
     * @param success the success
     */
    @JsonProperty("success")
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     * Gets updated date.
     *
     * @return the updated date
     */
    @JsonProperty("updatedDate")
    public String getUpdatedDate() {
        return updatedDate;
    }

    /**
     * Sets updated date.
     *
     * @param updatedDate the updated date
     */
    @JsonProperty("updatedDate")
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * Gets created date.
     *
     * @return the created date
     */
    @JsonProperty("createdDate")
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets created date.
     *
     * @param createdDate the created date
     */
    @JsonProperty("createdDate")
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Gets record type.
     *
     * @return the record type
     */
    @JsonProperty("recordType")
    public String getRecordType() {
        return recordType;
    }

    /**
     * Sets record type.
     *
     * @param recordType the record type
     */
    @JsonProperty("recordType")
    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    /**
     * Gets record.
     *
     * @return the record
     */
    @JsonProperty("record")
    public String getRecord() {
        return record;
    }

    /**
     * Sets record.
     *
     * @param record the record
     */
    @JsonProperty("record")
    public void setRecord(String record) {
        this.record = record;
    }

    /**
     * Gets nypl source.
     *
     * @return the nypl source
     */
    @JsonProperty("nyplSource")
    public String getNyplSource() {
        return nyplSource;
    }

    /**
     * Sets nypl source.
     *
     * @param nyplSource the nypl source
     */
    @JsonProperty("nyplSource")
    public void setNyplSource(String nyplSource) {
        this.nyplSource = nyplSource;
    }

    /**
     * Gets pickup location.
     *
     * @return the pickup location
     */
    @JsonProperty("pickupLocation")
    public String getPickupLocation() {
        return pickupLocation;
    }

    /**
     * Sets pickup location.
     *
     * @param pickupLocation the pickup location
     */
    @JsonProperty("pickupLocation")
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    /**
     * Gets needed by.
     *
     * @return the needed by
     */
    @JsonProperty("neededBy")
    public String getNeededBy() {
        return neededBy;
    }

    /**
     * Sets needed by.
     *
     * @param neededBy the needed by
     */
    @JsonProperty("neededBy")
    public void setNeededBy(String neededBy) {
        this.neededBy = neededBy;
    }

    /**
     * Gets number of copies.
     *
     * @return the number of copies
     */
    @JsonProperty("numberOfCopies")
    public Integer getNumberOfCopies() {
        return numberOfCopies;
    }

    /**
     * Sets number of copies.
     *
     * @param numberOfCopies the number of copies
     */
    @JsonProperty("numberOfCopies")
    public void setNumberOfCopies(Integer numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
    }

}