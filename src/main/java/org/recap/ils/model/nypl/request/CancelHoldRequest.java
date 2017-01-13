package org.recap.ils.model.nypl.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 8/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "trackingId",
        "owningInstitutionId",
        "itemBarcode",
        "patronBarcode"
})
public class CancelHoldRequest {

    @JsonProperty("trackingId")
    private String trackingId;
    @JsonProperty("owningInstitutionId")
    private String owningInstitutionId;
    @JsonProperty("itemBarcode")
    private String itemBarcode;
    @JsonProperty("patronBarcode")
    private String patronBarcode;

    /**
     *
     * @return
     * The trackingId
     */
    @JsonProperty("trackingId")
    public String getTrackingId() {
        return trackingId;
    }

    /**
     *
     * @param trackingId
     * The trackingId
     */
    @JsonProperty("trackingId")
    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    /**
     *
     * @return
     * The owningInstitutionId
     */
    @JsonProperty("owningInstitutionId")
    public String getOwningInstitutionId() {
        return owningInstitutionId;
    }

    /**
     *
     * @param owningInstitutionId
     * The owningInstitutionId
     */
    @JsonProperty("owningInstitutionId")
    public void setOwningInstitutionId(String owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }

    /**
     *
     * @return
     * The itemBarcode
     */
    @JsonProperty("itemBarcode")
    public String getItemBarcode() {
        return itemBarcode;
    }

    /**
     *
     * @param itemBarcode
     * The itemBarcode
     */
    @JsonProperty("itemBarcode")
    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    /**
     *
     * @return
     * The patronBarcode
     */
    @JsonProperty("patronBarcode")
    public String getPatronBarcode() {
        return patronBarcode;
    }

    /**
     *
     * @param patronBarcode
     * The patronBarcode
     */
    @JsonProperty("patronBarcode")
    public void setPatronBarcode(String patronBarcode) {
        this.patronBarcode = patronBarcode;
    }

}