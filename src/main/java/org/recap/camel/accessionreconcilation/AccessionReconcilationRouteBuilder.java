package org.recap.camel.accessionreconcilation;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.recap.ReCAPConstants;
import org.recap.camel.route.StopRouteProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by akulak on 16/5/17.
 */

@Component
public class AccessionReconcilationRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AccessionReconcilationRouteBuilder.class);

    public AccessionReconcilationRouteBuilder(CamelContext camelContext, ApplicationContext applicationContext,
                                              @Value("${ftp.userName}") String ftpUserName, @Value("${ftp.privateKey}") String ftpPrivateKey,
                                              @Value("${ftp.accession.reconcilation.pul}") String accessionReconcilationPulFtp,
                                              @Value("${ftp.accession.reconcilation.cul}") String accessionReconcilationCulFtp,
                                              @Value("${ftp.accession.reconcilation.nypl}") String accessionReconcilationNyplFtp,
                                              @Value("${ftp.accession.reconcilation.processed.pul}") String accessionReconcilationFtpPulProcessed,
                                              @Value("${ftp.accession.reconcilation.processed.cul}") String accessionReconcilationFtpCulProcessed,
                                              @Value("${ftp.accession.reconcilation.processed.nypl}") String accessionReconcilationFtpNyplProcessed,
                                              @Value("${ftp.knownHost}") String ftpKnownHost,
                                              @Value("${accession.reconcilation.filePath.pul}") String filePathPul,
                                              @Value("${accession.reconcilation.filePath.cul}") String filePathCul,
                                              @Value("${accession.reconcilation.filePath.nypl}") String filePathNypl) {
        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + accessionReconcilationPulFtp + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.ACCESSION_RR_FTP_OPTIONS)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_FTP_PUL_ROUTE)
                            .noAutoStartup()
                            .log("accession reconcilation pul started")
                            .split(body().tokenize("\n",1000,true))
                            .bean(applicationContext.getBean(AccessionReconcilationProcessor.class,ReCAPConstants.PRINCETON),ReCAPConstants.PROCESS_INPUT)
                            .end()
                            .onCompletion()
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    logger.info(ReCAPConstants.STARTING+ReCAPConstants.ACCESSION_RECONCILATION_FS_PUL_ROUTE);
                                    camelContext.startRoute(ReCAPConstants.ACCESSION_RECONCILATION_FS_PUL_ROUTE);
                                }
                            }).process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_FTP_PUL_ROUTE));
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + accessionReconcilationCulFtp + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.ACCESSION_RR_FTP_OPTIONS)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_FTP_CUL_ROUTE)
                            .noAutoStartup()
                            .log("accession reconcilation cul started")
                            .split(body().tokenize("\n",1000,true))
                            .bean(applicationContext.getBean(AccessionReconcilationProcessor.class,ReCAPConstants.COLUMBIA),ReCAPConstants.PROCESS_INPUT)
                            .end()
                            .onCompletion()
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    logger.info(ReCAPConstants.STARTING+ReCAPConstants.ACCESSION_RECONCILATION_FS_CUL_ROUTE);
                                    camelContext.startRoute(ReCAPConstants.ACCESSION_RECONCILATION_FS_CUL_ROUTE);
                                }
                            }).process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_FTP_CUL_ROUTE));
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + accessionReconcilationNyplFtp + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.ACCESSION_RR_FTP_OPTIONS)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_FTP_NYPL_ROUTE)
                            .noAutoStartup()
                            .log("accession reconcilation nypl started")
                            .split(body().tokenize("\n",1000,true))
                            .bean(applicationContext.getBean(AccessionReconcilationProcessor.class,ReCAPConstants.NYPL),ReCAPConstants.PROCESS_INPUT)
                            .end()
                            .onCompletion()
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    logger.info(ReCAPConstants.STARTING+ReCAPConstants.ACCESSION_RECONCILATION_FS_NYPL_ROUTE);
                                    camelContext.startRoute(ReCAPConstants.ACCESSION_RECONCILATION_FS_NYPL_ROUTE);
                                }
                            }).process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_FTP_NYPL_ROUTE));
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DAILY_RR_FS_FILE+filePathPul+ReCAPConstants.DAILY_RR_FS_OPTIONS)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_FS_PUL_ROUTE)
                            .noAutoStartup()
                            .to(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + accessionReconcilationFtpPulProcessed + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost)
                            .onCompletion()
                            .bean(applicationContext.getBean(AccessionReconcialtionEmailService.class,ReCAPConstants.PRINCETON),ReCAPConstants.PROCESS_INPUT)
                            .process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_FS_PUL_ROUTE))
                            .log("accession reconcilation pul completed");

                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DAILY_RR_FS_FILE+filePathCul+ReCAPConstants.DAILY_RR_FS_OPTIONS)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_FS_CUL_ROUTE)
                            .noAutoStartup()
                            .to(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + accessionReconcilationFtpCulProcessed + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost)
                            .onCompletion()
                            .bean(applicationContext.getBean(AccessionReconcialtionEmailService.class,ReCAPConstants.COLUMBIA),ReCAPConstants.PROCESS_INPUT)
                            .process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_FS_CUL_ROUTE))
                            .log("accession reconcilation cul completed");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DAILY_RR_FS_FILE+filePathNypl+ReCAPConstants.DAILY_RR_FS_OPTIONS)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_FS_NYPL_ROUTE)
                            .noAutoStartup()
                            .to(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + accessionReconcilationFtpNyplProcessed + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost)
                            .onCompletion()
                            .bean(applicationContext.getBean(AccessionReconcialtionEmailService.class,ReCAPConstants.NYPL),ReCAPConstants.PROCESS_INPUT)
                            .process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_FTP_NYPL_ROUTE))
                            .log("accession reconcilation nypl completed");
                }
            });

        } catch (Exception e) {
            logger.info(ReCAPConstants.LOG_ERROR+e);
        }

    }
}
