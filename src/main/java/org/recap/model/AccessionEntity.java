package org.recap.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by rajeshbabuk on 8/5/17.
 */
@Entity
@Table(name = "ACCESSION_T", schema = "recap", catalog = "")
public class AccessionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ACCESSION_ID")
    private Integer accessionId;

    @Column(name = "ACCESSION_REQUEST")
    private String accessionRequest;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Column(name = "ACCESSION_STATUS")
    private String accessionStatus;

    public Integer getAccessionId() {
        return accessionId;
    }

    public void setAccessionId(Integer accessionId) {
        this.accessionId = accessionId;
    }

    public String getAccessionRequest() {
        return accessionRequest;
    }

    public void setAccessionRequest(String accessionRequest) {
        this.accessionRequest = accessionRequest;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getAccessionStatus() {
        return accessionStatus;
    }

    public void setAccessionStatus(String accessionStatus) {
        this.accessionStatus = accessionStatus;
    }
}
