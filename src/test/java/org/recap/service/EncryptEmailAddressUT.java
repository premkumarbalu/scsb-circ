package org.recap.service;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.*;
import org.recap.repository.RequestItemDetailsRepository;
import org.recap.repository.RequestItemStatusDetailsRepository;
import org.recap.repository.RequestTypeDetailsRepository;
import org.recap.util.SecurityUtil;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by akulak on 20/9/17.
 */
public class EncryptEmailAddressUT extends BaseTestCase {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    RequestTypeDetailsRepository requestTypeDetailsRepository;

    @Autowired
    RequestItemStatusDetailsRepository requestItemStatusDetailsRepository;

    @Autowired
    RequestItemDetailsRepository requestItemDetailsRepository;

    @Autowired
    EncryptEmailAddressService encryptEmailAddressService;

    @Autowired
    SecurityUtil securityUtil;

    @Test
    public void checkEmailAddressEncryption() throws Exception{
        RequestItemEntity requestItem = createRequestItem();
        String encryptedValue = securityUtil.getEncryptedValue("test@gmail.com");
        encryptEmailAddressService.encryptEmailAddress();
        System.out.println(requestItem.getRequestId());
        RequestItemEntity requestItemEntity = requestItemDetailsRepository.findByRequestId(requestItem.getRequestId());
        assertEquals(requestItemEntity.getEmailId(),encryptedValue);
        String decryptedValue = securityUtil.getDecryptedValue(encryptedValue);
        assertEquals("test@gmail.com",decryptedValue);
    }

    private RequestItemEntity createRequestItem() throws Exception {
        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionCode("PUL");
        institutionEntity.setInstitutionName("PUL");

        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsSingleItem();

        RequestTypeEntity requestTypeEntity = new RequestTypeEntity();
        requestTypeEntity.setRequestTypeCode("Recall");
        requestTypeEntity.setRequestTypeDesc("Recall");
        RequestTypeEntity savedRequestTypeEntity = requestTypeDetailsRepository.save(requestTypeEntity);
        assertNotNull(savedRequestTypeEntity);

        RequestStatusEntity requestStatusEntity = requestItemStatusDetailsRepository.findByRequestStatusId(3);

        RequestItemEntity requestItemEntity = new RequestItemEntity();
        requestItemEntity.setItemId(bibliographicEntity.getItemEntities().get(0).getItemId());
        requestItemEntity.setRequestTypeId(savedRequestTypeEntity.getRequestTypeId());
        requestItemEntity.setRequestStatusEntity(requestStatusEntity);
        requestItemEntity.setRequestingInstitutionId(2);
        requestItemEntity.setStopCode("test");
        requestItemEntity.setNotes("test");
        requestItemEntity.setItemEntity(bibliographicEntity.getItemEntities().get(0));
        requestItemEntity.setInstitutionEntity(institutionEntity);
        requestItemEntity.setPatronId("1");
        requestItemEntity.setCreatedDate(new Date());
        requestItemEntity.setRequestExpirationDate(new Date());
        requestItemEntity.setRequestExpirationDate(new Date());
        requestItemEntity.setRequestStatusId(3);
        requestItemEntity.setCreatedBy("test");
        requestItemEntity.setEmailId("test@gmail.com");
        requestItemEntity.setLastUpdatedDate(new Date());
        RequestItemEntity savedRequestItemEntity = requestItemDetailsRepository.saveAndFlush(requestItemEntity);
        entityManager.refresh(savedRequestItemEntity);
        return savedRequestItemEntity;
    }

    private BibliographicEntity saveBibSingleHoldingsSingleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content".getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setCreatedBy("tst");
        bibliographicEntity.setLastUpdatedBy("tst");
        bibliographicEntity.setOwningInstitutionId(1);
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setCreatedBy("test");
        holdingsEntity.setLastUpdatedBy("test");
        holdingsEntity.setOwningInstitutionId(1);
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity.setOwningInstitutionId(1);
        itemEntity.setBarcode("8956");
        itemEntity.setCallNumber("x.12321");
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCallNumberType("1");
        itemEntity.setCustomerCode("4598");
        itemEntity.setCreatedDate(new Date());
        itemEntity.setCreatedBy("tst");
        itemEntity.setLastUpdatedBy("tst");
        itemEntity.setItemAvailabilityStatusId(1);
        itemEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);
        return savedBibliographicEntity;

    }
}
