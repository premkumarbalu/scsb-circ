package org.recap.model;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * Created by rajeshbabuk on 10/10/17.
 */
@Entity
@Table(name = "bulk_request_item_t", schema = "recap", catalog = "")
public class BulkRequestItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "BULK_REQUEST_ID")
    private Integer bulkRequestId;

    @Column(name = "BULK_REQUEST_NAME")
    private String bulkRequestName;

    @Column(name = "BULK_REQUEST_FILE_NAME")
    private String bulkRequestFileName;

    @Lob
    @Column(name = "BULK_REQUEST_FILE_DATA")
    private byte[] bulkRequestFileData;

    @Column(name = "REQUESTING_INST_ID")
    private Integer requestingInstitutionId;

    @Column(name = "REQUEST_STATUS")
    private String bulkRequestStatus;

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

    @Column(name = "NOTES")
    private String notes;

    @Column(name = "PATRON_ID")
    private String patronId;

    @Column(name = "EMAIL_ID")
    private String emailId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "REQUESTING_INST_ID", insertable = false, updatable = false)
    private InstitutionEntity institutionEntity;

    @OneToMany(cascade = CascadeType.MERGE)
    @JoinTable(name = "bulk_request_t",
            joinColumns = @JoinColumn(name = "BULK_REQUEST_ID"),
            inverseJoinColumns = @JoinColumn(name = "REQUEST_ID"))
    private List<RequestItemEntity> requestItemEntities;

    public Integer getBulkRequestId() {
        return bulkRequestId;
    }

    public void setBulkRequestId(Integer bulkRequestId) {
        this.bulkRequestId = bulkRequestId;
    }

    public String getBulkRequestName() {
        return bulkRequestName;
    }

    public void setBulkRequestName(String bulkRequestName) {
        this.bulkRequestName = bulkRequestName;
    }

    public String getBulkRequestFileName() {
        return bulkRequestFileName;
    }

    public void setBulkRequestFileName(String bulkRequestFileName) {
        this.bulkRequestFileName = bulkRequestFileName;
    }

    public byte[] getBulkRequestFileData() {
        return bulkRequestFileData;
    }

    public void setBulkRequestFileData(byte[] bulkRequestFileData) {
        this.bulkRequestFileData = bulkRequestFileData;
    }

    public Integer getRequestingInstitutionId() {
        return requestingInstitutionId;
    }

    public void setRequestingInstitutionId(Integer requestingInstitutionId) {
        this.requestingInstitutionId = requestingInstitutionId;
    }

    public String getBulkRequestStatus() {
        return bulkRequestStatus;
    }

    public void setBulkRequestStatus(String bulkRequestStatus) {
        this.bulkRequestStatus = bulkRequestStatus;
    }

    public InstitutionEntity getInstitutionEntity() {
        return institutionEntity;
    }

    public void setInstitutionEntity(InstitutionEntity institutionEntity) {
        this.institutionEntity = institutionEntity;
    }

    public List<RequestItemEntity> getRequestItemEntities() {
        return requestItemEntities;
    }

    public void setRequestItemEntities(List<RequestItemEntity> requestItemEntities) {
        this.requestItemEntities = requestItemEntities;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
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

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getPatronId() {
        return patronId;
    }

    public void setPatronId(String patronId) {
        this.patronId = patronId;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }
}
