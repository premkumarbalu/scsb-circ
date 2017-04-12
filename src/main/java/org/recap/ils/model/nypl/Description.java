package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 7/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "title",
        "author",
        "callNumber"
})
public class Description {

    @JsonProperty("title")
    private String title;
    @JsonProperty("author")
    private String author;
    @JsonProperty("callNumber")
    private String callNumber;

    /**
     * Gets title.
     *
     * @return The  title
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title The title
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets author.
     *
     * @return The  author
     */
    @JsonProperty("author")
    public String getAuthor() {
        return author;
    }

    /**
     * Sets author.
     *
     * @param author The author
     */
    @JsonProperty("author")
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets call number.
     *
     * @return The  callNumber
     */
    @JsonProperty("callNumber")
    public String getCallNumber() {
        return callNumber;
    }

    /**
     * Sets call number.
     *
     * @param callNumber The callNumber
     */
    @JsonProperty("callNumber")
    public void setCallNumber(String callNumber) {
        this.callNumber = callNumber;
    }

}
