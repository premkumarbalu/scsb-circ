package org.recap.camel;

import java.io.Serializable;

/**
 * Created by chenchulakshmig on 15/9/16.
 */
public class EmailPayLoad implements Serializable{

    private String to;
    private String cc;
    private String subject;
    private String itemBarcode;
    private String patronBarcode;
    private String customerCode;
    private String messageDisplay;
    private String location;
    private String institution;
    private String reportFileName;
    private String xmlFileName;
    private Exception exception;
    private String exceptionMessage;
    private String pendingRequestLimit;

    private String bulkRequestId;
    private String bulkRequestName;
    private String bulkRequestFileName;
    private String bulkRequestStatus;
    private String bulkRequestCsvFileData;


    /**
     * Gets to.
     *
     * @return to to
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets to.
     *
     * @param to the to
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Gets cc.
     *
     * @return the cc
     */
    public String getCc() {
        return cc;
    }

    /**
     * Sets cc.
     *
     * @param cc the cc
     */
    public void setCc(String cc) {
        this.cc = cc;
    }

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
     * Gets patron barcode.
     *
     * @return the patron barcode
     */
    public String getPatronBarcode() {
        return patronBarcode;
    }

    /**
     * Sets patron barcode.
     *
     * @param patronBarcode the patron barcode
     */
    public void setPatronBarcode(String patronBarcode) {
        this.patronBarcode = patronBarcode;
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
     * Gets message display.
     *
     * @return the message display
     */
    public String getMessageDisplay() {
        return messageDisplay;
    }

    /**
     * Sets message display.
     *
     * @param messageDisplay the message display
     */
    public void setMessageDisplay(String messageDisplay) {
        this.messageDisplay = messageDisplay;
    }

    /**
     * Gets subject.
     *
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets subject.
     *
     * @param subject the subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets location.
     *
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets location.
     *
     * @param location the location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets institution.
     *
     * @return the institution
     */
    public String getInstitution() {
        return institution;
    }

    /**
     * Sets institution.
     *
     * @param institution the institution
     */
    public void setInstitution(String institution) {
        this.institution = institution;
    }

    /**
     * Gets file name.
     *
     * @return the file name
     */
    public String getReportFileName() {
        return reportFileName;
    }

    /**
     * Sets file name.
     *
     * @param reportFileName the file name
     */
    public void setReportFileName(String reportFileName) {
        this.reportFileName = reportFileName;
    }

    /**
     * Gets xml file name.
     *
     * @return the xml file name
     */
    public String getXmlFileName() {
        return xmlFileName;
    }

    /**
     * Sets xml file name.
     *
     * @param xmlFileName the xml file name
     */
    public void setXmlFileName(String xmlFileName) {
        this.xmlFileName = xmlFileName;
    }

    /**
     * Gets exception.
     *
     * @return the exception
     */
    public Exception getException() {
        return exception;
    }

    /**
     * Sets exception.
     *
     * @param exception the exception
     */
    public void setException(Exception exception) {
        this.exception = exception;
    }

    /**
     * Gets exception message.
     *
     * @return the exception message
     */
    public String getExceptionMessage() {
        return exceptionMessage;
    }

    /**
     * Sets exception message.
     *
     * @param exceptionMessage the exception message
     */
    public void setExceptionMessage(String exceptionMessage) {
        this.exceptionMessage = exceptionMessage;
    }

    /**
     * Gets pending request limit.
     *
     * @return the pending request limit
     */
    public String getPendingRequestLimit() {
        return pendingRequestLimit;
    }

    /**
     * Sets pending request limit.
     *
     * @param pendingRequestLimit the pending request limit
     */
    public void setPendingRequestLimit(String pendingRequestLimit) {
        this.pendingRequestLimit = pendingRequestLimit;
    }

    /**
     * Gets bulk request id.
     *
     * @return the bulk request id
     */
    public String getBulkRequestId() {
        return bulkRequestId;
    }

    /**
     * Sets bulk request id.
     *
     * @param bulkRequestId the bulk request id
     */
    public void setBulkRequestId(String bulkRequestId) {
        this.bulkRequestId = bulkRequestId;
    }

    /**
     * Gets bulk request name.
     *
     * @return the bulk request name
     */
    public String getBulkRequestName() {
        return bulkRequestName;
    }

    /**
     * Sets bulk request name.
     *
     * @param bulkRequestName the bulk request name
     */
    public void setBulkRequestName(String bulkRequestName) {
        this.bulkRequestName = bulkRequestName;
    }

    /**
     * Gets bulk request file name.
     *
     * @return the bulk request file name
     */
    public String getBulkRequestFileName() {
        return bulkRequestFileName;
    }

    /**
     * Sets bulk request file name.
     *
     * @param bulkRequestFileName the bulk request file name
     */
    public void setBulkRequestFileName(String bulkRequestFileName) {
        this.bulkRequestFileName = bulkRequestFileName;
    }

    /**
     * Gets bulk request status.
     *
     * @return the bulk request status
     */
    public String getBulkRequestStatus() {
        return bulkRequestStatus;
    }

    /**
     * Sets bulk request status.
     *
     * @param bulkRequestStatus the bulk request status
     */
    public void setBulkRequestStatus(String bulkRequestStatus) {
        this.bulkRequestStatus = bulkRequestStatus;
    }

    /**
     * Gets bulk request csv file data.
     *
     * @return the bulk request csv file data
     */
    public String getBulkRequestCsvFileData() {
        return bulkRequestCsvFileData;
    }

    /**
     * Sets bulk request csv file data.
     *
     * @param bulkRequestCsvFileData the bulk request csv file data
     */
    public void setBulkRequestCsvFileData(String bulkRequestCsvFileData) {
        this.bulkRequestCsvFileData = bulkRequestCsvFileData;
    }
}
