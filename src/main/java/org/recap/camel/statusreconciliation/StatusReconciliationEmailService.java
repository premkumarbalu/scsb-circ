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
    private ProducerTemplate producerTemplate;

    @Value("${status.reconciliation.email.to}")
    private String statusReconciliationEmailTo;

    @Value("${status.reconciliation.email.cc}")
    private String statusReconciliationEmailCC;

    /**
     * Sets the email payload for the status reconciliation.
     *
     * @param exchange the exchange
     */
    public void processInput(Exchange exchange) {
        String fileLocation = (String) exchange.getIn().getHeaders().get("CamelFileNameProduced");
        producerTemplate.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(fileLocation), ReCAPConstants.EMAIL_BODY_FOR,"StatusReconcilation");
    }

    private EmailPayLoad getEmailPayLoad(String FileLocation){
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setCc(statusReconciliationEmailCC);
        emailPayLoad.setTo(statusReconciliationEmailTo);
        logger.info("Status Reconciliation : email sent to : {} and cc : {} ",emailPayLoad.getTo(),emailPayLoad.getCc());
        emailPayLoad.setMessageDisplay("The \"Out\" Status Reconciliation report is available at the FTP location "+FileLocation);
        return emailPayLoad;
    }
}
