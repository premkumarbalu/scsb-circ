package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by pvsubrah on 6/10/16.
 */
@Entity
@Table(name = "bibliographic_t", schema = "recap", catalog = "")
@IdClass(BibliographicPK.class)
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "BibliographicEntity.getNonDeletedHoldingsEntities",
                query = "SELECT HOLDINGS_T.* FROM HOLDINGS_T, BIBLIOGRAPHIC_HOLDINGS_T WHERE BIBLIOGRAPHIC_HOLDINGS_T.HOLDINGS_INST_ID = HOLDINGS_T.OWNING_INST_ID " +
                        "AND BIBLIOGRAPHIC_HOLDINGS_T.OWNING_INST_HOLDINGS_ID = HOLDINGS_T.OWNING_INST_HOLDINGS_ID AND HOLDINGS_T.IS_DELETED = 0 AND " +
                        "BIBLIOGRAPHIC_HOLDINGS_T.OWNING_INST_BIB_ID = :owningInstitutionBibId AND BIBLIOGRAPHIC_HOLDINGS_T.BIB_INST_ID = :owningInstitutionId",
                resultClass = HoldingsEntity.class),
        @NamedNativeQuery(
                name = "BibliographicEntity.getNonDeletedItemEntities",
                query = "SELECT ITEM_T.* FROM ITEM_T, BIBLIOGRAPHIC_ITEM_T WHERE BIBLIOGRAPHIC_ITEM_T.ITEM_INST_ID = ITEM_T.OWNING_INST_ID " +
                        "AND BIBLIOGRAPHIC_ITEM_T.OWNING_INST_ITEM_ID = ITEM_T.OWNING_INST_ITEM_ID AND ITEM_T.IS_DELETED = 0 AND " +
                        "BIBLIOGRAPHIC_ITEM_T.OWNING_INST_BIB_ID = :owningInstitutionBibId AND BIBLIOGRAPHIC_ITEM_T.BIB_INST_ID = :owningInstitutionId",
                resultClass = ItemEntity.class),
})
public class BibliographicEntity implements Serializable {
    @Column(name = "BIBLIOGRAPHIC_ID", insertable = false, updatable = false)
    private Integer bibliographicId;

    @Lob
    @Column(name = "CONTENT")
    private byte[] content;

    @Id
    @Column(name = "OWNING_INST_ID")
    private Integer owningInstitutionId;

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

    @Id
    @Column(name = "OWNING_INST_BIB_ID")
    private String owningInstitutionBibId;

    @Column(name = "IS_DELETED")
    private boolean isDeleted;

    @Column(name = "CATALOGING_STATUS")
    private String catalogingStatus;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "OWNING_INST_ID", insertable = false, updatable = false)
    private InstitutionEntity institutionEntity;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "bibliographic_holdings_t", joinColumns = {
            @JoinColumn(name = "OWNING_INST_BIB_ID", referencedColumnName = "OWNING_INST_BIB_ID"),
            @JoinColumn(name = "BIB_INST_ID", referencedColumnName = "OWNING_INST_ID")},
            inverseJoinColumns = {
                    @JoinColumn(name = "OWNING_INST_HOLDINGS_ID", referencedColumnName = "OWNING_INST_HOLDINGS_ID"),
                    @JoinColumn(name = "HOLDINGS_INST_ID", referencedColumnName = "OWNING_INST_ID")})
    private List<HoldingsEntity> holdingsEntities;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "bibliographic_item_t", joinColumns = {
            @JoinColumn(name = "OWNING_INST_BIB_ID", referencedColumnName = "OWNING_INST_BIB_ID"),
            @JoinColumn(name = "BIB_INST_ID", referencedColumnName = "OWNING_INST_ID")},
            inverseJoinColumns = {
                    @JoinColumn(name = "OWNING_INST_ITEM_ID", referencedColumnName = "OWNING_INST_ITEM_ID"),
                    @JoinColumn(name = "ITEM_INST_ID", referencedColumnName = "OWNING_INST_ID")})
    private List<ItemEntity> itemEntities;

    /**
     * Instantiates a new Bibliographic entity.
     */
    public BibliographicEntity() {
        //Do Nothing
    }

    /**
     * Gets bibliographic id.
     *
     * @return the bibliographic id
     */
    public Integer getBibliographicId() {
        return bibliographicId;
    }

    /**
     * Sets bibliographic id.
     *
     * @param bibliographicId the bibliographic id
     */
    public void setBibliographicId(Integer bibliographicId) {
        this.bibliographicId = bibliographicId;
    }

    /**
     * Get content byte [ ].
     *
     * @return the byte [ ]
     */
    public byte[] getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content the content
     */
    public void setContent(byte[] content) {
        this.content = content;
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
     * Gets owning institution bib id.
     *
     * @return the owning institution bib id
     */
    public String getOwningInstitutionBibId() {
        return owningInstitutionBibId;
    }

    /**
     * Sets owning institution bib id.
     *
     * @param owningInstitutionBibId the owning institution bib id
     */
    public void setOwningInstitutionBibId(String owningInstitutionBibId) {
        this.owningInstitutionBibId = owningInstitutionBibId;
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
     * Gets item entities.
     *
     * @return the item entities
     */
    public List<ItemEntity> getItemEntities() {
        return itemEntities;
    }

    /**
     * Sets item entities.
     *
     * @param itemEntities the item entities
     */
    public void setItemEntities(List<ItemEntity> itemEntities) {
        this.itemEntities = itemEntities;
    }


}

