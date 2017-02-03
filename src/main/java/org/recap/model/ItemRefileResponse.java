package org.recap.model;

/**
 * Created by sudhishk on 15/12/16.
 */
public class ItemRefileResponse {

    private Integer requestId;
    private String screenMessage;
    private boolean success;

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public String getScreenMessage() {
        return screenMessage;
    }

    public void setScreenMessage(String screenMessage) {
        this.screenMessage = screenMessage;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
