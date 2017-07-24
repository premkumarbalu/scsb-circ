package org.recap.camel.accessionreconciliation;

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
 * Created by akulak on 22/5/17.
 */
@Service
@Scope("prototype")
public class AccessionReconciliationEmailService {

    private static final Logger logger = LoggerFactory.getLogger(AccessionReconciliationEmailService.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @Value("${accession.reconciliation.email.pul.to}")
    private String pulEmailTo;

    @Value("${accession.reconciliation.email.cul.to}")
    private String culEmailTo;

    @Value("${accession.reconciliation.email.nypl.to}")
    private String nyplEmailTo;

    @Value("${accession.reconciliation.email.pul.cc}")
    private String pulEmailCC;

    @Value("${accession.reconciliation.email.cul.cc}")
    private String culEmailCC;

    @Value("${accession.reconciliation.email.nypl.cc}")
    private String nyplEmailCC;

    private String institutionCode;

    /**
     * Instantiates a new Accession reconciliation email service.
     *
     * @param institutionCode the institution code
     */
    public AccessionReconciliationEmailService(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    /**
     * Process input for accession reconciliation email service.
     *
     * @param exchange the exchange
     */
    public void processInput(Exchange exchange) {
        logger.info("accession email started for"+institutionCode);
        producerTemplate.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(exchange), ReCAPConstants.EMAIL_BODY_FOR,"AccessionReconcilation");
    }

    /**
     * Get email pay load for accession reconciliation email service.
     *
     * @return the email pay load
     */
    public EmailPayLoad getEmailPayLoad(Exchange exchange){
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        String fileNameWithPath = (String)exchange.getIn().getHeader("CamelFileNameProduced");
        emailIdTo(institutionCode,emailPayLoad);
        logger.info("Accession Reconciliation email sent to : {} and cc : {} ",emailPayLoad.getTo(),emailPayLoad.getCc());
        emailPayLoad.setMessageDisplay("Barcode Reconciliation has been completed for "+institutionCode.toUpperCase()+". The report is at the FTP location "+fileNameWithPath);
        return emailPayLoad;
    }

    /**
     * Generate Email To id for accession reconciliation email service.
     *
     * @param institution the institution
     * @param emailPayLoad
     * @return the string
     */
    public String emailIdTo(String institution, EmailPayLoad emailPayLoad) {
        if (ReCAPConstants.NYPL.equalsIgnoreCase(institution)) {
            emailPayLoad.setCc(nyplEmailCC);
            emailPayLoad.setTo(nyplEmailTo);
        } else if (ReCAPConstants.COLUMBIA.equalsIgnoreCase(institution)) {
            emailPayLoad.setCc(culEmailCC);
            emailPayLoad.setTo(culEmailTo);
        } else if (ReCAPConstants.PRINCETON.equalsIgnoreCase(institution)) {
            emailPayLoad.setCc(pulEmailCC);
            emailPayLoad.setTo(pulEmailTo);
        }
        return null;
    }

}
