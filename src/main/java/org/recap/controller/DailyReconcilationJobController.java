package org.recap.controller;

import org.apache.camel.CamelContext;
import org.recap.ReCAPConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by akulak on 9/5/17.
 */
@RestController
@RequestMapping("/dailyReconcilation")
public class DailyReconcilationJobController {

    @Autowired
    CamelContext camelContext;

    /**
     * This Rest service initiates, transaction comparions between LAS and SCSB on day-to-day basis.
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/startDailyReconcilation",method = RequestMethod.POST)
    public String statCamel() throws Exception{
        camelContext.startRoute(ReCAPConstants.DAILY_RR_FTP_ROUTE_ID);
        return ReCAPConstants.SUCCESS;
    }
}
