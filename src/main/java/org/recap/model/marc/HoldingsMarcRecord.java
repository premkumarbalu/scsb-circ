package org.recap.model.marc;

import org.marc4j.marc.Record;

import java.util.List;

/**
 * Created by chenchulakshmig on 14/10/16.
 */
public class HoldingsMarcRecord {
    /**
     * The Holdings record.
     */
    Record holdingsRecord;
    /**
     * The Item marc record list.
     */
    List<ItemMarcRecord> itemMarcRecordList;

    /**
     * Gets holdings record.
     *
     * @return the holdings record
     */
    public Record getHoldingsRecord() {
        return holdingsRecord;
    }

    /**
     * Sets holdings record.
     *
     * @param holdingsRecord the holdings record
     */
    public void setHoldingsRecord(Record holdingsRecord) {
        this.holdingsRecord = holdingsRecord;
    }

    /**
     * Gets item marc record list.
     *
     * @return the item marc record list
     */
    public List<ItemMarcRecord> getItemMarcRecordList() {
        return itemMarcRecordList;
    }

    /**
     * Sets item marc record list.
     *
     * @param itemMarcRecordList the item marc record list
     */
    public void setItemMarcRecordList(List<ItemMarcRecord> itemMarcRecordList) {
        this.itemMarcRecordList = itemMarcRecordList;
    }
}
