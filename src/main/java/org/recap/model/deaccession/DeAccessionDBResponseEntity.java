package org.recap.model.deaccession;

import java.util.List;

/**
 * Created by chenchulakshmig on 3/10/16.
 */
public class DeAccessionDBResponseEntity {

    private String barcode;

    private String deliveryLocation;

    private String itemStatus;

    private String customerCode;

    private String status;

    private String reasonForFailure;

    private String institutionCode;

    private String collectionGroupCode;

    private List<String> owningInstitutionBibIds;

    private Integer itemId;

    private List<Integer> bibliographicIds;

    private List<Integer> holdingIds;

    /**
     * Gets barcode.
     *
     * @return the barcode
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Sets barcode.
     *
     * @param barcode the barcode
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    /**
     * Gets delivery location.
     *
     * @return the delivery location
     */
    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    /**
     * Sets delivery location.
     *
     * @param deliveryLocation the delivery location
     */
    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    /**
     * Gets item status.
     *
     * @return the item status
     */
    public String getItemStatus() {
        return itemStatus;
    }

    /**
     * Sets item status.
     *
     * @param itemStatus the item status
     */
    public void setItemStatus(String itemStatus) {
        this.itemStatus = itemStatus;
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
     * Gets status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Gets reason for failure.
     *
     * @return the reason for failure
     */
    public String getReasonForFailure() {
        return reasonForFailure;
    }

    /**
     * Sets reason for failure.
     *
     * @param reasonForFailure the reason for failure
     */
    public void setReasonForFailure(String reasonForFailure) {
        this.reasonForFailure = reasonForFailure;
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
     * Gets owning institution bib ids.
     *
     * @return the owning institution bib ids
     */
    public List<String> getOwningInstitutionBibIds() {
        return owningInstitutionBibIds;
    }

    /**
     * Sets owning institution bib ids.
     *
     * @param owningInstitutionBibIds the owning institution bib ids
     */
    public void setOwningInstitutionBibIds(List<String> owningInstitutionBibIds) {
        this.owningInstitutionBibIds = owningInstitutionBibIds;
    }

    /**
     * Gets item id.
     *
     * @return the item id
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * Sets item id.
     *
     * @param itemId the item id
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * Gets bibliographic ids.
     *
     * @return the bibliographic ids
     */
    public List<Integer> getBibliographicIds() {
        return bibliographicIds;
    }

    /**
     * Sets bibliographic ids.
     *
     * @param bibliographicIds the bibliographic ids
     */
    public void setBibliographicIds(List<Integer> bibliographicIds) {
        this.bibliographicIds = bibliographicIds;
    }

    /**
     * Gets holding ids.
     *
     * @return the holding ids
     */
    public List<Integer> getHoldingIds() {
        return holdingIds;
    }

    /**
     * Sets holding ids.
     *
     * @param holdingIds the holding ids
     */
    public void setHoldingIds(List<Integer> holdingIds) {
        this.holdingIds = holdingIds;
    }
}
