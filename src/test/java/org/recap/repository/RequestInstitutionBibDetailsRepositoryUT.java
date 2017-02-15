package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by sudhishk on 3/2/17.
 */
public class RequestInstitutionBibDetailsRepositoryUT extends BaseTestCase {

    @Autowired
    private RequestInstitutionBibDetailsRepository requestInstitutionBibDetailsRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    InstitutionDetailsRepository institutionDetailsRepository;

    @Test
    public void testfindByItemIdAndOwningInstitutionId() throws Exception {
        RequestInstitutionBibEntity requestInstitutionBibEntity;
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsSingleItem();
        RequestInstitutionBibEntity requestInstitutionBibEntityIns= new RequestInstitutionBibEntity();
        requestInstitutionBibEntityIns.setItemId(bibliographicEntity.getItemEntities().get(0).getItemId());
        requestInstitutionBibEntityIns.setOwningInstitutionId(bibliographicEntity.getOwningInstitutionId());
        requestInstitutionBibEntityIns.setOwningInstitutionBibId("123456");
        requestInstitutionBibEntity = requestInstitutionBibDetailsRepository.save(requestInstitutionBibEntityIns);
        RequestInstitutionBibEntity requestInstBibEntity = requestInstitutionBibDetailsRepository.findByItemIdAndOwningInstitutionId(requestInstitutionBibEntity.getItemId(), requestInstitutionBibEntity.getOwningInstitutionId());
        assertNotNull(requestInstBibEntity);
    }

    @Test
    public void testfindByItemIdAndOwningInstitutionIdNull(){
        RequestInstitutionBibEntity requestInstitutionBibEntity = requestInstitutionBibDetailsRepository.findByItemIdAndOwningInstitutionId(123, 1);
        assertNull(requestInstitutionBibEntity);
    }

    @Test
    public void testSave() throws Exception {
        RequestInstitutionBibEntity requestInstitutionBibEntity;
        BibliographicEntity bibliographicEntity = saveBibSingleHoldingsSingleItem();
        RequestInstitutionBibEntity requestInstitutionBibEntityIns= new RequestInstitutionBibEntity();
        requestInstitutionBibEntityIns.setItemId(bibliographicEntity.getItemEntities().get(0).getItemId());
        requestInstitutionBibEntityIns.setOwningInstitutionId(bibliographicEntity.getOwningInstitutionId());
        requestInstitutionBibEntityIns.setOwningInstitutionBibId("123456");
        requestInstitutionBibEntity = requestInstitutionBibDetailsRepository.save(requestInstitutionBibEntityIns);
        assertNotNull(requestInstitutionBibEntity);

        requestInstitutionBibEntityIns= new RequestInstitutionBibEntity();
        requestInstitutionBibEntityIns.setItemId(bibliographicEntity.getItemEntities().get(0).getItemId());
        requestInstitutionBibEntityIns.setOwningInstitutionId(bibliographicEntity.getOwningInstitutionId());
        requestInstitutionBibEntityIns.setOwningInstitutionBibId("123456789");
        requestInstitutionBibEntity = requestInstitutionBibDetailsRepository.save(requestInstitutionBibEntityIns);
        assertNotNull(requestInstitutionBibEntity);
    }

    public BibliographicEntity saveBibSingleHoldingsSingleItem() throws Exception {

        InstitutionEntity institutionEntity = new InstitutionEntity();
        institutionEntity.setInstitutionCode("UC");
        institutionEntity.setInstitutionName("University of Chicago");
        InstitutionEntity entity = institutionDetailsRepository.save(institutionEntity);
        assertNotNull(entity);

        Random random = new Random();
        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        bibliographicEntity.setContent("mock Content".getBytes());
        bibliographicEntity.setCreatedDate(new Date());
        bibliographicEntity.setLastUpdatedDate(new Date());
        bibliographicEntity.setCreatedBy("tst");
        bibliographicEntity.setLastUpdatedBy("tst");
        bibliographicEntity.setOwningInstitutionId(entity.getInstitutionId());
        bibliographicEntity.setOwningInstitutionBibId(String.valueOf(random.nextInt()));
        HoldingsEntity holdingsEntity = new HoldingsEntity();
        holdingsEntity.setContent("mock holdings".getBytes());
        holdingsEntity.setCreatedDate(new Date());
        holdingsEntity.setLastUpdatedDate(new Date());
        holdingsEntity.setCreatedBy("tst");
        holdingsEntity.setLastUpdatedBy("tst");
        holdingsEntity.setOwningInstitutionId(1);
        holdingsEntity.setOwningInstitutionHoldingsId(String.valueOf(random.nextInt()));

        ItemEntity itemEntity = new ItemEntity();
        itemEntity.setLastUpdatedDate(new Date());
        itemEntity.setOwningInstitutionItemId(String.valueOf(random.nextInt()));
        itemEntity.setOwningInstitutionId(1);
        itemEntity.setBarcode("123");
        itemEntity.setCallNumber("x.12321");
        itemEntity.setCollectionGroupId(1);
        itemEntity.setCallNumberType("1");
        itemEntity.setCustomerCode("123");
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
