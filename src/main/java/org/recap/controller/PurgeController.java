package org.recap.controller;

import org.recap.service.purge.PurgeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * This controller is intended to be used from scsb-schduler project, The methods are used for deleting information from certain columns or records from certain tables, after certain period.
 *
 * Created by hemalathas on 10/4/17.
 */
@RestController
@RequestMapping("/purge")
public class PurgeController {

    @Autowired
    private PurgeService purgeService;

    /**
     * Purge email address  from request_t table after certain period.
     * This is required, part of security feature, as patron information(Email), should be stored only of certain period in SCSB.
     *This method will be accesssed from a schduled job.
     *
     * @return the response entity
     */
    @RequestMapping(value = "/purgeEmailAddress", method = RequestMethod.GET)
    public ResponseEntity purgeEmailAddress() {
        Map<String, String> responseMap = purgeService.purgeEmailAddress();
        return new ResponseEntity(responseMap, HttpStatus.OK);
    }

    /**
     * Purge exception requests from request_t table. This method will be accesssed from a schduled job.
     *
     * @return the response entity
     */
    @RequestMapping(value = "/purgeExceptionRequests", method = RequestMethod.GET)
    public ResponseEntity purgeExceptionRequests() {
        Map<String, String> responseMap = purgeService.purgeExceptionRequests();
        return new ResponseEntity(responseMap, HttpStatus.OK);
    }

    /**
     * Purge accession requests URI is used for removing records after batch accession process is completed.
     *
     * @return the response entity
     */
    @RequestMapping(value = "/purgeAccessionRequests", method = RequestMethod.GET)
    public ResponseEntity purgeAccessionRequests() {
        Map<String, String> responseMap = purgeService.purgeAccessionRequests();
        return new ResponseEntity(responseMap, HttpStatus.OK);
    }
}
