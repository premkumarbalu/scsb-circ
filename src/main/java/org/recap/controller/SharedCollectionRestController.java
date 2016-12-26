package org.recap.controller;

import org.recap.ReCAPConstants;
import org.recap.service.submitcollection.SubmitCollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

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
        try {
            List<Integer> bibIdList = submitCollectionService.process(inputRecords);
            response = submitCollectionService.indexData(bibIdList);
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
