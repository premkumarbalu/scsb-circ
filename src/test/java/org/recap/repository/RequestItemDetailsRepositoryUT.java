package org.recap.repository;

import org.jboss.logging.Logger;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.RequestItemEntity;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by sudhishk on 20/1/17.
 */
public class RequestItemDetailsRepositoryUT extends BaseTestCase {

    private Logger logger = Logger.getLogger(RequestItemDetailsRepositoryUT.class);

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    @Test
    public void testRecallPatronValidation(){
//        RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByItemBarcode("PULTST54333");
//        logger.info(requestItemEntity.getRequestTypeId());
    }
}
