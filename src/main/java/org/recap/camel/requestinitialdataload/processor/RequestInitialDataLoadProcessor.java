package org.recap.camel.requestinitialdataload.processor;

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
    String requestInitialLoadFilePath;

    private String institutionCode;

    public RequestInitialDataLoadProcessor(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    private Set<String> barcodeSet = new HashSet<>();
    private int totalCount = 0;

    public void processInput(Exchange exchange) throws ParseException {
        List<RequestDataLoadCSVRecord> requestDataLoadCSVRecordList = (List<RequestDataLoadCSVRecord>)exchange.getIn().getBody();
        logger.info("count from ftp" + requestDataLoadCSVRecordList.size());
        try {
            Set<String> barcodesNotInScsb = requestDataLoadService.process(requestDataLoadCSVRecordList,barcodeSet);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("ddMMMyyyy");
            Path filePath = Paths.get(requestInitialLoadFilePath+"/"+institutionCode+"/"+ReCAPConstants.REQUEST_INITIAL_FILE_NAME+institutionCode+simpleDateFormat.format(new Date())+".csv");
            if (!filePath.toFile().exists()) {
                Files.createDirectories(filePath.getParent());
                Files.createFile(filePath);
                logger.info("Request Initial Load File Created"+filePath);
            }
            Files.write(filePath,barcodesNotInScsb, StandardOpenOption.APPEND);
        }
        catch (Exception e){
            barcodeSet.clear();
            totalCount=0;
            logger.error(ReCAPConstants.LOG_ERROR+e);
        }
        barcodeSet.clear();
        totalCount = totalCount + requestDataLoadCSVRecordList.size();
        logger.info("Total count from las report ---->" + totalCount);
        totalCount = 0;
    }

    public Set<String> getBarcodeSet() {
        return barcodeSet;
    }

    public void setBarcodeSet(Set<String> barcodeSet) {
        this.barcodeSet = barcodeSet;
    }
}
