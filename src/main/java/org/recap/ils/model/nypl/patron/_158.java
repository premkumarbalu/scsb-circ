
package org.recap.ils.model.nypl.patron;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "label",
    "value",
    "display"
})
public class _158 {

    @JsonProperty("label")
    private String label;
    @JsonProperty("value")
    private String value;
    @JsonProperty("display")
    private Object display;

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    @JsonProperty("value")
    public String getValue() {
        return value;
    }

    @JsonProperty("value")
    public void setValue(String value) {
        this.value = value;
    }

    @JsonProperty("display")
    public Object getDisplay() {
        return display;
    }

    @JsonProperty("display")
    public void setDisplay(Object display) {
        this.display = display;
    }

}
