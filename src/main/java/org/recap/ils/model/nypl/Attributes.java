package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 9/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "version"
})
public class Attributes {

    @JsonProperty("version")
    private String version;

    /**
     * Gets version.
     *
     * @return The  version
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * Sets version.
     *
     * @param version The version
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

}
