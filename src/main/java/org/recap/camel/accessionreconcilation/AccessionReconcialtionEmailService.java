package org.recap.camel.accessionreconcilation;

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
public class AccessionReconcialtionEmailService {

    private static final Logger logger = LoggerFactory.getLogger(AccessionReconcialtionEmailService.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    @Value("${accession.reconcilation.email.pul.to}")
    private String pulEmailTo;

    @Value("${accession.reconcilation.email.cul.to}")
    private String culEmailTo;

    @Value("${accession.reconcilation.email.nypl.to}")
    private String nyplEmailTo;

    @Value("${ftp.accession.reconcilation.processed.pul}")
    private String pulReportLocation;

    @Value("${ftp.accession.reconcilation.processed.cul}")
    private String culReportLocation;

    @Value("${ftp.accession.reconcilation.processed.nypl}")
    private String nyplReportLocation;

    private String institutionCode;

    /**
     * Instantiates a new Accession reconcialtion email service.
     *
     * @param institutionCode the institution code
     */
    public AccessionReconcialtionEmailService(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    /**
     * Process input for accession reconcialtion email service.
     *
     * @param exchange the exchange
     */
    public void processInput(Exchange exchange) {
        logger.info("accession email started for"+institutionCode);
        producerTemplate.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(), ReCAPConstants.EMAIL_BODY_FOR,"AccessionReconcilation");
    }

    /**
     * Get email pay load for accession reconcialtion email service.
     *
     * @return the email pay load
     */
    public EmailPayLoad getEmailPayLoad(){
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setTo(emailIdTo(institutionCode));
        logger.info("Email sent to "+emailPayLoad.getTo());
        emailPayLoad.setMessageDisplay("Accession reconciliation report had generated for the "+institutionCode+" in the location - "+reportLocation(institutionCode));
        return emailPayLoad;
    }

    /**
     * Generate Email To id for accession reconcialtion email service.
     *
     * @param institution the institution
     * @return the string
     */
    public String emailIdTo(String institution) {
        if (ReCAPConstants.NYPL.equalsIgnoreCase(institution)) {
            return nyplEmailTo;
        } else if (ReCAPConstants.COLUMBIA.equalsIgnoreCase(institution)) {
            return culEmailTo;
        } else if (ReCAPConstants.PRINCETON.equalsIgnoreCase(institution)) {
            return pulEmailTo;
        }
        return null;
    }

    /**
     * Generate report location for accession reconcialtion email service.
     *
     * @param institution the institution
     * @return the string
     */
    public String reportLocation(String institution) {
        if (ReCAPConstants.PRINCETON.equalsIgnoreCase(institution)) {
            return pulReportLocation;
        } else if (ReCAPConstants.COLUMBIA.equalsIgnoreCase(institution)) {
            return culReportLocation;
        } else if (ReCAPConstants.NYPL.equalsIgnoreCase(institution)) {
            return nyplReportLocation;
        }
        return null;
    }

}
