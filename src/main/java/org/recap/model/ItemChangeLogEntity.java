package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by rajeshbabuk on 18/10/16.
 */
@Entity
@Table(name = "ITEM_CHANGE_LOG_T", schema = "recap", catalog = "")
public class ItemChangeLogEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ITEM_CHANGE_LOG_ID")
    private Integer changeLogId;

    @Column(name = "UPDATED_BY")
    private String updatedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATED_DATE")
    private Date updatedDate;

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
     * Gets updated date.
     *
     * @return the updated date
     */
    public Date getUpdatedDate() {
        return updatedDate;
    }

    /**
     * Sets updated date.
     *
     * @param updatedDate the updated date
     */
    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
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
