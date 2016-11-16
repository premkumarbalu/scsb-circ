package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by pvsubrah on 6/11/16.
 */

@Entity
@Table(name = "collection_group_t", schema = "recap", catalog = "")
public class CollectionGroupEntity implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "COLLECTION_GROUP_ID")
    private Integer collectionGroupId;

    @Column(name = "COLLECTION_GROUP_CODE")
    private String collectionGroupCode;

    @Column(name = "COLLECTION_GROUP_DESC")
    private String collectionGroupDescription;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATED_DATE")
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "LAST_UPDATED_DATE")
    private Date lastUpdatedDate;

    public Integer getCollectionGroupId() {
        return collectionGroupId;
    }

    public void setCollectionGroupId(Integer collectionGroupId) {
        this.collectionGroupId = collectionGroupId;
    }

    public String getCollectionGroupCode() {
        return collectionGroupCode;
    }

    public void setCollectionGroupCode(String collectionGroupCode) {
        this.collectionGroupCode = collectionGroupCode;
    }

    public String getCollectionGroupDescription() {
        return collectionGroupDescription;
    }

    public void setCollectionGroupDescription(String collectionGroupDescription) {
        this.collectionGroupDescription = collectionGroupDescription;
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
}
