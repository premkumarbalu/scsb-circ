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

    /**
     * Gets collection group id.
     *
     * @return the collection group id
     */
    public Integer getCollectionGroupId() {
        return collectionGroupId;
    }

    /**
     * Sets collection group id.
     *
     * @param collectionGroupId the collection group id
     */
    public void setCollectionGroupId(Integer collectionGroupId) {
        this.collectionGroupId = collectionGroupId;
    }

    /**
     * Gets collection group code.
     *
     * @return the collection group code
     */
    public String getCollectionGroupCode() {
        return collectionGroupCode;
    }

    /**
     * Sets collection group code.
     *
     * @param collectionGroupCode the collection group code
     */
    public void setCollectionGroupCode(String collectionGroupCode) {
        this.collectionGroupCode = collectionGroupCode;
    }

    /**
     * Gets collection group description.
     *
     * @return the collection group description
     */
    public String getCollectionGroupDescription() {
        return collectionGroupDescription;
    }

    /**
     * Sets collection group description.
     *
     * @param collectionGroupDescription the collection group description
     */
    public void setCollectionGroupDescription(String collectionGroupDescription) {
        this.collectionGroupDescription = collectionGroupDescription;
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
}
