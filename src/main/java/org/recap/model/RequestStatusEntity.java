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

    /**
     * Gets request status id.
     *
     * @return the request status id
     */
    public Integer getRequestStatusId() {
        return requestStatusId;
    }

    /**
     * Sets request status id.
     *
     * @param requestStatusId the request status id
     */
    public void setRequestStatusId(Integer requestStatusId) {
        this.requestStatusId = requestStatusId;
    }

    /**
     * Gets request status code.
     *
     * @return the request status code
     */
    public String getRequestStatusCode() {
        return requestStatusCode;
    }

    /**
     * Sets request status code.
     *
     * @param requestStatusCode the request status code
     */
    public void setRequestStatusCode(String requestStatusCode) {
        this.requestStatusCode = requestStatusCode;
    }

    /**
     * Gets request status description.
     *
     * @return the request status description
     */
    public String getRequestStatusDescription() {
        return requestStatusDescription;
    }

    /**
     * Sets request status description.
     *
     * @param requestStatusDescription the request status description
     */
    public void setRequestStatusDescription(String requestStatusDescription) {
        this.requestStatusDescription = requestStatusDescription;
    }
}
