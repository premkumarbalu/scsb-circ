package org.recap.controller;

import org.apache.camel.CamelContext;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by hemalathas on 16/6/17.
 */
@RestController
@RequestMapping("/requestInitialDataLoad")
public class RequestDataLoadController {

    private static final Logger logger = LoggerFactory.getLogger(RequestDataLoadController.class);

    @Autowired
    CamelContext camelContext;

    @RequestMapping(value = "/startRequestInitialLoad",method = RequestMethod.POST)
    public String startAccessionReconcilation() throws Exception{
        logger.info("Request Initial DataLoad Starting.....");
        camelContext.startRoute(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_FTP_ROUTE);
        camelContext.startRoute(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_FTP_ROUTE);
        camelContext.startRoute(ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_FTP_ROUTE);
        return ReCAPConstants.SUCCESS;
    }
}
