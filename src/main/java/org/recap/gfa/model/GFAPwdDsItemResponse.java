package org.recap.gfa.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 21/2/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "prods:hasChanges",
        "ttitem",
        "prods:before"
})
public class GFAPwdDsItemResponse {

    @JsonProperty("prods:hasChanges")
    private Boolean prodsHasChanges;
    @JsonProperty("ttitem")
    private List<GFAPwdTtItemResponse> ttitem = null;
    @JsonProperty("prods:before")
    private ProdsBefore prodsBefore;

    @JsonProperty("prods:hasChanges")
    public Boolean getProdsHasChanges() {
        return prodsHasChanges;
    }

    @JsonProperty("prods:hasChanges")
    public void setProdsHasChanges(Boolean prodsHasChanges) {
        this.prodsHasChanges = prodsHasChanges;
    }

    @JsonProperty("ttitem")
    public List<GFAPwdTtItemResponse> getTtitem() {
        return ttitem;
    }

    @JsonProperty("ttitem")
    public void setTtitem(List<GFAPwdTtItemResponse> ttitem) {
        this.ttitem = ttitem;
    }

    @JsonProperty("prods:before")
    public ProdsBefore getProdsBefore() {
        return prodsBefore;
    }

    @JsonProperty("prods:before")
    public void setProdsBefore(ProdsBefore prodsBefore) {
        this.prodsBefore = prodsBefore;
    }

}