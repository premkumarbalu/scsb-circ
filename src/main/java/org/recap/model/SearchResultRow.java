package org.recap.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajeshbabuk on 11/7/16.
 */
@ApiModel(value="SearchResultRow", description="Model for Displaying Search Result")
public class SearchResultRow {

    @ApiModelProperty(name= "bibId", value= "Bibliographic Id",position = 0)
    private Integer bibId;
    @ApiModelProperty(name= "title", value= "Book Title",position = 1)
    private String title;
    @ApiModelProperty(name= "author", value= "Author",position = 2)
    private String author;
    @ApiModelProperty(name= "publisher", value= "Publisher",position = 3)
    private String publisher;
    @ApiModelProperty(name= "publisherDate", value= "Publisher Date",position = 4)
    private String publisherDate;
    @ApiModelProperty(name= "owningInstitution", value= "Owning Institution",position = 5)
    private String owningInstitution;
    @ApiModelProperty(name= "customerCode", value= "Customer Code",position = 6)
    private String customerCode;
    @ApiModelProperty(name= "collectionGroupDesignation", value= "Collection Group Designation",position = 7)
    private String collectionGroupDesignation;
    @ApiModelProperty(name= "useRestriction", value= "use Restriction",position = 8)
    private String useRestriction;
    @ApiModelProperty(name= "barcode", value= "barcode",position = 9)
    private String barcode;
    @ApiModelProperty(name= "summary Holdings", value= "summary Holdings",position = 10)
    private String summaryHoldings;
    @ApiModelProperty(name= "availability", value= "availability",position = 11)
    private String availability;
    @ApiModelProperty(name= "leaderMaterialType", value= "Leader Material Type",position = 12)
    private String leaderMaterialType;
    @ApiModelProperty(name= "selected", value= "selected",position = 13)
    private boolean selected = false;
    @ApiModelProperty(name= "showItems", value= "Show Items",position = 14)
    private boolean showItems = false;
    @ApiModelProperty(name= "selectAllItems", value= "Select All Items",position = 15)
    private boolean selectAllItems = false;
    @ApiModelProperty(name= "searchItemResultRows", value= "Item Results",position = 16)
    private List<SearchItemResultRow> searchItemResultRows = new ArrayList<>();
    @ApiModelProperty(name= "itemId", value= "Item Id",position = 17)
    private Integer itemId;

    private Integer patronBarcode;
    private String requestingInstitution;
    private String deliveryLocation;
    private String requestType;
    private String requestNotes;

    /**
     * Gets bib id.
     *
     * @return the bib id
     */
    public Integer getBibId() {
        return bibId;
    }

    /**
     * Sets bib id.
     *
     * @param bibId the bib id
     */
    public void setBibId(Integer bibId) {
        this.bibId = bibId;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets title.
     *
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets author.
     *
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets author.
     *
     * @param author the author
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets publisher.
     *
     * @return the publisher
     */
    public String getPublisher() {
        return publisher;
    }

    /**
     * Sets publisher.
     *
     * @param publisher the publisher
     */
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    /**
     * Gets publisher date.
     *
     * @return the publisher date
     */
    public String getPublisherDate() {
        return publisherDate;
    }

    /**
     * Sets publisher date.
     *
     * @param publisherDate the publisher date
     */
    public void setPublisherDate(String publisherDate) {
        this.publisherDate = publisherDate;
    }

    /**
     * Gets owning institution.
     *
     * @return the owning institution
     */
    public String getOwningInstitution() {
        return owningInstitution;
    }

    /**
     * Sets owning institution.
     *
     * @param owningInstitution the owning institution
     */
    public void setOwningInstitution(String owningInstitution) {
        this.owningInstitution = owningInstitution;
    }

    /**
     * Gets customer code.
     *
     * @return the customer code
     */
    public String getCustomerCode() {
        return customerCode;
    }

    /**
     * Sets customer code.
     *
     * @param customerCode the customer code
     */
    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    /**
     * Gets collection group designation.
     *
     * @return the collection group designation
     */
    public String getCollectionGroupDesignation() {
        return collectionGroupDesignation;
    }

    /**
     * Sets collection group designation.
     *
     * @param collectionGroupDesignation the collection group designation
     */
    public void setCollectionGroupDesignation(String collectionGroupDesignation) {
        this.collectionGroupDesignation = collectionGroupDesignation;
    }

    /**
     * Gets use restriction.
     *
     * @return the use restriction
     */
    public String getUseRestriction() {
        return useRestriction;
    }

    /**
     * Sets use restriction.
     *
     * @param useRestriction the use restriction
     */
    public void setUseRestriction(String useRestriction) {
        this.useRestriction = useRestriction;
    }

    /**
     * Gets barcode.
     *
     * @return the barcode
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Sets barcode.
     *
     * @param barcode the barcode
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    /**
     * Gets summary holdings.
     *
     * @return the summary holdings
     */
    public String getSummaryHoldings() {
        return summaryHoldings;
    }

    /**
     * Sets summary holdings.
     *
     * @param summaryHoldings the summary holdings
     */
    public void setSummaryHoldings(String summaryHoldings) {
        this.summaryHoldings = summaryHoldings;
    }

    /**
     * Gets availability.
     *
     * @return the availability
     */
    public String getAvailability() {
        return availability;
    }

    /**
     * Sets availability.
     *
     * @param availability the availability
     */
    public void setAvailability(String availability) {
        this.availability = availability;
    }

    /**
     * Gets leader material type.
     *
     * @return the leader material type
     */
    public String getLeaderMaterialType() {
        return leaderMaterialType;
    }

    /**
     * Sets leader material type.
     *
     * @param leaderMaterialType the leader material type
     */
    public void setLeaderMaterialType(String leaderMaterialType) {
        this.leaderMaterialType = leaderMaterialType;
    }

    /**
     * Is selected boolean.
     *
     * @return the boolean
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Sets selected.
     *
     * @param selected the selected
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    /**
     * Is show items boolean.
     *
     * @return the boolean
     */
    public boolean isShowItems() {
        return showItems;
    }

    /**
     * Sets show items.
     *
     * @param showItems the show items
     */
    public void setShowItems(boolean showItems) {
        this.showItems = showItems;
    }

    /**
     * Gets search item result rows.
     *
     * @return the search item result rows
     */
    public List<SearchItemResultRow> getSearchItemResultRows() {
        return searchItemResultRows;
    }

    /**
     * Sets search item result rows.
     *
     * @param searchItemResultRows the search item result rows
     */
    public void setSearchItemResultRows(List<SearchItemResultRow> searchItemResultRows) {
        this.searchItemResultRows = searchItemResultRows;
    }

    /**
     * Is select all items boolean.
     *
     * @return the boolean
     */
    public boolean isSelectAllItems() {
        return selectAllItems;
    }

    /**
     * Sets select all items.
     *
     * @param selectAllItems the select all items
     */
    public void setSelectAllItems(boolean selectAllItems) {
        this.selectAllItems = selectAllItems;
    }

    /**
     * Gets item id.
     *
     * @return the item id
     */
    public Integer getItemId() {
        return itemId;
    }

    /**
     * Sets item id.
     *
     * @param itemId the item id
     */
    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    /**
     * Gets patron barcode.
     *
     * @return the patron barcode
     */
    public Integer getPatronBarcode() {
        return patronBarcode;
    }

    /**
     * Sets patron barcode.
     *
     * @param patronBarcode the patron barcode
     */
    public void setPatronBarcode(Integer patronBarcode) {
        this.patronBarcode = patronBarcode;
    }

    /**
     * Gets requesting institution.
     *
     * @return the requesting institution
     */
    public String getRequestingInstitution() {
        return requestingInstitution;
    }

    /**
     * Sets requesting institution.
     *
     * @param requestingInstitution the requesting institution
     */
    public void setRequestingInstitution(String requestingInstitution) {
        this.requestingInstitution = requestingInstitution;
    }

    /**
     * Gets delivery location.
     *
     * @return the delivery location
     */
    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    /**
     * Sets delivery location.
     *
     * @param deliveryLocation the delivery location
     */
    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    /**
     * Gets request type.
     *
     * @return the request type
     */
    public String getRequestType() {
        return requestType;
    }

    /**
     * Sets request type.
     *
     * @param requestType the request type
     */
    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    /**
     * Gets request notes.
     *
     * @return the request notes
     */
    public String getRequestNotes() {
        return requestNotes;
    }

    /**
     * Sets request notes.
     *
     * @param requestNotes the request notes
     */
    public void setRequestNotes(String requestNotes) {
        this.requestNotes = requestNotes;
    }
}