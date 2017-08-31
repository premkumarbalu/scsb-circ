package org.recap.camel.submitcollection;

import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.camel.route.StopRouteProcessor;
import org.recap.camel.submitcollection.processor.StartNextRoute;
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
     * Predicate to identify is the input file is gz
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
                                                  @Value("${ftp.userName}") String ftpUserName,@Value("${ftp.ftpHost}") String ftpHost,@Value("${ftp.ftpPort}") String ftpPort,
                                                  @Value("${ftp.submitcollection.cgdnotprotected.pul}") String pulFtpCGDNotProtectedFolder, @Value("${ftp.submitcollection.cgdprotected.pul}") String pulFtpCGDProtectedFolder,
                                                  @Value("${ftp.submitcollection.cgdnotprotected.cul}") String culFtpCGDNotProtectedFolder, @Value("${ftp.submitcollection.cgdprotected.cul}") String culFtpCGDProtectedFolder,
                                                  @Value("${ftp.submitcollection.cgdnotprotected.nypl}") String nyplFtpCGDNotProtectedFolder, @Value("${ftp.submitcollection.cgdprotected.nypl}") String nyplFtpCGDProtectedFolder,
                                                  @Value("${ftp.knownHost}") String ftpKnownHost, @Value("${ftp.privateKey}") String ftpPrivateKey, @Value("${submit.collection.fileprocess.pul.workdir}") String pulWorkDir,
                                                  @Value("${submit.collection.fileprocess.cul.workdir}") String culWorkDir, @Value("${submit.collection.fileprocess.nypl.workdir}") String nyplWorkDir){
        try{
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    /*
                     --> OnCompletion() is called when a file is processed , if there are many files then for each file processed it is called.
                     --> parallelProcessing() is used to create a thread pool to process asynchronously,here it is used to stop the route and start the next route in sequence.
                     */
                    onCompletion().parallelProcessing()
                            .choice()
                                /*
                                --> "CamelCompleteBatch"-exchange property is used to indicate that all the files received are processed,
                                when this condition is satisfied stopping route and starting next route processes are called.
                                */
                                .when(exchangeProperty(ReCAPConstants.CAMEL_BATCH_COMPLETE))
                                    .log("OnCompletion executing for PUL cgd protected")
                                    .process(new StopRouteProcessor(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_PUL_ROUTE))
                                    //To wait for the shutdown of current route and then start the next route.
                                    .delay(10)
                                    .log("ShuttingDownRoute")
                                    .bean(applicationContext.getBean(StartNextRoute.class, ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_PUL_ROUTE), ReCAPConstants.PROCESS);
                    onException(Exception.class)
                            .log("Exception caught during submit collection process - PUL CGD PROTECTED")
                            .handled(true)
                            .setHeader(ReCAPConstants.INSTITUTION,constant(ReCAPConstants.PRINCETON))
                            .to(ReCAPConstants.DIRECT_ROUTE_FOR_EXCEPTION);
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + ftpHost + ":" + ftpPort + pulFtpCGDProtectedFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost + ReCAPConstants.SUBMIT_COLLECTION_SFTP_OPTIONS + pulWorkDir)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_PUL_ROUTE)
                            /*ShutDownStrategy to wait until all files are processed ie until no inflight messages and then shutdown,
                                This is also used when the routes are dependent on each other.
                             */
                            .shutdownRoute(ShutdownRoute.Defer)
                            .noAutoStartup()
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("PUL Submit Collection FTP Route Unzip Complete")
                                    .bean(applicationContext.getBean(SubmitCollectionProcessor.class, ReCAPConstants.PRINCETON, true), ReCAPConstants.PROCESS_INPUT)
                                .when(body().isNull())//This condition is satisfied when there are no files in the directory(parameter-sendEmptyMessageWhenIdle=true)
                                    .log("PUL CGD Protected Directory is empty")
                                    .bean(applicationContext.getBean(StartNextRoute.class, ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_PUL_ROUTE), ReCAPConstants.SEND_EMAIL_FOR_EMPTY_DIRECTORY)
                                .otherwise()
                                    .log("submit collection for PUL cgd protected started")
                                    .bean(applicationContext.getBean(SubmitCollectionProcessor.class, ReCAPConstants.PRINCETON, true), ReCAPConstants.PROCESS_INPUT)
                                    .log("PUL Submit Collection cgd protected FTP Route Record Processing completed")
                            .end();
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    onCompletion().parallelProcessing()
                            .choice()
                                .when(exchangeProperty(ReCAPConstants.CAMEL_BATCH_COMPLETE))
                                    .log("OnCompletion executing for PUL cgd not protected")
                                    .process(new StopRouteProcessor(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_PUL_ROUTE))
                                    .delay(10)
                                    .log("ShuttingDownRoute")
                                    .bean(applicationContext.getBean(StartNextRoute.class, ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_PUL_ROUTE), ReCAPConstants.PROCESS);
                    onException(Exception.class)
                            .log("Exception caught during submit collection process - PUL CGD NOT PROTECTED")
                            .handled(true)
                            .setHeader(ReCAPConstants.INSTITUTION,constant(ReCAPConstants.PRINCETON))
                            .to(ReCAPConstants.DIRECT_ROUTE_FOR_EXCEPTION);
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + ftpHost + ":" + ftpPort + pulFtpCGDNotProtectedFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost + ReCAPConstants.SUBMIT_COLLECTION_SFTP_OPTIONS + pulWorkDir)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_PUL_ROUTE)
                            .shutdownRoute(ShutdownRoute.Defer)
                            .noAutoStartup()
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("PUL Submit Collection FTP Route Unzip Complete")
                                    .bean(applicationContext.getBean(SubmitCollectionProcessor.class, ReCAPConstants.PRINCETON, false), ReCAPConstants.PROCESS_INPUT)
                                .when(body().isNull())
                                    .log("PUL cgd not protected Directory is empty")
                                    .bean(applicationContext.getBean(StartNextRoute.class, ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_PUL_ROUTE), ReCAPConstants.SEND_EMAIL_FOR_EMPTY_DIRECTORY)
                                .otherwise()
                                    .log("submit collection for PUL cgd not protected started")
                                    .bean(applicationContext.getBean(SubmitCollectionProcessor.class, ReCAPConstants.PRINCETON, false), ReCAPConstants.PROCESS_INPUT)
                                    .log("PUL Submit Collection cgd not protected FTP Route Record Processing completed")
                            .end();
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    onCompletion().parallelProcessing()
                            .choice()
                                .when(exchangeProperty(ReCAPConstants.CAMEL_BATCH_COMPLETE))
                                    .log("OnCompletion executing for CUL cgd protected")
                                    .process(new StopRouteProcessor(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_CUL_ROUTE))
                                    .delay(10)
                                    .log("ShuttingDownRoute")
                                    .bean(applicationContext.getBean(StartNextRoute.class, ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_CUL_ROUTE), ReCAPConstants.PROCESS);
                    onException(Exception.class)
                            .log("Exception caught during submit collection process - CUL CGD PROTECTED")
                            .handled(true)
                            .setHeader(ReCAPConstants.INSTITUTION,constant(ReCAPConstants.COLUMBIA))
                            .to(ReCAPConstants.DIRECT_ROUTE_FOR_EXCEPTION);
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + ftpHost + ":" + ftpPort + culFtpCGDProtectedFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost + ReCAPConstants.SUBMIT_COLLECTION_SFTP_OPTIONS + culWorkDir)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_CUL_ROUTE)
                            .shutdownRoute(ShutdownRoute.Defer)
                            .noAutoStartup()
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("CUL Submit Collection FTP Route Unzip Complete")
                                    .bean(applicationContext.getBean(SubmitCollectionProcessor.class, ReCAPConstants.COLUMBIA, true), ReCAPConstants.PROCESS_INPUT)
                                .when(body().isNull())
                                    .log("CUL cgd protected Directory is empty")
                                    .bean(applicationContext.getBean(StartNextRoute.class, ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_CUL_ROUTE), ReCAPConstants.SEND_EMAIL_FOR_EMPTY_DIRECTORY)
                                .otherwise()
                                    .log("submit collection for CUL cgd protected started")
                                    .bean(applicationContext.getBean(SubmitCollectionProcessor.class, ReCAPConstants.COLUMBIA, true), ReCAPConstants.PROCESS_INPUT)
                                    .log("CUL cgd protected Submit Collection FTP Route Record Processing completed")
                            .end();
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    onCompletion().parallelProcessing()
                            .choice()
                                .when(exchangeProperty(ReCAPConstants.CAMEL_BATCH_COMPLETE))
                                    .log("OnCompletion executing for CUL cgd not protected")
                                    .process(new StopRouteProcessor(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_CUL_ROUTE))
                                    .delay(10)
                                    .log("ShuttingDownRoute")
                                    .bean(applicationContext.getBean(StartNextRoute.class, ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_CUL_ROUTE), ReCAPConstants.PROCESS);
                    onException(Exception.class)
                            .log("Exception caught during submit collection process - CUL CGD NOT PROTECTED")
                            .handled(true)
                            .setHeader(ReCAPConstants.INSTITUTION,constant(ReCAPConstants.COLUMBIA))
                            .to(ReCAPConstants.DIRECT_ROUTE_FOR_EXCEPTION);
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + ftpHost + ":" + ftpPort + culFtpCGDNotProtectedFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost + ReCAPConstants.SUBMIT_COLLECTION_SFTP_OPTIONS + culWorkDir)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_CUL_ROUTE)
                            .shutdownRoute(ShutdownRoute.Defer)
                            .noAutoStartup()
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("CUL Submit Collection FTP Route Unzip Complete")
                                    .bean(applicationContext.getBean(SubmitCollectionProcessor.class, ReCAPConstants.COLUMBIA, false), ReCAPConstants.PROCESS_INPUT)
                                .when(body().isNull())
                                    .log("CUL CGD not protected Directory is empty")
                                    .bean(applicationContext.getBean(StartNextRoute.class, ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_CUL_ROUTE), ReCAPConstants.SEND_EMAIL_FOR_EMPTY_DIRECTORY)
                                .otherwise()
                                    .log("submit collection for CUL cgd not protected started")
                                    .bean(applicationContext.getBean(SubmitCollectionProcessor.class, ReCAPConstants.COLUMBIA, false), ReCAPConstants.PROCESS_INPUT)
                                    .log("CUL cgd not protected Submit Collection FTP Route Record Processing completed")
                            .end();
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    onCompletion().parallelProcessing()
                            .choice()
                                .when(exchangeProperty(ReCAPConstants.CAMEL_BATCH_COMPLETE))
                                    .log("OnCompletion executing for NYPL cgd protected")
                                    .process(new StopRouteProcessor(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_NYPL_ROUTE))
                                    .delay(10)
                                    .log("ShuttingDownRoute")
                                    .bean(applicationContext.getBean(StartNextRoute.class, ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_NYPL_ROUTE), ReCAPConstants.PROCESS);
                    onException(Exception.class)
                            .log("Exception caught during submit collection process - NYPL CGD PROTECTED")
                            .handled(true)
                            .setHeader(ReCAPConstants.INSTITUTION,constant(ReCAPConstants.NYPL))
                            .to(ReCAPConstants.DIRECT_ROUTE_FOR_EXCEPTION);
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + ftpHost + ":" + ftpPort + nyplFtpCGDProtectedFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost + ReCAPConstants.SUBMIT_COLLECTION_SFTP_OPTIONS + nyplWorkDir)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_NYPL_ROUTE)
                            .shutdownRoute(ShutdownRoute.Defer)
                            .noAutoStartup()
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("NYPL cgd protected Submit Collection FTP Route Unzip Complete")
                                    .bean(applicationContext.getBean(SubmitCollectionProcessor.class, ReCAPConstants.NYPL, true), ReCAPConstants.PROCESS_INPUT)
                                .when(body().isNull())
                                    .log("NYPL cgd protected Directory is empty")
                                    .bean(applicationContext.getBean(StartNextRoute.class, ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_PROTECTED_NYPL_ROUTE), ReCAPConstants.SEND_EMAIL_FOR_EMPTY_DIRECTORY)
                                .otherwise()
                                    .log("submit collection for NYPL cgd protected started")
                                    .bean(applicationContext.getBean(SubmitCollectionProcessor.class, ReCAPConstants.NYPL, true), ReCAPConstants.PROCESS_INPUT)
                                    .log("NYPL cgd protected Submit Collection FTP Route Record Processing completed")
                            .end();

                }
            });

            //This route is used to send message to queue which is used in controller to identify the completion of submit collection process
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SUBMIT_COLLECTION_COMPLETION_QUEUE_FROM)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_COMPLETED_ROUTE)
                            .log("Completed Submit Collection Process")
                            .process(new Processor() {
                                @Override
                                public void process(Exchange exchange) throws Exception {
                                    exchange.getIn().setBody("Submit collection process completed sucessfully in sequential order");
                                }
                            })
                            .to(ReCAPConstants.SUBMIT_COLLECTION_COMPLETION_QUEUE_TO)
                            .end();
                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    onCompletion().parallelProcessing()
                            .choice()
                                .when(exchangeProperty(ReCAPConstants.CAMEL_BATCH_COMPLETE))
                                    .log("OnCompletion executing for NYPL cgd not protected")
                                    .process(new StopRouteProcessor(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_NYPL_ROUTE))
                                    .delay(10)
                                    .log("ShuttingDownRoute");
                    onException(Exception.class)
                            .log("Exception caught during submit collection process - NYPL CGD NOT PROTECTED")
                            .handled(true)
                            .setHeader(ReCAPConstants.INSTITUTION,constant(ReCAPConstants.NYPL))
                            .to(ReCAPConstants.DIRECT_ROUTE_FOR_EXCEPTION);
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + ftpHost + ":" + ftpPort + nyplFtpCGDNotProtectedFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost + ReCAPConstants.SUBMIT_COLLECTION_SFTP_OPTIONS + nyplWorkDir)
                            .routeId(ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_NYPL_ROUTE)
                            .shutdownRoute(ShutdownRoute.Defer)
                            .noAutoStartup()
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("NYPL Submit Collection FTP Route Unzip Complete")
                                    .bean(applicationContext.getBean(SubmitCollectionProcessor.class, ReCAPConstants.NYPL, false), ReCAPConstants.PROCESS_INPUT)
                                .when(body().isNull())
                                    .log("NYPL cgd not protected Directory is empty")
                                    .bean(applicationContext.getBean(StartNextRoute.class, ReCAPConstants.SUBMIT_COLLECTION_FTP_CGD_NOT_PROTECTED_NYPL_ROUTE), ReCAPConstants.SEND_EMAIL_FOR_EMPTY_DIRECTORY)
                                .otherwise()
                                    .log("Submit collection for NYPL cgd not protected started")
                                    .bean(applicationContext.getBean(SubmitCollectionProcessor.class, ReCAPConstants.NYPL, false), ReCAPConstants.PROCESS_INPUT)
                                    .log("NYPL cgd not protected Submit Collection FTP Route Record Processing completed")
                            .end()
                            .to(ReCAPConstants.SUBMIT_COLLECTION_COMPLETION_QUEUE_FROM);

                }
            });

            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.DIRECT_ROUTE_FOR_EXCEPTION)
                            .log("Calling direct route for exception")
                            .bean(applicationContext.getBean(SubmitCollectionProcessor.class), ReCAPConstants.SUBMIT_COLLECTION__CAUGHT_EXCEPTION_METHOD);

                }
            });


        } catch (Exception e){
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }
    }
}
