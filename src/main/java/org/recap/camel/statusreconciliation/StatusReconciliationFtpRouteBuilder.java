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

/**
 * Created by hemalathas on 22/5/17.
 */
@Component
public class StatusReconciliationFtpRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(StatusReconciliationFtpRouteBuilder.class);

    /**
     * Instantiates a new Status reconciliation ftp route builder.
     *
     * @param camelContext         the camel context
     * @param applicationContext   the application context
     * @param ftpUserName          the ftp user name
     * @param ftpKnownHost         the ftp known host
     * @param ftpPrivateKey        the ftp private key
     * @param statusReconciliation the status reconciliation
     */
    @Autowired
    public StatusReconciliationFtpRouteBuilder(CamelContext camelContext, ApplicationContext applicationContext,
                                                        @Value("${ftp.userName}") String ftpUserName,
                                                        @Value("${ftp.knownHost}") String ftpKnownHost, @Value("${ftp.privateKey}") String ftpPrivateKey,
                                                        @Value("${status.reconciliation}") String statusReconciliation){
        try{
            camelContext.addRoutes(new RouteBuilder(){
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.STATUS_RECONCILIATION_REPORT)
                            .routeId(ReCAPConstants.STATUS_RECONCILIATION_REPORT_ID)
                            .onCompletion().onWhen(header(ReCAPConstants.FOR).isEqualTo(ReCAPConstants.STATUS_RECONCILIATION))
                            .log("status reconciliation process finished files generated in ftp")
                            .bean(applicationContext.getBean(StatusReconciliationEmailService.class),ReCAPConstants.PROCESS_INPUT)
                            .end()
                            .choice()
                                .when(header(ReCAPConstants.FOR).isEqualTo(ReCAPConstants.STATUS_RECONCILIATION))
                                    .marshal().bindy(BindyType.Csv, StatusReconciliationCSVRecord.class)
                                    .to(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + statusReconciliation + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost + "&fileName=StatusReconciliation-${date:now:yyyyMMdd_HHmmss}.csv")
                                .when(header(ReCAPConstants.FOR).isEqualTo(ReCAPConstants.STATUS_RECONCILIATION_FAILURE))
                                    .marshal().bindy(BindyType.Csv, StatusReconciliationErrorCSVRecord.class)
                                    .to(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + statusReconciliation + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost + "&fileName=StatusReconciliationFailure-${date:now:yyyyMMdd_HHmmss}.csv")
                                    .log("status reconciliation failure report generated in ftp");
                }
            });
        }catch (Exception e){
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }
    }
}
