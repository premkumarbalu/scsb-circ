package org.recap.ils.model.nypl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

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

    /**
     * Gets id.
     *
     * @return the id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets updated date.
     *
     * @return the updated date
     */
    @JsonProperty("updatedDate")
    public String getUpdatedDate() {
        return updatedDate;
    }

    /**
     * Sets updated date.
     *
     * @param updatedDate the updated date
     */
    @JsonProperty("updatedDate")
    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    /**
     * Gets created date.
     *
     * @return the created date
     */
    @JsonProperty("createdDate")
    public String getCreatedDate() {
        return createdDate;
    }

    /**
     * Sets created date.
     *
     * @param createdDate the created date
     */
    @JsonProperty("createdDate")
    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    /**
     * Gets deleted date.
     *
     * @return the deleted date
     */
    @JsonProperty("deletedDate")
    public Object getDeletedDate() {
        return deletedDate;
    }

    /**
     * Sets deleted date.
     *
     * @param deletedDate the deleted date
     */
    @JsonProperty("deletedDate")
    public void setDeletedDate(Object deletedDate) {
        this.deletedDate = deletedDate;
    }

    /**
     * Gets deleted.
     *
     * @return the deleted
     */
    @JsonProperty("deleted")
    public Boolean getDeleted() {
        return deleted;
    }

    /**
     * Sets deleted.
     *
     * @param deleted the deleted
     */
    @JsonProperty("deleted")
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Gets suppressed.
     *
     * @return the suppressed
     */
    @JsonProperty("suppressed")
    public Boolean getSuppressed() {
        return suppressed;
    }

    /**
     * Sets suppressed.
     *
     * @param suppressed the suppressed
     */
    @JsonProperty("suppressed")
    public void setSuppressed(Boolean suppressed) {
        this.suppressed = suppressed;
    }

    /**
     * Gets names.
     *
     * @return the names
     */
    @JsonProperty("names")
    public List<String> getNames() {
        return names;
    }

    /**
     * Sets names.
     *
     * @param names the names
     */
    @JsonProperty("names")
    public void setNames(List<String> names) {
        this.names = names;
    }

    /**
     * Gets bar codes.
     *
     * @return the bar codes
     */
    @JsonProperty("barCodes")
    public List<String> getBarCodes() {
        return barCodes;
    }

    /**
     * Sets bar codes.
     *
     * @param barCodes the bar codes
     */
    @JsonProperty("barCodes")
    public void setBarCodes(List<String> barCodes) {
        this.barCodes = barCodes;
    }

    /**
     * Gets expiration date.
     *
     * @return the expiration date
     */
    @JsonProperty("expirationDate")
    public String getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets expiration date.
     *
     * @param expirationDate the expiration date
     */
    @JsonProperty("expirationDate")
    public void setExpirationDate(String expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Gets home library code.
     *
     * @return the home library code
     */
    @JsonProperty("homeLibraryCode")
    public String getHomeLibraryCode() {
        return homeLibraryCode;
    }

    /**
     * Sets home library code.
     *
     * @param homeLibraryCode the home library code
     */
    @JsonProperty("homeLibraryCode")
    public void setHomeLibraryCode(String homeLibraryCode) {
        this.homeLibraryCode = homeLibraryCode;
    }

    /**
     * Gets birth date.
     *
     * @return the birth date
     */
    @JsonProperty("birthDate")
    public String getBirthDate() {
        return birthDate;
    }

    /**
     * Sets birth date.
     *
     * @param birthDate the birth date
     */
    @JsonProperty("birthDate")
    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    /**
     * Gets emails.
     *
     * @return the emails
     */
    @JsonProperty("emails")
    public List<String> getEmails() {
        return emails;
    }

    /**
     * Sets emails.
     *
     * @param emails the emails
     */
    @JsonProperty("emails")
    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    /**
     * Gets fixed fields.
     *
     * @return the fixed fields
     */
    @JsonProperty("fixedFields")
    public FixedFields getFixedFields() {
        return fixedFields;
    }

    /**
     * Sets fixed fields.
     *
     * @param fixedFields the fixed fields
     */
    @JsonProperty("fixedFields")
    public void setFixedFields(FixedFields fixedFields) {
        this.fixedFields = fixedFields;
    }

    /**
     * Gets var fields.
     *
     * @return the var fields
     */
    @JsonProperty("varFields")
    public List<VarField> getVarFields() {
        return varFields;
    }

    /**
     * Sets var fields.
     *
     * @param varFields the var fields
     */
    @JsonProperty("varFields")
    public void setVarFields(List<VarField> varFields) {
        this.varFields = varFields;
    }

}
