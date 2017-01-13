package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 8/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "jobId",
        "processed",
        "success",
        "updatedDate",
        "createdDate",
        "patronBarcode",
        "itemBarcode",
        "desiredDateDue"
})
public class CheckoutData {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("jobId")
    private String jobId;
    @JsonProperty("processed")
    private Boolean processed;
    @JsonProperty("success")
    private Boolean success;
    @JsonProperty("updatedDate")
    private Object updatedDate;
    @JsonProperty("createdDate")
    private String createdDate;
    @JsonProperty("patronBarcode")
    private String patronBarcode;
    @JsonProperty("itemBarcode")
    private String itemBarcode;
    @JsonProperty("desiredDateDue")
    private String desiredDateDue;

    /**
     *
     * @return
     * The id
     */
    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The jobId
     */
    @JsonProperty("jobId")
    public String getJobId() {
        return jobId;
    }

    /**
     *
     * @param jobId
     * The jobId
     */
    @JsonProperty("jobId")
    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    /**
     *
     * @return
     * The processed
     */
    @JsonProperty("processed")
    public Boolean getProcessed() {
        return processed;
    }

    /**
     *
     * @param processed
     * The processed
     */
    @JsonProperty("processed")
    public void setProcessed(Boolean processed) {
        this.processed = processed;
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
     * The updatedDate
     */
    @JsonProperty("updatedDate")
    public Object getUpdatedDate() {
        return updatedDate;
    }

    /**
     *
     * @param updatedDate
     * The updatedDate
     */
    @JsonProperty("updatedDate")
    public void setUpdatedDate(Object updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     *
     * @return
     * The createdDate
     */
    @JsonProperty("createdDate")
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     *
     * @param createdDate
     * The createdDate
     */
    @JsonProperty("createdDate")
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     *
     * @return
     * The patronBarcode
     */
    @JsonProperty("patronBarcode")
    public String getPatronBarcode() {
        return patronBarcode;
    }

    /**
     *
     * @param patronBarcode
     * The patronBarcode
     */
    @JsonProperty("patronBarcode")
    public void setPatronBarcode(String patronBarcode) {
        this.patronBarcode = patronBarcode;
    }

    /**
     *
     * @return
     * The itemBarcode
     */
    @JsonProperty("itemBarcode")
    public String getItemBarcode() {
        return itemBarcode;
    }

    /**
     *
     * @param itemBarcode
     * The itemBarcode
     */
    @JsonProperty("itemBarcode")
    public void setItemBarcode(String itemBarcode) {
        this.itemBarcode = itemBarcode;
    }

    /**
     *
     * @return
     * The desiredDateDue
     */
    @JsonProperty("desiredDateDue")
    public String getDesiredDateDue() {
        return desiredDateDue;
    }

    /**
     *
     * @param desiredDateDue
     * The desiredDateDue
     */
    @JsonProperty("desiredDateDue")
    public void setDesiredDateDue(String desiredDateDue) {
        this.desiredDateDue = desiredDateDue;
    }

}

