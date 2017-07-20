package org.recap.camel.accessionreconciliation;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by akulak on 16/5/17.
 */
@Service
@Scope("prototype")
public class AccessionReconciliationProcessor {

    private static final Logger logger = LoggerFactory.getLogger(AccessionReconciliationProcessor.class);

    @Autowired
    CamelContext camelContext;

    @Value("${scsb.solr.client.url}")
    private String solrSolrClientUrl;

    @Value("${accession.reconciliation.filePath}")
    private String accessionFilePath;

    private String institutionCode;

    /**
     * Instantiates a new Accession reconcilation processor.
     *
     * @param institutionCode the institution code
     */
    public AccessionReconciliationProcessor(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    /**
     * Process input for accession reconcilation report.
     *
     * @param exchange the exchange
     */
    public void processInput(Exchange exchange) {
        String barcode = exchange.getIn().getBody(String.class);
        Integer index = (Integer) exchange.getProperty(ReCAPConstants.CAMEL_SPLIT_INDEX);
        String[] split = barcode.split("\n");
        Set<String> barcodes = new HashSet<>();
        for(String s :split){
            String[] split1 = s.split(",");
            barcodes.add(split1[0]);
        }
        String joinedBarcodes = StringUtils.join(barcodes, ",");
        HttpEntity httpEntity = new HttpEntity(joinedBarcodes);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Set> responseEntity = restTemplate.exchange(solrSolrClientUrl+ReCAPConstants.ACCESSION_RECONCILATION_SOLR_CLIENT_URL, HttpMethod.POST, httpEntity,Set.class);
        Set<String> body = responseEntity.getBody();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ReCAPConstants.DAILY_RR_FILE_DATE_FORMAT);
        try {
            Path filePath = Paths.get(accessionFilePath+"/"+institutionCode+"/"+ReCAPConstants.ACCESSION_RECONCILATION_FILE_NAME+institutionCode+simpleDateFormat.format(new Date())+".csv");
            if (!filePath.toFile().exists()) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
                logger.info("Accession Reconciliation File Created {} ",filePath);
            }
            if(index == 0){
                Set<String> headerSet = new HashSet<>();
                headerSet.add(ReCAPConstants.REQUEST_INITIAL_LOAD_HEADER);
                Files.write(filePath,headerSet, StandardOpenOption.APPEND);
            }
            Files.write(filePath,body,StandardOpenOption.APPEND);
        }
        catch (Exception e){
            logger.error(ReCAPConstants.LOG_ERROR+e);
        }

        startFileSystemRoutesForAccessionReconciliation(exchange,index);
    }

    private void startFileSystemRoutesForAccessionReconciliation(Exchange exchange,Integer index) {
        if ((boolean)exchange.getProperty(ReCAPConstants.CAMEL_SPLIT_COMPLETE)){
            logger.info("split last index-->{}",index);
            try {
                if(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL.equalsIgnoreCase(institutionCode)){
                    logger.info(ReCAPConstants.STARTING,ReCAPConstants.ACCESSION_RECONCILATION_FS_PUL_ROUTE);
                    camelContext.startRoute(ReCAPConstants.ACCESSION_RECONCILATION_FS_PUL_ROUTE);
                }
                if(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL.equalsIgnoreCase(institutionCode)){
                    logger.info(ReCAPConstants.STARTING,ReCAPConstants.ACCESSION_RECONCILATION_FS_CUL_ROUTE);
                    camelContext.startRoute(ReCAPConstants.ACCESSION_RECONCILATION_FS_CUL_ROUTE);
                }
                if(ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL.equalsIgnoreCase(institutionCode)){
                    logger.info(ReCAPConstants.STARTING,ReCAPConstants.ACCESSION_RECONCILATION_FS_NYPL_ROUTE);
                    camelContext.startRoute(ReCAPConstants.ACCESSION_RECONCILATION_FS_NYPL_ROUTE);
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR+e);
            }
        }
    }

}
