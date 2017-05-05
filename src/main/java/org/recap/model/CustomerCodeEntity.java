package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Created by rajeshbabuk on 18/10/16.
 */
@Entity
@Table(name = "customer_code_t", schema = "recap", catalog = "")
public class CustomerCodeEntity implements Serializable, Comparable<CustomerCodeEntity> {

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

    @Column(name = "PWD_DELIVERY_RESTRICTIONS")
    private String pwdDeliveryRestrictions;

    @Column(name = "RECAP_DELIVERY_RESTRICTIONS")
    private String recapDeliveryRestrictions;

    @Column(name = "DELIVERY_RESTRICTIONS")
    private String deliveryRestrictions;

    @Column(name = "CIRC_DESK_LOCATION")
    private String pickupLocation;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "OWNING_INST_ID", insertable = false, updatable = false)
    private InstitutionEntity institutionEntity;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(name = "cross_partner_mapping_t", joinColumns = {
            @JoinColumn(name = "CUSTOMER_CODE_ID", referencedColumnName = "CUSTOMER_CODE_ID")},
            inverseJoinColumns = {
                    @JoinColumn(name = "DELIVERY_RESTRICTION_CROSS_PARTNER_ID", referencedColumnName = "DELIVERY_RESTRICTION_CROSS_PARTNER_ID")})
    private List<DeliveryRestrictionEntity> deliveryRestrictionEntityList;


    public Integer getCustomerCodeId() {
        return customerCodeId;
    }

    public void setCustomerCodeId(Integer customerCodeId) {
        this.customerCodeId = customerCodeId;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getOwningInstitutionId() {
        return owningInstitutionId;
    }

    public void setOwningInstitutionId(Integer owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }

    public String getDeliveryRestrictions() {
        return deliveryRestrictions;
    }

    public void setDeliveryRestrictions(String deliveryRestrictions) {
        this.deliveryRestrictions = deliveryRestrictions;
    }

    public InstitutionEntity getInstitutionEntity() {
        return institutionEntity;
    }

    public void setInstitutionEntity(InstitutionEntity institutionEntity) {
        this.institutionEntity = institutionEntity;
    }

    @Override
    public int compareTo(CustomerCodeEntity customerCodeEntity) {
        if (null != this.getDescription() && null !=  customerCodeEntity && null != customerCodeEntity.getDescription()) {
            return this.getDescription().compareTo(customerCodeEntity.getDescription());
        }
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;

        CustomerCodeEntity customerCodeEntity = (CustomerCodeEntity) object;

        if (customerCodeId != null ? !customerCodeId.equals(customerCodeEntity.customerCodeId) : customerCodeEntity.customerCodeId != null)
            return false;
        if (customerCode != null ? !customerCode.equals(customerCodeEntity.customerCode) : customerCodeEntity.customerCode != null)
            return false;
        if (description != null ? !description.equals(customerCodeEntity.description) : customerCodeEntity.description != null)
            return false;
        return owningInstitutionId != null ? owningInstitutionId.equals(customerCodeEntity.owningInstitutionId) : customerCodeEntity.owningInstitutionId == null;

    }

    @Override
    public int hashCode() {
        int result = customerCodeId != null ? customerCodeId.hashCode() : 0;
        result = 31 * result + (customerCode != null ? customerCode.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (owningInstitutionId != null ? owningInstitutionId.hashCode() : 0);
        return result;
    }

    public List<DeliveryRestrictionEntity> getDeliveryRestrictionEntityList() {
        return deliveryRestrictionEntityList;
    }

    public void setDeliveryRestrictionEntityList(List<DeliveryRestrictionEntity> deliveryRestrictionEntityList) {
        this.deliveryRestrictionEntityList = deliveryRestrictionEntityList;
    }

    public String getPwdDeliveryRestrictions() {
        return pwdDeliveryRestrictions;
    }

    public void setPwdDeliveryRestrictions(String pwdDeliveryRestrictions) {
        this.pwdDeliveryRestrictions = pwdDeliveryRestrictions;
    }

    public String getRecapDeliveryRestrictions() {
        return recapDeliveryRestrictions;
    }

    public void setRecapDeliveryRestrictions(String recapDeliveryRestrictions) {
        this.recapDeliveryRestrictions = recapDeliveryRestrictions;
    }

    public String getPickupLocation() {
        return pickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
}
