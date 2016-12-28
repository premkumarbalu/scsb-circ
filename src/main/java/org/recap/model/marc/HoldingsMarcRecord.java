package org.recap.model.marc;

import org.marc4j.marc.Record;

import java.util.List;

/**
 * Created by chenchulakshmig on 14/10/16.
 */
public class HoldingsMarcRecord {
    Record holdingsRecord;
    List<ItemMarcRecord> itemMarcRecordList;

    public Record getHoldingsRecord() {
        return holdingsRecord;
    }

    public void setHoldingsRecord(Record holdingsRecord) {
        this.holdingsRecord = holdingsRecord;
    }

    public List<ItemMarcRecord> getItemMarcRecordList() {
        return itemMarcRecordList;
    }

    public void setItemMarcRecordList(List<ItemMarcRecord> itemMarcRecordList) {
        this.itemMarcRecordList = itemMarcRecordList;
    }
}
