package org.recap.controller;

import org.recap.ReCAPConstants;
import org.recap.model.deaccession.DeAccessionRequest;
import org.recap.model.submitcollection.SubmitCollectionResponse;
import org.recap.service.deaccession.DeAccessionService;
import org.recap.service.submitcollection.SubmitCollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by premkb on 21/12/16.
 */
@RestController
@RequestMapping("/sharedCollection")
public class SharedCollectionRestController {

    private static final Logger logger = LoggerFactory.getLogger(SharedCollectionRestController.class);

    @Autowired
    private SubmitCollectionService submitCollectionService;

    /**
     * The De accession service.
     */
    @Autowired
    DeAccessionService deAccessionService;

    /**
     * This controller method is the entry point for submit collection which receives
     * input xml either in marc xml or scsb xml and pass it to the service class
     *
     * @param inputRecords the input records
     * @return the response entity
     */
    @RequestMapping(value = "/submitCollection", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity submitCollection(@RequestBody String inputRecords){
        ResponseEntity responseEntity;
        List<Integer> reportRecordNumberList = new ArrayList<>();
        List<Integer> processedBibIdList = new ArrayList<>();
        Map<String,String> idMapToRemoveIndex = new HashMap<>();
        List<SubmitCollectionResponse> submitCollectionResponseList;
        try {
            submitCollectionResponseList = submitCollectionService.process(inputRecords,processedBibIdList,idMapToRemoveIndex,"",reportRecordNumberList);
            if (!processedBibIdList.isEmpty()) {
                logger.info("Calling indexing service to update data");
                submitCollectionService.indexData(processedBibIdList);
            }
            if (!idMapToRemoveIndex.isEmpty()) {//remove the incomplete record from solr index
                logger.info("Calling indexing to remove dummy record");
                submitCollectionService.removeSolrIndex(idMapToRemoveIndex);
            }
            submitCollectionService.generateSubmitCollectionReportFile(reportRecordNumberList);
            responseEntity = new ResponseEntity(submitCollectionResponseList,getHttpHeaders(), HttpStatus.OK);
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
            responseEntity = new ResponseEntity(ReCAPConstants.SUBMIT_COLLECTION_INTERNAL_ERROR,getHttpHeaders(), HttpStatus.OK);
        }
        return responseEntity;
    }

    /**
     * De accession response entity.
     *
     * @param deAccessionRequest the de accession request
     * @return the response entity
     */
    @RequestMapping(value = "/deAccession", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity deAccession(@RequestBody DeAccessionRequest deAccessionRequest) {
        Map<String, String> resultMap = deAccessionService.deAccession(deAccessionRequest);
        if (resultMap != null) {
            return new ResponseEntity(resultMap, getHttpHeaders(), HttpStatus.OK);
        }
        return null;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ReCAPConstants.RESPONSE_DATE, new Date().toString());
        return responseHeaders;
    }
}
