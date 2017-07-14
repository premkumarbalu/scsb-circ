package org.recap.controller;

import org.apache.camel.*;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by harikrishnanv on 20/6/17.
 */
@RestController
@RequestMapping("/submitCollectionJob")
public class SubmitCollectionJobController {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionJobController.class);

    @Autowired
    private ProducerTemplate producer;

    @Autowired
    private CamelContext camelContext;

    /**
     * This method is initiated from the scheduler to start the submit collection process in sequence
     * if the file exists in the respective folders.
     *
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/startSubmitCollection",method = RequestMethod.POST)
    public String startSubmitCollection() throws Exception{


        camelContext.startRoute(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_PUL_ROUTE);

        Endpoint endpoint = camelContext.getEndpoint(ReCAPConstants.SUBMIT_COLLECTION_COMPLETION_QUEUE_TO);
        PollingConsumer consumer = endpoint.createPollingConsumer();
        Exchange exchange = consumer.receive();

        logger.info("Message Received : {}", exchange.getIn().getBody());

        logger.info("Submit Collection Job ends");
        return ReCAPConstants.SUCCESS;
    }
}
