package org.recap.controller;

import org.recap.ReCAPConstants;
import org.recap.service.DeletedRecords.DeletedRecordsService;
import org.recap.service.purge.PurgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Controller for processing Deleted Records Report
 *
 * Created by sudhish on 02/Jun/2017.
 */
@RestController
@RequestMapping("/reportDeleted")
public class ReportDeletedRecordsController {

    @Autowired
    private DeletedRecordsService deletedRecordsService;

    public DeletedRecordsService getDeletedRecordsService() {
        return deletedRecordsService;
    }

    /**
     * This method processes, the deleted records, by sending email notification and then updating record status
     *
     * @return
     */
    @RequestMapping(value = "/records", method = RequestMethod.GET)
    public ResponseEntity deletedRecords() {
        String responseMsg = (getDeletedRecordsService().deletedRecords())? ReCAPConstants.DELETED_RECORDS_SUCCESS_MSG : ReCAPConstants.DELETED_RECORDS_FAILURE_MSG;
        return new ResponseEntity(responseMsg, HttpStatus.OK);
    }
}
