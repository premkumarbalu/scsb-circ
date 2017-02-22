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

    @JsonProperty("CustomerCode")
    public String getCustomerCode() {
        return customerCode;
    }

    @JsonProperty("CustomerCode")
    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    @JsonProperty("itemBarcode")
    public String getItemBarcode() {
        return itemBarcode;
    }

    @JsonProperty("itemBarcode")
    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    @JsonProperty("errorCode")
    public String getErrorCode() {
        return errorCode;
    }

    @JsonProperty("errorCode")
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @JsonProperty("errorNote")
    public String getErrorNote() {
        return errorNote;
    }

    @JsonProperty("errorNote")
    public void setErrorNote(String errorNote) {
        this.errorNote = errorNote;
    }

}
