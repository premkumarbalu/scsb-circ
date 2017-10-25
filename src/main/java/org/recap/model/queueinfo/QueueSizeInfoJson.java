package org.recap.model.queueinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by akulak on 20/10/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueueSizeInfoJson {

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
