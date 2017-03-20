package org.recap.model;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.ItemStatusDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 14/3/17.
 */
public class ItemStatusEntityUT extends BaseTestCase {

    @Autowired
    ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Test
    public void testItemStatus(){
        ItemStatusEntity itemStatusEntity = new ItemStatusEntity();
        itemStatusEntity.setStatusCode("test");
        itemStatusEntity.setStatusDescription("test");
        ItemStatusEntity savedItemStatusEntity = itemStatusDetailsRepository.save(itemStatusEntity);
        assertNotNull(savedItemStatusEntity);
        assertEquals(savedItemStatusEntity.getStatusCode(),"test");
        assertEquals(savedItemStatusEntity.getStatusDescription(),"test");
    }

}