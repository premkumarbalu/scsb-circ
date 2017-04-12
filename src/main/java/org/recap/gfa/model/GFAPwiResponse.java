package org.recap.gfa.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 21/2/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "dsitem"
})
public class GFAPwiResponse {

    @JsonProperty("dsitem")
    private GFAPwiDsItemResponse dsitem;

    /**
     * Gets dsitem.
     *
     * @return the dsitem
     */
    @JsonProperty("dsitem")
    public GFAPwiDsItemResponse getDsitem() {
        return dsitem;
    }

    /**
     * Sets dsitem.
     *
     * @param dsitem the dsitem
     */
    @JsonProperty("dsitem")
    public void setDsitem(GFAPwiDsItemResponse dsitem) {
        this.dsitem = dsitem;
    }

}