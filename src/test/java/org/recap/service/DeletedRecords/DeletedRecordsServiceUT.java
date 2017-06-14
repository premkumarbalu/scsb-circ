package org.recap.service.DeletedRecords;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertTrue;

/**
 * Created by sudhishk on 5/6/17.
 */
public class DeletedRecordsServiceUT extends BaseTestCase {
    @Autowired
    DeletedRecordsService deletedRecordsService;

    @Test
    public void testdeletedRecords(){
        boolean bflag = deletedRecordsService.deletedRecords();
        assertTrue(bflag);
    }
}
