package org.recap.ils.model.nypl.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 8/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "patronBarcode",
        "itemBarcode",
        "desiredDateDue"
})
public class CheckoutRequest {

    @JsonProperty("patronBarcode")
    private String patronBarcode;
    @JsonProperty("itemBarcode")
    private String itemBarcode;
    @JsonProperty("desiredDateDue")
    private String desiredDateDue;

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
     * Gets desired date due.
     *
     * @return The  desiredDateDue
     */
    @JsonProperty("desiredDateDue")
    public String getDesiredDateDue() {
        return desiredDateDue;
    }

    /**
     * Sets desired date due.
     *
     * @param desiredDateDue The desiredDateDue
     */
    @JsonProperty("desiredDateDue")
    public void setDesiredDateDue(String desiredDateDue) {
        this.desiredDateDue = desiredDateDue;
    }

}




