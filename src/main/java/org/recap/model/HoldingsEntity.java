package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by pvsubrah on 6/11/16.
 */
@Entity
@Table(name = "holdings_t", schema = "recap", catalog = "")
@IdClass(HoldingsPK.class)
@NamedNativeQueries({
        @NamedNativeQuery(
                name = "HoldingsEntity.getNonDeletedItemEntities",
                query = "SELECT ITEM_T.* FROM ITEM_T, ITEM_HOLDINGS_T WHERE ITEM_HOLDINGS_T.ITEM_INST_ID = ITEM_T.OWNING_INST_ID AND " +
                        "ITEM_HOLDINGS_T.OWNING_INST_ITEM_ID = ITEM_T.OWNING_INST_ITEM_ID AND ITEM_T.IS_DELETED = 0 AND " +
                        " ITEM_HOLDINGS_T.OWNING_INST_HOLDINGS_ID = :owningInstitutionHoldingsId AND ITEM_HOLDINGS_T.HOLDINGS_INST_ID = :owningInstitutionId",
                resultClass = ItemEntity.class)
})
public class HoldingsEntity implements Serializable {

    @Column(name = "HOLDINGS_ID", insertable = false, updatable = false)
    private Integer holdingsId;

    @Lob
    @Column(name = "CONTENT")
    private byte[] content;

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
    @Column(name = "OWNING_INST_ID")
    private Integer owningInstitutionId;

    @Id
    @Column(name = "OWNING_INST_HOLDINGS_ID")
    private String owningInstitutionHoldingsId;

    @Column(name = "IS_DELETED")
    private boolean isDeleted;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "OWNING_INST_ID", insertable = false, updatable = false)
    private InstitutionEntity institutionEntity;

    @ManyToMany(mappedBy = "holdingsEntities")
    private List<BibliographicEntity> bibliographicEntities;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "item_holdings_t", joinColumns = {
            @JoinColumn(name = "OWNING_INST_HOLDINGS_ID", referencedColumnName = "OWNING_INST_HOLDINGS_ID"),
            @JoinColumn(name = "HOLDINGS_INST_ID", referencedColumnName = "OWNING_INST_ID")},
            inverseJoinColumns = {
                    @JoinColumn(name = "OWNING_INST_ITEM_ID", referencedColumnName = "OWNING_INST_ITEM_ID"),
                    @JoinColumn(name = "ITEM_INST_ID", referencedColumnName = "OWNING_INST_ID")})
    private List<ItemEntity> itemEntities;

    /**
     * Instantiates a new Holdings entity.
     */
    public HoldingsEntity() {
        //Do nothing
    }

    /**
     * Gets holdings id.
     *
     * @return the holdings id
     */
    public Integer getHoldingsId() {
        return holdingsId;
    }

    /**
     * Sets holdings id.
     *
     * @param holdingsId the holdings id
     */
    public void setHoldingsId(Integer holdingsId) {
        this.holdingsId = holdingsId;
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
     * Gets owning institution holdings id.
     *
     * @return the owning institution holdings id
     */
    public String getOwningInstitutionHoldingsId() {
        return owningInstitutionHoldingsId;
    }

    /**
     * Sets owning institution holdings id.
     *
     * @param owningInstitutionHoldingsId the owning institution holdings id
     */
    public void setOwningInstitutionHoldingsId(String owningInstitutionHoldingsId) {
        this.owningInstitutionHoldingsId = owningInstitutionHoldingsId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        HoldingsEntity holdingsEntity = (HoldingsEntity) o;

        return owningInstitutionHoldingsId.equals(holdingsEntity.owningInstitutionHoldingsId);

    }

    @Override
    public int hashCode() {
        return owningInstitutionHoldingsId.hashCode();
    }
}
