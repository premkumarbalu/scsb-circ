package org.recap.controller;

import org.recap.ReCAPConstants;
import org.recap.service.submitcollection.SubmitCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * Created by premkb on 21/12/16.
 */
@RestController
@RequestMapping("/sharedCollection")
public class SharedCollectionRestController {

    @Autowired
    private SubmitCollectionService submitCollectionService;

    @RequestMapping(value = "/submitCollection", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity submitCollection(@RequestBody String inputRecords){
        ResponseEntity responseEntity;
        String response;
        List<Integer> processedBibIdList = new ArrayList<>();
        Map<String,String> idMapToRemoveIndex = new HashMap<>();
        try {
            response = submitCollectionService.process(inputRecords,processedBibIdList,idMapToRemoveIndex);
            if (response.contains(ReCAPConstants.SUMBIT_COLLECTION_UPDATE_MESSAGE)) {
                String indexResponse = submitCollectionService.indexData(processedBibIdList);
                String reponse  = submitCollectionService.removeSolrIndex(idMapToRemoveIndex);
                if(!indexResponse.equalsIgnoreCase(ReCAPConstants.SUCCESS)){
                    response = indexResponse;
                }
            }
            responseEntity = new ResponseEntity(response,getHttpHeaders(), HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace();
            response = ReCAPConstants.FAILURE;
            responseEntity = new ResponseEntity(response,getHttpHeaders(), HttpStatus.OK);
        }
        return responseEntity;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add(ReCAPConstants.RESPONSE_DATE, new Date().toString());
        return responseHeaders;
    }
}
