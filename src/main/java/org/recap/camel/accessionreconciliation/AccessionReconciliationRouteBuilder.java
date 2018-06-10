package org.recap.camel.accessionreconciliation;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.camel.route.StartRouteProcessor;
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
public class AccessionReconciliationRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(AccessionReconciliationRouteBuilder.class);

    /**
     * Instantiates a new Accession reconciliation route builder.
     *
     * @param camelContext                           the camel context
     * @param applicationContext                     the application context
     * @param ftpUserName                            the ftp user name
     * @param ftpPrivateKey                          the ftp private key
     * @param accessionReconciliationPulFtp           the accession reconciliation pul ftp
     * @param accessionReconciliationCulFtp           the accession reconciliation cul ftp
     * @param accessionReconciliationNyplFtp          the accession reconciliation nypl ftp
     * @param accessionReconciliationFtpPulProcessed  the accession reconciliation ftp pul processed
     * @param accessionReconciliationFtpCulProcessed  the accession reconciliation ftp cul processed
     * @param accessionReconciliationFtpNyplProcessed the accession reconciliation ftp nypl processed
     * @param ftpKnownHost                           the ftp known host
     * @param filePathPul                            the file path pul
     * @param filePathCul                            the file path cul
     * @param filePathNypl                           the file path nypl
     */
    public AccessionReconciliationRouteBuilder(CamelContext camelContext, ApplicationContext applicationContext,
                                               @Value("${ftp.userName}") String ftpUserName, @Value("${ftp.privateKey}") String ftpPrivateKey,
                                               @Value("${ftp.accession.reconciliation.pul}") String accessionReconciliationPulFtp,
                                               @Value("${ftp.accession.reconciliation.cul}") String accessionReconciliationCulFtp,
                                               @Value("${ftp.accession.reconciliation.nypl}") String accessionReconciliationNyplFtp,
                                               @Value("${ftp.accession.reconciliation.processed.pul}") String accessionReconciliationFtpPulProcessed,
                                               @Value("${ftp.accession.reconciliation.processed.cul}") String accessionReconciliationFtpCulProcessed,
                                               @Value("${ftp.accession.reconciliation.processed.nypl}") String accessionReconciliationFtpNyplProcessed,
                                               @Value("${ftp.knownHost}") String ftpKnownHost,
                                               @Value("${accession.reconciliation.filePath.pul}") String filePathPul,
                                               @Value("${accession.reconciliation.filePath.cul}") String filePathCul,
                                               @Value("${accession.reconciliation.filePath.nypl}") String filePathNypl,
                                               @Value("${accession.reconciliation.pul.workdir}") String pulWorkDir,
                                               @Value("${accession.reconciliation.cul.workdir}") String culWorkDir,
                                               @Value("${accession.reconciliation.nypl.workdir}") String nyplWorkDir) {

        /**
         * Predicate to idenitify is the input file is gz
         */
        Predicate gzipFile = new Predicate() {
            @Override
            public boolean matches(Exchange exchange) {
                String fileName = (String) exchange.getIn().getHeader(Exchange.FILE_NAME);
                return StringUtils.equalsIgnoreCase("gz", FilenameUtils.getExtension(fileName));
            }
        };

        try {
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + accessionReconciliationPulFtp + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.ACCESSION_RR_FTP_OPTIONS+pulWorkDir)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_FTP_PUL_ROUTE)
                            .noAutoStartup()
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("PUL Accession Reconciliation FTP Route Unzip Complete")
                                    .process(new StartRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_PUL_ROUTE))
                                    .to(ReCAPConstants.DIRECT+ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_PUL_ROUTE)
                                .when(body().isNull())
                                    .process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_FTP_PUL_ROUTE))
                                    .log("No File To Process For PUL Accession Reconciliation")
                                .otherwise()
                                    .process(new StartRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_PUL_ROUTE))
                                    .to(ReCAPConstants.DIRECT+ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_PUL_ROUTE)
                            .endChoice();

                    from(ReCAPConstants.DIRECT+ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_PUL_ROUTE)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_PUL_ROUTE)
                            .noAutoStartup()
                            .log("accession reconciliation pul started")
                            .split(body().tokenize("\n",1000,true))
                            .unmarshal().bindy(BindyType.Csv,BarcodeReconcilitaionReport.class)
                            .bean(applicationContext.getBean(AccessionReconciliationProcessor.class,ReCAPConstants.REQUEST_INITIAL_LOAD_PUL),ReCAPConstants.PROCESS_INPUT)
                            .end()
                            .onCompletion()
                            .process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_PUL_ROUTE));

                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + accessionReconciliationCulFtp + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.ACCESSION_RR_FTP_OPTIONS+culWorkDir)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_FTP_CUL_ROUTE)
                            .noAutoStartup()
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("CUL Accession Reconciliation FTP Route Unzip Complete")
                                    .process(new StartRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_CUL_ROUTE))
                                    .to(ReCAPConstants.DIRECT+ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_CUL_ROUTE)
                                .when(body().isNull())
                                    .process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_FTP_CUL_ROUTE))
                                    .log("No File To Process For CUL Accession Reconciliation")
                                .otherwise()
                                    .process(new StartRouteProcessor("ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_CUL_ROUTE"))
                                    .to(ReCAPConstants.DIRECT+ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_CUL_ROUTE)
                            .endChoice();

                    from(ReCAPConstants.DIRECT+ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_CUL_ROUTE)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_CUL_ROUTE)
                            .noAutoStartup()
                            .log("accession reconciliation cul started")
                            .split(body().tokenize("\n",1000,true))
                            .unmarshal().bindy(BindyType.Csv,BarcodeReconcilitaionReport.class)
                            .bean(applicationContext.getBean(AccessionReconciliationProcessor.class,ReCAPConstants.REQUEST_INITIAL_LOAD_CUL),ReCAPConstants.PROCESS_INPUT)
                            .end()
                            .onCompletion()
                            .process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_CUL_ROUTE));
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + accessionReconciliationNyplFtp + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.ACCESSION_RR_FTP_OPTIONS+nyplWorkDir)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_FTP_NYPL_ROUTE)
                            .noAutoStartup()
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("NYPL Accession Reconciliation FTP Route Unzip Complete")
                                    .process(new StartRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_NYPL_ROUTE))
                                    .to(ReCAPConstants.DIRECT+ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_NYPL_ROUTE)
                                .when(body().isNull())
                                    .process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_FTP_NYPL_ROUTE))
                                    .log("No File To Process For NYPL Accession Reconciliation")
                                .otherwise()
                                    .process(new StartRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_NYPL_ROUTE))
                                    .to(ReCAPConstants.DIRECT+ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_NYPL_ROUTE)
                            .endChoice();

                    from(ReCAPConstants.DIRECT+ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_NYPL_ROUTE)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_NYPL_ROUTE)
                            .noAutoStartup()
                            .log("accession reconciliation nypl started")
                            .split(body().tokenize("\n",1000,true))
                            .unmarshal().bindy(BindyType.Csv,BarcodeReconcilitaionReport.class)
                            .bean(applicationContext.getBean(AccessionReconciliationProcessor.class,ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL),ReCAPConstants.PROCESS_INPUT)
                            .end()
                            .onCompletion()
                            .process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_DIRECT_NYPL_ROUTE));

                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DAILY_RR_FS_FILE+filePathPul+ReCAPConstants.DAILY_RR_FS_OPTIONS)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_FS_PUL_ROUTE)
                            .noAutoStartup()
                            .to(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + accessionReconciliationFtpPulProcessed + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+"&fileName=BarcodeReconciliation_PUL_${date:now:yyyyMMdd_HHmmss}.csv")
                            .onCompletion()
                            .bean(applicationContext.getBean(AccessionReconciliationEmailService.class,ReCAPConstants.PRINCETON),ReCAPConstants.PROCESS_INPUT)
                            .process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_FS_PUL_ROUTE))
                            .log("accession reconciliation pul completed");

                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DAILY_RR_FS_FILE+filePathCul+ReCAPConstants.DAILY_RR_FS_OPTIONS)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_FS_CUL_ROUTE)
                            .noAutoStartup()
                            .to(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + accessionReconciliationFtpCulProcessed + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+"&fileName=BarcodeReconciliation_CUL_${date:now:yyyyMMdd_HHmmss}.csv")
                            .onCompletion()
                            .bean(applicationContext.getBean(AccessionReconciliationEmailService.class,ReCAPConstants.COLUMBIA),ReCAPConstants.PROCESS_INPUT)
                            .process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_FS_CUL_ROUTE))
                            .log("accession reconciliation cul completed");
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DAILY_RR_FS_FILE+filePathNypl+ReCAPConstants.DAILY_RR_FS_OPTIONS)
                            .routeId(ReCAPConstants.ACCESSION_RECONCILATION_FS_NYPL_ROUTE)
                            .noAutoStartup()
                            .to(ReCAPConstants.SFTP+ ftpUserName +  ReCAPConstants.AT + accessionReconciliationFtpNyplProcessed + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+"&fileName=BarcodeReconciliation_NYPL_${date:now:yyyyMMdd_HHmmss}.csv")
                            .onCompletion()
                            .bean(applicationContext.getBean(AccessionReconciliationEmailService.class,ReCAPConstants.NYPL),ReCAPConstants.PROCESS_INPUT)
                            .process(new StopRouteProcessor(ReCAPConstants.ACCESSION_RECONCILATION_FS_NYPL_ROUTE))
                            .log("accession reconciliation nypl completed");
                }
            });

        } catch (Exception e) {
            logger.info(ReCAPConstants.LOG_ERROR+e);
        }

    }
}
