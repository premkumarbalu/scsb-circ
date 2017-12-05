package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.controller.ItemController;
import org.recap.model.BibliographicEntity;
import org.recap.model.HoldingsEntity;
import org.recap.model.ItemEntity;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by hemalathas on 16/11/16.
 */
public class ItemDetailsRepositoryUT extends BaseTestCase {

    @Autowired
    ItemController itemController;

    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    ItemDetailsRepository itemDetailsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    public void testItemDetails() throws Exception {
        saveBibSingleHoldingsMultipleItem();
        List<ItemEntity> itemEntityList = itemController.findByBarcodeIn("00009,00010");
        assertNotNull(itemEntityList);
        assertEquals(2,itemEntityList.size());
    }

    @Test
    public void testfindByBarcodeInIsDeletedFalseCatalogingStatusComplete() throws Exception {
        saveMultipleItem();
        List<String> lstBarcode = Arrays.asList("100000999", "100009999");
        List<ItemEntity> itemEntities = itemDetailsRepository.findByBarcodeInAndComplete(lstBarcode);
        assertEquals("Size ", 2, itemEntities.size());
    }

    @Test
    public void testFindByAvailableStatusId(){
        List<ItemEntity> itemEntityList = new ArrayList<>();
        long itemCount = itemDetailsRepository.getNotAvailableItemsCount(1,Arrays.asList(1,9,2,5),2);
        System.out.println("Total Records : {}"+ itemCount);
        int totalPagesCount = (int) (itemCount / 100);
        System.out.println("Total Pages : {}" + totalPagesCount);
        for(int pageNum = 0; pageNum < totalPagesCount + 1; pageNum++) {
            long from = pageNum * Long.valueOf(100);
            itemEntityList =  itemDetailsRepository.getNotAvailableItems(1,Arrays.asList(1,9,2,5),0,100,2);
        }
        assertNotNull(itemEntityList);
    }

    @Test
    public void testFindNotAvailableitems(){
        List<ItemEntity> itemEntityList = itemDetailsRepository.findByBarcodeAndNotAvailable("AD00004588",2);
        assertNotNull(itemEntityList);
    }

    public BibliographicEntity saveBibSingleHoldingsMultipleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = getBibliographicEntity(1, String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = getHoldingsEntity(random, 1);

        ItemEntity itemEntity1 = new ItemEntity();
        itemEntity1.setCreatedDate(new Date());
        itemEntity1.setCreatedBy("etl");
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setLastUpdatedBy("etl");
        itemEntity1.setCustomerCode("1");
        itemEntity1.setItemAvailabilityStatusId(1);
        itemEntity1.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity1.setOwningInstitutionId(1);
        itemEntity1.setBarcode("00009");
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
        itemEntity2.setCustomerCode("1");
        itemEntity2.setBarcode("00010");
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

    public void saveMultipleItem() throws Exception {
        Random random = new Random();
        BibliographicEntity bibliographicEntity = getBibliographicEntity(1, String.valueOf(random.nextInt()));

        HoldingsEntity holdingsEntity = getHoldingsEntity(random, 1);

        ItemEntity itemEntity1 = new ItemEntity();
        itemEntity1.setCreatedDate(new Date());
        itemEntity1.setCreatedBy("etl");
        itemEntity1.setLastUpdatedDate(new Date());
        itemEntity1.setLastUpdatedBy("etl");
        itemEntity1.setCustomerCode("1");
        itemEntity1.setItemAvailabilityStatusId(1);
        itemEntity1.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity1.setOwningInstitutionId(1);
        itemEntity1.setBarcode("100000999");
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
        itemEntity2.setCustomerCode("1");
        itemEntity2.setBarcode("100009999");
        itemEntity2.setItemAvailabilityStatusId(1);
        itemEntity2.setCallNumber("x.12321");
        itemEntity2.setCollectionGroupId(1);
        itemEntity2.setCallNumberType("1");
        itemEntity2.setHoldingsEntities(Arrays.asList(holdingsEntity));
        itemEntity2.setCatalogingStatus("Complete");

        ItemEntity itemEntity3 = new ItemEntity();
        itemEntity3.setCreatedDate(new Date());
        itemEntity3.setCreatedBy("etl");
        itemEntity3.setLastUpdatedDate(new Date());
        itemEntity3.setLastUpdatedBy("etl");
        itemEntity3.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity3.setOwningInstitutionId(1);
        itemEntity3.setCustomerCode("1");
        itemEntity3.setBarcode("100099999");
        itemEntity3.setItemAvailabilityStatusId(1);
        itemEntity3.setCallNumber("x.12321");
        itemEntity3.setCollectionGroupId(1);
        itemEntity3.setCallNumberType("1");
        itemEntity3.setHoldingsEntities(Arrays.asList(holdingsEntity));
        itemEntity3.setCatalogingStatus("InComplete");

        bibliographicEntity.setHoldingsEntities(Arrays.asList(holdingsEntity));
        bibliographicEntity.setItemEntities(Arrays.asList(itemEntity1, itemEntity2, itemEntity3));

        BibliographicEntity savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
        entityManager.refresh(savedBibliographicEntity);

        assertNotNull(savedBibliographicEntity);
        assertNotNull(savedBibliographicEntity.getBibliographicId());
        assertNotNull(savedBibliographicEntity.getHoldingsEntities().get(0).getHoldingsId());
        assertNotNull(savedBibliographicEntity.getItemEntities().get(0).getItemId());
        assertNotNull(savedBibliographicEntity.getItemEntities().get(1).getItemId());
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