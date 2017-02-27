package org.recap.model;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.repository.RequestItemStatusDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 17/2/17.
 */
public class RequestStatusEntityUT extends BaseTestCase{

    @Autowired
    RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @Test
    public void testRequestStatus(){
        RequestStatusEntity requestStatusEntity = new RequestStatusEntity();
        requestStatusEntity.setRequestStatusCode("Refile");
        requestStatusEntity.setRequestStatusDescription("Refile");
        RequestStatusEntity savedRequestStatusEntity = requestItemStatusDetailsRepository.save(requestStatusEntity);
        assertNotNull(savedRequestStatusEntity);
        assertNotNull(savedRequestStatusEntity.getRequestStatusId());
    }

}