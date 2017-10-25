package org.recap.controller;

import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.camel.EmailPayLoad;
import org.recap.service.ActiveMqQueuesInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by angelind on 22/8/17.
 */
@RestController
@RequestMapping("/notifyPendingRequest")
public class EmailPendingRequestJobController {

    private static final Logger logger = LoggerFactory.getLogger(EmailPendingRequestJobController.class);

    private static String queueName = "lasOutgoingQ";

    /**
     * The Producer template.
     */
    @Autowired
    ProducerTemplate producerTemplate;

    /**
     * The Pending request limit.
     */
    @Value("${request.pending.limit}")
    Integer pendingRequestLimit;

    @Autowired
    private ActiveMqQueuesInfo activemqQueuesInfo;

    /**
     * Send email for pending request string.
     *
     * @return the string
     * @throws Exception the exception
     */
    @RequestMapping(value = "/sendEmailForPendingRequest",method = RequestMethod.POST)
    public String sendEmailForPendingRequest() throws Exception{
        Integer pendingRequests = activemqQueuesInfo.getActivemqQueuesInfo(queueName);
        if(pendingRequests >= pendingRequestLimit) {
            logger.info("Pending Request : {}", pendingRequests);
            EmailPayLoad emailPayLoad = new EmailPayLoad();
            emailPayLoad.setPendingRequestLimit(String.valueOf(pendingRequestLimit));
            producerTemplate.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, emailPayLoad, ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.EMAIL_HEADER_REQUEST_PENDING);
        }
        return ReCAPConstants.SUCCESS;
    }

}
