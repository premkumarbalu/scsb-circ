package org.recap.camel.statusreconciliation;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.io.Serializable;

/**
 * Created by hemalathas on 19/5/17.
 */
@CsvRecord(generateHeaderColumns = true, separator = ",", quoting = true, crlf = "UNIX", skipFirstLine = true)
public class StatusReconciliationCSVRecord implements Serializable{

    @DataField(pos = 1, columnName = "Barcode")
    private String barcode;

    @DataField(pos = 2, columnName = "RequestAvailability")
    private String requestAvailability;

    @DataField(pos = 3, columnName = "RequestId")
    private String requestId;

    @DataField(pos = 4, columnName = "StatusInScsb")
    private String statusInScsb;

    @DataField(pos = 5, columnName = "StatusInLas")
    private String statusInLas;

    @DataField(pos = 6, columnName = "DateTime")
    private String dateTime;

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
     * Gets request availability.
     *
     * @return the request availability
     */
    public String getRequestAvailability() {
        return requestAvailability;
    }

    /**
     * Sets request availability.
     *
     * @param requestAvailability the request availability
     */
    public void setRequestAvailability(String requestAvailability) {
        this.requestAvailability = requestAvailability;
    }

    /**
     * Gets request id.
     *
     * @return the request id
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Sets request id.
     *
     * @param requestId the request id
     */
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets date time.
     *
     * @return the date time
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * Sets date time.
     *
     * @param dateTime the date time
     */
    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    /**
     * Gets status in scsb.
     *
     * @return the status in scsb
     */
    public String getStatusInScsb() {
        return statusInScsb;
    }

    /**
     * Sets status in scsb.
     *
     * @param statusInScsb the status in scsb
     */
    public void setStatusInScsb(String statusInScsb) {
        this.statusInScsb = statusInScsb;
    }

    /**
     * Gets status in las.
     *
     * @return the status in las
     */
    public String getStatusInLas() {
        return statusInLas;
    }

    /**
     * Sets status in las.
     *
     * @param statusInLas the status in las
     */
    public void setStatusInLas(String statusInLas) {
        this.statusInLas = statusInLas;
    }
}
