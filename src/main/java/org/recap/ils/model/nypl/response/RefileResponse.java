package org.recap.ils.model.nypl.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.recap.ils.model.nypl.DebugInfo;
import org.recap.ils.model.nypl.RefileData;

import java.util.List;

/**
 * Created by rajeshbabuk on 14/7/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "data",
        "count",
        "statusCode",
        "debugInfo"
})
public class RefileResponse {

    @JsonProperty("data")
    private RefileData data;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("statusCode")
    private Integer statusCode;
    @JsonProperty("debugInfo")
    private List<DebugInfo> debugInfo = null;

    /**
     * Gets data.
     *
     * @return the data
     */
    @JsonProperty("data")
    public RefileData getData() {
        return data;
    }

    /**
     * Sets data.
     *
     * @param data the data
     */
    @JsonProperty("data")
    public void setData(RefileData data) {
        this.data = data;
    }

    /**
     * Gets count.
     *
     * @return the count
     */
    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    /**
     * Sets count.
     *
     * @param count the count
     */
    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    /**
     * Gets status code.
     *
     * @return the status code
     */
    @JsonProperty("statusCode")
    public Integer getStatusCode() {
        return statusCode;
    }

    /**
     * Sets status code.
     *
     * @param statusCode the status code
     */
    @JsonProperty("statusCode")
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    /**
     * Gets debug info.
     *
     * @return the debug info
     */
    @JsonProperty("debugInfo")
    public List<DebugInfo> getDebugInfo() {
        return debugInfo;
    }

    /**
     * Sets debug info.
     *
     * @param debugInfo the debug info
     */
    @JsonProperty("debugInfo")
    public void setDebugInfo(List<DebugInfo> debugInfo) {
        this.debugInfo = debugInfo;
    }
}
