package org.recap.camel.requestinitialdataload;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;
import java.io.Serializable;

/**
 * Created by hemalathas on 3/5/17.
 */
@CsvRecord(generateHeaderColumns = true, separator = ",", quoting = true, crlf = "UNIX", skipFirstLine = false)
public class RequestDataLoadCSVRecord implements Serializable {

    @DataField(pos = 1, columnName = "Barcode")
    private String barcode;

    @DataField(pos = 2, columnName = "CustomerCode")
    private String customerCode;

    @DataField(pos = 3, columnName = "DeliveryMethod")
    private String deliveryMethod;

    @DataField(pos = 4, columnName = "CreateDate")
    private String createdDate;

    @DataField(pos = 5, columnName = "LastUpdatedDate")
    private String lastUpdatedDate;

    @DataField(pos = 6, columnName = "PatronId")
    private String patronId;

    @DataField(pos = 7, columnName = "StopCode")
    private String stopCode;

    @DataField(pos = 8, columnName = "Email")
    private String email;

    /**
     * Gets barcode.
     *
     * @return the barcode
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Sets barcode.
     *
     * @param barcode the barcode
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
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
     * Gets created date.
     *
     * @return the created date
     */
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets created date.
     *
     * @param createdDate the created date
     */
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Gets last updated date.
     *
     * @return the last updated date
     */
    public String getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    /**
     * Sets last updated date.
     *
     * @param lastUpdatedDate the last updated date
     */
    public void setLastUpdatedDate(String lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    /**
     * Gets patron id.
     *
     * @return the patron id
     */
    public String getPatronId() {
        return patronId;
    }

    /**
     * Sets patron id.
     *
     * @param patronId the patron id
     */
    public void setPatronId(String patronId) {
        this.patronId = patronId;
    }

    /**
     * Gets stop code.
     *
     * @return the stop code
     */
    public String getStopCode() {
        return stopCode;
    }

    /**
     * Sets stop code.
     *
     * @param stopCode the stop code
     */
    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets email.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        this.email = email;
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
}
