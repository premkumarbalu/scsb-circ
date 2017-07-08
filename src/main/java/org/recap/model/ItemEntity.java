package org.recap.model;

import org.apache.commons.lang3.StringUtils;
import org.recap.ReCAPConstants;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by pvsubrah on 6/11/16.
 */
@Entity
@Table(name = "item_t", schema = "recap", catalog = "")
@IdClass(ItemPK.class)
public class ItemEntity implements Serializable {
    @Column(name = "ITEM_ID", insertable = false, updatable = false)
    private Integer itemId;

    @Column(name = "BARCODE")
    private String barcode;

    @Column(name = "CUSTOMER_CODE")
    private String customerCode;

    @Column(name = "CALL_NUMBER")
    private String callNumber;

    @Column(name = "CALL_NUMBER_TYPE")
    private String callNumberType;

    @Column(name = "ITEM_AVAIL_STATUS_ID")
    private Integer itemAvailabilityStatusId;

    @Column(name = "COPY_NUMBER")
    private Integer copyNumber;

    @Id
    @Column(name = "OWNING_INST_ID")
    private Integer owningInstitutionId;

    @Column(name = "COLLECTION_GROUP_ID")
    private Integer collectionGroupId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(name = "CREATED_BY")
    private String createdBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_UPDATED_DATE")
    private Date lastUpdatedDate;

    @Column(name = "LAST_UPDATED_BY")
    private String lastUpdatedBy;

    @Column(name = "USE_RESTRICTIONS")
    private String useRestrictions;

    @Column(name = "VOLUME_PART_YEAR")
    private String volumePartYear;

    @Id
    @Column(name = "OWNING_INST_ITEM_ID")
    private String owningInstitutionItemId;

    @Column(name = "IS_DELETED")
    private boolean isDeleted;

    @Column(name = "IS_CGD_PROTECTION")
    private boolean isCgdProtection;

    @Column(name = "CATALOGING_STATUS")
    private String catalogingStatus;

    @ManyToMany(mappedBy = "itemEntities")
    private List<HoldingsEntity> holdingsEntities;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "ITEM_AVAIL_STATUS_ID", insertable = false, updatable = false)
    private ItemStatusEntity itemStatusEntity;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "COLLECTION_GROUP_ID", insertable = false, updatable = false)
    private CollectionGroupEntity collectionGroupEntity;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "OWNING_INST_ID", insertable = false, updatable = false)
    private InstitutionEntity institutionEntity;

    @ManyToMany(mappedBy = "itemEntities",fetch = FetchType.EAGER)
    private List<BibliographicEntity> bibliographicEntities;

    /**
     * Gets item id.
     *
     * @return the item id
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * Sets item id.
     *
     * @param itemId the item id
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * Gets barcode.
     *
     * @return the barcode
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Sets barcode.
     *
     * @param barcode the barcode
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    /**
     * Gets customer code.
     *
     * @return the customer code
     */
    public String getCustomerCode() {
        return customerCode;
    }

    /**
     * Sets customer code.
     *
     * @param customerCode the customer code
     */
    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * Gets call number.
     *
     * @return the call number
     */
    public String getCallNumber() {
        return callNumber;
    }

    /**
     * Sets call number.
     *
     * @param callNumber the call number
     */
    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

    /**
     * Gets call number type.
     *
     * @return the call number type
     */
    public String getCallNumberType() {
        return callNumberType;
    }

    /**
     * Sets call number type.
     *
     * @param callNumberType the call number type
     */
    public void setCallNumberType(String callNumberType) {
        this.callNumberType = callNumberType;
    }

    /**
     * Gets item availability status id.
     *
     * @return the item availability status id
     */
    public Integer getItemAvailabilityStatusId() {
        return itemAvailabilityStatusId;
    }

    /**
     * Sets item availability status id.
     *
     * @param itemAvailabilityStatusId the item availability status id
     */
    public void setItemAvailabilityStatusId(Integer itemAvailabilityStatusId) {
        this.itemAvailabilityStatusId = itemAvailabilityStatusId;
    }

    /**
     * Gets copy number.
     *
     * @return the copy number
     */
    public Integer getCopyNumber() {
        return copyNumber;
    }

    /**
     * Sets copy number.
     *
     * @param copyNumber the copy number
     */
    public void setCopyNumber(Integer copyNumber) {
        this.copyNumber = copyNumber;
    }

    /**
     * Gets owning institution id.
     *
     * @return the owning institution id
     */
    public Integer getOwningInstitutionId() {
        return owningInstitutionId;
    }

    /**
     * Sets owning institution id.
     *
     * @param owningInstitutionId the owning institution id
     */
    public void setOwningInstitutionId(Integer owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }

    /**
     * Gets collection group id.
     *
     * @return the collection group id
     */
    public Integer getCollectionGroupId() {
        return collectionGroupId;
    }

    /**
     * Sets collection group id.
     *
     * @param collectionGroupId the collection group id
     */
    public void setCollectionGroupId(Integer collectionGroupId) {
        this.collectionGroupId = collectionGroupId;
    }

    /**
     * Gets created date.
     *
     * @return the created date
     */
    public Date getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets created date.
     *
     * @param createdDate the created date
     */
    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Gets created by.
     *
     * @return the created by
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * Sets created by.
     *
     * @param createdBy the created by
     */
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    /**
     * Gets last updated date.
     *
     * @return the last updated date
     */
    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    /**
     * Sets last updated date.
     *
     * @param lastUpdatedDate the last updated date
     */
    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    /**
     * Gets last updated by.
     *
     * @return the last updated by
     */
    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    /**
     * Sets last updated by.
     *
     * @param lastUpdatedBy the last updated by
     */
    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    /**
     * Gets use restrictions.
     *
     * @return the use restrictions
     */
    public String getUseRestrictions() {
        return useRestrictions;
    }

    /**
     * Sets use restrictions.
     *
     * @param useRestrictions the use restrictions
     */
    public void setUseRestrictions(String useRestrictions) {
        this.useRestrictions = useRestrictions;
    }

    /**
     * Gets volume part year.
     *
     * @return the volume part year
     */
    public String getVolumePartYear() {
        return volumePartYear;
    }

    /**
     * Sets volume part year.
     *
     * @param volumePartYear the volume part year
     */
    public void setVolumePartYear(String volumePartYear) {
        this.volumePartYear = volumePartYear;
    }

    /**
     * Gets owning institution item id.
     *
     * @return the owning institution item id
     */
    public String getOwningInstitutionItemId() {
        return owningInstitutionItemId;
    }

    /**
     * Sets owning institution item id.
     *
     * @param owningInstitutionItemId the owning institution item id
     */
    public void setOwningInstitutionItemId(String owningInstitutionItemId) {
        this.owningInstitutionItemId = owningInstitutionItemId;
    }

    /**
     * Is deleted boolean.
     *
     * @return the boolean
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Sets deleted.
     *
     * @param deleted the deleted
     */
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public boolean isCgdProtection() {
        return isCgdProtection;
    }

    public void setCgdProtection(boolean cgdProtection) {
        isCgdProtection = cgdProtection;
    }

    /**
     * Gets cataloging status.
     *
     * @return the cataloging status
     */
    public String getCatalogingStatus() {
        return catalogingStatus;
    }

    /**
     * Sets cataloging status.
     *
     * @param catalogingStatus the cataloging status
     */
    public void setCatalogingStatus(String catalogingStatus) {
        this.catalogingStatus = catalogingStatus;
    }

    /**
     * Gets holdings entities.
     *
     * @return the holdings entities
     */
    public List<HoldingsEntity> getHoldingsEntities() {
        return holdingsEntities;
    }

    /**
     * Sets holdings entities.
     *
     * @param holdingsEntities the holdings entities
     */
    public void setHoldingsEntities(List<HoldingsEntity> holdingsEntities) {
        this.holdingsEntities = holdingsEntities;
    }

    /**
     * Gets item status entity.
     *
     * @return the item status entity
     */
    public ItemStatusEntity getItemStatusEntity() {
        return itemStatusEntity;
    }

    /**
     * Sets item status entity.
     *
     * @param itemStatusEntity the item status entity
     */
    public void setItemStatusEntity(ItemStatusEntity itemStatusEntity) {
        this.itemStatusEntity = itemStatusEntity;
    }

    /**
     * Gets collection group entity.
     *
     * @return the collection group entity
     */
    public CollectionGroupEntity getCollectionGroupEntity() {
        return collectionGroupEntity;
    }

    /**
     * Sets collection group entity.
     *
     * @param collectionGroupEntity the collection group entity
     */
    public void setCollectionGroupEntity(CollectionGroupEntity collectionGroupEntity) {
        this.collectionGroupEntity = collectionGroupEntity;
    }

    /**
     * Gets institution entity.
     *
     * @return the institution entity
     */
    public InstitutionEntity getInstitutionEntity() {
        return institutionEntity;
    }

    /**
     * Sets institution entity.
     *
     * @param institutionEntity the institution entity
     */
    public void setInstitutionEntity(InstitutionEntity institutionEntity) {
        this.institutionEntity = institutionEntity;
    }

    /**
     * Gets bibliographic entities.
     *
     * @return the bibliographic entities
     */
    public List<BibliographicEntity> getBibliographicEntities() {
        return bibliographicEntities;
    }

    /**
     * Sets bibliographic entities.
     *
     * @param bibliographicEntities the bibliographic entities
     */
    public void setBibliographicEntities(List<BibliographicEntity> bibliographicEntities) {
        this.bibliographicEntities = bibliographicEntities;
    }

    /**
     * Is complete boolean.
     *
     * @return the boolean
     */
    public boolean isComplete() {
        if (StringUtils.isNotBlank(this.catalogingStatus) && ReCAPConstants.COMPLETE_STATUS.equals(this.catalogingStatus)) {
            return true;
        }
        return false;
    }
}


