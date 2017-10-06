package org.recap.service.submitcollection.callable;

import org.recap.model.InstitutionEntity;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.marc.BibRecords;
import org.recap.model.report.SubmitCollectionReportInfo;
import org.recap.service.submitcollection.SubmitCollectionHelperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Created by premkb on 19/9/17.
 */
@Component
@Scope("prototype")
public class SubmitCollectionCallable implements Callable {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionCallable.class);

    @Autowired
    private SubmitCollectionHelperService submitCollectionHelperService;

    private Map<String, List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap;
    private List<Map<String, String>> idMapToRemoveIndexList;
    private boolean isCGDProtected;
    private InstitutionEntity institutionEntity;
    private String format;
    private BibRecord bibRecord;
    private Object record;
    private Set<String> processedBarcodeSetForDummyRecords;

    public SubmitCollectionCallable(Object record, String format, Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String,String>> idMapToRemoveIndexList
            , boolean isCGDProtected, InstitutionEntity institutionEntity, Set<String> processedBarcodeSetForDummyRecords){
        //this.bibRecord = bibRecord;
        this.record = record;
        this.format = format;
        this.submitCollectionReportInfoMap = submitCollectionReportInfoMap;
        this.idMapToRemoveIndexList = idMapToRemoveIndexList;
        this.isCGDProtected = isCGDProtected;
        this.institutionEntity = institutionEntity;
        this.processedBarcodeSetForDummyRecords = processedBarcodeSetForDummyRecords;
    }

    @Override
    public Object call() throws Exception {
        logger.info("inside callable ....");
        return submitCollectionHelperService.loadData(record, format, submitCollectionReportInfoMap, idMapToRemoveIndexList,isCGDProtected,institutionEntity,processedBarcodeSetForDummyRecords);
    }
}
