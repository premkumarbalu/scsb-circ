package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 7/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "tag",
        "content"
})
public class SubField {

    @JsonProperty("tag")
    private String tag;
    @JsonProperty("content")
    private String content;

    /**
     * Gets tag.
     *
     * @return The  tag
     */
    @JsonProperty("tag")
    public String getTag() {
        return tag;
    }

    /**
     * Sets tag.
     *
     * @param tag The tag
     */
    @JsonProperty("tag")
    public void setTag(String tag) {
        this.tag = tag;
    }

    /**
     * Gets content.
     *
     * @return The  content
     */
    @JsonProperty("content")
    public String getContent() {
        return content;
    }

    /**
     * Sets content.
     *
     * @param content The content
     */
    @JsonProperty("content")
    public void setContent(String content) {
        this.content = content;
    }

}