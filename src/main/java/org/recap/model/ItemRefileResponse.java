package org.recap.model;

/**
 * Created by sudhishk on 15/12/16.
 */
public class ItemRefileResponse {

    private Integer requestId;
    private String screenMessage;
    private boolean success;

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
     * Gets screen message.
     *
     * @return the screen message
     */
    public String getScreenMessage() {
        return screenMessage;
    }

    /**
     * Sets screen message.
     *
     * @param screenMessage the screen message
     */
    public void setScreenMessage(String screenMessage) {
        this.screenMessage = screenMessage;
    }

    /**
     * Is success boolean.
     *
     * @return the boolean
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets success.
     *
     * @param success the success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
