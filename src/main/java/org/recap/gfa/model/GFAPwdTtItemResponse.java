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

    @JsonProperty("destination")
    public String getDestination() {
        return destination;
    }

    @JsonProperty("destination")
    public void setDestination(String destination) {
        this.destination = destination;
    }

    @JsonProperty("deliveryMethod")
    public Object getDeliveryMethod() {
        return deliveryMethod;
    }

    @JsonProperty("deliveryMethod")
    public void setDeliveryMethod(Object deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    @JsonProperty("requestor")
    public String getRequestor() {
        return requestor;
    }

    @JsonProperty("requestor")
    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }

    @JsonProperty("requestorFirstName")
    public Object getRequestorFirstName() {
        return requestorFirstName;
    }

    @JsonProperty("requestorFirstName")
    public void setRequestorFirstName(Object requestorFirstName) {
        this.requestorFirstName = requestorFirstName;
    }

    @JsonProperty("requestorLastName")
    public Object getRequestorLastName() {
        return requestorLastName;
    }

    @JsonProperty("requestorLastName")
    public void setRequestorLastName(Object requestorLastName) {
        this.requestorLastName = requestorLastName;
    }

    @JsonProperty("requestorMiddleName")
    public Object getRequestorMiddleName() {
        return requestorMiddleName;
    }

    @JsonProperty("requestorMiddleName")
    public void setRequestorMiddleName(Object requestorMiddleName) {
        this.requestorMiddleName = requestorMiddleName;
    }

    @JsonProperty("requestorEmail")
    public Object getRequestorEmail() {
        return requestorEmail;
    }

    @JsonProperty("requestorEmail")
    public void setRequestorEmail(Object requestorEmail) {
        this.requestorEmail = requestorEmail;
    }

    @JsonProperty("requestorOther")
    public Object getRequestorOther() {
        return requestorOther;
    }

    @JsonProperty("requestorOther")
    public void setRequestorOther(Object requestorOther) {
        this.requestorOther = requestorOther;
    }

    @JsonProperty("priority")
    public Object getPriority() {
        return priority;
    }

    @JsonProperty("priority")
    public void setPriority(Object priority) {
        this.priority = priority;
    }

    @JsonProperty("notes")
    public Object getNotes() {
        return notes;
    }

    @JsonProperty("notes")
    public void setNotes(Object notes) {
        this.notes = notes;
    }

    @JsonProperty("requestDate")
    public Object getRequestDate() {
        return requestDate;
    }

    @JsonProperty("requestDate")
    public void setRequestDate(Object requestDate) {
        this.requestDate = requestDate;
    }

    @JsonProperty("requestTime")
    public Object getRequestTime() {
        return requestTime;
    }

    @JsonProperty("requestTime")
    public void setRequestTime(Object requestTime) {
        this.requestTime = requestTime;
    }

    @JsonProperty("errorCode")
    public Object getErrorCode() {
        return errorCode;
    }

    @JsonProperty("errorCode")
    public void setErrorCode(Object errorCode) {
        this.errorCode = errorCode;
    }

    @JsonProperty("errorNote")
    public Object getErrorNote() {
        return errorNote;
    }

    @JsonProperty("errorNote")
    public void setErrorNote(Object errorNote) {
        this.errorNote = errorNote;
    }

}