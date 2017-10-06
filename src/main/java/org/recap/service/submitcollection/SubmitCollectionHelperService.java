package org.recap.service.submitcollection;

import org.recap.ReCAPConstants;
import org.recap.converter.MarcToBibEntityConverter;
import org.recap.converter.SCSBToBibEntityConverter;
import org.recap.converter.XmlToBibEntityConverterInterface;
import org.recap.model.BibliographicEntity;
import org.recap.model.HoldingsEntity;
import org.recap.model.InstitutionEntity;
import org.recap.model.ItemEntity;
import org.recap.model.report.SubmitCollectionReportInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by premkb on 19/9/17.
 */
@Service
public class SubmitCollectionHelperService {

    private static final Logger logger = LoggerFactory.getLogger(SubmitCollectionExecutorService.class);

    @Autowired
    private MarcToBibEntityConverter marcToBibEntityConverter;

    @Autowired
    private SCSBToBibEntityConverter scsbToBibEntityConverter;

    @Autowired
    private SubmitCollectionDAOService submitCollectionDAOService;

    @Autowired
    private SubmitCollectionReportHelperService submitCollectionReportHelperService;

    public BibliographicEntity loadData(Object record, String format, Map<String,List<SubmitCollectionReportInfo>> submitCollectionReportInfoMap, List<Map<String,String>> idMapToRemoveIndexList
            , boolean isCGDProtected, InstitutionEntity institutionEntity, Set<String> processedBarcodeSetForDummyRecords){
        BibliographicEntity savedBibliographicEntity = null;
        BibliographicEntity bibliographicEntity = null;
        try {
            Map responseMap = getConverter(format).convert(record,institutionEntity);
            StringBuilder errorMessage = (StringBuilder)responseMap.get("errorMessage");
            bibliographicEntity = responseMap.get("bibliographicEntity") != null ? (BibliographicEntity) responseMap.get("bibliographicEntity"):null;
            if (errorMessage != null && errorMessage.length()==0) {
                setCGDProtectionForItems(bibliographicEntity,isCGDProtected);
                if (bibliographicEntity != null) {
                    savedBibliographicEntity = submitCollectionDAOService.updateBibliographicEntity(bibliographicEntity, submitCollectionReportInfoMap,idMapToRemoveIndexList,processedBarcodeSetForDummyRecords);
                }
            } else {
                logger.error("Error while parsing xml for a barcode in submit collection");
                submitCollectionReportHelperService.setSubmitCollectionFailureReportForUnexpectedException(bibliographicEntity,
                        submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),"Failed record - Item not updated - "+errorMessage.toString(),institutionEntity);
            }
        } catch (Exception e) {
            submitCollectionReportHelperService.setSubmitCollectionFailureReportForUnexpectedException(bibliographicEntity,
                    submitCollectionReportInfoMap.get(ReCAPConstants.SUBMIT_COLLECTION_FAILURE_LIST),"Failed record - Item not updated - "+e.getMessage(),institutionEntity);
            logger.error(ReCAPConstants.LOG_ERROR,e);
        }
        return savedBibliographicEntity;
    }

    private XmlToBibEntityConverterInterface getConverter(String format){
        if(format.equalsIgnoreCase(ReCAPConstants.FORMAT_MARC)){
            return marcToBibEntityConverter;
        } else if(format.equalsIgnoreCase(ReCAPConstants.FORMAT_SCSB)){
            return scsbToBibEntityConverter;
        }
        return null;
    }

    private void setCGDProtectionForItems(BibliographicEntity bibliographicEntity, boolean isCGDProtected){
        if(bibliographicEntity != null && bibliographicEntity.getHoldingsEntities() != null){
            for(HoldingsEntity holdingsEntity : bibliographicEntity.getHoldingsEntities()){
                if (holdingsEntity.getItemEntities() != null){
                    for (ItemEntity itemEntity : holdingsEntity.getItemEntities()){
                        itemEntity.setCgdProtection(isCGDProtected);
                    }
                }
            }
        }
    }
}
