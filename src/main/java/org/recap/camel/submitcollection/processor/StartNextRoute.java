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

    @Value("${submit.collection.email.subject.for.empty.directory}")
    private String submitCollectionEmailSubjectForEmptyDirectory;

    @Value("${submit.collection.nofiles.email.pul.to}")
    private String emailToPUL;

    @Value("${submit.collection.nofiles.email.cul.to}")
    private String emailToCUL;

    @Value("${submit.collection.nofiles.email.nypl.to}")
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
        String ftpLocationPath = (String) exchange.getFromEndpoint().getEndpointConfiguration().getParameter("path");
        if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_PUL_ROUTE )){
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.PRINCETON,ftpLocationPath), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
            logger.info("Email Sent");
        }
        else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_PUL_ROUTE)){
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.PRINCETON, ftpLocationPath), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
        }
        else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_CUL_ROUTE)){
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.COLUMBIA, ftpLocationPath), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
        }
        else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_CUL_ROUTE)){
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.COLUMBIA, ftpLocationPath), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
        }
        else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_NYPL_ROUTE)){
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.NYPL, ftpLocationPath), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
        }
        else if(routeId.equalsIgnoreCase(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_NYPL_ROUTE)){
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(ReCAPConstants.NYPL, ftpLocationPath), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES);
        }
    }

    private EmailPayLoad getEmailPayLoad(String institutionCode, String ftpLocationPath) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setSubject(submitCollectionEmailSubjectForEmptyDirectory);
        emailPayLoad.setLocation(ftpLocationPath);
        if(ReCAPConstants.PRINCETON.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToPUL);
        } else if(ReCAPConstants.COLUMBIA.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToCUL);
        } else if(ReCAPConstants.NYPL.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToNYPL);
        }
        return  emailPayLoad;
    }

}
