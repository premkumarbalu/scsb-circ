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

    public Integer getItemStatusId() {
        return itemStatusId;
    }

    public void setItemStatusId(Integer itemStatusId) {
        this.itemStatusId = itemStatusId;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusDescription() {
        return statusDescription;
    }

    public void setStatusDescription(String statusDescription) {
        this.statusDescription = statusDescription;
    }
}
