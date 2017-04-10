package org.recap.model.marc;

import org.marc4j.marc.Record;

/**
 * Created by chenchulakshmig on 14/10/16.
 */
public class ItemMarcRecord {
    /**
     * The Item record.
     */
    Record itemRecord;

    /**
     * Gets item record.
     *
     * @return the item record
     */
    public Record getItemRecord() {
        return itemRecord;
    }

    /**
     * Sets item record.
     *
     * @param itemRecord the item record
     */
    public void setItemRecord(Record itemRecord) {
        this.itemRecord = itemRecord;
    }
}
