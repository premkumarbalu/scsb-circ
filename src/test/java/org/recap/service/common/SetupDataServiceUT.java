package org.recap.service.common;

import org.junit.Test;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by premkb on 18/6/17.
 */
public class SetupDataServiceUT extends BaseTestCase {

    @Autowired
    private SetupDataService setupDataService;

    @Test
    public void getItemStatusIdCodeMap(){
        Map<Integer,String> itemStatusIdCodeMap = setupDataService.getItemStatusIdCodeMap();
        assertNotNull(itemStatusIdCodeMap);
        String itemStatusCode = itemStatusIdCodeMap.get(1);
        assertEquals(ReCAPConstants.AVAILABLE,itemStatusCode);
    }

    @Test
    public void getItemStatusCodeIdMap(){
        Map<String,Integer> itemStatusCodeIdMap = setupDataService.getItemStatusCodeIdMap();
        assertNotNull(itemStatusCodeIdMap);
        Integer itemStatusId = itemStatusCodeIdMap.get(ReCAPConstants.AVAILABLE);
        assertEquals(new Integer(1),itemStatusId);
    }

    @Test
    public void getInstitutionEntityMap(){
        Map<Integer,String> institutionEntityMap = setupDataService.getInstitutionIdCodeMap();
        assertNotNull(institutionEntityMap);
        String institutionCode = institutionEntityMap.get(1);
        assertEquals(ReCAPConstants.PRINCETON,institutionCode);

    }
}
