package org.recap.repository;

import org.jboss.logging.Logger;
import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.model.RequestItemEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import javax.persistence.NonUniqueResultException;
import java.util.List;

/**
 * Created by sudhishk on 20/1/17.
 */
public class RequestItemDetailsRepositoryUT extends BaseTestCase {

    private Logger logger = Logger.getLogger(RequestItemDetailsRepositoryUT.class);

    @Autowired
    private RequestItemDetailsRepository requestItemDetailsRepository;

    @Test
    public void testRecallPatronValidation() {
        Pageable pageable = new PageRequest(0, 1);
        Page<RequestItemEntity> requestItemEntities = requestItemDetailsRepository.findByItemBarcode(pageable, "PULTST54333");
        if (requestItemEntities.iterator().hasNext()) {
            logger.info(requestItemEntities.iterator().next().getRequestTypeEntity().getRequestTypeDesc());
            logger.info(requestItemEntities.iterator().next().getRequestStatusId());
            logger.info(requestItemEntities.iterator().next().getRequestStatusEntity().getRequestStatusCode());
        } else {
            logger.info("No Value");
        }
    }

    @Test
    public void testFindByItemBarcodeAndRequestStatusCode() {

        try {
            RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByItemBarcodeAndRequestStaCode("PULTST54333", ReCAPConstants.REQUEST_STATUS_RETRIEVAL_ORDER_PLACED);
            if (requestItemEntity != null) {
                logger.info(requestItemEntity.getRequestId());
                logger.info(requestItemEntity.getRequestTypeEntity().getRequestTypeDesc());
                logger.info(requestItemEntity.getRequestStatusId());
                logger.info(requestItemEntity.getRequestStatusEntity().getRequestStatusCode());
            } else {
                logger.info("No Value");
            }
        } catch (IncorrectResultSizeDataAccessException e) {
            logger.error(e.getMessage());
        } catch (NonUniqueResultException e) {
            logger.error(e.getMessage());
        }
    }
}
