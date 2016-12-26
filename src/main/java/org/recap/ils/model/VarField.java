package org.recap.ils.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

/**
 * Created by rajeshbabuk on 7/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "fieldTag",
        "marcTag",
        "ind1",
        "ind2",
        "content",
        "subFields"
})
public class VarField {

    @JsonProperty("fieldTag")
    private Object fieldTag;
    @JsonProperty("marcTag")
    private String marcTag;
    @JsonProperty("ind1")
    private String ind1;
    @JsonProperty("ind2")
    private String ind2;
    @JsonProperty("content")
    private Object content;
    @JsonProperty("subFields")
    private List<SubField> subFields = null;

    /**
     *
     * @return
     * The fieldTag
     */
    @JsonProperty("fieldTag")
    public Object getFieldTag() {
        return fieldTag;
    }

    /**
     *
     * @param fieldTag
     * The fieldTag
     */
    @JsonProperty("fieldTag")
    public void setFieldTag(Object fieldTag) {
        this.fieldTag = fieldTag;
    }

    /**
     *
     * @return
     * The marcTag
     */
    @JsonProperty("marcTag")
    public String getMarcTag() {
        return marcTag;
    }

    /**
     *
     * @param marcTag
     * The marcTag
     */
    @JsonProperty("marcTag")
    public void setMarcTag(String marcTag) {
        this.marcTag = marcTag;
    }

    /**
     *
     * @return
     * The ind1
     */
    @JsonProperty("ind1")
    public String getInd1() {
        return ind1;
    }

    /**
     *
     * @param ind1
     * The ind1
     */
    @JsonProperty("ind1")
    public void setInd1(String ind1) {
        this.ind1 = ind1;
    }

    /**
     *
     * @return
     * The ind2
     */
    @JsonProperty("ind2")
    public String getInd2() {
        return ind2;
    }

    /**
     *
     * @param ind2
     * The ind2
     */
    @JsonProperty("ind2")
    public void setInd2(String ind2) {
        this.ind2 = ind2;
    }

    /**
     *
     * @return
     * The content
     */
    @JsonProperty("content")
    public Object getContent() {
        return content;
    }

    /**
     *
     * @param content
     * The content
     */
    @JsonProperty("content")
    public void setContent(Object content) {
        this.content = content;
    }

    /**
     *
     * @return
     * The subFields
     */
    @JsonProperty("subFields")
    public List<SubField> getSubFields() {
        return subFields;
    }

    /**
     *
     * @param subFields
     * The subFields
     */
    @JsonProperty("subFields")
    public void setSubFields(List<SubField> subFields) {
        this.subFields = subFields;
    }

}