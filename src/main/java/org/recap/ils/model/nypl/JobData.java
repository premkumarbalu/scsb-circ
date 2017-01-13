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
     *
     * @return
     * The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The started
     */
    @JsonProperty("started")
    public Boolean getStarted() {
        return started;
    }

    /**
     *
     * @param started
     * The started
     */
    @JsonProperty("started")
    public void setStarted(Boolean started) {
        this.started = started;
    }

    /**
     *
     * @return
     * The finished
     */
    @JsonProperty("finished")
    public Boolean getFinished() {
        return finished;
    }

    /**
     *
     * @param finished
     * The finished
     */
    @JsonProperty("finished")
    public void setFinished(Boolean finished) {
        this.finished = finished;
    }

    /**
     *
     * @return
     * The success
     */
    @JsonProperty("success")
    public Boolean getSuccess() {
        return success;
    }

    /**
     *
     * @param success
     * The success
     */
    @JsonProperty("success")
    public void setSuccess(Boolean success) {
        this.success = success;
    }

    /**
     *
     * @return
     * The notices
     */
    @JsonProperty("notices")
    public List<Notice> getNotices() {
        return notices;
    }

    /**
     *
     * @param notices
     * The notices
     */
    @JsonProperty("notices")
    public void setNotices(List<Notice> notices) {
        this.notices = notices;
    }

    /**
     *
     * @return
     * The successRedirectUrl
     */
    @JsonProperty("successRedirectUrl")
    public String getSuccessRedirectUrl() {
        return successRedirectUrl;
    }

    /**
     *
     * @param successRedirectUrl
     * The successRedirectUrl
     */
    @JsonProperty("successRedirectUrl")
    public void setSuccessRedirectUrl(String successRedirectUrl) {
        this.successRedirectUrl = successRedirectUrl;
    }

    /**
     *
     * @return
     * The startCallbackUrl
     */
    @JsonProperty("startCallbackUrl")
    public String getStartCallbackUrl() {
        return startCallbackUrl;
    }

    /**
     *
     * @param startCallbackUrl
     * The startCallbackUrl
     */
    @JsonProperty("startCallbackUrl")
    public void setStartCallbackUrl(String startCallbackUrl) {
        this.startCallbackUrl = startCallbackUrl;
    }

    /**
     *
     * @return
     * The successCallbackUrl
     */
    @JsonProperty("successCallbackUrl")
    public String getSuccessCallbackUrl() {
        return successCallbackUrl;
    }

    /**
     *
     * @param successCallbackUrl
     * The successCallbackUrl
     */
    @JsonProperty("successCallbackUrl")
    public void setSuccessCallbackUrl(String successCallbackUrl) {
        this.successCallbackUrl = successCallbackUrl;
    }

    /**
     *
     * @return
     * The failureCallbackUrl
     */
    @JsonProperty("failureCallbackUrl")
    public String getFailureCallbackUrl() {
        return failureCallbackUrl;
    }

    /**
     *
     * @param failureCallbackUrl
     * The failureCallbackUrl
     */
    @JsonProperty("failureCallbackUrl")
    public void setFailureCallbackUrl(String failureCallbackUrl) {
        this.failureCallbackUrl = failureCallbackUrl;
    }

    /**
     *
     * @return
     * The updateCallbackUrl
     */
    @JsonProperty("updateCallbackUrl")
    public String getUpdateCallbackUrl() {
        return updateCallbackUrl;
    }

    /**
     *
     * @param updateCallbackUrl
     * The updateCallbackUrl
     */
    @JsonProperty("updateCallbackUrl")
    public void setUpdateCallbackUrl(String updateCallbackUrl) {
        this.updateCallbackUrl = updateCallbackUrl;
    }

}
