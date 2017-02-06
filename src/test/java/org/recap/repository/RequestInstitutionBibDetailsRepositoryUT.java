package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.RequestInstitutionBibEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * Created by sudhishk on 3/2/17.
 */
public class RequestInstitutionBibDetailsRepositoryUT extends BaseTestCase {

    @Autowired
    private RequestInstitutionBibDetailsRepository requestInstitutionBibDetailsRepository;

    @Test
    public void testfindByItemIdAndOwningInstitutionId(){
        RequestInstitutionBibEntity requestInstitutionBibEntity = requestInstitutionBibDetailsRepository.findByItemIdAndOwningInstitutionId(11410, 2);
        assertNotNull(requestInstitutionBibEntity);
    }

    @Test
    public void testfindByItemIdAndOwningInstitutionIdNull(){
        RequestInstitutionBibEntity requestInstitutionBibEntity = requestInstitutionBibDetailsRepository.findByItemIdAndOwningInstitutionId(123, 1);
        assertNull(requestInstitutionBibEntity);
    }

    @Test
    public void testSave(){
        RequestInstitutionBibEntity requestInstitutionBibEntity;
        RequestInstitutionBibEntity requestInstitutionBibEntityIns= new RequestInstitutionBibEntity();
        requestInstitutionBibEntityIns.setItemId(83166);
        requestInstitutionBibEntityIns.setOwningInstitutionId(1);
        requestInstitutionBibEntityIns.setOwningInstitutionBibId("123456");
        requestInstitutionBibEntity = requestInstitutionBibDetailsRepository.save(requestInstitutionBibEntityIns);
        assertNotNull(requestInstitutionBibEntity);

        requestInstitutionBibEntityIns= new RequestInstitutionBibEntity();
        requestInstitutionBibEntityIns.setItemId(83166);
        requestInstitutionBibEntityIns.setOwningInstitutionId(1);
        requestInstitutionBibEntityIns.setOwningInstitutionBibId("123456789");
        requestInstitutionBibEntity = requestInstitutionBibDetailsRepository.save(requestInstitutionBibEntityIns);
        assertNotNull(requestInstitutionBibEntity);
    }

}
