package org.recap.camel.statusreconciliation;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Created by hemalathas on 22/5/17.
 */
@Component
public class StatusReconciliationFtpRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(StatusReconciliationFtpRouteBuilder.class);

    @Autowired
    public StatusReconciliationFtpRouteBuilder(CamelContext camelContext, ApplicationContext applicationContext,
                                                        @Value("${ftp.userName}") String ftpUserName, @Value("${request.initial.accession}") String requestAccessionFolder,
                                                        @Value("${ftp.knownHost}") String ftpKnownHost, @Value("${ftp.privateKey}") String ftpPrivateKey,
                                                        @Value("${status.reconciliation}") String statusReconciliation){
        try{
            camelContext.addRoutes(new RouteBuilder(){
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.STATUS_RECONCILIATION_REPORT)
                            .routeId(ReCAPConstants.STATUS_RECONCILIATION_REPORT_ID)
                            .marshal().bindy(BindyType.Csv, StatusReconciliationCSVRecord.class)
                            .to(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + statusReconciliation + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost + "&fileName=${in.header.fileName}-${date:now:ddMMMyyyy-HH:mm:ss}.csv")
                            .onCompletion()
                            .bean(applicationContext.getBean(StatusReconciliationEmailService.class),ReCAPConstants.PROCESS_INPUT)
                            .log("Status reconcilation completed");

                }
            });
        }catch (Exception e){
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }
    }
}
