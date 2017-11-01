package org.recap.model;

import org.recap.ils.model.response.AbstractResponseItem;

/**
 * Created by sudhishk on 15/12/16.
 */
public class ItemRefileResponse extends AbstractResponseItem {

    private Integer requestId;
    private String jobId;

    /**
     * Gets request id.
     *
     * @return the request id
     */
    public Integer getRequestId() {
        return requestId;
    }

    /**
     * Sets request id.
     *
     * @param requestId the request id
     */
    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    /**
     * Gets job id.
     *
     * @return the job id
     */
    public String getJobId() {
        return jobId;
    }

    /**
     * Sets job id.
     *
     * @param jobId the job id
     */
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }
}
