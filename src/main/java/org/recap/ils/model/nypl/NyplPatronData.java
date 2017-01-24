package org.recap.ils.model.nypl;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Created by rajeshbabuk on 10/1/17.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "updatedDate",
        "createdDate",
        "deletedDate",
        "deleted",
        "suppressed",
        "names",
        "barCodes",
        "expirationDate",
        "homeLibraryCode",
        "birthDate",
        "emails",
        "fixedFields",
        "varFields"
})
public class NyplPatronData {

    @JsonProperty("id")
    private String id;
    @JsonProperty("updatedDate")
    private String updatedDate;
    @JsonProperty("createdDate")
    private String createdDate;
    @JsonProperty("deletedDate")
    private Object deletedDate;
    @JsonProperty("deleted")
    private Boolean deleted;
    @JsonProperty("suppressed")
    private Boolean suppressed;
    @JsonProperty("names")
    private List<String> names = null;
    @JsonProperty("barCodes")
    private List<String> barCodes = null;
    @JsonProperty("expirationDate")
    private String expirationDate;
    @JsonProperty("homeLibraryCode")
    private String homeLibraryCode;
    @JsonProperty("birthDate")
    private String birthDate;
    @JsonProperty("emails")
    private List<String> emails = null;
    @JsonProperty("fixedFields")
    private FixedFields fixedFields;
    @JsonProperty("varFields")
    private List<VarField> varFields = null;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("updatedDate")
    public String getUpdatedDate() {
        return updatedDate;
    }

    @JsonProperty("updatedDate")
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    @JsonProperty("createdDate")
    public String getCreatedDate() {
        return createdDate;
    }

    @JsonProperty("createdDate")
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    @JsonProperty("deletedDate")
    public Object getDeletedDate() {
        return deletedDate;
    }

    @JsonProperty("deletedDate")
    public void setDeletedDate(Object deletedDate) {
        this.deletedDate = deletedDate;
    }

    @JsonProperty("deleted")
    public Boolean getDeleted() {
        return deleted;
    }

    @JsonProperty("deleted")
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    @JsonProperty("suppressed")
    public Boolean getSuppressed() {
        return suppressed;
    }

    @JsonProperty("suppressed")
    public void setSuppressed(Boolean suppressed) {
        this.suppressed = suppressed;
    }

    @JsonProperty("names")
    public List<String> getNames() {
        return names;
    }

    @JsonProperty("names")
    public void setNames(List<String> names) {
        this.names = names;
    }

    @JsonProperty("barCodes")
    public List<String> getBarCodes() {
        return barCodes;
    }

    @JsonProperty("barCodes")
    public void setBarCodes(List<String> barCodes) {
        this.barCodes = barCodes;
    }

    @JsonProperty("expirationDate")
    public String getExpirationDate() {
        return expirationDate;
    }

    @JsonProperty("expirationDate")
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    @JsonProperty("homeLibraryCode")
    public String getHomeLibraryCode() {
        return homeLibraryCode;
    }

    @JsonProperty("homeLibraryCode")
    public void setHomeLibraryCode(String homeLibraryCode) {
        this.homeLibraryCode = homeLibraryCode;
    }

    @JsonProperty("birthDate")
    public String getBirthDate() {
        return birthDate;
    }

    @JsonProperty("birthDate")
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    @JsonProperty("emails")
    public List<String> getEmails() {
        return emails;
    }

    @JsonProperty("emails")
    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    @JsonProperty("fixedFields")
    public FixedFields getFixedFields() {
        return fixedFields;
    }

    @JsonProperty("fixedFields")
    public void setFixedFields(FixedFields fixedFields) {
        this.fixedFields = fixedFields;
    }

    @JsonProperty("varFields")
    public List<VarField> getVarFields() {
        return varFields;
    }

    @JsonProperty("varFields")
    public void setVarFields(List<VarField> varFields) {
        this.varFields = varFields;
    }

}
