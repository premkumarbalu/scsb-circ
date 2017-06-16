package org.recap.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by rajeshbabuk on 8/5/17.
 */
@Entity
@Table(name = "DELETED_RECORDS_T", schema = "recap", catalog = "")
public class DeletedRecordsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "DELETED_RECORDS_ID")
    private Integer deletedRecordsId;

    @Column(name = "RECORDS_TABLE")
    private String Records_Table;

    @Column(name = "RECORDS_PRIMARY_KEY")
    private String recordsPrimaryKey;

    @Column(name = "DELETED_REPORTED_STATUS")
    private String deletedReportedStatus;

    @Column(name = "DELETED_BY")
    private String deletedBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DELETED_DATE")
    private Date deletedDate;

    @Column(name = "RECORDS_LOG")
    private String recordsLog;

    public Integer getDeletedRecordsId() {
        return deletedRecordsId;
    }

    public void setDeletedRecordsId(Integer deletedRecordsId) {
        this.deletedRecordsId = deletedRecordsId;
    }

    public String getRecords_Table() {
        return Records_Table;
    }

    public void setRecords_Table(String records_Table) {
        Records_Table = records_Table;
    }

    public String getRecordsPrimaryKey() {
        return recordsPrimaryKey;
    }

    public void setRecordsPrimaryKey(String recordsPrimaryKey) {
        this.recordsPrimaryKey = recordsPrimaryKey;
    }

    public String getDeletedReportedStatus() {
        return deletedReportedStatus;
    }

    public void setDeletedReportedStatus(String deletedReportedStatus) {
        this.deletedReportedStatus = deletedReportedStatus;
    }

    public String getDeletedBy() {
        return deletedBy;
    }

    public void setDeletedBy(String deletedBy) {
        this.deletedBy = deletedBy;
    }

    public Date getDeletedDate() {
        return deletedDate;
    }

    public void setDeletedDate(Date deletedDate) {
        this.deletedDate = deletedDate;
    }

    public String getRecordsLog() {
        return recordsLog;
    }

    public void setRecordsLog(String recordsLog) {
        this.recordsLog = recordsLog;
    }
}
