package org.recap.service.submitcollection;

import org.apache.commons.collections.CollectionUtils;
import org.marc4j.marc.Record;
import org.recap.ReCAPConstants;
import org.recap.converter.MarcToBibEntityConverter;
import org.recap.converter.SCSBToBibEntityConverter;
import org.recap.converter.XmlToBibEntityConverterInterface;
import org.recap.model.*;
import org.recap.model.jaxb.BibRecord;
import org.recap.model.jaxb.JAXBHandler;
import org.recap.model.jaxb.marc.BibRecords;
import org.recap.model.report.SubmitCollectionRejectionInfo;
import org.recap.repository.*;
import org.recap.util.MarcUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.xml.bind.JAXBException;
import java.util.*;

/**
 * Created by premkb on 20/12/16.
 */
@Service
public class SubmitCollectionService {

    private final Logger logger = LoggerFactory.getLogger(SubmitCollectionService.class);

    @Autowired
    private BibliographicDetailsRepository bibliographicDetailsRepository;

    @Autowired
    private CustomerCodeDetailsRepository customerCodeDetailsRepository;

    @Autowired
    private ItemDetailsRepository itemDetailsRepository;

    @Autowired
    private MarcToBibEntityConverter marcToBibEntityConverter;

    @Autowired
    private SCSBToBibEntityConverter scsbToBibEntityConverter;

    @Autowired
    private ReportDetailRepository reportDetailRepository;

    @Autowired
    private ItemStatusDetailsRepository itemStatusDetailsRepository;

    @Autowired
    private InstitutionDetailsRepository institutionDetailsRepository;

    @Autowired
    private MarcUtil marcUtil;

    @PersistenceContext
    private EntityManager entityManager;

    private RestTemplate restTemplate;

    private Map<Integer,String> itemStatusMap;

    private Map<Integer,String> institutionEntityMap;

    @Value("${server.protocol}")
    private String serverProtocol;

    @Value("${scsb.solr.client.url}")
    private String scsbSolrClientUrl;

    @Value(("${submit.collection.input.limit}"))
    private Integer inputLimit;

    @Transactional
    public String process(String inputRecords, List<Integer> processedBibIdList) {
        String reponse = null;
        List<SubmitCollectionRejectionInfo> submitCollectionRejectionInfos = new ArrayList<>();
        try{
            if(!inputRecords.equals("")) {
                if (inputRecords.contains("<bibRecords>")) {
                    reponse = processSCSB(inputRecords, processedBibIdList, submitCollectionRejectionInfos);
                    if (reponse != null)
                        return reponse;
                } else
                    reponse = processMarc(inputRecords, processedBibIdList, submitCollectionRejectionInfos);
                    if (reponse != null)
                        return reponse;
                generateSubmitCollectionRejectionReport(submitCollectionRejectionInfos);
                reponse = ReCAPConstants.SUCCESS;
            }
        }catch (Exception e){
            e.printStackTrace();
            reponse = ReCAPConstants.SUBMIT_COLLECTION_INTERNAL_ERROR;
        }
        return reponse;
    }

    private String processMarc(String inputRecords, List<Integer> processedBibIdList, List<SubmitCollectionRejectionInfo> submitCollectionRejectionInfos) {
        String format;
        format = ReCAPConstants.FORMAT_MARC;
        List<Record> records = null;
        try {
            records = getMarcUtil().convertMarcXmlToRecord(inputRecords);
            if(records.size() > inputLimit){
                return ReCAPConstants.SUBMIT_COLLECTION_LIMIT_EXCEED_MESSAGE + inputLimit;
            }
        } catch (Exception e) {
            logger.info(String.valueOf(e.getCause()));
            e.printStackTrace();
            return ReCAPConstants.INVALID_MARC_XML_FORMAT_MESSAGE;
        }
        if (CollectionUtils.isNotEmpty(records)) {
            for (Record record : records) {
                BibliographicEntity bibliographicEntity = loadData(record, format,submitCollectionRejectionInfos);
                processedBibIdList.add(bibliographicEntity.getBibliographicId());
            }
        }
        return null;
    }

    private String processSCSB(String inputRecords, List<Integer> processedBibIdList, List<SubmitCollectionRejectionInfo> submitCollectionRejectionInfos) {
        String format;
        format = ReCAPConstants.FORMAT_SCSB;
        BibRecords bibRecords = null;
        try {
            bibRecords = (BibRecords) JAXBHandler.getInstance().unmarshal(inputRecords, BibRecords.class);
            if(bibRecords.getBibRecords().size() > inputLimit){
                return ReCAPConstants.SUBMIT_COLLECTION_LIMIT_EXCEED_MESSAGE + " " +inputLimit;
            }
        } catch (JAXBException e) {
            logger.info(String.valueOf(e.getCause()));
            return ReCAPConstants.INVALID_SCSB_XML_FORMAT_MESSAGE;
        }
        for (BibRecord bibRecord : bibRecords.getBibRecords()) {
            BibliographicEntity bibliographicEntity = loadData(bibRecord, format,submitCollectionRejectionInfos);
            processedBibIdList.add(bibliographicEntity.getBibliographicId());
        }
        return null;
    }


    private BibliographicEntity loadData(Object record, String format, List<SubmitCollectionRejectionInfo> submitCollectionRejectionInfos){
        BibliographicEntity savedBibliographicEntity = null;
        Map responseMap = getConverter(format).convert(record);
        BibliographicEntity bibliographicEntity = (BibliographicEntity) responseMap.get("bibliographicEntity");
        List<ReportEntity> reportEntityList = (List<ReportEntity>) responseMap.get("reportEntities");
        if (CollectionUtils.isNotEmpty(reportEntityList)) {
            reportDetailRepository.save(reportEntityList);
        }
        if (bibliographicEntity != null) {
            savedBibliographicEntity = updateBibliographicEntity(bibliographicEntity,submitCollectionRejectionInfos);
        }
        return savedBibliographicEntity;
    }

    public void setSubmitCollectionRejectionInfo(BibliographicEntity bibliographicEntity,List<SubmitCollectionRejectionInfo> submitCollectionRejectionInfos){
        for(ItemEntity itemEntity : bibliographicEntity.getItemEntities()){
            ItemStatusEntity itemStatusEntity = getItemStatusDetailsRepository().findByItemStatusId(itemEntity.getItemAvailabilityStatusId());
            if(!itemStatusEntity.getStatusCode().equalsIgnoreCase(ReCAPConstants.ITEM_STATUS_AVAILABLE)){
                SubmitCollectionRejectionInfo submitCollectionRejectionInfo = new SubmitCollectionRejectionInfo();
                submitCollectionRejectionInfo.setItemBarcode(itemEntity.getBarcode());
                submitCollectionRejectionInfo.setCustomerCode(itemEntity.getCustomerCode());
                submitCollectionRejectionInfo.setOwningInstitution((String) getInstitutionEntityMap().get(itemEntity.getOwningInstitutionId()));
                submitCollectionRejectionInfos.add(submitCollectionRejectionInfo);
            }
        }
    }

    public String indexData(List<Integer> bibliographicIdList){
        return getRestTemplate().postForObject(serverProtocol + scsbSolrClientUrl + "solrIndexer/indexByBibliographicId", bibliographicIdList, String.class);
    }

    public BibliographicEntity updateBibliographicEntity(BibliographicEntity bibliographicEntity,List<SubmitCollectionRejectionInfo> submitCollectionRejectionInfos) {
        BibliographicEntity savedBibliographicEntity=null;

        BibliographicEntity fetchBibliographicEntity = getBibliographicDetailsRepository().findByOwningInstitutionIdAndOwningInstitutionBibId(bibliographicEntity.getOwningInstitutionId(),bibliographicEntity.getOwningInstitutionBibId());
        if(fetchBibliographicEntity  == null){
            savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(bibliographicEntity);
            entityManager.refresh(savedBibliographicEntity);
        }else{ // Existing bib Record
            setSubmitCollectionRejectionInfo(fetchBibliographicEntity,submitCollectionRejectionInfos);
            fetchBibliographicEntity.setContent(bibliographicEntity.getContent());
            fetchBibliographicEntity.setCreatedBy(bibliographicEntity.getCreatedBy());
            fetchBibliographicEntity.setCreatedDate(bibliographicEntity.getCreatedDate());
            fetchBibliographicEntity.setDeleted(bibliographicEntity.isDeleted());
            fetchBibliographicEntity.setLastUpdatedBy(bibliographicEntity.getLastUpdatedBy());
            fetchBibliographicEntity.setLastUpdatedDate(bibliographicEntity.getLastUpdatedDate());
            List<HoldingsEntity> fetchHoldingsEntities =fetchBibliographicEntity.getHoldingsEntities();
            List<HoldingsEntity> holdingsEntities = new ArrayList<>(bibliographicEntity.getHoldingsEntities());
            for (Iterator iholdings = holdingsEntities.iterator(); iholdings.hasNext();) {
                HoldingsEntity holdingsEntity =(HoldingsEntity) iholdings.next();
                for (int j=0;j<fetchHoldingsEntities.size();j++) {
                    HoldingsEntity fetchHolding=fetchHoldingsEntities.get(j);
                    if(fetchHolding.getOwningInstitutionHoldingsId().equalsIgnoreCase(holdingsEntity.getOwningInstitutionHoldingsId())  && fetchHolding.getOwningInstitutionId().equals(holdingsEntity.getOwningInstitutionId())) {
                        copyHoldingsEntity(fetchHolding,holdingsEntity);
                        iholdings.remove();
                    }
                }
            }
            fetchHoldingsEntities.addAll(holdingsEntities);
            // Item
            List<ItemEntity> fetchItemsEntities =fetchBibliographicEntity.getItemEntities();
            List<ItemEntity> itemsEntities = new ArrayList<>(bibliographicEntity.getItemEntities());
            for (Iterator iItems=itemsEntities.iterator();iItems.hasNext();) {
                ItemEntity itemEntity =(ItemEntity) iItems.next();
                for (Iterator ifetchItems=fetchItemsEntities.iterator();ifetchItems.hasNext();) {
                    ItemEntity fetchItem=(ItemEntity) ifetchItems.next();
                    if(fetchItem.getOwningInstitutionItemId().equalsIgnoreCase(itemEntity.getOwningInstitutionItemId())  && fetchItem.getOwningInstitutionId().equals(itemEntity.getOwningInstitutionId())) {
                        copyItemEntity(fetchItem,itemEntity);
                        iItems.remove();
                    }
                }
            }
            fetchItemsEntities.addAll(itemsEntities);
            fetchBibliographicEntity.setHoldingsEntities(fetchHoldingsEntities);
            fetchBibliographicEntity.setItemEntities(fetchItemsEntities);
            savedBibliographicEntity = bibliographicDetailsRepository.saveAndFlush(fetchBibliographicEntity);
        }
        return savedBibliographicEntity;
    }

    private HoldingsEntity copyHoldingsEntity(HoldingsEntity fetchHoldingsEntity, HoldingsEntity holdingsEntity){
        fetchHoldingsEntity.setContent(holdingsEntity.getContent());
        fetchHoldingsEntity.setCreatedBy(holdingsEntity.getCreatedBy());
        fetchHoldingsEntity.setCreatedDate(holdingsEntity.getCreatedDate());
        fetchHoldingsEntity.setDeleted(holdingsEntity.isDeleted());
        fetchHoldingsEntity.setLastUpdatedBy(holdingsEntity.getLastUpdatedBy());
        fetchHoldingsEntity.setLastUpdatedDate(holdingsEntity.getLastUpdatedDate());
        return fetchHoldingsEntity;
    }

    private ItemEntity copyItemEntity(ItemEntity fetchItemEntity, ItemEntity itemEntity){
        fetchItemEntity.setBarcode(itemEntity.getBarcode());
        fetchItemEntity.setCreatedBy(itemEntity.getCreatedBy());
        fetchItemEntity.setCreatedDate(itemEntity.getCreatedDate());
        fetchItemEntity.setDeleted(itemEntity.isDeleted());
        fetchItemEntity.setLastUpdatedBy(itemEntity.getLastUpdatedBy());
        fetchItemEntity.setLastUpdatedDate(itemEntity.getLastUpdatedDate());
        fetchItemEntity.setCallNumber(itemEntity.getCallNumber());
        fetchItemEntity.setCustomerCode(itemEntity.getCustomerCode());
        fetchItemEntity.setCallNumberType(itemEntity.getCallNumberType());
        if (isAvailableItem(fetchItemEntity.getItemAvailabilityStatusId())) {
            fetchItemEntity.setCollectionGroupId(itemEntity.getCollectionGroupId());
            fetchItemEntity.setUseRestrictions(itemEntity.getUseRestrictions());
        }
        fetchItemEntity.setCopyNumber(itemEntity.getCopyNumber());
        fetchItemEntity.setVolumePartYear(itemEntity.getVolumePartYear());
        return fetchItemEntity;
    }

    public boolean isAvailableItem(Integer itemAvailabilityStatusId){
        String itemStatusCode = (String) getItemStatusMap().get(itemAvailabilityStatusId);
        if (itemStatusCode.equalsIgnoreCase(ReCAPConstants.ITEM_STATUS_AVAILABLE)) {
            return true;
        }
        return false;
    }

    private XmlToBibEntityConverterInterface getConverter(String format){
        if(format.equalsIgnoreCase(ReCAPConstants.FORMAT_MARC)){
            return marcToBibEntityConverter;
        } else if(format.equalsIgnoreCase(ReCAPConstants.FORMAT_SCSB)){
            return scsbToBibEntityConverter;
        }
        return null;
    }


    public void generateSubmitCollectionRejectionReport(List<SubmitCollectionRejectionInfo> submitCollectionRequestList){
        if(submitCollectionRequestList != null && submitCollectionRequestList.size() > 0){
            try {
                for(SubmitCollectionRejectionInfo submitCollectionRejectionInfo : submitCollectionRequestList){
                    if(!StringUtils.isEmpty(submitCollectionRejectionInfo.getItemBarcode()) && !StringUtils.isEmpty(submitCollectionRejectionInfo.getCustomerCode())){
                        List<ReportDataEntity> reportDataEntities = new ArrayList<>();
                        List<ReportEntity> reportEntityList = new ArrayList<>();
                        ReportEntity reportEntity = new ReportEntity();
                        reportEntity.setFileName(ReCAPConstants.SUBMIT_COLLECTION_REPORT);
                        reportEntity.setType(ReCAPConstants.SUBMIT_COLLECTION_REJECTION_REPORT);
                        reportEntity.setCreatedDate(new Date());
                        String owningInstitution = submitCollectionRejectionInfo.getOwningInstitution();
                        reportEntity.setInstitutionName(owningInstitution);

                        ReportDataEntity itemBarcodeReportDataEntity = new ReportDataEntity();
                        itemBarcodeReportDataEntity.setHeaderName(ReCAPConstants.SUBMIT_COLLECTION_ITEM_BARCODE);
                        itemBarcodeReportDataEntity.setHeaderValue(submitCollectionRejectionInfo.getItemBarcode());
                        reportDataEntities.add(itemBarcodeReportDataEntity);

                        ReportDataEntity customerCodeReportDataEntity = new ReportDataEntity();
                        customerCodeReportDataEntity.setHeaderName(ReCAPConstants.SUBMIT_COLLECTION_CUSTOMER_CODE);
                        customerCodeReportDataEntity.setHeaderValue(submitCollectionRejectionInfo.getCustomerCode());
                        reportDataEntities.add(customerCodeReportDataEntity);

                        ReportDataEntity owningInstitutionReportDataEntity = new ReportDataEntity();
                        owningInstitutionReportDataEntity.setHeaderName(ReCAPConstants.OWNING_INSTITUTION);
                        owningInstitutionReportDataEntity.setHeaderValue(owningInstitution);
                        reportDataEntities.add(owningInstitutionReportDataEntity);

                        reportEntity.setReportDataEntities(reportDataEntities);
                        reportEntityList.add(reportEntity);
                        reportDetailRepository.save(reportEntityList);

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public BibliographicDetailsRepository getBibliographicDetailsRepository() {
        return bibliographicDetailsRepository;
    }

    public void setBibliographicDetailsRepository(BibliographicDetailsRepository bibliographicDetailsRepository) {
        this.bibliographicDetailsRepository = bibliographicDetailsRepository;
    }

    public CustomerCodeDetailsRepository getCustomerCodeDetailsRepository() {
        return customerCodeDetailsRepository;
    }

    public void setCustomerCodeDetailsRepository(CustomerCodeDetailsRepository customerCodeDetailsRepository) {
        this.customerCodeDetailsRepository = customerCodeDetailsRepository;
    }

    public MarcUtil getMarcUtil() {
        return marcUtil;
    }

    public void setMarcUtil(MarcUtil marcUtil) {
        this.marcUtil = marcUtil;
    }

    public ItemDetailsRepository getItemDetailsRepository() {
        return itemDetailsRepository;
    }

    public void setItemDetailsRepository(ItemDetailsRepository itemDetailsRepository) {
        this.itemDetailsRepository = itemDetailsRepository;
    }

    public ItemStatusDetailsRepository getItemStatusDetailsRepository() {
        return itemStatusDetailsRepository;
    }

    public void setItemStatusDetailsRepository(ItemStatusDetailsRepository itemStatusDetailsRepository) {
        this.itemStatusDetailsRepository = itemStatusDetailsRepository;
    }

    public InstitutionDetailsRepository getInstitutionDetailsRepository() {
        return institutionDetailsRepository;
    }

    public void setInstitutionDetailsRepository(InstitutionDetailsRepository institutionDetailsRepository) {
        this.institutionDetailsRepository = institutionDetailsRepository;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public RestTemplate getRestTemplate() {
        if(restTemplate == null){
            restTemplate = new RestTemplate();
        }
        return restTemplate;
    }

    public void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map getItemStatusMap() {
        if (null == itemStatusMap) {
            itemStatusMap = new HashMap();
            try {
                Iterable<ItemStatusEntity> itemStatusEntities = itemStatusDetailsRepository.findAll();
                for (Iterator iterator = itemStatusEntities.iterator(); iterator.hasNext(); ) {
                    ItemStatusEntity itemStatusEntity = (ItemStatusEntity) iterator.next();
                    itemStatusMap.put(itemStatusEntity.getItemStatusId(), itemStatusEntity.getStatusCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return itemStatusMap;
    }

    public Map getInstitutionEntityMap() {
        if (null == institutionEntityMap) {
            institutionEntityMap = new HashMap();
            try {
                Iterable<InstitutionEntity> institutionEntities = institutionDetailsRepository.findAll();
                for (Iterator iterator = institutionEntities.iterator(); iterator.hasNext(); ) {
                    InstitutionEntity institutionEntity = (InstitutionEntity) iterator.next();
                    institutionEntityMap.put(institutionEntity.getInstitutionId(), institutionEntity.getInstitutionCode());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return institutionEntityMap;
    }
}