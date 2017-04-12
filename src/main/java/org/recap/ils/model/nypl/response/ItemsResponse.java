package org.recap.ils.model.nypl.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.recap.ils.model.nypl.DebugInfo;
import org.recap.ils.model.nypl.ItemData;

import java.util.List;

/**
 * Created by rajeshbabuk on 7/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "itemData",
        "count",
        "statusCode",
        "debugInfo"
})
public class ItemsResponse {

    @JsonProperty("data")
    private List<ItemData> itemsData;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("statusCode")
    private Integer statusCode;
    @JsonProperty("debugInfo")
    private List<DebugInfo> debugInfo = null;

    /**
     * Gets items data.
     *
     * @return The  itemData
     */
    @JsonProperty("data")
    public List<ItemData> getItemsData() {
        return itemsData;
    }

    /**
     * Sets items data.
     *
     * @param itemsData The itemData
     */
    @JsonProperty("data")
    public void setItemsData(List<ItemData> itemsData) {
        this.itemsData = itemsData;
    }

    /**
     * Gets count.
     *
     * @return The  count
     */
    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    /**
     * Sets count.
     *
     * @param count The count
     */
    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Gets status code.
     *
     * @return The  statusCode
     */
    @JsonProperty("statusCode")
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Sets status code.
     *
     * @param statusCode The statusCode
     */
    @JsonProperty("statusCode")
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Gets debug info.
     *
     * @return The  debugInfo
     */
    @JsonProperty("debugInfo")
    public List<DebugInfo> getDebugInfo() {
        return debugInfo;
    }

    /**
     * Sets debug info.
     *
     * @param debugInfo The debugInfo
     */
    @JsonProperty("debugInfo")
    public void setDebugInfo(List<DebugInfo> debugInfo) {
        this.debugInfo = debugInfo;
    }
}
