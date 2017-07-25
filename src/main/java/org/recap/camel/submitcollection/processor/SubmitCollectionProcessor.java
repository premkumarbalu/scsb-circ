package org.recap.camel.submitcollection.processor;

import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.recap.ReCAPConstants;
import org.recap.camel.EmailPayLoad;
import org.recap.model.ReportDataRequest;
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
        Map<String,String> idMapToRemoveIndex = new HashMap<>();
        List<Integer> reportRecordNumList = new ArrayList<>();
        try {
            submitCollectionService.process(institutionCode,inputXml,processedBibIds,idMapToRemoveIndex,xmlFileName,reportRecordNumList, false, isCGDProtection);
            logger.info("Submit Collection : Solr indexing started for {} records", processedBibIds.size());
            if (processedBibIds.size()>0) {
                submitCollectionService.indexData(processedBibIds);
                logger.info("Submit Collection : Solr indexing completed and remove the incomplete record from solr index for {} records", idMapToRemoveIndex.size());
                if (idMapToRemoveIndex.size()>0) {//remove the incomplete record from solr index
                    submitCollectionService.removeSolrIndex(idMapToRemoveIndex);
                }
            }
            ReportDataRequest reportRequest = getReportDataRequest(xmlFileName);
            String generatedReportFileName = submitCollectionReportGenerator.generateReport(reportRequest);
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(xmlFileName,generatedReportFileName), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION);
            stopWatch.stop();
            logger.info("Submit Collection : Total time taken for processing through ftp---> {}",stopWatch.getTotalTimeSeconds());
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }
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
