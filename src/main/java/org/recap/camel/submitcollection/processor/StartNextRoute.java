package org.recap.camel.submitcollection.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.log4j.Logger;
import org.recap.ReCAPConstants;
import org.recap.camel.EmailPayLoad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Created by harikrishnanv on 13/7/17.
 */
@Service
@Scope("prototype")
public class StartNextRoute implements Processor{

    @Autowired
    CamelContext camelContext;

    @Autowired
    private ProducerTemplate producer;

    @Value("${submit.collection.email.subject}")
    private String submitCollectionEmailSubject;

    @Value("${submit.collection.email.subject.for.empty.directory}")
    private String submitCollectionEmailSubjectForEmptyDirectory;

    @Value("${ftp.submit.collection.pul.report}")
    private String submitCollectionPULReportLocation;

    @Value("${ftp.submit.collection.cul.report}")
    private String submitCollectionCULReportLocation;

    @Value("${ftp.submit.collection.nypl.report}")
    private String submitCollectionNYPLReportLocation;

    @Value("${submit.collection.email.pul.to}")
    private String emailToPUL;

    @Value("${submit.collection.email.cul.to}")
    private String emailToCUL;

    @Value("${submit.collection.email.nypl.to}")
    private String emailToNYPL;


    private static final Logger logger = Logger.getLogger(StartNextRoute.class);
    private String routeId;

    public StartNextRoute(String routeId) {
        this.routeId = routeId;
    }

    /**
     * This method is used to start the next route in sequence.
     * @param exchange
     * @throws Exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {
       if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_PUL_ROUTE)){
           camelContext.startRoute(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_PUL_ROUTE);
       }
       else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_PUL_ROUTE)){
           camelContext.startRoute(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_CUL_ROUTE);
       }
       else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_CUL_ROUTE)){
           camelContext.startRoute(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_CUL_ROUTE);
       }
       else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_CUL_ROUTE)){
           camelContext.startRoute(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_NYPL_ROUTE);
       }
       else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_NYPL_ROUTE)){
           camelContext.startRoute(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_NYPL_ROUTE);
       }
       else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_NYPL_ROUTE)){
           logger.info("SubmitCollection Sequence completed");
       }
    }

    /**
     * This method is used to send email when there are no files in the respective directory
     * @param exchange
     * @throws Exception
     */
    public void sendEmailForEmptyDirectory(Exchange exchange) throws Exception {
        if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_PUL_ROUTE )){
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.PRINCETON), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
            logger.info("Email Sent");
        }
        else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_PUL_ROUTE)){
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.PRINCETON), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
        }
        else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_CUL_ROUTE)){
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.COLUMBIA), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
        }
        else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_CUL_ROUTE)){
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.COLUMBIA), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
        }
        else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_NYPL_ROUTE)){
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.NYPL), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
        }
        else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_NYPL_ROUTE)){
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.NYPL), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
        }
    }

    private EmailPayLoad getEmailPayLoad(String institutionCode) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setSubject(submitCollectionEmailSubjectForEmptyDirectory);
        if(ReCAPConstants.PRINCETON.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToPUL);
            emailPayLoad.setLocation(getFtpLocation(submitCollectionPULReportLocation));
        } else if(ReCAPConstants.COLUMBIA.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToCUL);
            emailPayLoad.setLocation(getFtpLocation(submitCollectionCULReportLocation));
        } else if(ReCAPConstants.NYPL.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToNYPL);
            emailPayLoad.setLocation(getFtpLocation(submitCollectionNYPLReportLocation));
        }
        return  emailPayLoad;
    }

    private String getFtpLocation(String ftpLocation) {
        if (ftpLocation.contains(ReCAPConstants.FTP_PORT)){
            String[] splittedFtpLocation = ftpLocation.split(ReCAPConstants.FTP_PORT);
            return splittedFtpLocation[1];
        }else {
            return ftpLocation;
        }

    }

}
