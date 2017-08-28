package org.recap.camel.dailyreconciliation;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.camel.route.StopRouteProcessor;
import org.recap.model.csv.DailyReconcilationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by akulak on 3/5/17.
 */
@Component
public class DailyReconciliationRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DailyReconciliationRouteBuilder.class);

    /**
     * Instantiates a new Daily reconcilation route builder.
     *
     * @param camelContext                   the camel context
     * @param applicationContext             the application context
     * @param ftpUserName                    the ftp user name
     * @param dailyReconciliationFtp          the daily reconciliation ftp
     * @param dailyReconciliationFtpProcessed the daily reconciliation ftp processed
     * @param ftpKnownHost                   the ftp known host
     * @param ftpPrivateKey                  the ftp private key
     * @param filePath                       the file path
     */

    /**
     * Predicate to identify is the input file is gz
     */
    Predicate gzipFile = new Predicate() {
        @Override
        public boolean matches(Exchange exchange) {

            String fileName = (String) exchange.getIn().getHeader(Exchange.FILE_NAME);
            return StringUtils.equalsIgnoreCase("gz", FilenameUtils.getExtension(fileName));

        }
    };

    public DailyReconciliationRouteBuilder(CamelContext camelContext, ApplicationContext applicationContext,
                                           @Value("${ftp.userName}") String ftpUserName, @Value("${ftp.daily.reconciliation}") String dailyReconciliationFtp,
                                           @Value("${ftp.daily.reconciliation.processed}") String dailyReconciliationFtpProcessed, @Value("${ftp.knownHost}") String ftpKnownHost,
                                           @Value("${ftp.privateKey}") String ftpPrivateKey,
                                           @Value("${daily.reconciliation.file}") String filePath,
                                           @Value("${daily.reconciliation.local.work.dir}") String localWorkDir) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + dailyReconciliationFtp + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.DAILY_RR_FTP_OPTIONS+localWorkDir)
                            .routeId(ReCAPConstants.DAILY_RR_FTP_ROUTE_ID)
                            .noAutoStartup()
                            .log("daily reconciliation started")
                            .choice()
                            .when(gzipFile)
                            .unmarshal()
                            .gzip()
                            .log("Unzip processed completed for daily reconciliation file")
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    String fileName = (String)exchange.getIn().getHeader(Exchange.FILE_NAME);
                                    exchange.getIn().setHeader(Exchange.FILE_NAME, fileName.replaceFirst(".gz", ".csv"));
                                }
                            })
                            .to(ReCAPConstants.DIRECT+ReCAPConstants.PROCESS_DAILY_RECONCILIATION)
                            .otherwise()
                            .to(ReCAPConstants.DIRECT+ReCAPConstants.PROCESS_DAILY_RECONCILIATION)
                            .end()
                            .onCompletion()
                            .choice()
                            .when(exchangeProperty(ReCAPConstants.CAMEL_BATCH_COMPLETE))
                            .log("Stopping DailyReconciliation Process")
                            .process(new StopRouteProcessor(ReCAPConstants.DAILY_RR_FTP_ROUTE_ID));

                    from(ReCAPConstants.DIRECT+ReCAPConstants.PROCESS_DAILY_RECONCILIATION)
                            .unmarshal().bindy(BindyType.Csv, DailyReconcilationRecord.class)
                            .bean(applicationContext.getBean(DailyReconciliationProcessor.class),ReCAPConstants.PROCESS_INPUT)
                            .end();
                }
            });



            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DAILY_RR_FS_FILE+filePath+ReCAPConstants.DAILY_RR_FS_OPTIONS)
                            .routeId(ReCAPConstants.DAILY_RR_FS_ROUTE_ID)
                            .noAutoStartup()
                            .to(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + dailyReconciliationFtpProcessed + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost)
                            .onCompletion()
                            .log("email service started for daily reconciliation")
                            .bean(applicationContext.getBean(DailyReconciliationEmailService.class))
                            .process(new StopRouteProcessor(ReCAPConstants.DAILY_RR_FS_ROUTE_ID));
                }
            });

        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }

    }
}
