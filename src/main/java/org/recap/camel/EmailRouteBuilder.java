package org.recap.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.FileUtils;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * Created by chenchulakshmig on 13/9/16.
 */
@Component
public class EmailRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(EmailRouteBuilder.class);

    private String emailBodyLasStatus;
    private String emailBodyRecall;
    private String emailBodyDeletedRecords;
    private String emailPassword;
    private String emailBodyForSubmitCollection;
    private String emailBodyForSubmitCollectionEmptyDirectory;

    /**
     * Instantiates a new Email route builder.
     *
     * @param context           the context
     * @param username          the username
     * @param passwordDirectory the password directory
     * @param from              the from
     * @param subject           the subject
     * @param smtpServer        the smtp server
     */
    @Autowired
    public EmailRouteBuilder(CamelContext context, @Value("${scsb.email.username}") String username, @Value("${scsb.email.password.file}") String passwordDirectory,
                             @Value("${scsb.email.from}") String from, @Value("${request.recall.email.subject}") String subject,
                             @Value("${smtpServer}") String smtpServer) {
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    loadEmailPassword();
                    loadEmailBodyTemplateForNoData();
                    loadEmailBodyTemplateForSubmitCollectionEmptyDirectory();
                    emailBodyRecall = loadEmailLasStatus(ReCAPConstants.REQUEST_RECALL_EMAIL_TEMPLATE);
                    emailBodyLasStatus = loadEmailLasStatus(ReCAPConstants.REQUEST_LAS_STATUS_EMAIL_TEMPLATE);
                    emailBodyDeletedRecords=loadEmailLasStatus(ReCAPConstants.DELETED_RECORDS_EMAIL_TEMPLATE);

                    from(ReCAPConstants.EMAIL_Q)
                            .routeId(ReCAPConstants.EMAIL_ROUTE_ID)
                            .setHeader("emailPayLoad").body(EmailPayLoad.class)
                            .onCompletion().log("Email has been sent successfully.")
                            .end()
                            .choice()
                                .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo(ReCAPConstants.REQUEST_RECALL_MAIL_QUEUE))
                                    .setHeader("subject", simple("${header.emailPayLoad.subject}"))
                                    .setBody(simple(emailBodyRecall))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .log("Email for Recall")
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                                .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo(ReCAPConstants.REQUEST_LAS_STATUS_MAIL_QUEUE))
                                    .setHeader("subject", simple("${header.emailPayLoad.subject}"))
                                    .setBody(simple(emailBodyLasStatus))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .log("Email for LAS Status")
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                                .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo(ReCAPConstants.SUBMIT_COLLECTION))
                                    .setHeader("subject", simple("${header.emailPayLoad.subject}"))
                                    .setBody(simple(emailBodyForSubmitCollection))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .log("email body for submit collection")
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                                .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo(ReCAPConstants.SUBMIT_COLLECTION_FOR_NO_FILES))
                                    .setHeader("subject", simple("${header.emailPayLoad.subject}"))
                                    .setBody(simple(emailBodyForSubmitCollectionEmptyDirectory))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .log("email body for submit collection")
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                                .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo(ReCAPConstants.REQUEST_ACCESSION_RECONCILATION_MAIL_QUEUE))
                                    .log("email for accesion Reconciliation")
                                    .setHeader("subject", simple("Accession Reconciliation"))
                                    .setBody(simple("${header.emailPayLoad.messageDisplay}"))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                                .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo(ReCAPConstants.DELETED_MAIL_QUEUE))
                                    .setHeader("subject", simple("${header.emailPayLoad.subject}"))
                                    .setBody(simple(emailBodyDeletedRecords))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .log("Email Send for Deleted Records")
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                                .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo("StatusReconcilation"))
                                    .log("email for status Reconciliation")
                                    .setHeader("subject", simple("Status Reconciliation"))
                                    .setBody(simple("${header.emailPayLoad.messageDisplay}"))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                               .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo(ReCAPConstants.DAILY_RECONCILIATION))
                                    .log("email for Daily Reconciliation")
                                    .setHeader("subject", simple("Daily Reconciliation"))
                                    .setBody(simple("${header.emailPayLoad.messageDisplay}"))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                    ;
                }

                private String loadEmailLasStatus(String emailTemplate) {
                    InputStream inputStream = getClass().getResourceAsStream(emailTemplate);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder out = new StringBuilder();
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                out.append("\n");
                            } else {
                                out.append(line);
                                out.append("\n");
                            }
                        }
                    } catch (IOException e) {
                        logger.error(ReCAPConstants.LOG_ERROR,e);
                    }
                    return out.toString();
                }

                private void loadEmailBodyTemplateForNoData() {
                    InputStream inputStream = getClass().getResourceAsStream(ReCAPConstants.SUBMIT_COLLECTION_EMAIL_BODY_VM);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder out = new StringBuilder();
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                out.append("\n");
                            } else {
                                out.append(line);
                                out.append("\n");
                            }
                        }
                    } catch (IOException e) {
                        logger.error(ReCAPConstants.LOG_ERROR,e);
                    }
                    emailBodyForSubmitCollection = out.toString();
                }

                private void loadEmailBodyTemplateForSubmitCollectionEmptyDirectory() {
                    InputStream inputStream = getClass().getResourceAsStream(ReCAPConstants.SUBMIT_COLLECTION_EMAIL_BODY_FOR_EMPTY_DIRECTORY_VM);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder out = new StringBuilder();
                    String line;
                    try {
                        while ((line = reader.readLine()) != null) {
                            if (line.isEmpty()) {
                                out.append("\n");
                            } else {
                                out.append(line);
                                out.append("\n");
                            }
                        }
                    } catch (IOException e) {
                        logger.error(ReCAPConstants.LOG_ERROR,e);
                    }
                    emailBodyForSubmitCollectionEmptyDirectory = out.toString();
                }

                private void loadEmailPassword() {
                    File file = new File(passwordDirectory);
                    if (file.exists()) {
                        try {
                            emailPassword = FileUtils.readFileToString(file, "UTF-8").trim();
                        } catch (IOException e) {
                            logger.error(ReCAPConstants.LOG_ERROR,e);
                        }
                    }
                }
            });
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION,e);
        }
    }

}
