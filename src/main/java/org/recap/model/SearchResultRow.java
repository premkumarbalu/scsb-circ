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

    public Integer getBibId() {
        return bibId;
    }

    public void setBibId(Integer bibId) {
        this.bibId = bibId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getPublisherDate() {
        return publisherDate;
    }

    public void setPublisherDate(String publisherDate) {
        this.publisherDate = publisherDate;
    }

    public String getOwningInstitution() {
        return owningInstitution;
    }

    public void setOwningInstitution(String owningInstitution) {
        this.owningInstitution = owningInstitution;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCollectionGroupDesignation() {
        return collectionGroupDesignation;
    }

    public void setCollectionGroupDesignation(String collectionGroupDesignation) {
        this.collectionGroupDesignation = collectionGroupDesignation;
    }

    public String getUseRestriction() {
        return useRestriction;
    }

    public void setUseRestriction(String useRestriction) {
        this.useRestriction = useRestriction;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getSummaryHoldings() {
        return summaryHoldings;
    }

    public void setSummaryHoldings(String summaryHoldings) {
        this.summaryHoldings = summaryHoldings;
    }

    public String getAvailability() {
        return availability;
    }

    public void setAvailability(String availability) {
        this.availability = availability;
    }

    public String getLeaderMaterialType() {
        return leaderMaterialType;
    }

    public void setLeaderMaterialType(String leaderMaterialType) {
        this.leaderMaterialType = leaderMaterialType;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isShowItems() {
        return showItems;
    }

    public void setShowItems(boolean showItems) {
        this.showItems = showItems;
    }

    public List<SearchItemResultRow> getSearchItemResultRows() {
        return searchItemResultRows;
    }

    public void setSearchItemResultRows(List<SearchItemResultRow> searchItemResultRows) {
        this.searchItemResultRows = searchItemResultRows;
    }

    public boolean isSelectAllItems() {
        return selectAllItems;
    }

    public void setSelectAllItems(boolean selectAllItems) {
        this.selectAllItems = selectAllItems;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getPatronBarcode() {
        return patronBarcode;
    }

    public void setPatronBarcode(Integer patronBarcode) {
        this.patronBarcode = patronBarcode;
    }

    public String getRequestingInstitution() {
        return requestingInstitution;
    }

    public void setRequestingInstitution(String requestingInstitution) {
        this.requestingInstitution = requestingInstitution;
    }

    public String getDeliveryLocation() {
        return deliveryLocation;
    }

    public void setDeliveryLocation(String deliveryLocation) {
        this.deliveryLocation = deliveryLocation;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

    public String getRequestNotes() {
        return requestNotes;
    }

    public void setRequestNotes(String requestNotes) {
        this.requestNotes = requestNotes;
    }
}