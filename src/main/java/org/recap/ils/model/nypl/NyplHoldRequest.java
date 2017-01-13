package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 9/1/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "patron",
        "recordType",
        "record",
        "nyplSource",
        "pickupLocation",
        "numberOfCopies",
        "neededBy"
})
public class NyplHoldRequest {

    @JsonProperty("patron")
    private String patron;
    @JsonProperty("recordType")
    private String recordType;
    @JsonProperty("record")
    private String record;
    @JsonProperty("nyplSource")
    private String nyplSource;
    @JsonProperty("pickupLocation")
    private String pickupLocation;
    @JsonProperty("numberOfCopies")
    private Integer numberOfCopies;
    @JsonProperty("neededBy")
    private String neededBy;

    @JsonProperty("patron")
    public String getPatron() {
        return patron;
    }

    @JsonProperty("patron")
    public void setPatron(String patron) {
        this.patron = patron;
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

    @JsonProperty("numberOfCopies")
    public Integer getNumberOfCopies() {
        return numberOfCopies;
    }

    @JsonProperty("numberOfCopies")
    public void setNumberOfCopies(Integer numberOfCopies) {
        this.numberOfCopies = numberOfCopies;
    }

    @JsonProperty("neededBy")
    public String getNeededBy() {
        return neededBy;
    }

    @JsonProperty("neededBy")
    public void setNeededBy(String neededBy) {
        this.neededBy = neededBy;
    }

}
