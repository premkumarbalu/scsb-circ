package org.recap.model.csv;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.io.Serializable;

/**
 * Created by akulak on 4/5/17.
 */
@CsvRecord(generateHeaderColumns = true, separator = ",", quoting = true, crlf = "UNIX")
public class DailyReconcilationRecord implements Serializable{

    @DataField(pos = 1, columnName = "RequestId")
    private String requestId;

    @DataField(pos= 2 , columnName = "Barcode")
    private String barcode;

    @DataField(pos = 3 , columnName = "CustomerCode")
    private String customerCode;

    @DataField(pos = 4 , columnName = "StopCode")
    private String stopCode;

    @DataField(pos = 5 , columnName = "PatronId")
    private String patronId;

    @DataField(pos = 6 , columnName = "CreateDate")
    private String createDate;

    @DataField(pos = 7 , columnName = "LastUpdatedDate")
    private String lastUpdatedDate;

    @DataField(pos = 8 , columnName = "RequestingInst")
    private String requestingInst;

    @DataField(pos = 9 , columnName = "OwningInst")
    private String OwningInst;

    @DataField(pos = 10 , columnName = "DeliveryMethod")
    private String deliveryMethod;

    @DataField(pos = 11 , columnName = "Status")
    private String status;

    @DataField(pos = 12 , columnName = "Email")
    private String email;

    @DataField(pos = 13 , columnName = "ErrorCode")
    private String errorCode;

    @DataField(pos = 14 , columnName = "ErrorNote")
    private String errorNote;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getStopCode() {
        return stopCode;
    }

    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    public String getPatronId() {
        return patronId;
    }

    public void setPatronId(String patronId) {
        this.patronId = patronId;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getRequestingInst() {
        return requestingInst;
    }

    public void setRequestingInst(String requestingInst) {
        this.requestingInst = requestingInst;
    }

    public String getOwningInst() {
        return OwningInst;
    }

    public void setOwningInst(String owningInst) {
        OwningInst = owningInst;
    }

    public String getDeliveryMethod() {
        return deliveryMethod;
    }

    public void setDeliveryMethod(String deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorNote() {
        return errorNote;
    }

    public void setErrorNote(String errorNote) {
        this.errorNote = errorNote;
    }
}
