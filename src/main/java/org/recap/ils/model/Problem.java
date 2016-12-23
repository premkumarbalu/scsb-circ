package org.recap.ils.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 9/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "ProblemType",
        "ProblemDetail"
})
public class Problem {

    @JsonProperty("ProblemType")
    private String problemType;
    @JsonProperty("ProblemDetail")
    private String problemDetail;

    /**
     *
     * @return
     * The problemType
     */
    @JsonProperty("ProblemType")
    public String getProblemType() {
        return problemType;
    }

    /**
     *
     * @param problemType
     * The ProblemType
     */
    @JsonProperty("ProblemType")
    public void setProblemType(String problemType) {
        this.problemType = problemType;
    }

    /**
     *
     * @return
     * The problemDetail
     */
    @JsonProperty("ProblemDetail")
    public String getProblemDetail() {
        return problemDetail;
    }

    /**
     *
     * @param problemDetail
     * The ProblemDetail
     */
    @JsonProperty("ProblemDetail")
    public void setProblemDetail(String problemDetail) {
        this.problemDetail = problemDetail;
    }

}
