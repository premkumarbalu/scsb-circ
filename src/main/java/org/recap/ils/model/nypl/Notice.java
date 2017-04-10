package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 9/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "createdDate",
        "text",
        "data"
})
public class Notice {

    @JsonProperty("createdDate")
    private String createdDate;
    @JsonProperty("text")
    private String text;
    @JsonProperty("data")
    private Object data;

    /**
     * Gets created date.
     *
     * @return The  createdDate
     */
    @JsonProperty("createdDate")
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets created date.
     *
     * @param createdDate The createdDate
     */
    @JsonProperty("createdDate")
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Gets text.
     *
     * @return The  text
     */
    @JsonProperty("text")
    public String getText() {
        return text;
    }

    /**
     * Sets text.
     *
     * @param text The text
     */
    @JsonProperty("text")
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets data.
     *
     * @return The  data
     */
    @JsonProperty("data")
    public Object getData() {
        return data;
    }

    /**
     * Sets data.
     *
     * @param data The data
     */
    @JsonProperty("data")
    public void setData(Object data) {
        this.data = data;
    }

}
