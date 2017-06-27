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
    @JsonProperty("deliveryLocation")
    private String deliveryLocation;
    @JsonProperty("numberOfCopies")
    private Integer numberOfCopies;
    @JsonProperty("neededBy")
    private String neededBy;

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
     * Gets delivery location.
     *
     * @return the pickup location
     */
    @JsonProperty("deliveryLocation")
    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    /**
     * Sets delivery location.
     *
     * @param deliveryLocation the delivery location
     */
    @JsonProperty("deliveryLocation")
    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
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

}
