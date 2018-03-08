package org.recap.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by akulak on 26/2/18 .
 */
@Entity
@Table(name = "deaccession_item_change_log_t", schema = "recap", catalog = "")
public class DeaccessionItemChangeLog {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "CHANGE_LOG_ID")
    private Integer changeLogId;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(name = "OPERATION_TYPE")
    private String operationType;

    @Column(name = "RECORD_ID")
    private Integer recordId;

    @Column(name = "NOTES")
    private String notes;

    /**
     * Gets change log id.
     *
     * @return the change log id
     */
    public Integer getChangeLogId() {
        return changeLogId;
    }

    /**
     * Sets change log id.
     *
     * @param changeLogId the change log id
     */
    public void setChangeLogId(Integer changeLogId) {
        this.changeLogId = changeLogId;
    }

    /**
     * Gets updated by.
     *
     * @return the updated by
     */
    public String getUpdatedBy() {
        return updatedBy;
    }

    /**
     * Sets updated by.
     *
     * @param updatedBy the updated by
     */
    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
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
     * Gets operation type.
     *
     * @return the operation type
     */
    public String getOperationType() {
        return operationType;
    }

    /**
     * Sets operation type.
     *
     * @param operationType the operation type
     */
    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    /**
     * Gets record id.
     *
     * @return the record id
     */
    public Integer getRecordId() {
        return recordId;
    }

    /**
     * Sets record id.
     *
     * @param recordId the record id
     */
    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
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
}
