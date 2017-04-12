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
        "destination",
        "requestor"
})
public class GFAPwdTtItemRequest {

    @JsonProperty("CustomerCode")
    private String customerCode;
    @JsonProperty("itemBarcode")
    private String itemBarcode;
    @JsonProperty("destination")
    private String destination;
    @JsonProperty("requestor")
    private String requestor;

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
     * Gets destination.
     *
     * @return the destination
     */
    @JsonProperty("destination")
    public String getDestination() {
        return destination;
    }

    /**
     * Sets destination.
     *
     * @param destination the destination
     */
    @JsonProperty("destination")
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Gets requestor.
     *
     * @return the requestor
     */
    @JsonProperty("requestor")
    public String getRequestor() {
        return requestor;
    }

    /**
     * Sets requestor.
     *
     * @param requestor the requestor
     */
    @JsonProperty("requestor")
    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

}
