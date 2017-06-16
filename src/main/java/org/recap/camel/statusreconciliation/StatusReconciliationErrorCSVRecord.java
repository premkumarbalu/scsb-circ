package org.recap.camel.statusreconciliation;

import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import java.io.Serializable;

/**
 * Created by hemalathas on 5/6/17.
 */
@CsvRecord(generateHeaderColumns = true, separator = ",", quoting = true, crlf = "UNIX", skipFirstLine = true)
public class StatusReconciliationErrorCSVRecord implements Serializable {

    @DataField(pos = 1, columnName = "Barcode")
    private String barcode;

    @DataField(pos = 2, columnName = "Institution")
    private String institution;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }
}
