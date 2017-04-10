package org.recap.gfa.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 21/2/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "CustomerCode",
        "itemBarcode",
        "errorCode",
        "errorNote"
})
public class GFAPwiTtItemResponse {

    @JsonProperty("CustomerCode")
    private String customerCode;
    @JsonProperty("itemBarcode")
    private String itemBarcode;
    @JsonProperty("errorCode")
    private String errorCode;
    @JsonProperty("errorNote")
    private String errorNote;

    /**
     * Gets customer code.
     *
     * @return the customer code
     */
    @JsonProperty("CustomerCode")
    public String getCustomerCode() {
        return customerCode;
    }

    /**
     * Sets customer code.
     *
     * @param customerCode the customer code
     */
    @JsonProperty("CustomerCode")
    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * Gets item barcode.
     *
     * @return the item barcode
     */
    @JsonProperty("itemBarcode")
    public String getItemBarcode() {
        return itemBarcode;
    }

    /**
     * Sets item barcode.
     *
     * @param itemBarcode the item barcode
     */
    @JsonProperty("itemBarcode")
    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    /**
     * Gets error code.
     *
     * @return the error code
     */
    @JsonProperty("errorCode")
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets error code.
     *
     * @param errorCode the error code
     */
    @JsonProperty("errorCode")
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Gets error note.
     *
     * @return the error note
     */
    @JsonProperty("errorNote")
    public String getErrorNote() {
        return errorNote;
    }

    /**
     * Sets error note.
     *
     * @param errorNote the error note
     */
    @JsonProperty("errorNote")
    public void setErrorNote(String errorNote) {
        this.errorNote = errorNote;
    }

}
