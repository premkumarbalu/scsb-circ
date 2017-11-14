package org.recap.converter;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.Leader;
import org.marc4j.marc.Record;
import org.recap.ReCAPConstants;
import org.recap.model.*;
import org.recap.model.marc.BibMarcRecord;
import org.recap.model.marc.HoldingsMarcRecord;
import org.recap.model.marc.ItemMarcRecord;
import org.recap.repository.*;
import org.recap.util.DBReportUtil;
import org.recap.util.MarcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by chenchulakshmig on 17/10/16.
 */
@Service
public class MarcToBibEntityConverter implements XmlToBibEntityConverterInterface{

    private static final Logger logger = LoggerFactory.getLogger(MarcToBibEntityConverter.class);

    @Autowired
    private MarcUtil marcUtil;

    @Autowired
    private DBReportUtil dbReportUtil;

    @Autowired
    private CollectionGroupDetailsRepository collectionGroupDetailsRepository;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    private CustomerCodeDetailsRepository customerCodeDetailsRepository;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    private Map itemStatusMap;
    private Map collectionGroupMap;
    private Map institutionEntityMap;
    
    @Override
    public Map convert(Object marcRecord, InstitutionEntity institutionEntity) {
        Map<String, Object> map = new HashMap<>();
        boolean processBib = false;

        Record record = (Record) marcRecord;
        List<HoldingsEntity> holdingsEntities = new ArrayList<>();
        List<ItemEntity> itemEntities = new ArrayList<>();
        List<ReportEntity> reportEntities = new ArrayList<>();

        getDbReportUtil().setInstitutionEntitiesMap(getInstitutionEntityMap());
        getDbReportUtil().setCollectionGroupMap(getCollectionGroupMap());

        StringBuilder errorMessage = new StringBuilder();

        try {
            BibMarcRecord bibMarcRecord = marcUtil.buildBibMarcRecord(record);
            Record bibRecord = bibMarcRecord.getBibRecord();
            Integer owningInstitutionId;
            if(institutionEntity == null){
                owningInstitutionId = getOwningInstitutionId(bibMarcRecord);
                institutionEntity = institutionDetailsRepository.findByInstitutionId(owningInstitutionId);
            }
            Date currentDate = new Date();
            Map<String, Object> bibMap = processAndValidateBibliographicEntity(bibRecord, institutionEntity,currentDate,errorMessage);
            BibliographicEntity bibliographicEntity = (BibliographicEntity) bibMap.get(ReCAPConstants.BIBLIOGRAPHIC_ENTITY);
            ReportEntity bibReportEntity = (ReportEntity) bibMap.get("bibReportEntity");
            if (bibReportEntity != null) {
                reportEntities.add(bibReportEntity);
            } else {
                processBib = true;
            }

            List<HoldingsMarcRecord> holdingsMarcRecords = bibMarcRecord.getHoldingsMarcRecords();
            if (CollectionUtils.isNotEmpty(holdingsMarcRecords)) {
                for (HoldingsMarcRecord holdingsMarcRecord : holdingsMarcRecords) {
                    boolean processHoldings = false;
                    Record holdingsRecord = holdingsMarcRecord.getHoldingsRecord();
                    Map<String, Object> holdingsMap = processAndValidateHoldingsEntity(bibliographicEntity, holdingsRecord, currentDate,errorMessage);
                    HoldingsEntity holdingsEntity = (HoldingsEntity) holdingsMap.get("holdingsEntity");
                    ReportEntity holdingsReportEntity = (ReportEntity) holdingsMap.get("holdingsReportEntity");
                    if (holdingsReportEntity != null) {
                        reportEntities.add(holdingsReportEntity);
                    } else {
                        processHoldings = true;
                        holdingsEntities.add(holdingsEntity);
                    }
                    String holdingsCallNumber = marcUtil.getDataFieldValue(holdingsRecord, "852", 'h');
                    if(holdingsCallNumber == null){
                        holdingsCallNumber = "";
                    }
                    Character holdingsCallNumberType = marcUtil.getInd1(holdingsRecord, "852", 'h');

                    List<ItemMarcRecord> itemMarcRecordList = holdingsMarcRecord.getItemMarcRecordList();
                    if (CollectionUtils.isNotEmpty(itemMarcRecordList)) {
                        for (ItemMarcRecord itemMarcRecord : itemMarcRecordList) {
                            Record itemRecord = itemMarcRecord.getItemRecord();
                            Map<String, Object> itemMap = processAndValidateItemEntity(institutionEntity, holdingsCallNumber, holdingsCallNumberType, itemRecord,currentDate,errorMessage);
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

    private Map<String, Object> processAndValidateBibliographicEntity(Record bibRecord, InstitutionEntity institutionEntity,Date currentDate,StringBuilder errorMessage) {
        Map<String, Object> map = new HashMap<>();

        BibliographicEntity bibliographicEntity = new BibliographicEntity();
        String owningInstitutionBibId = marcUtil.getControlFieldValue(bibRecord, "001");
        if (StringUtils.isNotBlank(owningInstitutionBibId)) {
            bibliographicEntity.setOwningInstitutionBibId(owningInstitutionBibId);
        } else {
            errorMessage.append(" Owning Institution Bib Id cannot be null");
        }
        if (institutionEntity != null) {
            bibliographicEntity.setOwningInstitutionId(institutionEntity.getInstitutionId());
        } else {
            errorMessage.append(" Owning Institution Id cannot be null");
        }
        bibliographicEntity.setCreatedDate(currentDate);
        bibliographicEntity.setCreatedBy(ReCAPConstants.SUBMIT_COLLECTION);
        bibliographicEntity.setLastUpdatedDate(currentDate);
        bibliographicEntity.setLastUpdatedBy(ReCAPConstants.SUBMIT_COLLECTION);

        String bibContent = marcUtil.writeMarcXml(bibRecord);
        if (StringUtils.isNotBlank(bibContent)) {
            bibliographicEntity.setContent(bibContent.getBytes());
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

    private Map<String, Object> processAndValidateHoldingsEntity(BibliographicEntity bibliographicEntity, Record holdingsRecord, Date currentDate
    ,StringBuilder errorMessage) {
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
        String owningInstitutionHoldingsId = marcUtil.getDataFieldValue(holdingsRecord, "852", '0');
        if (StringUtils.isBlank(owningInstitutionHoldingsId)) {
            owningInstitutionHoldingsId = UUID.randomUUID().toString();
        } else if (owningInstitutionHoldingsId.length() > 100) {
            owningInstitutionHoldingsId = UUID.randomUUID().toString();
        }
        holdingsEntity.setOwningInstitutionHoldingsId(owningInstitutionHoldingsId);
        map.put("holdingsEntity", holdingsEntity);
        return map;
    }

    private Map<String, Object> processAndValidateItemEntity(InstitutionEntity institutionEntity, String holdingsCallNumber, Character holdingsCallNumberType, Record itemRecord, Date currentDate,
                                                             StringBuilder errorMessage) {
        Map<String, Object> map = new HashMap<>();
        ItemEntity itemEntity = new ItemEntity();
        String itemBarcode = marcUtil.getDataFieldValue(itemRecord, "876", 'p');
        if (StringUtils.isNotBlank(itemBarcode)) {
            itemEntity.setBarcode(itemBarcode);
            map.put("itemBarcode",itemBarcode);
        } else {
            errorMessage.append(" Item Barcode cannot be null");
        }
        String customerCode = marcUtil.getDataFieldValue(itemRecord, "876", 'z');
        if (StringUtils.isNotBlank(customerCode)) {
            itemEntity.setCustomerCode(customerCode);
        }
        itemEntity.setCallNumber(holdingsCallNumber);
        itemEntity.setCallNumberType(holdingsCallNumberType != null ? String.valueOf(holdingsCallNumberType) : "");
        String copyNumber = marcUtil.getDataFieldValue(itemRecord, "876", 't');
        if (StringUtils.isNotBlank(copyNumber) && org.apache.commons.lang3.math.NumberUtils.isNumber(copyNumber)) {
            itemEntity.setCopyNumber(Integer.valueOf(copyNumber));
        }
        if (institutionEntity != null) {
            itemEntity.setOwningInstitutionId(institutionEntity.getInstitutionId());
        } else {
            errorMessage.append(" Owning Institution Id cannot be null");
        }
        String collectionGroupCode = marcUtil.getDataFieldValue(itemRecord, "876", 'x');
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

    private Integer getOwningInstitutionId(BibMarcRecord bibMarcRecord) {
        Record itemRecord = bibMarcRecord.getHoldingsMarcRecords().get(0).getItemMarcRecordList().get(0).getItemRecord();
        String customerCode = marcUtil.getDataFieldValue(itemRecord, "876", 'z');
        CustomerCodeEntity customerCodeEntity;
        if(null != customerCode) {
            customerCodeEntity = customerCodeDetailsRepository.findByCustomerCode(customerCode);
            return customerCodeEntity.getOwningInstitutionId();
        } else {
            String barcode = marcUtil.getDataFieldValue(bibMarcRecord.getHoldingsMarcRecords().get(0).getItemMarcRecordList().get(0).getItemRecord(), "876",'p');
            List<ItemEntity> itemEntityList = itemDetailsRepository.findByBarcode(barcode);
            return itemEntityList.get(0).getOwningInstitutionId();
        }
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
