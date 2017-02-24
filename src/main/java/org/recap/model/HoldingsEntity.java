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

    public HoldingsEntity() {
        //Do nothing
    }

    public Integer getHoldingsId() {
        return holdingsId;
    }

    public void setHoldingsId(Integer holdingsId) {
        this.holdingsId = holdingsId;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getLastUpdatedBy() {
        return lastUpdatedBy;
    }

    public void setLastUpdatedBy(String lastUpdatedBy) {
        this.lastUpdatedBy = lastUpdatedBy;
    }

    public Integer getOwningInstitutionId() {
        return owningInstitutionId;
    }

    public void setOwningInstitutionId(Integer owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }

    public String getOwningInstitutionHoldingsId() {
        return owningInstitutionHoldingsId;
    }

    public void setOwningInstitutionHoldingsId(String owningInstitutionHoldingsId) {
        this.owningInstitutionHoldingsId = owningInstitutionHoldingsId;
    }

    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public InstitutionEntity getInstitutionEntity() {
        return institutionEntity;
    }

    public void setInstitutionEntity(InstitutionEntity institutionEntity) {
        this.institutionEntity = institutionEntity;
    }

    public List<BibliographicEntity> getBibliographicEntities() {
        return bibliographicEntities;
    }

    public void setBibliographicEntities(List<BibliographicEntity> bibliographicEntities) {
        this.bibliographicEntities = bibliographicEntities;
    }

    public List<ItemEntity> getItemEntities() {
        return itemEntities;
    }

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
