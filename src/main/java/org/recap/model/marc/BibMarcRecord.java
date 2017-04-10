package org.recap.model.marc;

import org.marc4j.marc.Record;

import java.util.List;

/**
 * Created by chenchulakshmig on 14/10/16.
 */
public class BibMarcRecord {
    /**
     * The Bib record.
     */
    Record bibRecord;
    /**
     * The Holdings marc records.
     */
    List<HoldingsMarcRecord> holdingsMarcRecords;

    /**
     * Gets bib record.
     *
     * @return the bib record
     */
    public Record getBibRecord() {
        return bibRecord;
    }

    /**
     * Sets bib record.
     *
     * @param bibRecord the bib record
     */
    public void setBibRecord(Record bibRecord) {
        this.bibRecord = bibRecord;
    }

    /**
     * Gets holdings marc records.
     *
     * @return the holdings marc records
     */
    public List<HoldingsMarcRecord> getHoldingsMarcRecords() {
        return holdingsMarcRecords;
    }

    /**
     * Sets holdings marc records.
     *
     * @param holdingsMarcRecords the holdings marc records
     */
    public void setHoldingsMarcRecords(List<HoldingsMarcRecord> holdingsMarcRecords) {
        this.holdingsMarcRecords = holdingsMarcRecords;
    }
}
