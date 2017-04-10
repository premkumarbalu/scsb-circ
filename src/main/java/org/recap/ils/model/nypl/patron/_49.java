
package org.recap.ils.model.nypl.patron;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The type 49.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "label",
    "value",
    "display"
})
public class _49 {

    @JsonProperty("label")
    private String label;
    @JsonProperty("value")
    private Integer value;
    @JsonProperty("display")
    private Object display;

    /**
     * Gets label.
     *
     * @return the label
     */
    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    /**
     * Sets label.
     *
     * @param label the label
     */
    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Gets value.
     *
     * @return the value
     */
    @JsonProperty("value")
    public Integer getValue() {
        return value;
    }

    /**
     * Sets value.
     *
     * @param value the value
     */
    @JsonProperty("value")
    public void setValue(Integer value) {
        this.value = value;
    }

    /**
     * Gets display.
     *
     * @return the display
     */
    @JsonProperty("display")
    public Object getDisplay() {
        return display;
    }

    /**
     * Sets display.
     *
     * @param display the display
     */
    @JsonProperty("display")
    public void setDisplay(Object display) {
        this.display = display;
    }

}
