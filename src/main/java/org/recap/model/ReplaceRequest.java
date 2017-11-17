package org.recap.model;

/**
 * Created by rajeshbabuk on 15/11/17.
 */
public class ReplaceRequest {

    private String replaceRequestByType;
    private String requestStatus;
    private String requestIds;
    private String startRequestId;
    private String endRequestId;
    private String fromDate;
    private String toDate;

    /**
     * Gets replace request by type.
     *
     * @return the replace request by type
     */
    public String getReplaceRequestByType() {
        return replaceRequestByType;
    }

    /**
     * Sets replace request by type.
     *
     * @param replaceRequestByType the replace request by type
     */
    public void setReplaceRequestByType(String replaceRequestByType) {
        this.replaceRequestByType = replaceRequestByType;
    }

    /**
     * Gets request status.
     *
     * @return the request status
     */
    public String getRequestStatus() {
        return requestStatus;
    }

    /**
     * Sets request status.
     *
     * @param requestStatus the request status
     */
    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }

    /**
     * Gets request ids.
     *
     * @return the request ids
     */
    public String getRequestIds() {
        return requestIds;
    }

    /**
     * Sets request ids.
     *
     * @param requestIds the request ids
     */
    public void setRequestIds(String requestIds) {
        this.requestIds = requestIds;
    }

    /**
     * Gets start request id.
     *
     * @return the start request id
     */
    public String getStartRequestId() {
        return startRequestId;
    }

    /**
     * Sets start request id.
     *
     * @param startRequestId the start request id
     */
    public void setStartRequestId(String startRequestId) {
        this.startRequestId = startRequestId;
    }

    /**
     * Gets end request id.
     *
     * @return the end request id
     */
    public String getEndRequestId() {
        return endRequestId;
    }

    /**
     * Sets end request id.
     *
     * @param endRequestId the end request id
     */
    public void setEndRequestId(String endRequestId) {
        this.endRequestId = endRequestId;
    }

    /**
     * Gets from date.
     *
     * @return the from date
     */
    public String getFromDate() {
        return fromDate;
    }

    /**
     * Sets from date.
     *
     * @param fromDate the from date
     */
    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * Gets to date.
     *
     * @return the to date
     */
    public String getToDate() {
        return toDate;
    }

    /**
     * Sets to date.
     *
     * @param toDate the to date
     */
    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
}
