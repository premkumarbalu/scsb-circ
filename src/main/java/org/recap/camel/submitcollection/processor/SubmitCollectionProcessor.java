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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public SubmitCollectionProcessor(String inputInstitutionCode) {
        this.institutionCode = inputInstitutionCode;
    }

    /**
     * Process input.
     *
     * @param exchange the exchange
     * @throws Exception the exception
     */
    public void processInput(Exchange exchange) {
        String inputXml = exchange.getIn().getBody(String.class);
        String xmlFileName = exchange.getIn().toString();
        List<Integer> processedBibIdList = new ArrayList<>();
        Map<String,String> idMapToRemoveIndex = new HashMap<>();
        String response = "";
        try {
            response = submitCollectionService.process(inputXml,processedBibIdList,idMapToRemoveIndex,xmlFileName);
            if (response.contains(ReCAPConstants.SUMBIT_COLLECTION_UPDATE_MESSAGE)) {
                submitCollectionService.indexData(processedBibIdList);
                if (idMapToRemoveIndex.size()>0) {//remove the incomplete record from solr index
                    submitCollectionService.removeSolrIndex(idMapToRemoveIndex);
                }
            }
            ReportDataRequest reportRequest = getReportDataRequest(xmlFileName);
            submitCollectionReportGenerator.generateReport(reportRequest);
            producer.sendBodyAndHeader(ReCAPConstants.EMAIL_Q, getEmailPayLoad(), ReCAPConstants.EMAIL_BODY_FOR,ReCAPConstants.SUBMIT_COLLECTION);
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

    private EmailPayLoad getEmailPayLoad() {
        EmailPayLoad emailPayLoad = new EmailPayLoad();
        emailPayLoad.setSubject(submitCollectionEmailSubject);
        if(ReCAPConstants.PRINCETON.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToPUL);
            emailPayLoad.setLocation(submitCollectionPULReportLocation);
        } else if(ReCAPConstants.COLUMBIA.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToCUL);
            emailPayLoad.setLocation(submitCollectionCULReportLocation);
        } else if(ReCAPConstants.NYPL.equalsIgnoreCase(institutionCode)){
            emailPayLoad.setTo(emailToNYPL);
            emailPayLoad.setLocation(submitCollectionNYPLReportLocation);
        }
        return  emailPayLoad;
    }
}
