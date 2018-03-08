package org.recap.repository;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.model.DeaccessionItemChangeLog;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.junit.Assert.assertNotNull;

/**
 * Created by akulak on 1/3/18 .
 */
public class DeaccessionItemChangeLogDetailsReppositoryUT extends BaseTestCase {

    @Autowired
    private DeaccesionItemChangeLogDetailsRepository deaccesionItemChangeLogDetailsRepository;

    @Test
    public void saveDeaccessionItemChangeLog(){
        DeaccessionItemChangeLog deaccessionItemChangeLog = getDeaccessionItemChangeLog();
        DeaccessionItemChangeLog saveddeaccessionItemChangeLog = deaccesionItemChangeLogDetailsRepository.save(deaccessionItemChangeLog);
        assertNotNull(saveddeaccessionItemChangeLog);
    }


    private DeaccessionItemChangeLog getDeaccessionItemChangeLog() {
        DeaccessionItemChangeLog deaccessionItemChangeLog = new DeaccessionItemChangeLog();
        deaccessionItemChangeLog.setCreatedDate(new Date());
        deaccessionItemChangeLog.setNotes("testing");
        deaccessionItemChangeLog.setOperationType("Deaccession");
        deaccessionItemChangeLog.setRecordId(123);
        deaccessionItemChangeLog.setUpdatedBy("tstuser");
        return deaccessionItemChangeLog;
    }
}
