package org.recap.gfa.model;

/**
 * Created by sudhishk on 27/1/17.
 */
public class Ttitem {
    private String itemBarcode;
    private String itemStatus;
    private String customerCode;
    private String destination;
    private String deliveryMethod;

    private String requestDate;
    private String requestTime;
    private String errorCode;
    private String errorNote;
    private Integer requestId;

    /**
     * Gets item barcode.
     *
     * @return the item barcode
     */
    public String getItemBarcode() {
        return itemBarcode;
    }

    /**
     * Sets item barcode.
     *
     * @param itemBarcode the item barcode
     */
    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    /**
     * Gets item status.
     *
     * @return the item status
     */
    public String getItemStatus() {
        return itemStatus;
    }

    /**
     * Sets item status.
     *
     * @param itemStatus the item status
     */
    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
    }

    /**
     * Gets customer code.
     *
     * @return the customer code
     */
    public String getCustomerCode() {
        return customerCode;
    }

    /**
     * Sets customer code.
     *
     * @param customerCode the customer code
     */
    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * Gets destination.
     *
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Sets destination.
     *
     * @param destination the destination
     */
    public void setDestination(String destination) {
        this.destination = destination;
    }

    /**
     * Gets delivery method.
     *
     * @return the delivery method
     */
    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    /**
     * Sets delivery method.
     *
     * @param deliveryMethod the delivery method
     */
    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    /**
     * Gets request date.
     *
     * @return the request date
     */
    public String getRequestDate() {
        return requestDate;
    }

    /**
     * Sets request date.
     *
     * @param requestDate the request date
     */
    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }

    /**
     * Gets request time.
     *
     * @return the request time
     */
    public String getRequestTime() {
        return requestTime;
    }

    /**
     * Sets request time.
     *
     * @param requestTime the request time
     */
    public void setRequestTime(String requestTime) {
        this.requestTime = requestTime;
    }

    /**
     * Gets error code.
     *
     * @return the error code
     */
    public String getErrorCode() {
        return errorCode;
    }

    /**
     * Sets error code.
     *
     * @param errorCode the error code
     */
    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    /**
     * Gets error note.
     *
     * @return the error note
     */
    public String getErrorNote() {
        return errorNote;
    }

    /**
     * Sets error note.
     *
     * @param errorNote the error note
     */
    public void setErrorNote(String errorNote) {
        this.errorNote = errorNote;
    }

    /**
     * Gets request id.
     *
     * @return the request id
     */
    public Integer getRequestId() {
        return requestId;
    }

    /**
     * Sets request id.
     *
     * @param requestId the request id
     */
    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }
}
