package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.model.BulkRequestItemEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

/**
 * Created by rajeshbabuk on 10/10/17.
 */
public class BulkRequestItemDetailsRepositoryUT extends BaseTestCase {

    @Autowired
    BulkRequestItemDetailsRepository bulkRequestItemDetailsRepository;

    @Test
    public void testSaveBulkItemRequest() throws Exception {
        BulkRequestItemEntity bulkRequestItemEntity = new BulkRequestItemEntity();
        bulkRequestItemEntity.setBulkRequestName("TestFirstBulkRequest");
        bulkRequestItemEntity.setBulkRequestFileName("bulkItemUpload");
        bulkRequestItemEntity.setBulkRequestFileData("BARCODE\tCUSTOMER_CODE\n32101075852275\tPK".getBytes());
        bulkRequestItemEntity.setRequestingInstitutionId(1);
        bulkRequestItemEntity.setBulkRequestStatus(ReCAPConstants.PROCESSED);
        bulkRequestItemEntity.setCreatedBy("TestUser");
        bulkRequestItemEntity.setCreatedDate(new Date());
        bulkRequestItemEntity.setStopCode("PA");
        bulkRequestItemEntity.setPatronId("45678915");

        BulkRequestItemEntity savedBulkRequestItemEntity = bulkRequestItemDetailsRepository.save(bulkRequestItemEntity);
        assertNotNull(savedBulkRequestItemEntity);
    }

    @Test
    public void testBulkRequestItemEntity(){
        BulkRequestItemEntity bulkRequestItemEntity = new BulkRequestItemEntity();
        bulkRequestItemEntity.setBulkRequestId(1);
        bulkRequestItemEntity.setBulkRequestName("TestFirstBulkRequest");
        bulkRequestItemEntity.setBulkRequestFileName("bulkItemUpload");
        bulkRequestItemEntity.setBulkRequestFileData("BARCODE\tCUSTOMER_CODE\n32101075852275\tPK".getBytes());
        bulkRequestItemEntity.setRequestingInstitutionId(1);
        bulkRequestItemEntity.setBulkRequestStatus(ReCAPConstants.PROCESSED);
        bulkRequestItemEntity.setCreatedBy("TestUser");
        bulkRequestItemEntity.setCreatedDate(new Date());
        bulkRequestItemEntity.setStopCode("PA");
        bulkRequestItemEntity.setPatronId("45678915");

        assertNotNull(bulkRequestItemEntity.getBulkRequestId());
        assertNotNull(bulkRequestItemEntity.getBulkRequestName());
        assertNotNull(bulkRequestItemEntity.getBulkRequestFileName());
        assertNotNull(bulkRequestItemEntity.getBulkRequestFileData());
        assertNotNull(bulkRequestItemEntity.getRequestingInstitutionId());
        assertNotNull(bulkRequestItemEntity.getBulkRequestStatus());
        assertNotNull(bulkRequestItemEntity.getCreatedBy());
        assertNotNull(bulkRequestItemEntity.getCreatedDate());
        assertNotNull(bulkRequestItemEntity.getStopCode());
        assertNotNull(bulkRequestItemEntity.getPatronId());
    }
}
