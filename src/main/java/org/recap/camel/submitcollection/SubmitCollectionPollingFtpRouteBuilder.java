package org.recap.camel.submitcollection;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.camel.route.StopRouteProcessor;
import org.recap.camel.submitcollection.processor.SubmitCollectionProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;


/**
 * Created by premkb on 19/3/17.
 */
@Component
public class SubmitCollectionPollingFtpRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionPollingFtpRouteBuilder.class);

    @Autowired
    private ProducerTemplate producer;

    @Value("${ftp.userName}")
    private String ftpUserName;

    @Value("${ftp.knownHost}")
    private String ftpKnownHost;

    @Value("${ftp.privateKey}")
    private String ftpPrivateKey;

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

    /**
     * Instantiates a router ftp for Submit collection process for each institution
     *
     * @param camelContext                 the camel context
     * @param applicationContext           the application context
     * @param ftpUserName                  the ftp user name
     * @param pulFtpCGDNotProtectedFolder  the pul ftp cgd not protected folder
     * @param pulFtpCGDProtectedFolder     the pul ftp cgd protected folder
     * @param culFtpCGDNotProtectedFolder  the cul ftp cgd not protected folder
     * @param culFtpCGDProtectedFolder     the cul ftp cgd protected folder
     * @param nyplFtpCGDNotProtectedFolder the nypl ftp cgd not protected folder
     * @param nyplFtpCGDProtectedFolder    the nypl ftp cgd protected folder
     * @param ftpKnownHost                 the ftp known host
     * @param ftpPrivateKey                the ftp private key
     * @param pulWorkDir                   the pul work dir
     * @param culWorkDir                   the cul work dir
     * @param nyplWorkDir                  the nypl work dir
     */
    public SubmitCollectionPollingFtpRouteBuilder(CamelContext camelContext,ApplicationContext applicationContext,
                                                  @Value("${ftp.userName}") String ftpUserName,
                                                  @Value("${ftp.submitcollection.cgdnotprotected.pul}") String pulFtpCGDNotProtectedFolder, @Value("${ftp.submitcollection.cgdprotected.pul}") String pulFtpCGDProtectedFolder,
                                                  @Value("${ftp.submitcollection.cgdnotprotected.cul}") String culFtpCGDNotProtectedFolder, @Value("${ftp.submitcollection.cgdprotected.cul}") String culFtpCGDProtectedFolder,
                                                  @Value("${ftp.submitcollection.cgdnotprotected.nypl}") String nyplFtpCGDNotProtectedFolder, @Value("${ftp.submitcollection.cgdprotected.nypl}") String nyplFtpCGDProtectedFolder,
                                                  @Value("${ftp.knownHost}") String ftpKnownHost, @Value("${ftp.privateKey}") String ftpPrivateKey, @Value("${submit.collection.fileprocess.pul.workdir}") String pulWorkDir,
                                                  @Value("${submit.collection.fileprocess.cul.workdir}") String culWorkDir, @Value("${submit.collection.fileprocess.nypl.workdir}") String nyplWorkDir){
        try{
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    camelContext.getShutdownStrategy().setTimeout(600);
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + pulFtpCGDProtectedFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.SUBMIT_COLLECTION_SFTP_OPTIONS+pulWorkDir)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_PUL_ROUTE)
                            .shutdownRoute(ShutdownRoute.Defer)
                            .noAutoStartup()
                            .log("Submit collection route started")
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("PUL Submit Collection FTP Route Unzip Complete")
                                .end()
                            .log("submit collection for PUL started")
                            .bean(applicationContext.getBean(SubmitCollectionProcessor.class,ReCAPConstants.PRINCETON,true),ReCAPConstants.PROCESS_INPUT)
                            .log("PUL Submit Collection FTP Route Record Processing completed")
                            .end()
                            .process(new StopRouteProcessor(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_PUL_ROUTE));

                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    camelContext.getShutdownStrategy().setTimeout(600);
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + pulFtpCGDNotProtectedFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.SUBMIT_COLLECTION_SFTP_OPTIONS+pulWorkDir)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_PUL_ROUTE)
                            .shutdownRoute(ShutdownRoute.Defer)
                            .noAutoStartup()
                            .log("Submit collection route started")
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("PUL Submit Collection FTP Route Unzip Complete")
                                .end()
                            .log("submit collection for PUL started")
                            .bean(applicationContext.getBean(SubmitCollectionProcessor.class,ReCAPConstants.PRINCETON,false),ReCAPConstants.PROCESS_INPUT)
                            .log("PUL Submit Collection FTP Route Record Processing completed")
                            .end()
                            .process(new StopRouteProcessor(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_PUL_ROUTE));

                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    camelContext.getShutdownStrategy().setTimeout(600);
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + culFtpCGDProtectedFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.SUBMIT_COLLECTION_SFTP_OPTIONS+culWorkDir)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_CUL_ROUTE)
                            .shutdownRoute(ShutdownRoute.Defer)
                            .noAutoStartup()
                            .log("Submit collection route started")
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("CUL Submit Collection FTP Route Unzip Complete")
                                .end()
                            .log("submit collection for CUL started")
                            .bean(applicationContext.getBean(SubmitCollectionProcessor.class,ReCAPConstants.COLUMBIA,true),ReCAPConstants.PROCESS_INPUT)
                            .log("CUL Submit Collection FTP Route Record Processing completed")
                            .end()
                            .process(new StopRouteProcessor(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_CUL_ROUTE));
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    camelContext.getShutdownStrategy().setTimeout(600);
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + culFtpCGDNotProtectedFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.SUBMIT_COLLECTION_SFTP_OPTIONS+culWorkDir)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_CUL_ROUTE)
                            .shutdownRoute(ShutdownRoute.Defer)
                            .noAutoStartup()
                            .log("Submit collection route started")
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("CUL Submit Collection FTP Route Unzip Complete")
                                .end()
                            .log("submit collection for CUL started")
                            .bean(applicationContext.getBean(SubmitCollectionProcessor.class,ReCAPConstants.COLUMBIA,false),ReCAPConstants.PROCESS_INPUT)
                            .log("CUL Submit Collection FTP Route Record Processing completed")
                            .end()
                            .process(new StopRouteProcessor(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_CUL_ROUTE));
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    camelContext.getShutdownStrategy().setTimeout(600);
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + nyplFtpCGDProtectedFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.SUBMIT_COLLECTION_SFTP_OPTIONS+nyplWorkDir)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_NYPL_ROUTE)
                            .shutdownRoute(ShutdownRoute.Defer)
                            .noAutoStartup()
                            .log("Submit collection route started")
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("NLYP Submit Collection FTP Route Unzip Complete")
                                .end()
                            .log("submit collection for NYPL started")
                            .bean(applicationContext.getBean(SubmitCollectionProcessor.class,ReCAPConstants.NYPL,true),ReCAPConstants.PROCESS_INPUT)
                            .log("NYPL Submit Collection FTP Route Record Processing completed")
                            .end()
                            .process(new StopRouteProcessor(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_NYPL_ROUTE));
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    camelContext.getShutdownStrategy().setTimeout(600);
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + nyplFtpCGDNotProtectedFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.SUBMIT_COLLECTION_SFTP_OPTIONS+nyplWorkDir)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_NYPL_ROUTE)
                            .shutdownRoute(ShutdownRoute.Defer)
                            .noAutoStartup()
                            .log("Submit collection route started")
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("NLYP Submit Collection FTP Route Unzip Complete")
                                .end()
                            .log("submit collection for NYPL started")
                            .bean(applicationContext.getBean(SubmitCollectionProcessor.class,ReCAPConstants.NYPL,false),ReCAPConstants.PROCESS_INPUT)
                            .log("NYPL Submit Collection FTP Route Record Processing completed")
                            .end()
                            .process(new StopRouteProcessor(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_NYPL_ROUTE));
                }
            });

        } catch (Exception e){
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }
    }
}
