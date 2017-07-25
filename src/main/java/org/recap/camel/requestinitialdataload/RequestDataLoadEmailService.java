package org.recap.camel.requestinitialdataload;

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
 * Created by harikrishnanv on 18/7/17.
 */
@Service
@Scope("prototype")
public class RequestDataLoadEmailService {

    private static final Logger logger = LoggerFactory.getLogger(RequestDataLoadEmailService.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @Value("${request.initial.load.email.subject}")
    private String subjectForRequestInitialDataLoad;
    @Value("${request.initial.load.email.to.pul}")
    private String emailToPUL;
    @Value("${request.initial.load.email.to.cul}")
    private String emailToCUL;
    @Value("${request.initial.load.email.to.nypl}")
    private String emailToNYPL;

    private String institutionCode;

    public RequestDataLoadEmailService(String institutionCode){this.institutionCode=institutionCode;}

    public void processInput(Exchange exchange) {
        logger.info("ReqeustDataLoad EMailservice started for"+institutionCode);
        String fileNameWithPath = (String)exchange.getIn().getHeader("CamelFileNameProduced");
        producerTemplate.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(fileNameWithPath), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.REQUEST_INITIAL_DATA_LOAD);
    }

    public EmailPayLoad getEmailPayLoad(String fileNameWithPath){
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setTo(emailIdTo(institutionCode));
        emailPayLoad.setSubject(subjectForRequestInitialDataLoad);
        logger.info("RequestDataLoad email sent to "+emailPayLoad.getTo());
        emailPayLoad.setMessageDisplay(messageDisplayForInstitution(fileNameWithPath));
        return emailPayLoad;
    }

    public String emailIdTo(String institution) {
        if (ReCAPConstants.NYPL.equalsIgnoreCase(institution)) {
            return emailToNYPL;
        } else if (ReCAPConstants.COLUMBIA.equalsIgnoreCase(institution)) {
            return emailToCUL;
        } else if (ReCAPConstants.PRINCETON.equalsIgnoreCase(institution)) {
            return emailToPUL;
        }
        return null;
    }

    public String messageDisplayForInstitution(String fileNameWithPath){
        return "A report containing barcodes that have requests in GFA but which are not in SCSB has been created and can be found at the FTP location "+fileNameWithPath;
    }

}
