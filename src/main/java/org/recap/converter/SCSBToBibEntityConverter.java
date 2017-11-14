package org.recap.converter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.recap.ReCAPConstants;
import org.recap.model.*;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.Holding;
import org.recap.model.jaxb.Holdings;
import org.recap.model.jaxb.Items;
import org.recap.model.jaxb.marc.CollectionType;
import org.recap.model.marc.BibMarcRecord;
import org.recap.repository.BibliographicDetailsRepository;
import org.recap.repository.CollectionGroupDetailsRepository;
import org.recap.repository.InstitutionDetailsRepository;
import org.recap.repository.ItemStatusDetailsRepository;
import org.recap.util.DBReportUtil;
import org.recap.util.MarcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by premkb on 15/12/16.
 */
@Service
public class SCSBToBibEntityConverter implements XmlToBibEntityConverterInterface {

    private static final Logger logger = LoggerFactory.getLogger(SCSBToBibEntityConverter.class);

    @Autowired
    private DBReportUtil dbReportUtil;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    private MarcUtil marcUtil;

    /**
     * The Bibliographic details repository.
     */
    @Autowired
    BibliographicDetailsRepository bibliographicDetailsRepository;
    private Map itemStatusMap;
    private Map collectionGroupMap;
    private Map institutionEntityMap;

    /**
     *
     * @param scsbRecord
     * @return
     */
    @Override
    public Map convert(Object scsbRecord, InstitutionEntity institutionEntity) {
        Map<String, Object> map = new HashMap<>();
        boolean processBib = false;

        List<HoldingsEntity> holdingsEntities = new ArrayList<>();
        List<ItemEntity> itemEntities = new ArrayList<>();
        List<ReportEntity> reportEntities = new ArrayList<>();

        getDbReportUtil().setInstitutionEntitiesMap(getInstitutionEntityMap());
        getDbReportUtil().setCollectionGroupMap(getCollectionGroupMap());

        BibRecord bibRecord = (BibRecord) scsbRecord;
        String owningInstitutionBibId = bibRecord.getBib().getOwningInstitutionBibId();
        StringBuilder errorMessage = new StringBuilder();
        try {
            BibMarcRecord bibMarcRecord = marcUtil.buildBibMarcRecord(bibRecord);
            Record bibRecordObject = bibMarcRecord.getBibRecord();
            String institutionName = bibRecord.getBib().getOwningInstitutionId();

            Integer owningInstitutionId = institutionEntity.getInstitutionId();
            Date currentDate = new Date();
            Map<String, Object> bibMap = processAndValidateBibliographicEntity(bibRecordObject, owningInstitutionId, institutionName, owningInstitutionBibId,currentDate,errorMessage);
            BibliographicEntity bibliographicEntity = (BibliographicEntity) bibMap.get(ReCAPConstants.BIBLIOGRAPHIC_ENTITY);
            ReportEntity bibReportEntity = (ReportEntity) bibMap.get("bibReportEntity");
            if (bibReportEntity != null) {
                reportEntities.add(bibReportEntity);
            } else {
                processBib = true;
            }

            List<Holdings> holdings = bibRecord.getHoldings();
            for(Holdings holdings1 : holdings){
                for(Holding holding:holdings1.getHolding()){
                    String owninigInstitutionHoldingId = holding.getOwningInstitutionHoldingsId();
                    CollectionType holdingContentCollection = holding.getContent().getCollection();
                    String holdingsContent = holdingContentCollection.serialize(holdingContentCollection);
                    List<Record> holdingsRecords = marcUtil.convertMarcXmlToRecord(holdingsContent);
                    Map<String, Object> holdingsMap = processAndValidateHoldingsEntity(bibliographicEntity, institutionName, owninigInstitutionHoldingId,holdingsRecords.get(0),currentDate,errorMessage);
                    HoldingsEntity holdingsEntity = (HoldingsEntity) holdingsMap.get("holdingsEntity");
                    ReportEntity holdingsReportEntity = (ReportEntity) holdingsMap.get("holdingsReportEntity");
                    boolean processHoldings = false;
                    if (holdingsReportEntity != null) {
                        reportEntities.add(holdingsReportEntity);
                    } else {
                        processHoldings = true;
                        holdingsEntities.add(holdingsEntity);
                    }
                    String holdingsCallNumber = marcUtil.getDataFieldValue(holdingsRecords.get(0), "852", 'h');
                    if(holdingsCallNumber == null){
                        holdingsCallNumber = "";
                    }
                    Character holdingsCallNumberType = marcUtil.getInd1(holdingsRecords.get(0), "852", 'h');
                    List<Items> itemEntityList = holding.getItems();
                    for(Items items:itemEntityList){
                        CollectionType itemContentCollection = items.getContent().getCollection();
                        String itemContent = itemContentCollection.serialize(itemContentCollection);
                        List<Record> itemRecordList = marcUtil.convertMarcXmlToRecord(itemContent);
                        for (Record itemRecord : itemRecordList) {
                            Map<String, Object> itemMap = processAndValidateItemEntity(owningInstitutionId, holdingsCallNumber, holdingsCallNumberType, itemRecord, institutionName, currentDate,errorMessage);
                            ItemEntity itemEntity = (ItemEntity) itemMap.get("itemEntity");
                            ReportEntity itemReportEntity = (ReportEntity) itemMap.get("itemReportEntity");
                            if (itemReportEntity != null) {
                                reportEntities.add(itemReportEntity);
                            } else if (processHoldings) {
                                if (holdingsEntity.getItemEntities() == null) {
                                    holdingsEntity.setItemEntities(new ArrayList<>());
                                }
                                holdingsEntity.getItemEntities().add(itemEntity);
                                itemEntities.add(itemEntity);
                            }
                        }
                    }
                }
                bibliographicEntity.setHoldingsEntities(holdingsEntities);
                bibliographicEntity.setItemEntities(itemEntities);
            }
            if (processBib) {
                map.put(ReCAPConstants.BIBLIOGRAPHIC_ENTITY, bibliographicEntity);
            }
        } catch (Exception e) {
            logger.error(ReCAPConstants.LOG_ERROR,e);
            errorMessage.append(e.getMessage());
        }
        map.put("errorMessage",errorMessage);
        return map;
    }

    /**
     *
     * @param bibRecord
     * @param owningInstitutionId
     * @param institutionName
     * @param owningInstitutionBibId
     * @param currentDate
     * @return
     */
    private Map<String, Object> processAndValidateBibliographicEntity(Record bibRecord, Integer owningInstitutionId, String institutionName,String owningInstitutionBibId,Date currentDate,StringBuilder errorMessage) {
        Map<String, Object> map = new HashMap<>();

        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        if(StringUtils.isEmpty(owningInstitutionBibId)){
            owningInstitutionBibId = marcUtil.getControlFieldValue(bibRecord, "001");
        }
        if (StringUtils.isNotBlank(owningInstitutionBibId)) {
            bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);
        } else {
            errorMessage.append(" Owning Institution Bib Id cannot be null");
        }
        if (owningInstitutionId != null) {
            bibliographicEntity.setOwningInstitutionId(owningInstitutionId);
        } else {
            errorMessage.append(" Owning Institution Id cannot be null");
        }
        bibliographicEntity.setCreatedDate(currentDate);
        bibliographicEntity.setCreatedBy(ReCAPConstants.SUBMIT_COLLECTION);
        bibliographicEntity.setLastUpdatedDate(currentDate);
        bibliographicEntity.setLastUpdatedBy(ReCAPConstants.SUBMIT_COLLECTION);
        bibliographicEntity.setCatalogingStatus(ReCAPConstants.COMPLETE_STATUS);

        String bibXmlStringContent = marcUtil.writeMarcXml(bibRecord);
        if (StringUtils.isNotBlank(bibXmlStringContent)) {
            bibliographicEntity.setContent(bibXmlStringContent.getBytes());
        } else {
            errorMessage.append(" Bib Content cannot be empty");
        }

        boolean subFieldExistsFor245 = marcUtil.isSubFieldExists(bibRecord, "245");
        if (!subFieldExistsFor245) {
            errorMessage.append(" Atleast one subfield should be there for 245 tag");
        }
        Leader leader = bibRecord.getLeader();
        if (leader != null) {
            String leaderValue = bibRecord.getLeader().toString();
            if (!(StringUtils.isNotBlank(leaderValue) && leaderValue.length() == 24)) {
                errorMessage.append(" Leader Field value should be 24 characters");
            }
        }
        map.put(ReCAPConstants.BIBLIOGRAPHIC_ENTITY, bibliographicEntity);
        return map;
    }

    /**
     *
     * @param bibliographicEntity
     * @param institutionName
     * @param holdingsRecord
     * @param currentDate
     * @return
     */
    private Map<String, Object> processAndValidateHoldingsEntity(BibliographicEntity bibliographicEntity, String institutionName, String owningInstitutionHoldingsId,
                                                                 Record holdingsRecord, Date currentDate,StringBuilder errorMessage) {
        Map<String, Object> map = new HashMap<>();
        HoldingsEntity holdingsEntity = new HoldingsEntity();

        String holdingsContent = new MarcUtil().writeMarcXml(holdingsRecord);
        if (StringUtils.isNotBlank(holdingsContent)) {
            holdingsEntity.setContent(holdingsContent.getBytes());
        } else {
            errorMessage.append(" Holdings Content cannot be empty");
        }
        holdingsEntity.setCreatedDate(currentDate);
        holdingsEntity.setCreatedBy(ReCAPConstants.SUBMIT_COLLECTION);
        holdingsEntity.setLastUpdatedDate(currentDate);
        holdingsEntity.setLastUpdatedBy(ReCAPConstants.SUBMIT_COLLECTION);
        Integer owningInstitutionId = bibliographicEntity.getOwningInstitutionId();
        holdingsEntity.setOwningInstitutionId(owningInstitutionId);
        if (StringUtils.isBlank(owningInstitutionHoldingsId)) {
            owningInstitutionHoldingsId = UUID.randomUUID().toString();
        }
        holdingsEntity.setOwningInstitutionHoldingsId(owningInstitutionHoldingsId);
        map.put("holdingsEntity", holdingsEntity);
        return map;
    }

    /**
     *
     * @param owningInstitutionId
     * @param holdingsCallNumber
     * @param holdingsCallNumberType
     * @param itemRecord
     * @param institutionName
     * @param currentDate
     * @return
     */
    private Map<String, Object> processAndValidateItemEntity(Integer owningInstitutionId, String holdingsCallNumber, Character holdingsCallNumberType, Record itemRecord, String institutionName,
                                                             Date currentDate,StringBuilder errorMessage) {
        Map<String, Object> map = new HashMap<>();
        ItemEntity itemEntity = new ItemEntity();
        String itemBarcode = marcUtil.getDataFieldValue(itemRecord, "876", 'p');
        if (StringUtils.isNotBlank(itemBarcode)) {
            itemEntity.setBarcode(itemBarcode);
            map.put("itemBarcode",itemBarcode);
        } else {
            errorMessage.append(" Item Barcode cannot be null");
        }
        String customerCode = marcUtil.getDataFieldValue(itemRecord, "900", 'b');
        if (StringUtils.isNotBlank(customerCode)) {
            itemEntity.setCustomerCode(customerCode);
        }
        itemEntity.setCallNumber(holdingsCallNumber);
        itemEntity.setCallNumberType(holdingsCallNumberType != null ? String.valueOf(holdingsCallNumberType) : "");
        String copyNumber = marcUtil.getDataFieldValue(itemRecord, "876", 't');
        if (StringUtils.isNotBlank(copyNumber) && org.apache.commons.lang3.math.NumberUtils.isNumber(copyNumber)) {
            itemEntity.setCopyNumber(Integer.valueOf(copyNumber));
        }
        if (owningInstitutionId != null) {
            itemEntity.setOwningInstitutionId(owningInstitutionId);
        } else {
            errorMessage.append(" Owning Institution Id cannot be null");
        }
        String collectionGroupCode = marcUtil.getDataFieldValue(itemRecord, "900", 'a');
        if (StringUtils.isNotBlank(collectionGroupCode) && getCollectionGroupMap().containsKey(collectionGroupCode)) {
            itemEntity.setCollectionGroupId((Integer) getCollectionGroupMap().get(collectionGroupCode));
        }

        String useRestrictions = marcUtil.getDataFieldValue(itemRecord, "876", 'h');
        if (useRestrictions != null) {
            itemEntity.setUseRestrictions(useRestrictions);
        }

        itemEntity.setVolumePartYear(marcUtil.getDataFieldValue(itemRecord, "876", '3'));
        String owningInstitutionItemId = marcUtil.getDataFieldValue(itemRecord, "876", 'a');
        if (StringUtils.isNotBlank(owningInstitutionItemId)) {
            itemEntity.setOwningInstitutionItemId(owningInstitutionItemId);
        } else {
            errorMessage.append(" Item Owning Institution Id cannot be null");
        }

        itemEntity.setCreatedDate(currentDate);
        itemEntity.setCreatedBy(ReCAPConstants.SUBMIT_COLLECTION);
        itemEntity.setLastUpdatedDate(currentDate);
        itemEntity.setLastUpdatedBy(ReCAPConstants.SUBMIT_COLLECTION);
        map.put("itemEntity", itemEntity);
        return map;
    }

    /**
     * Gets item status map.
     *
     * @return the item status map
     */
    public Map getItemStatusMap() {
        if (null == itemStatusMap) {
            itemStatusMap = new HashMap();
            try {
                Iterable<ItemStatusEntity> itemStatusEntities = itemStatusDetailsRepository.findAll();
                for (Iterator iterator = itemStatusEntities.iterator(); iterator.hasNext(); ) {
                    ItemStatusEntity itemStatusEntity = (ItemStatusEntity) iterator.next();
                    itemStatusMap.put(itemStatusEntity.getStatusCode(), itemStatusEntity.getItemStatusId());
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR,e);
            }
        }
        return itemStatusMap;
    }

    /**
     * Gets collection group map.
     *
     * @return the collection group map
     */
    public Map getCollectionGroupMap() {
        if (null == collectionGroupMap) {
            collectionGroupMap = new HashMap();
            try {
                Iterable<CollectionGroupEntity> collectionGroupEntities = collectionGroupDetailsRepository.findAll();
                for (Iterator iterator = collectionGroupEntities.iterator(); iterator.hasNext(); ) {
                    CollectionGroupEntity collectionGroupEntity = (CollectionGroupEntity) iterator.next();
                    collectionGroupMap.put(collectionGroupEntity.getCollectionGroupCode(), collectionGroupEntity.getCollectionGroupId());
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR,e);
            }
        }
        return collectionGroupMap;
    }

    /**
     * Gets institution entity map.
     *
     * @return the institution entity map
     */
    public Map getInstitutionEntityMap() {
        if (null == institutionEntityMap) {
            institutionEntityMap = new HashMap();
            try {
                Iterable<InstitutionEntity> institutionEntities = institutionDetailsRepository.findAll();
                for (Iterator iterator = institutionEntities.iterator(); iterator.hasNext(); ) {
                    InstitutionEntity institutionEntity = (InstitutionEntity) iterator.next();
                    institutionEntityMap.put(institutionEntity.getInstitutionCode(), institutionEntity.getInstitutionId());
                }
            } catch (Exception e) {
                logger.error(ReCAPConstants.LOG_ERROR,e);
            }
        }
        return institutionEntityMap;
    }

    /**
     * Gets db report util.
     *
     * @return the db report util
     */
    public DBReportUtil getDbReportUtil() {
        return dbReportUtil;
    }

    /**
     * Sets db report util.
     *
     * @param dbReportUtil the db report util
     */
    public void setDbReportUtil(DBReportUtil dbReportUtil) {
        this.dbReportUtil = dbReportUtil;
    }
}
