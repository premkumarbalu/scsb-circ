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
        "deliveryMethod",
        "requestor",
        "requestorFirstName",
        "requestorLastName",
        "requestorMiddleName",
        "requestorEmail",
        "requestorOther",
        "priority",
        "notes",
        "requestDate",
        "requestTime",
        "errorCode",
        "errorNote"
})
public class GFAPwdTtItemResponse {

    @JsonProperty("CustomerCode")
    private String customerCode;
    @JsonProperty("itemBarcode")
    private String itemBarcode;
    @JsonProperty("destination")
    private String destination;
    @JsonProperty("deliveryMethod")
    private Object deliveryMethod;
    @JsonProperty("requestor")
    private String requestor;
    @JsonProperty("requestorFirstName")
    private Object requestorFirstName;
    @JsonProperty("requestorLastName")
    private Object requestorLastName;
    @JsonProperty("requestorMiddleName")
    private Object requestorMiddleName;
    @JsonProperty("requestorEmail")
    private Object requestorEmail;
    @JsonProperty("requestorOther")
    private Object requestorOther;
    @JsonProperty("priority")
    private Object priority;
    @JsonProperty("notes")
    private Object notes;
    @JsonProperty("requestDate")
    private Object requestDate;
    @JsonProperty("requestTime")
    private Object requestTime;
    @JsonProperty("errorCode")
    private Object errorCode;
    @JsonProperty("errorNote")
    private Object errorNote;

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
     * Gets delivery method.
     *
     * @return the delivery method
     */
    @JsonProperty("deliveryMethod")
    public Object getDeliveryMethod() {
        return deliveryMethod;
    }

    /**
     * Sets delivery method.
     *
     * @param deliveryMethod the delivery method
     */
    @JsonProperty("deliveryMethod")
    public void setDeliveryMethod(Object deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
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

    /**
     * Gets requestor first name.
     *
     * @return the requestor first name
     */
    @JsonProperty("requestorFirstName")
    public Object getRequestorFirstName() {
        return requestorFirstName;
    }

    /**
     * Sets requestor first name.
     *
     * @param requestorFirstName the requestor first name
     */
    @JsonProperty("requestorFirstName")
    public void setRequestorFirstName(Object requestorFirstName) {
        this.requestorFirstName = requestorFirstName;
    }

    /**
     * Gets requestor last name.
     *
     * @return the requestor last name
     */
    @JsonProperty("requestorLastName")
    public Object getRequestorLastName() {
        return requestorLastName;
    }

    /**
     * Sets requestor last name.
     *
     * @param requestorLastName the requestor last name
     */
    @JsonProperty("requestorLastName")
    public void setRequestorLastName(Object requestorLastName) {
        this.requestorLastName = requestorLastName;
    }

    /**
     * Gets requestor middle name.
     *
     * @return the requestor middle name
     */
    @JsonProperty("requestorMiddleName")
    public Object getRequestorMiddleName() {
        return requestorMiddleName;
    }

    /**
     * Sets requestor middle name.
     *
     * @param requestorMiddleName the requestor middle name
     */
    @JsonProperty("requestorMiddleName")
    public void setRequestorMiddleName(Object requestorMiddleName) {
        this.requestorMiddleName = requestorMiddleName;
    }

    /**
     * Gets requestor email.
     *
     * @return the requestor email
     */
    @JsonProperty("requestorEmail")
    public Object getRequestorEmail() {
        return requestorEmail;
    }

    /**
     * Sets requestor email.
     *
     * @param requestorEmail the requestor email
     */
    @JsonProperty("requestorEmail")
    public void setRequestorEmail(Object requestorEmail) {
        this.requestorEmail = requestorEmail;
    }

    /**
     * Gets requestor other.
     *
     * @return the requestor other
     */
    @JsonProperty("requestorOther")
    public Object getRequestorOther() {
        return requestorOther;
    }

    /**
     * Sets requestor other.
     *
     * @param requestorOther the requestor other
     */
    @JsonProperty("requestorOther")
    public void setRequestorOther(Object requestorOther) {
        this.requestorOther = requestorOther;
    }

    /**
     * Gets priority.
     *
     * @return the priority
     */
    @JsonProperty("priority")
    public Object getPriority() {
        return priority;
    }

    /**
     * Sets priority.
     *
     * @param priority the priority
     */
    @JsonProperty("priority")
    public void setPriority(Object priority) {
        this.priority = priority;
    }

    /**
     * Gets notes.
     *
     * @return the notes
     */
    @JsonProperty("notes")
    public Object getNotes() {
        return notes;
    }

    /**
     * Sets notes.
     *
     * @param notes the notes
     */
    @JsonProperty("notes")
    public void setNotes(Object notes) {
        this.notes = notes;
    }

    /**
     * Gets request date.
     *
     * @return the request date
     */
    @JsonProperty("requestDate")
    public Object getRequestDate() {
        return requestDate;
    }

    /**
     * Sets request date.
     *
     * @param requestDate the request date
     */
    @JsonProperty("requestDate")
    public void setRequestDate(Object requestDate) {
        this.requestDate = requestDate;
    }

    /**
     * Gets request time.
     *
     * @return the request time
     */
    @JsonProperty("requestTime")
    public Object getRequestTime() {
        return requestTime;
    }

    /**
     * Sets request time.
     *
     * @param requestTime the request time
     */
    @JsonProperty("requestTime")
    public void setRequestTime(Object requestTime) {
        this.requestTime = requestTime;
    }

    /**
     * Gets error code.
     *
     * @return the error code
     */
    @JsonProperty("errorCode")
    public Object getErrorCode() {
        return errorCode;
    }

    /**
     * Sets error code.
     *
     * @param errorCode the error code
     */
    @JsonProperty("errorCode")
    public void setErrorCode(Object errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Gets error note.
     *
     * @return the error note
     */
    @JsonProperty("errorNote")
    public Object getErrorNote() {
        return errorNote;
    }

    /**
     * Sets error note.
     *
     * @param errorNote the error note
     */
    @JsonProperty("errorNote")
    public void setErrorNote(Object errorNote) {
        this.errorNote = errorNote;
    }

}