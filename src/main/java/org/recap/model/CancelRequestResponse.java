package org.recap.model;

/**
 * Created by sudhishk on 15/12/16.
 */
public class CancelRequestResponse {
    private String screenMessage;
    private boolean success;

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
