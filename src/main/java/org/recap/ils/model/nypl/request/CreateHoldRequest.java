package org.recap.ils.model.nypl.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.recap.ils.model.nypl.Description;

/**
 * Created by rajeshbabuk on 7/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "trackingId",
        "owningInstitutionId",
        "itemBarcode",
        "patronBarcode",
        "description"
})
public class CreateHoldRequest {

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
