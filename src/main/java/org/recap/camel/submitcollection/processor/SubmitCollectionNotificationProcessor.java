package org.recap.camel.submitcollection.processor;

import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.camel.EmailPayLoad;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by premkb on 20/3/17.
 */
public class SubmitCollectionNotificationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionNotificationProcessor.class);

    @Autowired
    private ProducerTemplate producer;

    public void sendSubmitCollectionNotification(){
        producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION);
    }

    private EmailPayLoad getEmailPayLoad(){
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setSubject("Sub collec not");
        emailPayLoad.setMessageDisplay("sucess started");
        emailPayLoad.setTo("premlovesindia@gmail.com");
        return  emailPayLoad;
    }
}
