package org.recap.ils.model.response;

import java.util.List;

/**
 * Created by sudhishk on 26/12/16.
 */
public class PatronInformationResponse extends AbstractResponseItem {

    String patronIdentifier = "";
    String patronName = "";
    String Email = "";
    String BirthDate;
    String Phone;
    String PermanentLocation;
    String PickupLocation;
    int ChargedItemsCount;
    int ChargedItemsLimit;
    String FeeLimit;
    String FeeType;
    int HoldItemsCount;
    int HoldItemsLimit;
    int UnavailableHoldsCount;
    int FineItemsCount;
    String FeeAmount;
    String HomeAddress;
    List<String> Items;
    String ItemType;
    int OverdueItemsCount;
    int OverdueItemsLimit;
    String PacAccessType;
    String PatronGroup;
    String PatronType;
    String DueDate;
    String ExpirationDate;
    String Status;

    public String getPatronIdentifier() {
        return patronIdentifier;
    }

    public void setPatronIdentifier(String patronIdentifier) {
        this.patronIdentifier = patronIdentifier;
    }

    public String getPatronName() {
        return patronName;
    }

    public void setPatronName(String patronName) {
        this.patronName = patronName;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getBirthDate() {
        return BirthDate;
    }

    public void setBirthDate(String birthDate) {
        BirthDate = birthDate;
    }

    public String getPhone() {
        return Phone;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getPermanentLocation() {
        return PermanentLocation;
    }

    public void setPermanentLocation(String permanentLocation) {
        PermanentLocation = permanentLocation;
    }

    public String getPickupLocation() {
        return PickupLocation;
    }

    public void setPickupLocation(String pickupLocation) {
        PickupLocation = pickupLocation;
    }

    public int getChargedItemsCount() {
        return ChargedItemsCount;
    }

    public void setChargedItemsCount(int chargedItemsCount) {
        ChargedItemsCount = chargedItemsCount;
    }

    public int getChargedItemsLimit() {
        return ChargedItemsLimit;
    }

    public void setChargedItemsLimit(int chargedItemsLimit) {
        ChargedItemsLimit = chargedItemsLimit;
    }

    public String getFeeLimit() {
        return FeeLimit;
    }

    public void setFeeLimit(String feeLimit) {
        FeeLimit = feeLimit;
    }

    public String getFeeType() {
        return FeeType;
    }

    public void setFeeType(String feeType) {
        FeeType = feeType;
    }

    public int getHoldItemsCount() {
        return HoldItemsCount;
    }

    public void setHoldItemsCount(int holdItemsCount) {
        HoldItemsCount = holdItemsCount;
    }

    public int getHoldItemsLimit() {
        return HoldItemsLimit;
    }

    public void setHoldItemsLimit(int holdItemsLimit) {
        HoldItemsLimit = holdItemsLimit;
    }

    public int getUnavailableHoldsCount() {
        return UnavailableHoldsCount;
    }

    public void setUnavailableHoldsCount(int unavailableHoldsCount) {
        UnavailableHoldsCount = unavailableHoldsCount;
    }

    public int getFineItemsCount() {
        return FineItemsCount;
    }

    public void setFineItemsCount(int fineItemsCount) {
        FineItemsCount = fineItemsCount;
    }

    public String getFeeAmount() {
        return FeeAmount;
    }

    public void setFeeAmount(String feeAmount) {
        FeeAmount = feeAmount;
    }

    public String getHomeAddress() {
        return HomeAddress;
    }

    public void setHomeAddress(String homeAddress) {
        HomeAddress = homeAddress;
    }

    public List<String> getItems() {
        return Items;
    }

    public void setItems(List<String> items) {
        Items = items;
    }

    public String getItemType() {
        return ItemType;
    }

    public void setItemType(String itemType) {
        ItemType = itemType;
    }

    public int getOverdueItemsCount() {
        return OverdueItemsCount;
    }

    public void setOverdueItemsCount(int overdueItemsCount) {
        OverdueItemsCount = overdueItemsCount;
    }

    public int getOverdueItemsLimit() {
        return OverdueItemsLimit;
    }

    public void setOverdueItemsLimit(int overdueItemsLimit) {
        OverdueItemsLimit = overdueItemsLimit;
    }

    public String getPacAccessType() {
        return PacAccessType;
    }

    public void setPacAccessType(String pacAccessType) {
        PacAccessType = pacAccessType;
    }

    public String getPatronGroup() {
        return PatronGroup;
    }

    public void setPatronGroup(String patronGroup) {
        PatronGroup = patronGroup;
    }

    public String getPatronType() {
        return PatronType;
    }

    public void setPatronType(String patronType) {
        PatronType = patronType;
    }

    public String getDueDate() {
        return DueDate;
    }

    public void setDueDate(String dueDate) {
        DueDate = dueDate;
    }

    public String getExpirationDate() {
        return ExpirationDate;
    }

    public void setExpirationDate(String expirationDate) {
        ExpirationDate = expirationDate;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
