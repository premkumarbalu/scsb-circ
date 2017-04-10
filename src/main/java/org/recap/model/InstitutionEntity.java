package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by pvsubrah on 6/11/16.
 */
@Entity
@Table(name = "institution_t", schema = "recap", catalog = "")
public class InstitutionEntity implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "INSTITUTION_ID")
    private Integer institutionId;

    @Column(name = "INSTITUTION_CODE")
    private String institutionCode;

    @Column(name = "INSTITUTION_NAME")
    private String institutionName;

    /**
     * Gets institution id.
     *
     * @return the institution id
     */
    public Integer getInstitutionId() {
        return institutionId;
    }

    /**
     * Sets institution id.
     *
     * @param institutionId the institution id
     */
    public void setInstitutionId(Integer institutionId) {
        this.institutionId = institutionId;
    }

    /**
     * Gets institution code.
     *
     * @return the institution code
     */
    public String getInstitutionCode() {
        return institutionCode;
    }

    /**
     * Sets institution code.
     *
     * @param institutionCode the institution code
     */
    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
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
}
