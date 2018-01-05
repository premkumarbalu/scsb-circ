package org.recap.camel.submitcollection.processor;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.camel.EmailPayLoad;
import org.recap.model.ReportDataRequest;
import org.recap.service.common.SetupDataService;
import org.recap.service.submitcollection.SubmitCollectionBatchService;
import org.recap.service.submitcollection.SubmitCollectionReportGenerator;
import org.recap.service.submitcollection.SubmitCollectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import java.io.File;
import java.util.*;

/**
 * Created by premkb on 19/3/17.
 */
@Service
@Scope("prototype")
public class SubmitCollectionProcessor {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionProcessor.class);

    @Autowired
    private SubmitCollectionService submitCollectionService;

    @Autowired
    private SubmitCollectionBatchService submitCollectionBatchService;

    @Autowired
    private SubmitCollectionReportGenerator submitCollectionReportGenerator;

    @Autowired
    private ProducerTemplate producer;

    @Value("${submit.collection.email.subject}")
    private String submitCollectionEmailSubject;

    @Value("${ftp.submit.collection.pul.report}")
    private String submitCollectionPULReportLocation;

    @Value("${ftp.submit.collection.cul.report}")
    private String submitCollectionCULReportLocation;

    @Value("${ftp.submit.collection.nypl.report}")
    private String submitCollectionNYPLReportLocation;

    @Value("${submit.collection.email.pul.to}")
    private String emailToPUL;

    @Value("${submit.collection.email.cul.to}")
    private String emailToCUL;

    @Value("${submit.collection.email.nypl.to}")
    private String emailToNYPL;

    private String institutionCode;

    private boolean isCGDProtection;
    @Value("${submit.collection.email.pul.cc}")
    private String emailCCForPul;
    @Value("${submit.collection.email.cul.cc}")
    private String emailCCForCul;
    @Value("${submit.collection.email.nypl.cc}")
    private String emailCCForNypl;

    @Autowired
    private SetupDataService setupDataService;

    public SubmitCollectionProcessor(){};

    public SubmitCollectionProcessor(String inputInstitutionCode,boolean isCGDProtection) {
        this.institutionCode = inputInstitutionCode;
        this.isCGDProtection = isCGDProtection;
    }

    /**
     * Process input.
     *
     * @param exchange the exchange
     * @throws Exception the exception
     */
    public void processInput(Exchange exchange) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        logger.info("Submit Collection : Route started and started processing the records from ftp for submitcollection");
        String inputXml = exchange.getIn().getBody(String.class);
        String xmlFileName = exchange.getIn().toString();
        logger.info("Processing xmlFileName----->{}",xmlFileName);
        Set<Integer> processedBibIds = new HashSet<>();
        Set<String> updatedBoundWithDummyRecordOwnInstBibIdSet = new HashSet<>();
        List<Map<String,String>> idMapToRemoveIndexList = new ArrayList<>();
        List<Map<String,String>> bibIdMapToRemoveIndexList = new ArrayList<>();
        List<Integer> reportRecordNumList = new ArrayList<>();
        Integer institutionId = (Integer) setupDataService.getInstitutionCodeIdMap().get(institutionCode);
        try {
            submitCollectionBatchService.process(institutionCode,inputXml,processedBibIds,idMapToRemoveIndexList,bibIdMapToRemoveIndexList,xmlFileName,reportRecordNumList, false, isCGDProtection,updatedBoundWithDummyRecordOwnInstBibIdSet);
            logger.info("Submit Collection : Solr indexing started for {} records", processedBibIds.size());
            logger.info("idMapToRemoveIndex--->"+idMapToRemoveIndexList.size());
            if (processedBibIds.size()>0) {
                submitCollectionBatchService.indexData(processedBibIds);
                logger.info("Submit Collection : Solr indexing completed and remove the incomplete record from solr index for {} records", idMapToRemoveIndexList.size());
            }
            if(!updatedBoundWithDummyRecordOwnInstBibIdSet.isEmpty()){
                logger.info("Updated boudwith dummy record own inst bib id size-->{}",updatedBoundWithDummyRecordOwnInstBibIdSet.size());
                submitCollectionService.indexDataUsingOwningInstBibId(new ArrayList<>(updatedBoundWithDummyRecordOwnInstBibIdSet),institutionId);
            }
            if (idMapToRemoveIndexList.size() > 0 || bibIdMapToRemoveIndexList.size() > 0) {//remove the incomplete record from solr index
                StopWatch stopWatchRemovingDummy = new StopWatch();
                stopWatchRemovingDummy.start();
                logger.info("Calling indexing to remove dummy records");
                new Thread(() -> {
                    try {
                        submitCollectionBatchService.removeBibFromSolrIndex(bibIdMapToRemoveIndexList);
                        submitCollectionBatchService.removeSolrIndex(idMapToRemoveIndexList);
                        logger.info("Removed dummy records from solr");
                    } catch (Exception e) {
                        logger.error(ReCAPConstants.LOG_ERROR,e);
                    }
                }).start();
                stopWatchRemovingDummy.stop();
                logger.info("Time take to call and execute solr call to remove dummy-->{} sec",stopWatchRemovingDummy.getTotalTimeSeconds());
            }
            ReportDataRequest reportRequest = getReportDataRequest(xmlFileName);
            String generatedReportFileName = submitCollectionReportGenerator.generateReport(reportRequest);
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(xmlFileName,generatedReportFileName), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION);
            stopWatch.stop();
            logger.info("Submit Collection : Total time taken for processing through ftp---> {} sec",stopWatch.getTotalTimeSeconds());
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
            exchange.setException(e);
        }
    }

    public void caughtException(Exchange exchange){
        logger.info("inside caught exception..........");
        Exception exception = (Exception) exchange.getProperty(Exchange.EXCEPTION_CAUGHT);
        if(exception!=null){
            String fileName = (String)exchange.getIn().getHeader(Exchange.FILE_NAME);
            String filePath = (String)exchange.getIn().getHeader(Exchange.FILE_PARENT);
            String institutionCode=(String)exchange.getIn().getHeader(ReCAPConstants.INSTITUTION);
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoadForExcepion(institutionCode,fileName,filePath,exception,exception.getMessage()), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION_EXCEPTION);
        }
    }

    private EmailPayLoad getEmailPayLoadForExcepion(String institutionCode,String name,String filePath,Exception exception,String exceptionMessage) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setSubject(ReCAPConstants.SUBJECT_FOR_SUBMIT_COL_EXCEPTION);
        emailPayLoad.setXmlFileName(name);
        if(ReCAPConstants.PRINCETON.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToPUL);
            emailPayLoad.setLocation(getFtpLocation(submitCollectionPULReportLocation));
            emailPayLoad.setInstitution(ReCAPConstants.PRINCETON);
            emailPayLoad.setCc(emailCCForPul);
            emailPayLoad.setLocation(filePath);
            emailPayLoad.setException(exception);
            emailPayLoad.setExceptionMessage(exceptionMessage);
        } else if(ReCAPConstants.COLUMBIA.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToCUL);
            emailPayLoad.setLocation(getFtpLocation(submitCollectionCULReportLocation));
            emailPayLoad.setInstitution(ReCAPConstants.COLUMBIA);
            emailPayLoad.setCc(emailCCForCul);
            emailPayLoad.setLocation(filePath);
            emailPayLoad.setException(exception);
            emailPayLoad.setExceptionMessage(exceptionMessage);
        } else if(ReCAPConstants.NYPL.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToNYPL);
            emailPayLoad.setLocation(getFtpLocation(submitCollectionNYPLReportLocation));
            emailPayLoad.setInstitution(ReCAPConstants.NYPL);
            emailPayLoad.setCc(emailCCForNypl);
            emailPayLoad.setLocation(filePath);
            emailPayLoad.setException(exception);
            emailPayLoad.setExceptionMessage(exceptionMessage);
        }
        return  emailPayLoad;

    }

    private ReportDataRequest getReportDataRequest(String xmlFileName) {
        ReportDataRequest reportRequest = new ReportDataRequest();
        logger.info("filename--->{}-{}", ReCAPConstants.SUBMIT_COLLECTION_REPORT,xmlFileName);
        reportRequest.setFileName(ReCAPConstants.SUBMIT_COLLECTION_REPORT+"-"+xmlFileName);
        reportRequest.setInstitutionCode(institutionCode.toUpperCase());
        reportRequest.setReportType(ReCAPConstants.SUBMIT_COLLECTION_SUMMARY);
        reportRequest.setTransmissionType(ReCAPConstants.FTP);
        return reportRequest;
    }

    private EmailPayLoad getEmailPayLoad(String xmlFileName,String reportFileName) {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setSubject(submitCollectionEmailSubject);
        emailPayLoad.setReportFileName(reportFileName);
        emailPayLoad.setXmlFileName(xmlFileName);
        if(ReCAPConstants.PRINCETON.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToPUL);
            emailPayLoad.setLocation(getFtpLocation(submitCollectionPULReportLocation));
            emailPayLoad.setInstitution(ReCAPConstants.PRINCETON);
            emailPayLoad.setCc(emailCCForPul);
        } else if(ReCAPConstants.COLUMBIA.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToCUL);
            emailPayLoad.setLocation(getFtpLocation(submitCollectionCULReportLocation));
            emailPayLoad.setInstitution(ReCAPConstants.COLUMBIA);
            emailPayLoad.setCc(emailCCForCul);
        } else if(ReCAPConstants.NYPL.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToNYPL);
            emailPayLoad.setLocation(getFtpLocation(submitCollectionNYPLReportLocation));
            emailPayLoad.setInstitution(ReCAPConstants.NYPL);
            emailPayLoad.setCc(emailCCForNypl);
        }
        return  emailPayLoad;
    }

    private String getFtpLocation(String ftpLocation) {
        if (ftpLocation.contains(File.separator)){
            String[] splittedFtpLocation = ftpLocation.split(File.separator,2);
            return splittedFtpLocation[1];
        }else {
            return ftpLocation;
        }

    }
}
