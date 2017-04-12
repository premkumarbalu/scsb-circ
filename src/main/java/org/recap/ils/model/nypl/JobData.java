package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Created by rajeshbabuk on 9/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "started",
        "finished",
        "success",
        "notices",
        "successRedirectUrl",
        "startCallbackUrl",
        "successCallbackUrl",
        "failureCallbackUrl",
        "updateCallbackUrl"
})
public class JobData {

    @JsonProperty("id")
    private String id;
    @JsonProperty("started")
    private Boolean started;
    @JsonProperty("finished")
    private Boolean finished;
    @JsonProperty("success")
    private Boolean success;
    @JsonProperty("notices")
    private List<Notice> notices = null;
    @JsonProperty("successRedirectUrl")
    private String successRedirectUrl;
    @JsonProperty("startCallbackUrl")
    private String startCallbackUrl;
    @JsonProperty("successCallbackUrl")
    private String successCallbackUrl;
    @JsonProperty("failureCallbackUrl")
    private String failureCallbackUrl;
    @JsonProperty("updateCallbackUrl")
    private String updateCallbackUrl;

    /**
     * Gets id.
     *
     * @return The  id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets started.
     *
     * @return The  started
     */
    @JsonProperty("started")
    public Boolean getStarted() {
        return started;
    }

    /**
     * Sets started.
     *
     * @param started The started
     */
    @JsonProperty("started")
    public void setStarted(Boolean started) {
        this.started = started;
    }

    /**
     * Gets finished.
     *
     * @return The  finished
     */
    @JsonProperty("finished")
    public Boolean getFinished() {
        return finished;
    }

    /**
     * Sets finished.
     *
     * @param finished The finished
     */
    @JsonProperty("finished")
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    /**
     * Gets success.
     *
     * @return The  success
     */
    @JsonProperty("success")
    public Boolean getSuccess() {
        return success;
    }

    /**
     * Sets success.
     *
     * @param success The success
     */
    @JsonProperty("success")
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     * Gets notices.
     *
     * @return The  notices
     */
    @JsonProperty("notices")
    public List<Notice> getNotices() {
        return notices;
    }

    /**
     * Sets notices.
     *
     * @param notices The notices
     */
    @JsonProperty("notices")
    public void setNotices(List<Notice> notices) {
        this.notices = notices;
    }

    /**
     * Gets success redirect url.
     *
     * @return The  successRedirectUrl
     */
    @JsonProperty("successRedirectUrl")
    public String getSuccessRedirectUrl() {
        return successRedirectUrl;
    }

    /**
     * Sets success redirect url.
     *
     * @param successRedirectUrl The successRedirectUrl
     */
    @JsonProperty("successRedirectUrl")
    public void setSuccessRedirectUrl(String successRedirectUrl) {
        this.successRedirectUrl = successRedirectUrl;
    }

    /**
     * Gets start callback url.
     *
     * @return The  startCallbackUrl
     */
    @JsonProperty("startCallbackUrl")
    public String getStartCallbackUrl() {
        return startCallbackUrl;
    }

    /**
     * Sets start callback url.
     *
     * @param startCallbackUrl The startCallbackUrl
     */
    @JsonProperty("startCallbackUrl")
    public void setStartCallbackUrl(String startCallbackUrl) {
        this.startCallbackUrl = startCallbackUrl;
    }

    /**
     * Gets success callback url.
     *
     * @return The  successCallbackUrl
     */
    @JsonProperty("successCallbackUrl")
    public String getSuccessCallbackUrl() {
        return successCallbackUrl;
    }

    /**
     * Sets success callback url.
     *
     * @param successCallbackUrl The successCallbackUrl
     */
    @JsonProperty("successCallbackUrl")
    public void setSuccessCallbackUrl(String successCallbackUrl) {
        this.successCallbackUrl = successCallbackUrl;
    }

    /**
     * Gets failure callback url.
     *
     * @return The  failureCallbackUrl
     */
    @JsonProperty("failureCallbackUrl")
    public String getFailureCallbackUrl() {
        return failureCallbackUrl;
    }

    /**
     * Sets failure callback url.
     *
     * @param failureCallbackUrl The failureCallbackUrl
     */
    @JsonProperty("failureCallbackUrl")
    public void setFailureCallbackUrl(String failureCallbackUrl) {
        this.failureCallbackUrl = failureCallbackUrl;
    }

    /**
     * Gets update callback url.
     *
     * @return The  updateCallbackUrl
     */
    @JsonProperty("updateCallbackUrl")
    public String getUpdateCallbackUrl() {
        return updateCallbackUrl;
    }

    /**
     * Sets update callback url.
     *
     * @param updateCallbackUrl The updateCallbackUrl
     */
    @JsonProperty("updateCallbackUrl")
    public void setUpdateCallbackUrl(String updateCallbackUrl) {
        this.updateCallbackUrl = updateCallbackUrl;
    }

}
