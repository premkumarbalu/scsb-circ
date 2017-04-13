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
 * Created by hemalathas on 10/4/17.
 */
@RestController
@RequestMapping("/purge")
public class PurgeController {

    @Autowired
    private PurgeService purgeService;

    @RequestMapping(value = "/purgeEmailAddress", method = RequestMethod.GET)
    public ResponseEntity purgeEmailAddress(){
        Map<String,Integer> responseMap = purgeService.purgeEmailAddress();
        return new ResponseEntity(responseMap, HttpStatus.OK);
    }
}
