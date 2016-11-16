package org.recap.model;

import java.io.Serializable;

/**
 * Created by angelind on 29/7/16.
 */
public class ItemPK implements Serializable {
    private Integer owningInstitutionId;
    private String owningInstitutionItemId;


    public ItemPK(){

    }

    public ItemPK(Integer owningInstitutionId, String owningInstitutionItemId) {
        this.owningInstitutionId = owningInstitutionId;
        this.owningInstitutionItemId = owningInstitutionItemId;
    }

    public Integer getOwningInstitutionId() {
        return owningInstitutionId;
    }

    public void setOwningInstitutionId(Integer owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }

    public String getOwningInstitutionItemId() {
        return owningInstitutionItemId;
    }

    public void setOwningInstitutionItemId(String owningInstitutionItemId) {
        this.owningInstitutionItemId = owningInstitutionItemId;
    }

    @Override
    public int hashCode() {
        return Integer.valueOf(owningInstitutionId.toString()+owningInstitutionItemId);
    }

    @Override
    public boolean equals(Object obj) {
        ItemPK itemPK  = (ItemPK) obj;
        if(itemPK.getOwningInstitutionId().equals(owningInstitutionId) && itemPK.getOwningInstitutionItemId().equals(owningInstitutionItemId)){
            return true;
        }

        return false;
    }
}
