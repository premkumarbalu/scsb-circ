package org.recap.ils.model;

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
     *
     * @return
     * The id
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
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

    /**
     *
     * @return
     * The processed
     */
    @JsonProperty("processed")
    public Boolean getProcessed() {
        return processed;
    }

    /**
     *
     * @param processed
     * The processed
     */
    @JsonProperty("processed")
    public void setProcessed(Boolean processed) {
        this.processed = processed;
    }

    /**
     *
     * @return
     * The patron
     */
    @JsonProperty("patron")
    public Object getPatron() {
        return patron;
    }

    /**
     *
     * @param patron
     * The patron
     */
    @JsonProperty("patron")
    public void setPatron(Object patron) {
        this.patron = patron;
    }

    /**
     *
     * @return
     * The desiredDateDue
     */
    @JsonProperty("desiredDateDue")
    public String getDesiredDateDue() {
        return desiredDateDue;
    }

    /**
     *
     * @param desiredDateDue
     * The desiredDateDue
     */
    @JsonProperty("desiredDateDue")
    public void setDesiredDateDue(String desiredDateDue) {
        this.desiredDateDue = desiredDateDue;
    }

    /**
     *
     * @return
     * The attributes
     */
    @JsonProperty("@attributes")
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     *
     * @param attributes
     * The @attributes
     */
    @JsonProperty("@attributes")
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    /**
     *
     * @return
     * The checkOutItemResponse
     */
    @JsonProperty("CheckOutItemResponse")
    public CheckOutItemResponse getCheckOutItemResponse() {
        return checkOutItemResponse;
    }

    /**
     *
     * @param checkOutItemResponse
     * The CheckOutItemResponse
     */
    @JsonProperty("CheckOutItemResponse")
    public void setCheckOutItemResponse(CheckOutItemResponse checkOutItemResponse) {
        this.checkOutItemResponse = checkOutItemResponse;
    }

}