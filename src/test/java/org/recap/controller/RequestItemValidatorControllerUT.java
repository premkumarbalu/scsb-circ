package org.recap.controller;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.model.BibliographicEntity;
import org.recap.model.HoldingsEntity;
import org.recap.model.ItemEntity;
import org.recap.model.ItemRequestInformation;
import org.recap.repository.BibliographicDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hemalathas on 11/11/16.
 */
public class RequestItemValidatorControllerUT extends BaseTestCase {
    @Autowired
    RequestItemValidatorController requestItemValidatorController;

    @Autowired
    ItemController itemController;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testValidRequest() throws Exception {
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsMultipleItem();
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setPatronBarcode("45678915");
        itemRequestInformation.setRequestType(ReCAPConstants.REQUEST_TYPE_RETRIEVAL);
        itemRequestInformation.setDeliveryLocation("PB");
        itemRequestInformation.setItemOwningInstitution("PUL");
        itemRequestInformation.setEmailAddress("hemalatha.s@htcindia.com");
        itemRequestInformation.setRequestingInstitution("PUL");
        itemRequestInformation.setItemBarcodes(Arrays.asList(bibliographicEntity.getItemEntities().get(0).getBarcode()));
        ResponseEntity responseEntity = requestItemValidatorController.validateItemRequestInformations(itemRequestInformation);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getBody(), ReCAPConstants.VALID_REQUEST);

    }

    @Test
    public void testInValidRequest() throws Exception {
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsMultipleItem();
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setPatronBarcode("4567gfdr8915");
        itemRequestInformation.setRequestType(ReCAPConstants.REQUEST_TYPE_RETRIEVAL);
        itemRequestInformation.setDeliveryLocation("PB");
        itemRequestInformation.setEmailAddress("hemalatha.s@htcindia.com");
        itemRequestInformation.setItemOwningInstitution("PUL");
        itemRequestInformation.setRequestingInstitution("PUL");
        itemRequestInformation.setItemBarcodes(Arrays.asList(bibliographicEntity.getItemEntities().get(0).getBarcode()));
        ResponseEntity responseEntity = requestItemValidatorController.validateItemRequestInformations(itemRequestInformation);
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getBody(), ReCAPConstants.INVALID_PATRON);
    }

    @Test
    public void testvalidateItemRequest() throws Exception {
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsMultipleItem();
        ItemRequestInformation itemRequestInformation = new ItemRequestInformation();
        itemRequestInformation.setPatronBarcode("4567gfdr8915");
        itemRequestInformation.setRequestType(ReCAPConstants.REQUEST_TYPE_RETRIEVAL);
        itemRequestInformation.setDeliveryLocation("PB");
        itemRequestInformation.setEmailAddress("hemalatha.s@htcindia.com");
        itemRequestInformation.setItemOwningInstitution("PUL");
        itemRequestInformation.setRequestingInstitution("PUL");
        itemRequestInformation.setItemBarcodes(Arrays.asList(bibliographicEntity.getItemEntities().get(0).getBarcode()));
        ResponseEntity responseEntity = requestItemValidatorController.validateItemRequest(itemRequestInformation);
        assertNotNull(responseEntity);
        assertEquals("Request Parameter ","All request parameters are valid.Patron is eligible to raise a request",responseEntity.getBody());
    }
    private BibliographicEntity saveBibSingleHoldingsMultipleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = getBibliographicEntity(1, String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = getHoldingsEntity(random, 1);

        ItemEntity itemEntity1 = new ItemEntity();
        itemEntity1.setCreatedDate(new Date());
        itemEntity1.setCreatedBy("etl");
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setLastUpdatedBy("etl");
        itemEntity1.setCustomerCode("PB");
        itemEntity1.setItemAvailabilityStatusId(1);
        itemEntity1.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity1.setOwningInstitutionId(1);
        itemEntity1.setBarcode("009");
        itemEntity1.setCallNumber("x.12321");
        itemEntity1.setCollectionGroupId(1);
        itemEntity1.setCallNumberType("1");
        itemEntity1.setHoldingsEntities(Arrays.asList(holdingsEntity));
        itemEntity1.setCatalogingStatus("Complete");

        ItemEntity itemEntity2 = new ItemEntity();
        itemEntity2.setCreatedDate(new Date());
        itemEntity2.setCreatedBy("etl");
        itemEntity2.setLastUpdatedDate(new Date());
        itemEntity2.setLastUpdatedBy("etl");
        itemEntity2.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity2.setOwningInstitutionId(1);
        itemEntity2.setCustomerCode("PB");
        itemEntity2.setBarcode("010");
        itemEntity2.setItemAvailabilityStatusId(1);
        itemEntity2.setCallNumber("x.12321");
        itemEntity2.setCollectionGroupId(1);
        itemEntity2.setCallNumberType("1");
        itemEntity2.setHoldingsEntities(Arrays.asList(holdingsEntity));
        itemEntity2.setCatalogingStatus("Complete");

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity1, itemEntity2));

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity1, itemEntity2));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);

        entityManager.refresh(savedBibliographicEntity);

        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getBibliographicId());
        assertNotNull(savedBibliographicEntity.getHoldingsEntities().get(0).getHoldingsId());
        assertNotNull(savedBibliographicEntity.getItemEntities().get(0).getItemId());
        assertNotNull(savedBibliographicEntity.getItemEntities().get(1).getItemId());

        return savedBibliographicEntity;
    }

    private HoldingsEntity getHoldingsEntity(Random random, Integer institutionId) {
        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setCreatedBy("etl");
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setLastUpdatedBy("etl");
        holdingsEntity.setOwningInstitutionId(institutionId);
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));
        return holdingsEntity;
    }

    private BibliographicEntity getBibliographicEntity(Integer institutionId, String owningInstitutionBibId1) {
        BibliographicEntity bibliographicEntity1 = new BibliographicEntity();
        bibliographicEntity1.setContent("mock Content".getBytes());
        bibliographicEntity1.setCreatedDate(new Date());
        bibliographicEntity1.setCreatedBy("etl");
        bibliographicEntity1.setLastUpdatedBy("etl");
        bibliographicEntity1.setLastUpdatedDate(new Date());
        bibliographicEntity1.setOwningInstitutionId(institutionId);
        bibliographicEntity1.setOwningInstitutionBibId(owningInstitutionBibId1);
        return bibliographicEntity1;
    }

}