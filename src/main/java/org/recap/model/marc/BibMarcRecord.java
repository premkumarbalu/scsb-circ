package org.recap.model.marc;

import org.marc4j.marc.Record;

import java.util.List;

/**
 * Created by chenchulakshmig on 14/10/16.
 */
public class BibMarcRecord {
    Record bibRecord;
    List<HoldingsMarcRecord> holdingsMarcRecords;

    public Record getBibRecord() {
        return bibRecord;
    }

    public void setBibRecord(Record bibRecord) {
        this.bibRecord = bibRecord;
    }

    public List<HoldingsMarcRecord> getHoldingsMarcRecords() {
        return holdingsMarcRecords;
    }

    public void setHoldingsMarcRecords(List<HoldingsMarcRecord> holdingsMarcRecords) {
        this.holdingsMarcRecords = holdingsMarcRecords;
    }
}
