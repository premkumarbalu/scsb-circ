package org.recap.camel.statusreconciliation;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.camel.EmailPayLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by hemalathas on 31/5/17.
 */
@Service
@Scope("prototype")
public class StatusReconciliationEmailService {
    private static final Logger logger = LoggerFactory.getLogger(StatusReconciliationEmailService.class);

    @Autowired
    ProducerTemplate producerTemplate;

    @Value("${status.reconciliation.email.to}")
    private String statusReconciliationEmailTo;


    public void processInput(Exchange exchange) {
        String fileLocation = (String) exchange.getIn().getHeaders().get("CamelFileNameProduced");
        producerTemplate.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(fileLocation), ReCAPConstants.EMAIL_BODY_FOR,"StatusReconcilation");
    }

    private EmailPayLoad getEmailPayLoad(String FileLocation){
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setTo(statusReconciliationEmailTo);
        logger.info("Email sent to "+emailPayLoad.getTo());
        emailPayLoad.setMessageDisplay("Status Recocilation report has Generated in the ftp location - "+FileLocation);
        return emailPayLoad;
    }
}
