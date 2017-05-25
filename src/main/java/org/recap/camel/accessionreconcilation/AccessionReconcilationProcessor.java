package org.recap.camel.accessionreconcilation;

import org.apache.camel.Exchange;
import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.*;

/**
 * Created by akulak on 16/5/17.
 */
@Service
@Scope("prototype")
public class AccessionReconcilationProcessor {

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.solr.client.url}")
    String solrSolrClientUrl;

    @Value("${accession.reconcilation.filePath}")
    String accessionFilePath;

    private String institutionCode;

    public AccessionReconcilationProcessor(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    private static final Logger logger = LoggerFactory.getLogger(AccessionReconcilationProcessor.class);

    public void processInput(Exchange exchange) {
        String barcode = exchange.getIn().getBody(String.class);
        String[] split = barcode.split("\n");
        Set<String> barcodes = new HashSet<>();
        for(String s :split){
            String[] split1 = s.split(",");
            barcodes.add(split1[0]);
        }
        String joinedBarcodes = StringUtils.join(barcodes, ",");
        HttpEntity httpEntity = new HttpEntity(joinedBarcodes);
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Set> responseEntity = restTemplate.exchange(serverProtocol+solrSolrClientUrl+ReCAPConstants.ACCESSION_RECONCILATION_SOLR_CLIENT_URL, HttpMethod.POST, httpEntity,Set.class);
        Set<String> body = responseEntity.getBody();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(ReCAPConstants.DAILY_RR_FILE_DATE_FORMAT);
        try {
            Path filePath = Paths.get(accessionFilePath+"/"+institutionCode+"/"+ReCAPConstants.ACCESSION_RECONCILATION_FILE_NAME+institutionCode+simpleDateFormat.format(new Date())+".csv");
            if (!filePath.toFile().exists()) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
                logger.info("Accession Reconcilation File Created"+filePath);
            }
            Files.write(filePath,body,StandardOpenOption.APPEND);
        }
        catch (Exception e){
            logger.error(ReCAPConstants.LOG_ERROR+e);
        }
    }

}
