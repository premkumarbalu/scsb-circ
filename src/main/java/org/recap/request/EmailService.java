package org.recap.request;

import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.camel.EmailPayLoad;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by sudhishk on 19/1/17.
 */
@Service
public class EmailService {

    @Value("${request.recall.email.nypl.to}")
    private String nyplMailTo;

    @Value("${request.recall.email.pul.to}")
    private String pulMailTo;

    @Value("${request.recall.email.cul.to}")
    private String culMailTo;

    @Value("${request.cancel.email.recap.to}")
    private String recapMailTo;

    @Value("${deleted.records.email.to}")
    private String deletedRecordsMailTo;

    @Value("${request.recall.email.nypl.cc}")
    private String nyplMailCC;

    @Value("${request.recall.email.cul.cc}")
    private String culMailCC;

    @Value("${request.recall.email.pul.cc}")
    private String pulMailCC;

    @Value("${request.recall.email.recap.cc}")
    private String recapMailCC;

    @Value("${request.refile.email.nypl.to}")
    private String refileNyplMailTo;
    @Value("${request.refile.email.cul.to}")
    private String refileCulMailTo;
    @Value("${request.refile.email.pul.to}")
    private String refilePulMailTo;
    @Value("${request.refile.email.recap.to}")
    private String refileRecapMailTo;

    @Value("${bulk.request.email.to}")
    private String bulkRequestEmailTo;

    @Autowired
    private ProducerTemplate producer;


    /**
     * Send email method for recall process, the information is send to the mail queue, with .
     *
     * @param customerCode   the customer code
     * @param itemBarcode    the item barcode
     * @param messageDisplay the message display
     * @param patronBarcode  the patron barcode
     * @param toInstitution  the to institution
     */
    public void sendEmail(String customerCode, String itemBarcode, String messageDisplay, String patronBarcode, String toInstitution, String subject) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setTo(emailIdTo(toInstitution));
        emailPayLoad.setCc(emailIdCC(toInstitution));
        emailPayLoad.setCustomerCode(customerCode);
        emailPayLoad.setItemBarcode(itemBarcode);
        emailPayLoad.setMessageDisplay(messageDisplay);
        emailPayLoad.setPatronBarcode(patronBarcode);
        emailPayLoad.setSubject(subject + itemBarcode);
        producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, emailPayLoad, ReCAPConstants.EMAIL_BODY_FOR, ReCAPConstants.REQUEST_RECALL_MAIL_QUEUE);
    }

    /**
     *  Send email method for deleted records reporting.
     *
     * @param messageDisplay
     * @param patronBarcode
     * @param toInstitution
     * @param subject
     */
    public void sendEmail(String messageDisplay, String patronBarcode, String toInstitution, String subject) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setTo(emailIdTo(toInstitution));
        emailPayLoad.setMessageDisplay(messageDisplay);
        emailPayLoad.setPatronBarcode(patronBarcode);
        emailPayLoad.setSubject(subject);
        producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, emailPayLoad, ReCAPConstants.EMAIL_BODY_FOR, ReCAPConstants.DELETED_MAIL_QUEUE);
    }

    public void sendEmail(String itemBarcode, String toInstitution, String subject) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setTo(refileEmailIdTo(toInstitution));
        emailPayLoad.setItemBarcode(itemBarcode);
        emailPayLoad.setSubject(subject);
        producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, emailPayLoad, ReCAPConstants.EMAIL_BODY_FOR, ReCAPConstants.REQUEST_LAS_STATUS_MAIL_QUEUE);
    }

    /**
     * Send email for bulk request process.
     *
     * @param bulkRequestId
     * @param bulkRequestName
     * @param bulkRequestFileName
     * @param bulkRequestStatus
     * @param subject
     */
    public void sendBulkRequestEmail(String bulkRequestId, String bulkRequestName, String bulkRequestFileName, String bulkRequestStatus, String bulkRequestCsvFileData, String subject) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setTo(bulkRequestEmailTo);
        emailPayLoad.setBulkRequestId(bulkRequestId);
        emailPayLoad.setBulkRequestName(bulkRequestName);
        emailPayLoad.setBulkRequestFileName(bulkRequestFileName);
        emailPayLoad.setBulkRequestStatus(bulkRequestStatus);
        emailPayLoad.setBulkRequestCsvFileData(bulkRequestCsvFileData);
        emailPayLoad.setSubject(subject);
        producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, emailPayLoad, ReCAPConstants.EMAIL_BODY_FOR, ReCAPConstants.BULK_REQUEST_EMAIL_QUEUE);
    }

    private String refileEmailIdTo(String institution) {
        if (institution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
            return refileNyplMailTo;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
            return refileCulMailTo;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
            return refilePulMailTo;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.GFA)) {
            return refileRecapMailTo;
        }
        return null;
    }

    /**
     * @param institution
     * @return
     */
    private String emailIdTo(String institution) {
        if (institution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
            return nyplMailTo;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
            return culMailTo;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
            return pulMailTo;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.GFA)) {
            return recapMailTo;
        } else if(institution.equalsIgnoreCase(ReCAPConstants.DELETED_MAIl_TO)){
            return deletedRecordsMailTo;
        }
        return null;
    }

    private String emailIdCC(String institution) {
        if (institution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
            return nyplMailCC;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
            return culMailCC;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
            return pulMailCC;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.GFA)) {
            return recapMailCC;
        }
        return null;
    }
}
