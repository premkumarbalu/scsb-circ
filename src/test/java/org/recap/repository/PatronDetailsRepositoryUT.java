package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.PatronEntity;
import org.recap.model.RequestItemEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Date;

/**
 * Created by sudhishk on 9/12/16.
 */
public class PatronDetailsRepositoryUT extends BaseTestCase {

    @Autowired
    PatronDetailsRepository patronDetailsRepository;

    @Autowired
    RequestItemDetailsRepository requestItemDetailsRepository;

    @Test
    public void testPatronInsert(){
        PatronEntity patronEntity =new PatronEntity();
        patronEntity.setInstitutionIdentifier("12121212");
        patronEntity.setInstitutionId(1);
        patronEntity.setEmailId("test@gmail.com");
//        PatronEntity savedPatronEntity = patronDetailsRepository.save(patronEntity);

        RequestItemEntity requestItemEntity = new RequestItemEntity();
        requestItemEntity.setItemId(1);
        requestItemEntity.setRequestTypeId(1);
        requestItemEntity.setRequestingInstitutionId(1);
//        requestItemEntity.setPatronId(savedPatronEntity.getPatronId());
        requestItemEntity.setPatronEntity(patronEntity);
        requestItemEntity.setRequestPosition(99);
        requestItemEntity.setCreatedDate(new Date());
        requestItemEntity.setStopCode("PA");

        RequestItemEntity savedRequestItemEntity = requestItemDetailsRepository.saveAndFlush(requestItemEntity);

    }
}
