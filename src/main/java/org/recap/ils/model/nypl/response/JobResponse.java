package org.recap.ils.model.nypl.response;

/**
 * Created by rajeshbabuk on 9/12/16.
 */

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import jdk.nashorn.internal.ir.annotations.Ignore;
import org.recap.ils.model.nypl.DebugInfo;
import org.recap.ils.model.nypl.JobData;

import java.util.List;

/**
 * The type Job response.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "data",
        "count",
        "statusCode",
        "debugInfo"
})
public class JobResponse {

    @JsonProperty("data")
    private JobData data;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("statusCode")
    private Integer statusCode;
    @JsonProperty("debugInfo")
    private List<DebugInfo> debugInfo = null;
    @Ignore
    private String statusMessage;

    /**
     * Gets data.
     *
     * @return The  data
     */
    @JsonProperty("data")
    public JobData getData() {
        return data;
    }

    /**
     * Sets data.
     *
     * @param data The data
     */
    @JsonProperty("data")
    public void setData(JobData data) {
        this.data = data;
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

    /**
     * Gets status message.
     *
     * @return the status message
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Sets status message.
     *
     * @param statusMessage the status message
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
