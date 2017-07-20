package org.recap.camel.requestinitialdataload.processor;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.recap.ReCAPConstants;
import org.recap.camel.requestinitialdataload.RequestDataLoadCSVRecord;
import org.recap.service.requestdataload.RequestDataLoadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by hemalathas on 3/5/17.
 */
@Service
@Scope("prototype")
public class RequestInitialDataLoadProcessor {

    private static final Logger logger = LoggerFactory.getLogger(RequestInitialDataLoadProcessor.class);

    @Autowired
    private RequestDataLoadService requestDataLoadService;

    @Value("${request.initial.load.filepath}")
    private String requestInitialLoadFilePath;

    private String institutionCode;

    @Autowired
    private CamelContext camelContext;

    /**
     * Instantiates a new request initial data load processor.
     *
     * @param institutionCode the institution code
     */
    public RequestInitialDataLoadProcessor(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    private Set<String> barcodeSet = new HashSet<>();
    private int totalCount = 0;

    /**
     * To load the request initial data in scsb.
     *
     * @param exchange the exchange
     * @throws ParseException the parse exception
     */
    public void processInput(Exchange exchange) throws ParseException {
        List<RequestDataLoadCSVRecord> requestDataLoadCSVRecordList = (List<RequestDataLoadCSVRecord>)exchange.getIn().getBody();
        Integer index = (Integer) exchange.getProperty(ReCAPConstants.CAMEL_SPLIT_INDEX);
        logger.info("count from ftp" + requestDataLoadCSVRecordList.size());
        try {
            Set<String> barcodesNotInScsb = requestDataLoadService.process(requestDataLoadCSVRecordList,barcodeSet);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyyyy");
            Path filePath = Paths.get(requestInitialLoadFilePath+"/"+institutionCode+"/"+ReCAPConstants.REQUEST_INITIAL_FILE_NAME+institutionCode+simpleDateFormat.format(new Date())+".csv");
            if (!filePath.toFile().exists()) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
                logger.info("Request Initial Load File Created--->{}",filePath);
            }
            if(index == 0){
                Set<String> headerSet = new HashSet<>();
                headerSet.add(ReCAPConstants.REQUEST_INITIAL_LOAD_HEADER);
                Files.write(filePath,headerSet, StandardOpenOption.APPEND);
            }

            Files.write(filePath,barcodesNotInScsb, StandardOpenOption.APPEND);
        }
        catch (Exception e){
            barcodeSet.clear();
            totalCount=0;
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }
        barcodeSet.clear();
        totalCount = totalCount + requestDataLoadCSVRecordList.size();
        logger.info("Total count from las report---->{}",totalCount);
        totalCount = 0;

        startFileSystemRoutesForAccessionReconciliation(exchange,index);
    }

    private void startFileSystemRoutesForAccessionReconciliation(Exchange exchange, Integer index) {
        if ((boolean)exchange.getProperty(ReCAPConstants.CAMEL_SPLIT_COMPLETE)){
            logger.info("split last index-->{}",index);
            try {
                if(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL.equalsIgnoreCase(institutionCode)){
                    logger.info(ReCAPConstants.STARTING+ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_FS_ROUTE);
                    camelContext.startRoute(ReCAPConstants.REQUEST_INITIAL_LOAD_PUL_FS_ROUTE);
                }
                if(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL.equalsIgnoreCase(institutionCode)){
                    logger.info(ReCAPConstants.STARTING+ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_FS_ROUTE);
                    camelContext.startRoute(ReCAPConstants.REQUEST_INITIAL_LOAD_CUL_FS_ROUTE);
                }
                if(ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL.equalsIgnoreCase(institutionCode)){
                    logger.info(ReCAPConstants.STARTING+ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_FS_ROUTE);
                    camelContext.startRoute(ReCAPConstants.REQUEST_INITIAL_LOAD_NYPL_FS_ROUTE);
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR+e);
            }
        }
    }

    /**
     * Gets barcode set.
     *
     * @return the barcode set
     */
    public Set<String> getBarcodeSet() {
        return barcodeSet;
    }

    /**
     * Sets barcode set.
     *
     * @param barcodeSet the barcode set
     */
    public void setBarcodeSet(Set<String> barcodeSet) {
        this.barcodeSet = barcodeSet;
    }
}
