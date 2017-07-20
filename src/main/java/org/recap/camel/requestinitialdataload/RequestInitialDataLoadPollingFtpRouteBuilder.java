package org.recap.camel.requestinitialdataload;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Predicate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.BindyType;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.recap.camel.requestinitialdataload.processor.RequestInitialDataLoadProcessor;
import org.recap.camel.route.StartRouteProcessor;
import org.recap.camel.route.StopRouteProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by hemalathas on 3/5/17.
 */
@Component
public class RequestInitialDataLoadPollingFtpRouteBuilder {

    private static final Logger logger = LoggerFactory.getLogger(RequestInitialDataLoadPollingFtpRouteBuilder.class);

    /**
     * Instantiates a new Request initial data load polling ftp route builder.
     *
     * @param camelContext               the camel context
     * @param applicationContext         the application context
     * @param ftpUserName                the ftp user name
     * @param requestAccessionPulFolder  the request accession pul folder
     * @param requestAccessionCulFolder  the request accession cul folder
     * @param requestAccessionNyplFolder the request accession nypl folder
     * @param ftpKnownHost               the ftp known host
     * @param ftpPrivateKey              the ftp private key
     * @param requestInvalidPulFile      the request invalid pul file
     * @param requestInvalidCulFile      the request invalid cul file
     * @param requestInvalidNyplFile     the request invalid nypl file
     * @param requestLoadPulFilePath     the request load pul file path
     * @param requestLoadCulFilePath     the request load cul file path
     * @param requestLoadNyplFilePath    the request load nypl file path
     * @param pulWorkDirectory           the pul work directory
     * @param culWorkDirectory           the cul work directory
     * @param nyplWorkDirectory          the nypl work directory
     */
    public RequestInitialDataLoadPollingFtpRouteBuilder(CamelContext camelContext, ApplicationContext applicationContext,
                                                        @Value("${ftp.userName}") String ftpUserName, @Value("${request.initial.accession.pul}") String requestAccessionPulFolder,
                                                        @Value("${request.initial.accession.cul}") String requestAccessionCulFolder, @Value("${request.initial.accession.nypl}") String requestAccessionNyplFolder,
                                                        @Value("${ftp.knownHost}") String ftpKnownHost, @Value("${ftp.privateKey}") String ftpPrivateKey,
                                                        @Value("${request.initial.accession.pul.error.file}") String requestInvalidPulFile,
                                                        @Value("${request.initial.accession.cul.error.file}") String requestInvalidCulFile,
                                                        @Value("${request.initial.accession.nypl.error.file}") String requestInvalidNyplFile,
                                                        @Value("${request.initial.load.pul.filepath}") String requestLoadPulFilePath,
                                                        @Value("${request.initial.load.cul.filepath}") String requestLoadCulFilePath,
                                                        @Value("${request.initial.load.nypl.filepath}") String requestLoadNyplFilePath,
                                                        @Value("${request.initial.load.pul.workdir}") String pulWorkDirectory,
                                                        @Value("${request.initial.load.cul.workdir}") String culWorkDirectory,
                                                        @Value("${request.initial.load.nypl.workdir}") String nyplWorkDirectory){

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

        try{
            camelContext.addRoutes(new RouteBuilder(){
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + requestAccessionPulFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.ACCESSION_RR_FTP_OPTIONS+pulWorkDirectory)
                            .routeId(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_FTP_ROUTE)
                            .noAutoStartup()
                            .choice()
                            .when(gzipFile)
                                .unmarshal()
                                .gzip()
                                .log("PUL Request Initial load FTP Route Unzip Complete")
                                .process(new StartRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_DIRECT_ROUTE))
                                .to(ReCAPConstants.DIRECT+ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_DIRECT_ROUTE)
                            .when(body().isNull())
                                .process(new StopRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_FTP_ROUTE))
                                .log("No File To Process PUL Request Initial load")
                            .otherwise()
                                .process(new StartRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_DIRECT_ROUTE))
                                .to(ReCAPConstants.DIRECT+ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_DIRECT_ROUTE)
                            .endChoice();

                    from(ReCAPConstants.DIRECT+ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_DIRECT_ROUTE)
                            .routeId(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_DIRECT_ROUTE)
                            .noAutoStartup()
                            .log("Request data load started for PUL")
                            .split(body().tokenize("\n",1000,true))
                            .unmarshal().bindy(BindyType.Csv, RequestDataLoadCSVRecord.class)
                            .bean(applicationContext.getBean(RequestInitialDataLoadProcessor.class,ReCAPConstants.REQUEST_INITIAL_LOAD_PUL), ReCAPConstants.PROCESS_INPUT)
                            .end()
                            .onCompletion()
                            .process(new StopRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_DIRECT_ROUTE));

                }
            });
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.REQUEST_INITIAL_LOAD_FS_FILE+requestLoadPulFilePath+"?delete=true")
                            .routeId(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_FS_ROUTE)
                            .noAutoStartup()
                            .to(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + requestInvalidPulFile +  ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey +  ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+"&fileName=InitialRequestLoadBarcodeFail_PUL_${date:now:yyyyMMdd_HHmmss}.csv")
                            .onCompletion()
                            .bean(applicationContext.getBean(RequestDataLoadEmailService.class,ReCAPConstants.PRINCETON),ReCAPConstants.PROCESS_INPUT)
                            .process(new StopRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_FS_ROUTE))
                            .log("Request data load completed for PUL");
                }

            });

            camelContext.addRoutes(new RouteBuilder(){
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + requestAccessionCulFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.ACCESSION_RR_FTP_OPTIONS+culWorkDirectory)
                            .routeId(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_FTP_ROUTE)
                            .noAutoStartup()
                            .choice()
                                .when(gzipFile)
                                    .unmarshal()
                                    .gzip()
                                    .log("CUL Request Initial load FTP Route Unzip Complete")
                                    .process(new StartRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_DIRECT_ROUTE))
                                    .to(ReCAPConstants.DIRECT+ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_DIRECT_ROUTE)
                                .when(body().isNull())
                                    .process(new StopRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_FTP_ROUTE))
                                    .log("No File To Process CUL Request Initial load")
                                .otherwise()
                                    .process(new StartRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_DIRECT_ROUTE))
                                    .to(ReCAPConstants.DIRECT+ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_DIRECT_ROUTE)
                            .endChoice();

                    from(ReCAPConstants.DIRECT+ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_DIRECT_ROUTE)
                            .routeId(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_DIRECT_ROUTE)
                            .noAutoStartup()
                            .log("Request data load started for CUL")
                            .split(body().tokenize("\n",1000,true))
                            .unmarshal().bindy(BindyType.Csv, RequestDataLoadCSVRecord.class)
                            .bean(applicationContext.getBean(RequestInitialDataLoadProcessor.class,ReCAPConstants.REQUEST_INITIAL_LOAD_CUL), ReCAPConstants.PROCESS_INPUT)
                            .end()
                            .onCompletion()
                            .process(new StopRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_DIRECT_ROUTE));
                }
            });
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.REQUEST_INITIAL_LOAD_FS_FILE+requestLoadCulFilePath+"?delete=true")
                            .routeId(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_FS_ROUTE)
                            .noAutoStartup()
                            .to(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + requestInvalidCulFile +  ReCAPConstants.PRIVATE_KEY_FILE  + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ "&fileName=InitialRequestLoadBarcodeFail_CUL_${date:now:yyyyMMdd_HHmmss}.csv")
                            .onCompletion()
                            .bean(applicationContext.getBean(RequestDataLoadEmailService.class,ReCAPConstants.COLUMBIA),ReCAPConstants.PROCESS_INPUT)
                            .process(new StopRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_FS_ROUTE))
                            .log("Request data load completed for CUL");
                }

            });

            camelContext.addRoutes(new RouteBuilder(){
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + requestAccessionNyplFolder + ReCAPConstants.PRIVATE_KEY_FILE + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+ReCAPConstants.ACCESSION_RR_FTP_OPTIONS+nyplWorkDirectory)
                            .routeId(ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_FTP_ROUTE)
                            .noAutoStartup()
                            .choice()
                                .when(gzipFile)
                                .unmarshal()
                                .gzip()
                                .log("Nypl Request Initial load FTP Route Unzip Complete")
                                .process(new StartRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_DIRECT_ROUTE))
                                .to(ReCAPConstants.DIRECT+ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_DIRECT_ROUTE)
                            .when(body().isNull())
                                .process(new StopRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_FTP_ROUTE))
                                .log("No File To Process Nypl Request Initial load")
                            .otherwise()
                                .process(new StartRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_DIRECT_ROUTE))
                                .to(ReCAPConstants.DIRECT+ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_DIRECT_ROUTE)
                            .endChoice();

                    from(ReCAPConstants.DIRECT+ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_DIRECT_ROUTE)
                            .routeId(ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_DIRECT_ROUTE)
                            .noAutoStartup()
                            .log("Request data load started for NYPL")
                            .split(body().tokenize("\n",1000,true))
                            .unmarshal().bindy(BindyType.Csv, RequestDataLoadCSVRecord.class)
                            .bean(applicationContext.getBean(RequestInitialDataLoadProcessor.class,ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL), ReCAPConstants.PROCESS_INPUT)
                            .end()
                            .onCompletion()
                            .process(new StopRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_DIRECT_ROUTE));

                }
            });
            camelContext.addRoutes(new RouteBuilder() {
                @Override
                public void configure() throws Exception {
                    from(ReCAPConstants.REQUEST_INITIAL_LOAD_FS_FILE+requestLoadNyplFilePath+"?delete=true")
                            .routeId(ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_FS_ROUTE)
                            .noAutoStartup()
                            .to(ReCAPConstants.SFTP + ftpUserName + ReCAPConstants.AT + requestInvalidNyplFile +  ReCAPConstants.PRIVATE_KEY_FILE  + ftpPrivateKey + ReCAPConstants.KNOWN_HOST_FILE + ftpKnownHost+"&fileName=InitialRequestLoadBarcodeFail_NYPL_${date:now:yyyyMMdd_HHmmss}.csv")
                            .onCompletion()
                            .bean(applicationContext.getBean(RequestDataLoadEmailService.class,ReCAPConstants.NYPL),ReCAPConstants.PROCESS_INPUT)
                            .process(new StopRouteProcessor(ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_FS_ROUTE))
                            .log("Request data load completed for NYPL");
                }

            });
        }catch (Exception e){
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }

    }
}
