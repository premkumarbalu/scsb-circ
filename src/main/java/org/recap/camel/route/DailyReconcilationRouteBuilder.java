package org.recap.camel.route;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.recap.ReCAPConstants;
import org.recap.model.csv.DailyReconcilationRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by akulak on 3/5/17.
 */
@Component
public class DailyReconcilationRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(DailyReconcilationRouteBuilder.class);

    public DailyReconcilationRouteBuilder(CamelContext camelContext, ApplicationContext applicationContext,
                                          @Value("${ftp.userName}") String ftpUserName, @Value("${ftp.daily.reconcilation}") String dailyReconcilationFtp,
                                          @Value("${ftp.daily.reconcilation.processed}") String dailyReconcilationFtpProcessed, @Value("${ftp.knownHost}") String ftpKnownHost,
                                          @Value("${ftp.privateKey}") String ftpPrivateKey,
                                          @Value("${daily.reconcilation.file}") String filePath) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + dailyReconcilationFtp + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.DAILY_RR_FTP_OPTIONS)
                            .routeId(ReCAPConstants.DAILY_RR_FTP_ROUTE_ID)
                            .noAutoStartup()
                            .unmarshal().bindy(BindyType.Csv, DailyReconcilationRecord.class)
                            .bean(applicationContext.getBean(DailyRRProcessor.class),ReCAPConstants.PROCESS_INPUT)
                            .onCompletion()
                            .process(new StopRouteProcessor(ReCAPConstants.DAILY_RR_FTP_ROUTE_ID));
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DAILY_RR_FS_FILE+filePath+ReCAPConstants.DAILY_RR_FS_OPTIONS)
                            .routeId(ReCAPConstants.DAILY_RR_FS_ROUTE_ID)
                            .noAutoStartup()
                            .to(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + dailyReconcilationFtpProcessed + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost)
                            .onCompletion()
                            .process(new StopRouteProcessor(ReCAPConstants.DAILY_RR_FS_ROUTE_ID));
                }
            });

        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }

    }
}
