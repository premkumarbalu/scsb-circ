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

    public Integer getChangeLogId() {
        return changeLogId;
    }

    public void setChangeLogId(Integer changeLogId) {
        this.changeLogId = changeLogId;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
