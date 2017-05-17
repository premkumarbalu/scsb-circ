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

    @Autowired
    private ProducerTemplate producer;

    /**
     * Send email.
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
        emailPayLoad.setCustomerCode(customerCode);
        emailPayLoad.setItemBarcode(itemBarcode);
        emailPayLoad.setMessageDisplay(messageDisplay);
        emailPayLoad.setPatronBarcode(patronBarcode);
        emailPayLoad.setSubject(subject + itemBarcode);
        producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, emailPayLoad, ReCAPConstants.REQUEST_RECALL_EMAILBODY_FOR, ReCAPConstants.REQUEST_RECALL_MAIL_QUEUE);
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
        }
        return null;
    }
}
