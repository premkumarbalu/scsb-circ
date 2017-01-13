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

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("patron")
    public String getPatron() {
        return patron;
    }

    @JsonProperty("patron")
    public void setPatron(String patron) {
        this.patron = patron;
    }

    @JsonProperty("jobId")
    public String getJobId() {
        return jobId;
    }

    @JsonProperty("jobId")
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    @JsonProperty("processed")
    public Boolean getProcessed() {
        return processed;
    }

    @JsonProperty("processed")
    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    @JsonProperty("success")
    public Boolean getSuccess() {
        return success;
    }

    @JsonProperty("success")
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    @JsonProperty("updatedDate")
    public String getUpdatedDate() {
        return updatedDate;
    }

    @JsonProperty("updatedDate")
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    @JsonProperty("createdDate")
    public String getCreatedDate() {
        return createdDate;
    }

    @JsonProperty("createdDate")
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @JsonProperty("recordType")
    public String getRecordType() {
        return recordType;
    }

    @JsonProperty("recordType")
    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    @JsonProperty("record")
    public String getRecord() {
        return record;
    }

    @JsonProperty("record")
    public void setRecord(String record) {
        this.record = record;
    }

    @JsonProperty("nyplSource")
    public String getNyplSource() {
        return nyplSource;
    }

    @JsonProperty("nyplSource")
    public void setNyplSource(String nyplSource) {
        this.nyplSource = nyplSource;
    }

    @JsonProperty("pickupLocation")
    public String getPickupLocation() {
        return pickupLocation;
    }

    @JsonProperty("pickupLocation")
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }

    @JsonProperty("neededBy")
    public String getNeededBy() {
        return neededBy;
    }

    @JsonProperty("neededBy")
    public void setNeededBy(String neededBy) {
        this.neededBy = neededBy;
    }

    @JsonProperty("numberOfCopies")
    public Integer getNumberOfCopies() {
        return numberOfCopies;
    }

    @JsonProperty("numberOfCopies")
    public void setNumberOfCopies(Integer numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
    }

}