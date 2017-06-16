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

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getRequestAvailability() {
        return requestAvailability;
    }

    public void setRequestAvailability(String requestAvailability) {
        this.requestAvailability = requestAvailability;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getStatusInScsb() {
        return statusInScsb;
    }

    public void setStatusInScsb(String statusInScsb) {
        this.statusInScsb = statusInScsb;
    }

    public String getStatusInLas() {
        return statusInLas;
    }

    public void setStatusInLas(String statusInLas) {
        this.statusInLas = statusInLas;
    }
}
