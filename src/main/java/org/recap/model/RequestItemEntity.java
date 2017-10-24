package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

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

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "REQ_EXP_DATE")
    private Date requestExpirationDate;

    @Column(name = "CREATED_BY")
    private String createdBy;

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

    @Column(name = "NOTES")
    private String notes;

    @Column(name = "PATRON_ID")
    private String patronId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "REQUESTING_INST_ID", insertable = false, updatable = false)
    private InstitutionEntity institutionEntity;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "REQUEST_TYPE_ID", insertable = false, updatable = false)
    private RequestTypeEntity requestTypeEntity;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ITEM_ID", referencedColumnName = "ITEM_ID", insertable = false, updatable = false)
    private ItemEntity itemEntity;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "REQUEST_STATUS_ID", insertable = false, updatable = false)
    private RequestStatusEntity requestStatusEntity;

    @Column(name = "EMAIL_ID")
    private String emailId;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinTable(name = "bulk_request_t",
            joinColumns = @JoinColumn(name = "REQUEST_ID"),
            inverseJoinColumns = @JoinColumn(name = "BULK_REQUEST_ID"))
    private BulkRequestItemEntity bulkRequestItemEntity;

    /**
     * Gets request id.
     *
     * @return the request id
     */
    public Integer getRequestId() {
        return requestId;
    }

    /**
     * Sets request id.
     *
     * @param requestId the request id
     */
    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

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
     * Gets request type id.
     *
     * @return the request type id
     */
    public Integer getRequestTypeId() {
        return requestTypeId;
    }

    /**
     * Sets request type id.
     *
     * @param requestTypeId the request type id
     */
    public void setRequestTypeId(Integer requestTypeId) {
        this.requestTypeId = requestTypeId;
    }

    /**
     * Gets requesting institution id.
     *
     * @return the requesting institution id
     */
    public Integer getRequestingInstitutionId() {
        return requestingInstitutionId;
    }

    /**
     * Sets requesting institution id.
     *
     * @param requestingInstitutionId the requesting institution id
     */
    public void setRequestingInstitutionId(Integer requestingInstitutionId) {
        this.requestingInstitutionId = requestingInstitutionId;
    }

    /**
     * Gets request expiration date.
     *
     * @return the request expiration date
     */
    public Date getRequestExpirationDate() {
        return requestExpirationDate;
    }

    /**
     * Sets request expiration date.
     *
     * @param requestExpirationDate the request expiration date
     */
    public void setRequestExpirationDate(Date requestExpirationDate) {
        this.requestExpirationDate = requestExpirationDate;
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
     * Gets stop code.
     *
     * @return the stop code
     */
    public String getStopCode() {
        return stopCode;
    }

    /**
     * Sets stop code.
     *
     * @param stopCode the stop code
     */
    public void setStopCode(String stopCode) {
        this.stopCode = stopCode;
    }

    /**
     * Gets request status id.
     *
     * @return the request status id
     */
    public Integer getRequestStatusId() {
        return requestStatusId;
    }

    /**
     * Sets request status id.
     *
     * @param requestStatusId the request status id
     */
    public void setRequestStatusId(Integer requestStatusId) {
        this.requestStatusId = requestStatusId;
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
     * Gets request type entity.
     *
     * @return the request type entity
     */
    public RequestTypeEntity getRequestTypeEntity() {
        return requestTypeEntity;
    }

    /**
     * Sets request type entity.
     *
     * @param requestTypeEntity the request type entity
     */
    public void setRequestTypeEntity(RequestTypeEntity requestTypeEntity) {
        this.requestTypeEntity = requestTypeEntity;
    }

    /**
     * Gets item entity.
     *
     * @return the item entity
     */
    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    /**
     * Sets item entity.
     *
     * @param itemEntity the item entity
     */
    public void setItemEntity(ItemEntity itemEntity) {
        this.itemEntity = itemEntity;
    }

    /**
     * Gets request status entity.
     *
     * @return the request status entity
     */
    public RequestStatusEntity getRequestStatusEntity() {
        return requestStatusEntity;
    }

    /**
     * Sets request status entity.
     *
     * @param requestStatusEntity the request status entity
     */
    public void setRequestStatusEntity(RequestStatusEntity requestStatusEntity) {
        this.requestStatusEntity = requestStatusEntity;
    }

    /**
     * Gets notes.
     *
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets notes.
     *
     * @param notes the notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Gets patron id.
     *
     * @return the patron id
     */
    public String getPatronId() {
        return patronId;
    }

    /**
     * Sets patron id.
     *
     * @param patronId the patron id
     */
    public void setPatronId(String patronId) {
        this.patronId = patronId;
    }

    /**
     * Gets email id.
     *
     * @return the email id
     */
    public String getEmailId() {
        return emailId;
    }

    /**
     * Sets email id.
     *
     * @param emailId the email id
     */
    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public BulkRequestItemEntity getBulkRequestItemEntity() {
        return bulkRequestItemEntity;
    }

    public void setBulkRequestItemEntity(BulkRequestItemEntity bulkRequestItemEntity) {
        this.bulkRequestItemEntity = bulkRequestItemEntity;
    }
}
