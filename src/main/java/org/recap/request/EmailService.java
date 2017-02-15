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

    @Autowired
    private ProducerTemplate producer;

    public void recallEmail(String institution, String itemBarcode, String titleIdentifier, String patronBarcode) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setTo(emailIdTo(institution));
        emailPayLoad.setInstitutionCode(institution);
        emailPayLoad.setItemBarcode(itemBarcode);
        emailPayLoad.setTitleIdentifier(titleIdentifier);
        emailPayLoad.setPatronBarcode(patronBarcode);
        producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, emailPayLoad, ReCAPConstants.REQUEST_RECALL_EMAILBODY_FOR, ReCAPConstants.REQUEST_RECALL_MAIL_QUEUE);
    }

    private String emailIdTo(String institution) {
        if (institution.equalsIgnoreCase(ReCAPConstants.NYPL)) {
            return nyplMailTo;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.COLUMBIA)) {
            return culMailTo;
        } else if (institution.equalsIgnoreCase(ReCAPConstants.PRINCETON)) {
            return pulMailTo;
        }
        return null;
    }
}
