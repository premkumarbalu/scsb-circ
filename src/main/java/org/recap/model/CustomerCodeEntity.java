package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by rajeshbabuk on 18/10/16.
 */
@Entity
@Table(name = "customer_code_t", schema = "recap", catalog = "")
public class CustomerCodeEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "CUSTOMER_CODE_ID")
    private Integer customerCodeId;

    @Column(name = "CUSTOMER_CODE")
    private String customerCode;

    @Column(name = "DESCRIPTION")
    private String description;

    @Column(name = "OWNING_INST_ID")
    private Integer owningInstitutionId;

    @Column(name = "DELIVERY_RESTRICTIONS")
    private String deliveryRestrictions;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "OWNING_INST_ID", insertable = false, updatable = false)
    private InstitutionEntity institutionEntity;

    /**
     * Gets customer code id.
     *
     * @return the customer code id
     */
    public Integer getCustomerCodeId() {
        return customerCodeId;
    }

    /**
     * Sets customer code id.
     *
     * @param customerCodeId the customer code id
     */
    public void setCustomerCodeId(Integer customerCodeId) {
        this.customerCodeId = customerCodeId;
    }

    /**
     * Gets customer code.
     *
     * @return the customer code
     */
    public String getCustomerCode() {
        return customerCode;
    }

    /**
     * Sets customer code.
     *
     * @param customerCode the customer code
     */
    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets owning institution id.
     *
     * @return the owning institution id
     */
    public Integer getOwningInstitutionId() {
        return owningInstitutionId;
    }

    /**
     * Sets owning institution id.
     *
     * @param owningInstitutionId the owning institution id
     */
    public void setOwningInstitutionId(Integer owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }

    /**
     * Gets delivery restrictions.
     *
     * @return the delivery restrictions
     */
    public String getDeliveryRestrictions() {
        return deliveryRestrictions;
    }

    /**
     * Sets delivery restrictions.
     *
     * @param deliveryRestrictions the delivery restrictions
     */
    public void setDeliveryRestrictions(String deliveryRestrictions) {
        this.deliveryRestrictions = deliveryRestrictions;
    }

    /**
     * Gets institution entity.
     *
     * @return the institution entity
     */
    public InstitutionEntity getInstitutionEntity() {
        return institutionEntity;
    }

    /**
     * Sets institution entity.
     *
     * @param institutionEntity the institution entity
     */
    public void setInstitutionEntity(InstitutionEntity institutionEntity) {
        this.institutionEntity = institutionEntity;
    }
}
