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
 * Created by akulak on 24/5/17.
 */
@RestController
@RequestMapping("/accessionReconcilation")
public class AccessionReconcilationJobController {

    private static final Logger logger = LoggerFactory.getLogger(AccessionReconcilationJobController.class);

    @Autowired
    CamelContext camelContext;

    /**
     * This method is used for generating report by, comparing LAS(ReCAP) barcodes and SCSB barcodes. The LAS barcodes are send to SCSB as CVS files, in specific FTP folder.
     * The barcodes are physically seprated by institution. This method will initiate the comparison of all the three institution at the same time.
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/startAccessionReconcilation",method = RequestMethod.POST)
    public String startAccessionReconcilation() throws Exception{
        logger.info("Starting Accession Reconcilation Routes");
        camelContext.startRoute(ReCAPConstants.ACCESSION_RECONCILATION_FTP_PUL_ROUTE);
        camelContext.startRoute(ReCAPConstants.ACCESSION_RECONCILATION_FTP_CUL_ROUTE);
        camelContext.startRoute(ReCAPConstants.ACCESSION_RECONCILATION_FTP_NYPL_ROUTE);
        return ReCAPConstants.SUCCESS;
    }
}
