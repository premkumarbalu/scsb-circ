package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.recap.ils.model.nypl.VarField;

import java.util.List;

/**
 * Created by rajeshbabuk on 7/12/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "nyplSource",
        "bibIds",
        "id",
        "nyplType",
        "updatedDate",
        "createdDate",
        "deletedDate",
        "deleted",
        "location",
        "status",
        "barcode",
        "callNumber",
        "itemType",
        "fixedFields",
        "varFields"
})
public class ItemData {

    @JsonProperty("nyplSource")
    private String nyplSource;
    @JsonProperty("bibIds")
    private List<String> bibIds = null;
    @JsonProperty("id")
    private String id;
    @JsonProperty("nyplType")
    private String nyplType;
    @JsonProperty("updatedDate")
    private String updatedDate;
    @JsonProperty("createdDate")
    private String createdDate;
    @JsonProperty("deletedDate")
    private Object deletedDate;
    @JsonProperty("deleted")
    private Object deleted;
    @JsonProperty("location")
    private Object location;
    @JsonProperty("status")
    private Object status;
    @JsonProperty("barcode")
    private Object barcode;
    @JsonProperty("callNumber")
    private Object callNumber;
    @JsonProperty("itemType")
    private Object itemType;
    @JsonProperty("fixedFields")
    private Object fixedFields;
    @JsonProperty("varFields")
    private List<VarField> varFields = null;

    /**
     * @return The nyplSource
     */
    @JsonProperty("nyplSource")
    public String getNyplSource() {
        return nyplSource;
    }

    /**
     * @param nyplSource The nyplSource
     */
    @JsonProperty("nyplSource")
    public void setNyplSource(String nyplSource) {
        this.nyplSource = nyplSource;
    }

    /**
     * @return The bibIds
     */
    @JsonProperty("bibIds")
    public List<String> getBibIds() {
        return bibIds;
    }

    /**
     * @param bibIds The bibIds
     */
    @JsonProperty("bibIds")
    public void setBibIds(List<String> bibIds) {
        this.bibIds = bibIds;
    }

    /**
     * @return The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * @param id The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return The nyplType
     */
    @JsonProperty("nyplType")
    public String getNyplType() {
        return nyplType;
    }

    /**
     * @param nyplType The nyplType
     */
    @JsonProperty("nyplType")
    public void setNyplType(String nyplType) {
        this.nyplType = nyplType;
    }

    /**
     * @return The updatedDate
     */
    @JsonProperty("updatedDate")
    public String getUpdatedDate() {
        return updatedDate;
    }

    /**
     * @param updatedDate The updatedDate
     */
    @JsonProperty("updatedDate")
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * @return The createdDate
     */
    @JsonProperty("createdDate")
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * @param createdDate The createdDate
     */
    @JsonProperty("createdDate")
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * @return The deletedDate
     */
    @JsonProperty("deletedDate")
    public Object getDeletedDate() {
        return deletedDate;
    }

    /**
     * @param deletedDate The deletedDate
     */
    @JsonProperty("deletedDate")
    public void setDeletedDate(Object deletedDate) {
        this.deletedDate = deletedDate;
    }

    /**
     * @return The deleted
     */
    @JsonProperty("deleted")
    public Object getDeleted() {
        return deleted;
    }

    /**
     * @param deleted The deleted
     */
    @JsonProperty("deleted")
    public void setDeleted(Object deleted) {
        this.deleted = deleted;
    }

    /**
     * @return The location
     */
    @JsonProperty("location")
    public Object getLocation() {
        return location;
    }

    /**
     * @param location The location
     */
    @JsonProperty("location")
    public void setLocation(Object location) {
        this.location = location;
    }

    /**
     * @return The status
     */
    @JsonProperty("status")
    public Object getStatus() {
        return status;
    }

    /**
     * @param status The status
     */
    @JsonProperty("status")
    public void setStatus(Object status) {
        this.status = status;
    }

    /**
     * @return The barcode
     */
    @JsonProperty("barcode")
    public Object getBarcode() {
        return barcode;
    }

    /**
     * @param barcode The barcode
     */
    @JsonProperty("barcode")
    public void setBarcode(Object barcode) {
        this.barcode = barcode;
    }

    /**
     * @return The callNumber
     */
    @JsonProperty("callNumber")
    public Object getCallNumber() {
        return callNumber;
    }

    /**
     * @param callNumber The callNumber
     */
    @JsonProperty("callNumber")
    public void setCallNumber(Object callNumber) {
        this.callNumber = callNumber;
    }

    /**
     * @return The itemType
     */
    @JsonProperty("itemType")
    public Object getItemType() {
        return itemType;
    }

    /**
     * @param itemType The itemType
     */
    @JsonProperty("itemType")
    public void setItemType(Object itemType) {
        this.itemType = itemType;
    }

    /**
     * @return The fixedFields
     */
    @JsonProperty("fixedFields")
    public Object getFixedFields() {
        return fixedFields;
    }

    /**
     * @param fixedFields The fixedFields
     */
    @JsonProperty("fixedFields")
    public void setFixedFields(Object fixedFields) {
        this.fixedFields = fixedFields;
    }

    /**
     * @return The varFields
     */
    @JsonProperty("varFields")
    public List<VarField> getVarFields() {
        return varFields;
    }

    /**
     * @param varFields The varFields
     */
    @JsonProperty("varFields")
    public void setVarFields(List<VarField> varFields) {
        this.varFields = varFields;
    }
}
