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

    /**
     * Gets prods has changes.
     *
     * @return the prods has changes
     */
    @JsonProperty("prods:hasChanges")
    public Boolean getProdsHasChanges() {
        return prodsHasChanges;
    }

    /**
     * Sets prods has changes.
     *
     * @param prodsHasChanges the prods has changes
     */
    @JsonProperty("prods:hasChanges")
    public void setProdsHasChanges(Boolean prodsHasChanges) {
        this.prodsHasChanges = prodsHasChanges;
    }

    /**
     * Gets ttitem.
     *
     * @return the ttitem
     */
    @JsonProperty("ttitem")
    public List<GFAPwdTtItemResponse> getTtitem() {
        return ttitem;
    }

    /**
     * Sets ttitem.
     *
     * @param ttitem the ttitem
     */
    @JsonProperty("ttitem")
    public void setTtitem(List<GFAPwdTtItemResponse> ttitem) {
        this.ttitem = ttitem;
    }

    /**
     * Gets prods before.
     *
     * @return the prods before
     */
    @JsonProperty("prods:before")
    public ProdsBefore getProdsBefore() {
        return prodsBefore;
    }

    /**
     * Sets prods before.
     *
     * @param prodsBefore the prods before
     */
    @JsonProperty("prods:before")
    public void setProdsBefore(ProdsBefore prodsBefore) {
        this.prodsBefore = prodsBefore;
    }

}