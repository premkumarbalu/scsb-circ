package org.recap.model;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.ItemChangeLogDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 17/2/17.
 */
public class ItemChangeLogEntityUT extends BaseTestCase{

    @Autowired
    ItemChangeLogDetailsRepository itemChangeLogDetailsRepository;

    @Test
    public void testItemChangeLogEntity(){
        ItemChangeLogEntity itemChangeLogEntity = new ItemChangeLogEntity();
        itemChangeLogEntity.setNotes("test");
        itemChangeLogEntity.setOperationType("test");
        itemChangeLogEntity.setUpdatedBy("test");
        itemChangeLogEntity.setRecordId(1);
        itemChangeLogEntity.setChangeLogId(12);
        itemChangeLogEntity.setUpdatedDate(new Date());
        ItemChangeLogEntity savedItemChangeLogEntity = itemChangeLogDetailsRepository.save(itemChangeLogEntity);
        assertNotNull(savedItemChangeLogEntity);
        assertNotNull(savedItemChangeLogEntity.getChangeLogId());
        assertNotNull(savedItemChangeLogEntity.getNotes());
        assertNotNull(savedItemChangeLogEntity.getOperationType());
        assertNotNull(savedItemChangeLogEntity.getUpdatedBy());
        assertNotNull(savedItemChangeLogEntity.getUpdatedDate());
        assertNotNull(savedItemChangeLogEntity.getChangeLogId());
    }


}