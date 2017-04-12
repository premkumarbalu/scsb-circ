package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by peris on 8/10/16.
 */
@Entity
@Table(name = "REPORT_T", schema = "RECAP", catalog = "")
public class ReportEntity implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "RECORD_NUM")
    private Integer recordNumber;

    @Column(name = "FILE_NAME")
    private String fileName;

    @Column(name = "TYPE")
    private String type;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(name = "INSTITUTION_NAME")
    private String institutionName;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "RECORD_NUM")
    private List<ReportDataEntity> reportDataEntities = new ArrayList<>();

    /**
     * Gets file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * Sets file name.
     *
     * @param fileName the file name
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    /**
     * Gets record number.
     *
     * @return the record number
     */
    public Integer getRecordNumber() {
        return recordNumber;
    }

    /**
     * Sets record number.
     *
     * @param recordNumber the record number
     */
    public void setRecordNumber(Integer recordNumber) {
        this.recordNumber = recordNumber;
    }

    /**
     * Gets report data entities.
     *
     * @return the report data entities
     */
    public List<ReportDataEntity> getReportDataEntities() {
        return reportDataEntities;
    }

    /**
     * Sets report data entities.
     *
     * @param reportDataEntities the report data entities
     */
    public void setReportDataEntities(List<ReportDataEntity> reportDataEntities) {
        this.reportDataEntities = reportDataEntities;
    }

    /**
     * Gets type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets type.
     *
     * @param type the type
     */
    public void setType(String type) {
        this.type = type;
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
     * Gets institution name.
     *
     * @return the institution name
     */
    public String getInstitutionName() {
        return institutionName;
    }

    /**
     * Sets institution name.
     *
     * @param institutionName the institution name
     */
    public void setInstitutionName(String institutionName) {
        this.institutionName = institutionName;
    }

    /**
     * Add all.
     *
     * @param reportDataEntities the report data entities
     */
    public void addAll(List<ReportDataEntity> reportDataEntities) {
        if(null == getReportDataEntities()){
            reportDataEntities = new ArrayList<>();
        }
        this.reportDataEntities.addAll(reportDataEntities);
    }
}
