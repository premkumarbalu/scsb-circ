package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 7/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "updatedDate",
        "createdDate",
        "trackingId",
        "owningInstitutionId",
        "itemBarcode",
        "patronBarcode",
        "description"
})
public class CreateHoldData {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("updatedDate")
    private Object updatedDate;
    @JsonProperty("createdDate")
    private String createdDate;
    @JsonProperty("trackingId")
    private String trackingId;
    @JsonProperty("owningInstitutionId")
    private String owningInstitutionId;
    @JsonProperty("itemBarcode")
    private String itemBarcode;
    @JsonProperty("patronBarcode")
    private String patronBarcode;
    @JsonProperty("description")
    private Description description;

    /**
     * Gets id.
     *
     * @return The  id
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id The id
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * Gets updated date.
     *
     * @return The  updatedDate
     */
    @JsonProperty("updatedDate")
    public Object getUpdatedDate() {
        return updatedDate;
    }

    /**
     * Sets updated date.
     *
     * @param updatedDate The updatedDate
     */
    @JsonProperty("updatedDate")
    public void setUpdatedDate(Object updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * Gets created date.
     *
     * @return The  createdDate
     */
    @JsonProperty("createdDate")
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets created date.
     *
     * @param createdDate The createdDate
     */
    @JsonProperty("createdDate")
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Gets tracking id.
     *
     * @return The  trackingId
     */
    @JsonProperty("trackingId")
    public String getTrackingId() {
        return trackingId;
    }

    /**
     * Sets tracking id.
     *
     * @param trackingId The trackingId
     */
    @JsonProperty("trackingId")
    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    /**
     * Gets owning institution id.
     *
     * @return The  owningInstitutionId
     */
    @JsonProperty("owningInstitutionId")
    public String getOwningInstitutionId() {
        return owningInstitutionId;
    }

    /**
     * Sets owning institution id.
     *
     * @param owningInstitutionId The owningInstitutionId
     */
    @JsonProperty("owningInstitutionId")
    public void setOwningInstitutionId(String owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }

    /**
     * Gets item barcode.
     *
     * @return The  itemBarcode
     */
    @JsonProperty("itemBarcode")
    public String getItemBarcode() {
        return itemBarcode;
    }

    /**
     * Sets item barcode.
     *
     * @param itemBarcode The itemBarcode
     */
    @JsonProperty("itemBarcode")
    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    /**
     * Gets patron barcode.
     *
     * @return The  patronBarcode
     */
    @JsonProperty("patronBarcode")
    public String getPatronBarcode() {
        return patronBarcode;
    }

    /**
     * Sets patron barcode.
     *
     * @param patronBarcode The patronBarcode
     */
    @JsonProperty("patronBarcode")
    public void setPatronBarcode(String patronBarcode) {
        this.patronBarcode = patronBarcode;
    }

    /**
     * Gets description.
     *
     * @return The  description
     */
    @JsonProperty("description")
    public Description getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description The description
     */
    @JsonProperty("description")
    public void setDescription(Description description) {
        this.description = description;
    }

}
