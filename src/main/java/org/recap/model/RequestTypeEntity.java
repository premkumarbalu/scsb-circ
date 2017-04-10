package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by rajeshbabuk on 26/10/16.
 */
@Entity
@Table(name = "request_type_t", schema = "recap", catalog = "")
public class RequestTypeEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "REQUEST_TYPE_ID")
    private Integer requestTypeId;

    @Column(name = "REQUEST_TYPE_CODE")
    private String requestTypeCode;

    @Column(name = "REQUEST_TYPE_DESC")
    private String requestTypeDesc;

    /**
     * Gets request type id.
     *
     * @return the request type id
     */
    public Integer getRequestTypeId() {
        return requestTypeId;
    }

    /**
     * Sets request type id.
     *
     * @param requestTypeId the request type id
     */
    public void setRequestTypeId(Integer requestTypeId) {
        this.requestTypeId = requestTypeId;
    }

    /**
     * Gets request type code.
     *
     * @return the request type code
     */
    public String getRequestTypeCode() {
        return requestTypeCode;
    }

    /**
     * Sets request type code.
     *
     * @param requestTypeCode the request type code
     */
    public void setRequestTypeCode(String requestTypeCode) {
        this.requestTypeCode = requestTypeCode;
    }

    /**
     * Gets request type desc.
     *
     * @return the request type desc
     */
    public String getRequestTypeDesc() {
        return requestTypeDesc;
    }

    /**
     * Sets request type desc.
     *
     * @param requestTypeDesc the request type desc
     */
    public void setRequestTypeDesc(String requestTypeDesc) {
        this.requestTypeDesc = requestTypeDesc;
    }
}
