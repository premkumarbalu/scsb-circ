package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 9/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "itemBarcode",
        "patronBarcode",
        "processed",
        "patron",
        "desiredDateDue",
        "@attributes",
        "CheckOutItemResponse"
})
public class NoticesData {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("itemBarcode")
    private String itemBarcode;
    @JsonProperty("patronBarcode")
    private String patronBarcode;
    @JsonProperty("processed")
    private Boolean processed;
    @JsonProperty("patron")
    private Object patron;
    @JsonProperty("desiredDateDue")
    private String desiredDateDue;
    @JsonProperty("@attributes")
    private Attributes attributes;
    @JsonProperty("CheckOutItemResponse")
    private CheckOutItemResponse checkOutItemResponse;

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
     * Gets processed.
     *
     * @return The  processed
     */
    @JsonProperty("processed")
    public Boolean getProcessed() {
        return processed;
    }

    /**
     * Sets processed.
     *
     * @param processed The processed
     */
    @JsonProperty("processed")
    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    /**
     * Gets patron.
     *
     * @return The  patron
     */
    @JsonProperty("patron")
    public Object getPatron() {
        return patron;
    }

    /**
     * Sets patron.
     *
     * @param patron The patron
     */
    @JsonProperty("patron")
    public void setPatron(Object patron) {
        this.patron = patron;
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

    /**
     * Gets attributes.
     *
     * @return The  attributes
     */
    @JsonProperty("@attributes")
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * Sets attributes.
     *
     * @param attributes The @attributes
     */
    @JsonProperty("@attributes")
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    /**
     * Gets check out item response.
     *
     * @return The  checkOutItemResponse
     */
    @JsonProperty("CheckOutItemResponse")
    public CheckOutItemResponse getCheckOutItemResponse() {
        return checkOutItemResponse;
    }

    /**
     * Sets check out item response.
     *
     * @param checkOutItemResponse The CheckOutItemResponse
     */
    @JsonProperty("CheckOutItemResponse")
    public void setCheckOutItemResponse(CheckOutItemResponse checkOutItemResponse) {
        this.checkOutItemResponse = checkOutItemResponse;
    }

}