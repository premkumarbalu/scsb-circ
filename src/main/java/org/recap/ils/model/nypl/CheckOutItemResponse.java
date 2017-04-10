package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 9/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "Problem"
})
public class CheckOutItemResponse {

    @JsonProperty("Problem")
    private Problem problem;

    /**
     * Gets problem.
     *
     * @return The  problem
     */
    @JsonProperty("Problem")
    public Problem getProblem() {
        return problem;
    }

    /**
     * Sets problem.
     *
     * @param problem The Problem
     */
    @JsonProperty("Problem")
    public void setProblem(Problem problem) {
        this.problem = problem;
    }

}
