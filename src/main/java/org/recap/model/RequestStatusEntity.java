package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by pvsubrah on 6/11/16.
 */

@Entity
@Table(name = "request_item_status_t", schema = "recap", catalog = "")
public class RequestStatusEntity implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "REQUEST_STATUS_ID")
    private Integer requestStatusId;

    @Column(name = "REQUEST_STATUS_CODE")
    private String requestStatusCode;

    @Column(name = "REQUEST_STATUS_DESC")
    private String requestStatusDescription;

    public Integer getRequestStatusId() {
        return requestStatusId;
    }

    public void setRequestStatusId(Integer requestStatusId) {
        this.requestStatusId = requestStatusId;
    }

    public String getRequestStatusCode() {
        return requestStatusCode;
    }

    public void setRequestStatusCode(String requestStatusCode) {
        this.requestStatusCode = requestStatusCode;
    }

    public String getRequestStatusDescription() {
        return requestStatusDescription;
    }

    public void setRequestStatusDescription(String requestStatusDescription) {
        this.requestStatusDescription = requestStatusDescription;
    }
}
