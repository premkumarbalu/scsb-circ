package org.recap.model.deAccession;

import java.util.List;

/**
 * Created by chenchulakshmig on 3/10/16.
 */
public class DeAccessionDBResponseEntity {

    private String barcode;

    private String status;

    private String reasonForFailure;

    private String institutionCode;

    private String collectionGroupCode;

    private List<String> owningInstitutionBibIds;

    private Integer itemId;

    private List<Integer> bibliographicIds;

    private List<Integer> holdingIds;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReasonForFailure() {
        return reasonForFailure;
    }

    public void setReasonForFailure(String reasonForFailure) {
        this.reasonForFailure = reasonForFailure;
    }

    public String getInstitutionCode() {
        return institutionCode;
    }

    public void setInstitutionCode(String institutionCode) {
        this.institutionCode = institutionCode;
    }

    public String getCollectionGroupCode() {
        return collectionGroupCode;
    }

    public void setCollectionGroupCode(String collectionGroupCode) {
        this.collectionGroupCode = collectionGroupCode;
    }

    public List<String> getOwningInstitutionBibIds() {
        return owningInstitutionBibIds;
    }

    public void setOwningInstitutionBibIds(List<String> owningInstitutionBibIds) {
        this.owningInstitutionBibIds = owningInstitutionBibIds;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public List<Integer> getBibliographicIds() {
        return bibliographicIds;
    }

    public void setBibliographicIds(List<Integer> bibliographicIds) {
        this.bibliographicIds = bibliographicIds;
    }

    public List<Integer> getHoldingIds() {
        return holdingIds;
    }

    public void setHoldingIds(List<Integer> holdingIds) {
        this.holdingIds = holdingIds;
    }
}
