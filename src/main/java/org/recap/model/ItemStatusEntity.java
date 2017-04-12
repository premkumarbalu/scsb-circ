package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by pvsubrah on 6/11/16.
 */
@Entity
@Table(name = "item_status_t", schema = "recap", catalog = "")
public class ItemStatusEntity implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ITEM_STATUS_ID")
    private Integer itemStatusId;

    @Column(name = "STATUS_CODE")
    private String statusCode;

    @Column(name = "STATUS_DESC")
    private String statusDescription;

    /**
     * Gets item status id.
     *
     * @return the item status id
     */
    public Integer getItemStatusId() {
        return itemStatusId;
    }

    /**
     * Sets item status id.
     *
     * @param itemStatusId the item status id
     */
    public void setItemStatusId(Integer itemStatusId) {
        this.itemStatusId = itemStatusId;
    }

    /**
     * Gets status code.
     *
     * @return the status code
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * Sets status code.
     *
     * @param statusCode the status code
     */
    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Gets status description.
     *
     * @return the status description
     */
    public String getStatusDescription() {
        return statusDescription;
    }

    /**
     * Sets status description.
     *
     * @param statusDescription the status description
     */
    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }
}
