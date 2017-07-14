package org.recap.model;

import org.junit.Test;
import org.recap.BaseTestCase;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 13/7/17.
 */
public class DeletedRecordsEntityUT extends BaseTestCase{

    @Test
    public void testDeletedRecordsEntity(){
        DeletedRecordsEntity deletedRecordsEntity = new DeletedRecordsEntity();
        deletedRecordsEntity.setDeletedRecordsId(1);
        deletedRecordsEntity.setRecords_Table("Test");
        deletedRecordsEntity.setRecordsPrimaryKey("Test");
        deletedRecordsEntity.setDeletedReportedStatus("Deleted");
        deletedRecordsEntity.setDeletedBy("Guest");
        deletedRecordsEntity.setDeletedDate(new Date());
        deletedRecordsEntity.setRecordsLog("Test");

        assertNotNull(deletedRecordsEntity.getDeletedBy());
        assertNotNull(deletedRecordsEntity.getDeletedDate());
        assertNotNull(deletedRecordsEntity.getDeletedRecordsId());
        assertNotNull(deletedRecordsEntity.getDeletedReportedStatus());
        assertNotNull(deletedRecordsEntity.getRecords_Table());
        assertNotNull(deletedRecordsEntity.getRecordsLog());
        assertNotNull(deletedRecordsEntity.getRecordsPrimaryKey());
    }

}