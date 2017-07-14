package org.recap.controller;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.recap.BaseTestCase;
import org.recap.ReCAPConstants;
import org.recap.service.DeletedRecords.DeletedRecordsService;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

/**
 * Created by hemalathas on 13/7/17.
 */
public class ReportDeletedRecordsControllerUT extends BaseTestCase{

    @Mock
    ReportDeletedRecordsController reportDeletedRecordsController;

    @Mock
    DeletedRecordsService deletedRecordsService;

    @Test
    public void testReportDeletedRecordsController(){
        Mockito.when(reportDeletedRecordsController.getDeletedRecordsService()).thenReturn(deletedRecordsService);
        Mockito.when(deletedRecordsService.deletedRecords()).thenReturn(true);
        Mockito.when(reportDeletedRecordsController.deletedRecords()).thenCallRealMethod();
        ResponseEntity responseEntity = reportDeletedRecordsController.deletedRecords();
        assertNotNull(responseEntity);
        assertEquals(responseEntity.getBody(), ReCAPConstants.DELETED_RECORDS_SUCCESS_MSG);
        Mockito.when(reportDeletedRecordsController.getDeletedRecordsService()).thenCallRealMethod();
        assertNotEquals(reportDeletedRecordsController.getDeletedRecordsService(),deletedRecordsService);
    }

}