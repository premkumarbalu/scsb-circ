package org.recap.camel.dailyreconciliation;

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
 * Created by akulak on 12/7/17.
 */
@Service
@Scope("singleton")
public class DailyReconciliationEmailService{

    private static final Logger logger = LoggerFactory.getLogger(DailyReconciliationEmailService.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @Value("${daily.reconciliation.email.to}")
    private String emailTo;

    private String fileLocation = new String();


    public void process(Exchange exchange) throws Exception {
        fileLocation = (String) exchange.getIn().getHeaders().get("CamelFileNameProduced");
        producerTemplate.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.DAILY_RECONCILIATION);
        logger.info("Daily Reconciliation file created in the path {}",fileLocation);
    }

    private EmailPayLoad getEmailPayLoad(){
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setTo(emailTo);
        logger.info("Daily Reconciliation email sent to "+emailPayLoad.getTo());
        emailPayLoad.setMessageDisplay("Daily reconciliation report is available at the FTP location "+fileLocation);
        return emailPayLoad;
    }
}
