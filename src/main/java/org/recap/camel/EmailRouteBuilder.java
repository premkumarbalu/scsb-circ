package org.recap.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.FileUtils;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;
import java.io.*;

import static org.apache.camel.component.xslt.XsltOutput.file;

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
    private String emailBodyForRequestPending;
    private String emailBodyForSubmitCollection;
    private String emailBodyForSubmitCollectionEmptyDirectory;
    private String emailBodyForExceptionInSubmitColletion;
    private String emailBodyForBulkRequestProcess;

    /**
     * Instantiates a new Email route builder.
     *
     * @param context           the context
     * @param username          the username
     * @param passwordDirectory the password directory
     * @param from              the from
     * @param subject           the subject
     * @param requestPendingTo  the request pending to
     * @param smtpServer        the smtp server
     */
    @Autowired
    public EmailRouteBuilder(CamelContext context, @Value("${scsb.email.username}") String username, @Value("${scsb.email.password.file}") String passwordDirectory,
                             @Value("${scsb.email.from}") String from, @Value("${request.recall.email.subject}") String subject,
                             @Value("${recap.assist.email.to}") String requestPendingTo, @Value("${smtpServer}") String smtpServer) {
        try {
            context.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    loadEmailPassword();
                    loadEmailBodyForRequestPending();
                    loadEmailBodyTemplateForNoData();
                    loadEmailBodyTemplateForSubmitCollectionEmptyDirectory();
                    loadEmailBodyTemplateForExceptionInSubmitCollection();
                    loadEmailBodyForBulkRequest();
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
                                    .setHeader("cc", simple("${header.emailPayLoad.cc}"))
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
                                    .log("email for accession Reconciliation")
                                    .setHeader("subject", simple("Barcode Reconciliation Report"))
                                    .setBody(simple("${header.emailPayLoad.messageDisplay}"))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .setHeader("cc", simple("${header.emailPayLoad.cc}"))
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
                                    .setHeader("subject", simple("\"Out\" Status Reconciliation Report"))
                                    .setBody(simple("${header.emailPayLoad.messageDisplay}"))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .setHeader("cc", simple("${header.emailPayLoad.cc}"))
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                               .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo(ReCAPConstants.DAILY_RECONCILIATION))
                                    .log("email for Daily Reconciliation")
                                    .setHeader("subject", simple("Daily Reconciliation Report"))
                                    .setBody(simple("${header.emailPayLoad.messageDisplay}"))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                                .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo(ReCAPConstants.REQUEST_INITIAL_DATA_LOAD))
                                    .setHeader("subject", simple("${header.emailPayLoad.subject}"))
                                    .setBody(simple("${header.emailPayLoad.messageDisplay}"))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .log("Email for request initial data load")
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                                .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo(ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION))
                                    .setHeader("subject", simple("${header.emailPayLoad.subject}"))
                                    .setBody(simple(emailBodyForExceptionInSubmitColletion))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .setHeader("cc", simple("${header.emailPayLoad.cc}"))
                                    .log("Email sent for exception in submit collection")
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                                .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo(ReCAPConstants.EMAIL_HEADER_REQUEST_PENDING))
                                    .setHeader("subject", simple("LAS Pending Request Queue"))
                                    .setBody(simple(emailBodyForRequestPending))
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple(requestPendingTo))
                                    .log("Email for request pending")
                                    .to("smtps://" + smtpServer + "?username=" + username + "&password=" + emailPassword)
                                 .when(header(ReCAPConstants.EMAIL_BODY_FOR).isEqualTo(ReCAPConstants.BULK_REQUEST_EMAIL_QUEUE))
                                    .setHeader("subject", simple("${header.emailPayLoad.subject}"))
                                    .setBody(simple(emailBodyForBulkRequestProcess))
                                    .process(new Processor() {
                                        @Override
                                        public void process(Exchange exchange) throws Exception {
                                            try {
                                                Message in = exchange.getIn();
                                                EmailPayLoad emailPayLoad = (EmailPayLoad) in.getHeader("emailPayLoad");
                                                in.addAttachment("Results_" + emailPayLoad.getBulkRequestFileName(), new DataHandler(emailPayLoad.getBulkRequestCsvFileData(), "text/csv"));
                                            } catch (Exception ex) {
                                                logger.info(ReCAPConstants.LOG_ERROR, ex);
                                            }
                                        }
                                    })
                                    .setHeader("from", simple(from))
                                    .setHeader("to", simple("${header.emailPayLoad.to}"))
                                    .log("Email sent for bulk request process")
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

                private void loadEmailBodyTemplateForExceptionInSubmitCollection() {
                    InputStream inputStream = getClass().getResourceAsStream(ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION_BODY_VM);
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
                    emailBodyForExceptionInSubmitColletion = out.toString();
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

                private void loadEmailBodyForRequestPending() {
                    InputStream inputStream = getClass().getResourceAsStream(ReCAPConstants.REQUEST_PENDING_EMAIL_BODY_VM);
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
                    emailBodyForRequestPending = out.toString();
                }

                private void loadEmailBodyForBulkRequest() {
                    InputStream inputStream = getClass().getResourceAsStream(ReCAPConstants.BULK_REQUEST_EMAIL_BODY_VM);
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
                        logger.error(ReCAPConstants.LOG_ERROR, e);
                    }
                    emailBodyForBulkRequestProcess = out.toString();
                }
            });
        } catch (Exception e) {
            logger.error(ReCAPConstants.REQUEST_EXCEPTION,e);
        }
    }

}
