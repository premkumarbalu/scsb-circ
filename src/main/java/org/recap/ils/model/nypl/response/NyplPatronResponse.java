package org.recap.ils.model.nypl.response;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.recap.ils.model.nypl.DebugInfo;
import org.recap.ils.model.nypl.NyplPatronData;

/**
 * Created by rajeshbabuk on 10/1/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "data",
        "count",
        "statusCode",
        "debugInfo"
})
public class NyplPatronResponse {

    @JsonProperty("data")
    private List<NyplPatronData> data = null;
    @JsonProperty("count")
    private Integer count;
    @JsonProperty("statusCode")
    private Integer statusCode;
    @JsonProperty("debugInfo")
    private List<DebugInfo> debugInfo = null;

    @JsonProperty("data")
    public List<NyplPatronData> getData() {
        return data;
    }

    @JsonProperty("data")
    public void setData(List<NyplPatronData> data) {
        this.data = data;
    }

    @JsonProperty("count")
    public Integer getCount() {
        return count;
    }

    @JsonProperty("count")
    public void setCount(Integer count) {
        this.count = count;
    }

    @JsonProperty("statusCode")
    public Integer getStatusCode() {
        return statusCode;
    }

    @JsonProperty("statusCode")
    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    @JsonProperty("debugInfo")
    public List<DebugInfo> getDebugInfo() {
        return debugInfo;
    }

    @JsonProperty("debugInfo")
    public void setDebugInfo(List<DebugInfo> debugInfo) {
        this.debugInfo = debugInfo;
    }

}
