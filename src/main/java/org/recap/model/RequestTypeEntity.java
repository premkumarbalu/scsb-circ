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

    public Integer getRequestTypeId() {
        return requestTypeId;
    }

    public void setRequestTypeId(Integer requestTypeId) {
        this.requestTypeId = requestTypeId;
    }

    public String getRequestTypeCode() {
        return requestTypeCode;
    }

    public void setRequestTypeCode(String requestTypeCode) {
        this.requestTypeCode = requestTypeCode;
    }

    public String getRequestTypeDesc() {
        return requestTypeDesc;
    }

    public void setRequestTypeDesc(String requestTypeDesc) {
        this.requestTypeDesc = requestTypeDesc;
    }
}
