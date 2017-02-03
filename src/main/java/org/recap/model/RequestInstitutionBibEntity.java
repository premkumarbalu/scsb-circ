package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by pvsubrah on 6/11/16.
 */

@Entity
@Table(name = "request_inst_bib_t", schema = "recap", catalog = "")
@IdClass(RequestInstitutionBibPK.class)
public class RequestInstitutionBibEntity implements Serializable{

    @Id
    @Column(name = "ITEM_ID")
    private Integer itemId;

    @Column(name = "OWNING_INST_BIB_ID")
    private String owningInstitutionBibId;

    @Id
    @Column(name = "OWNING_INST_ID")
    private Integer owningInstitutionId;

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public String getOwningInstitutionBibId() {
        return owningInstitutionBibId;
    }

    public void setOwningInstitutionBibId(String owningInstitutionBibId) {
        this.owningInstitutionBibId = owningInstitutionBibId;
    }

    public Integer getOwningInstitutionId() {
        return owningInstitutionId;
    }

    public void setOwningInstitutionId(Integer owningInstitutionId) {
        this.owningInstitutionId = owningInstitutionId;
    }
}
