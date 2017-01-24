package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by pvsubrah on 6/11/16.
 */

@Entity
@Table(name = "request_item_t", schema = "recap", catalog = "")
public class RequestItemEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "REQUEST_ID")
    private Integer requestId;

    @Column(name = "ITEM_ID")
    private Integer itemId;

    @Column(name = "REQUEST_TYPE_ID")
    private Integer requestTypeId;

    @Column(name = "REQUESTING_INST_ID")
    private Integer requestingInstitutionId;

    @Column(name = "PATRON_ID")
    private Integer patronId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "REQ_EXP_DATE")
    private Date requestExpirationDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_UPDATED_DATE")
    private Date lastUpdatedDate;

    @Column(name = "STOP_CODE")
    private String stopCode;

    @Column(name = "REQUEST_STATUS_ID")
    private Integer requestStatusId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "REQUESTING_INST_ID", insertable = false, updatable = false)
    private InstitutionEntity institutionEntity;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "REQUEST_TYPE_ID", insertable = false, updatable = false)
    private RequestTypeEntity requestTypeEntity;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "PATRON_ID", insertable = false, updatable = false)
    private PatronEntity patronEntity;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ITEM_ID", referencedColumnName = "ITEM_ID", insertable = false, updatable = false)
    private ItemEntity itemEntity;

    @OneToMany(mappedBy = "requestItemEntity", cascade = CascadeType.ALL)
    private List<NotesEntity> notesEntities;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "REQUEST_STATUS_ID", insertable = false, updatable = false)
    private RequestStatusEntity requestStatusEntity;

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getRequestTypeId() {
        return requestTypeId;
    }

    public void setRequestTypeId(Integer requestTypeId) {
        this.requestTypeId = requestTypeId;
    }

    public Integer getRequestingInstitutionId() {
        return requestingInstitutionId;
    }

    public void setRequestingInstitutionId(Integer requestingInstitutionId) {
        this.requestingInstitutionId = requestingInstitutionId;
    }

    public Integer getPatronId() {
        return patronId;
    }

    public void setPatronId(Integer patronId) {
        this.patronId = patronId;
    }

    public Date getRequestExpirationDate() {
        return requestExpirationDate;
    }

    public void setRequestExpirationDate(Date requestExpirationDate) {
        this.requestExpirationDate = requestExpirationDate;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public String getStopCode() {
        return stopCode;
    }

    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    public Integer getRequestStatusId() {
        return requestStatusId;
    }

    public void setRequestStatusId(Integer requestStatusId) {
        this.requestStatusId = requestStatusId;
    }

    public InstitutionEntity getInstitutionEntity() {
        return institutionEntity;
    }

    public void setInstitutionEntity(InstitutionEntity institutionEntity) {
        this.institutionEntity = institutionEntity;
    }

    public RequestTypeEntity getRequestTypeEntity() {
        return requestTypeEntity;
    }

    public void setRequestTypeEntity(RequestTypeEntity requestTypeEntity) {
        this.requestTypeEntity = requestTypeEntity;
    }

    public PatronEntity getPatronEntity() {
        return patronEntity;
    }

    public void setPatronEntity(PatronEntity patronEntity) {
        this.patronEntity = patronEntity;
    }

    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    public void setItemEntity(ItemEntity itemEntity) {
        this.itemEntity = itemEntity;
    }

    public List<NotesEntity> getNotesEntities() {
        return notesEntities;
    }

    public void setNotesEntities(List<NotesEntity> notesEntities) {
        this.notesEntities = notesEntities;
    }

    public RequestStatusEntity getRequestStatusEntity() {
        return requestStatusEntity;
    }

    public void setRequestStatusEntity(RequestStatusEntity requestStatusEntity) {
        this.requestStatusEntity = requestStatusEntity;
    }
}
