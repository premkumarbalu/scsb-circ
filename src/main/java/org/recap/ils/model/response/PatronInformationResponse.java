package org.recap.ils.model.response;

import java.util.List;

/**
 * Created by sudhishk on 26/12/16.
 */
public class PatronInformationResponse extends AbstractResponseItem {

    /**
     * The Patron identifier.
     */
    String patronIdentifier = "";
    /**
     * The Patron name.
     */
    String patronName = "";
    /**
     * The Email.
     */
    String Email = "";
    /**
     * The Birth date.
     */
    String BirthDate;
    /**
     * The Phone.
     */
    String Phone;
    /**
     * The Permanent location.
     */
    String PermanentLocation;
    /**
     * The Pickup location.
     */
    String PickupLocation;
    /**
     * The Charged items count.
     */
    int ChargedItemsCount;
    /**
     * The Charged items limit.
     */
    int ChargedItemsLimit;
    /**
     * The Fee limit.
     */
    String FeeLimit;
    /**
     * The Fee type.
     */
    String FeeType;
    /**
     * The Hold items count.
     */
    int HoldItemsCount;
    /**
     * The Hold items limit.
     */
    int HoldItemsLimit;
    /**
     * The Unavailable holds count.
     */
    int UnavailableHoldsCount;
    /**
     * The Fine items count.
     */
    int FineItemsCount;
    /**
     * The Fee amount.
     */
    String FeeAmount;
    /**
     * The Home address.
     */
    String HomeAddress;
    /**
     * The Items.
     */
    List<String> Items;
    /**
     * The Item type.
     */
    String ItemType;
    /**
     * The Overdue items count.
     */
    int OverdueItemsCount;
    /**
     * The Overdue items limit.
     */
    int OverdueItemsLimit;
    /**
     * The Pac access type.
     */
    String PacAccessType;
    /**
     * The Patron group.
     */
    String PatronGroup;
    /**
     * The Patron type.
     */
    String PatronType;
    /**
     * The Due date.
     */
    String DueDate;
    /**
     * The Expiration date.
     */
    String ExpirationDate;
    /**
     * The Status.
     */
    String Status;

    /**
     * Gets patron identifier.
     *
     * @return the patron identifier
     */
    public String getPatronIdentifier() {
        return patronIdentifier;
    }

    /**
     * Sets patron identifier.
     *
     * @param patronIdentifier the patron identifier
     */
    public void setPatronIdentifier(String patronIdentifier) {
        this.patronIdentifier = patronIdentifier;
    }

    /**
     * Gets patron name.
     *
     * @return the patron name
     */
    public String getPatronName() {
        return patronName;
    }

    /**
     * Sets patron name.
     *
     * @param patronName the patron name
     */
    public void setPatronName(String patronName) {
        this.patronName = patronName;
    }

    /**
     * Gets email.
     *
     * @return the email
     */
    public String getEmail() {
        return Email;
    }

    /**
     * Sets email.
     *
     * @param email the email
     */
    public void setEmail(String email) {
        Email = email;
    }

    /**
     * Gets birth date.
     *
     * @return the birth date
     */
    public String getBirthDate() {
        return BirthDate;
    }

    /**
     * Sets birth date.
     *
     * @param birthDate the birth date
     */
    public void setBirthDate(String birthDate) {
        BirthDate = birthDate;
    }

    /**
     * Gets phone.
     *
     * @return the phone
     */
    public String getPhone() {
        return Phone;
    }

    /**
     * Sets phone.
     *
     * @param phone the phone
     */
    public void setPhone(String phone) {
        Phone = phone;
    }

    /**
     * Gets permanent location.
     *
     * @return the permanent location
     */
    public String getPermanentLocation() {
        return PermanentLocation;
    }

    /**
     * Sets permanent location.
     *
     * @param permanentLocation the permanent location
     */
    public void setPermanentLocation(String permanentLocation) {
        PermanentLocation = permanentLocation;
    }

    /**
     * Gets pickup location.
     *
     * @return the pickup location
     */
    public String getPickupLocation() {
        return PickupLocation;
    }

    /**
     * Sets pickup location.
     *
     * @param pickupLocation the pickup location
     */
    public void setPickupLocation(String pickupLocation) {
        PickupLocation = pickupLocation;
    }

    /**
     * Gets charged items count.
     *
     * @return the charged items count
     */
    public int getChargedItemsCount() {
        return ChargedItemsCount;
    }

    /**
     * Sets charged items count.
     *
     * @param chargedItemsCount the charged items count
     */
    public void setChargedItemsCount(int chargedItemsCount) {
        ChargedItemsCount = chargedItemsCount;
    }

    /**
     * Gets charged items limit.
     *
     * @return the charged items limit
     */
    public int getChargedItemsLimit() {
        return ChargedItemsLimit;
    }

    /**
     * Sets charged items limit.
     *
     * @param chargedItemsLimit the charged items limit
     */
    public void setChargedItemsLimit(int chargedItemsLimit) {
        ChargedItemsLimit = chargedItemsLimit;
    }

    /**
     * Gets fee limit.
     *
     * @return the fee limit
     */
    public String getFeeLimit() {
        return FeeLimit;
    }

    /**
     * Sets fee limit.
     *
     * @param feeLimit the fee limit
     */
    public void setFeeLimit(String feeLimit) {
        FeeLimit = feeLimit;
    }

    /**
     * Gets fee type.
     *
     * @return the fee type
     */
    public String getFeeType() {
        return FeeType;
    }

    /**
     * Sets fee type.
     *
     * @param feeType the fee type
     */
    public void setFeeType(String feeType) {
        FeeType = feeType;
    }

    /**
     * Gets hold items count.
     *
     * @return the hold items count
     */
    public int getHoldItemsCount() {
        return HoldItemsCount;
    }

    /**
     * Sets hold items count.
     *
     * @param holdItemsCount the hold items count
     */
    public void setHoldItemsCount(int holdItemsCount) {
        HoldItemsCount = holdItemsCount;
    }

    /**
     * Gets hold items limit.
     *
     * @return the hold items limit
     */
    public int getHoldItemsLimit() {
        return HoldItemsLimit;
    }

    /**
     * Sets hold items limit.
     *
     * @param holdItemsLimit the hold items limit
     */
    public void setHoldItemsLimit(int holdItemsLimit) {
        HoldItemsLimit = holdItemsLimit;
    }

    /**
     * Gets unavailable holds count.
     *
     * @return the unavailable holds count
     */
    public int getUnavailableHoldsCount() {
        return UnavailableHoldsCount;
    }

    /**
     * Sets unavailable holds count.
     *
     * @param unavailableHoldsCount the unavailable holds count
     */
    public void setUnavailableHoldsCount(int unavailableHoldsCount) {
        UnavailableHoldsCount = unavailableHoldsCount;
    }

    /**
     * Gets fine items count.
     *
     * @return the fine items count
     */
    public int getFineItemsCount() {
        return FineItemsCount;
    }

    /**
     * Sets fine items count.
     *
     * @param fineItemsCount the fine items count
     */
    public void setFineItemsCount(int fineItemsCount) {
        FineItemsCount = fineItemsCount;
    }

    /**
     * Gets fee amount.
     *
     * @return the fee amount
     */
    public String getFeeAmount() {
        return FeeAmount;
    }

    /**
     * Sets fee amount.
     *
     * @param feeAmount the fee amount
     */
    public void setFeeAmount(String feeAmount) {
        FeeAmount = feeAmount;
    }

    /**
     * Gets home address.
     *
     * @return the home address
     */
    public String getHomeAddress() {
        return HomeAddress;
    }

    /**
     * Sets home address.
     *
     * @param homeAddress the home address
     */
    public void setHomeAddress(String homeAddress) {
        HomeAddress = homeAddress;
    }

    /**
     * Gets items.
     *
     * @return the items
     */
    public List<String> getItems() {
        return Items;
    }

    /**
     * Sets items.
     *
     * @param items the items
     */
    public void setItems(List<String> items) {
        Items = items;
    }

    /**
     * Gets item type.
     *
     * @return the item type
     */
    public String getItemType() {
        return ItemType;
    }

    /**
     * Sets item type.
     *
     * @param itemType the item type
     */
    public void setItemType(String itemType) {
        ItemType = itemType;
    }

    /**
     * Gets overdue items count.
     *
     * @return the overdue items count
     */
    public int getOverdueItemsCount() {
        return OverdueItemsCount;
    }

    /**
     * Sets overdue items count.
     *
     * @param overdueItemsCount the overdue items count
     */
    public void setOverdueItemsCount(int overdueItemsCount) {
        OverdueItemsCount = overdueItemsCount;
    }

    /**
     * Gets overdue items limit.
     *
     * @return the overdue items limit
     */
    public int getOverdueItemsLimit() {
        return OverdueItemsLimit;
    }

    /**
     * Sets overdue items limit.
     *
     * @param overdueItemsLimit the overdue items limit
     */
    public void setOverdueItemsLimit(int overdueItemsLimit) {
        OverdueItemsLimit = overdueItemsLimit;
    }

    /**
     * Gets pac access type.
     *
     * @return the pac access type
     */
    public String getPacAccessType() {
        return PacAccessType;
    }

    /**
     * Sets pac access type.
     *
     * @param pacAccessType the pac access type
     */
    public void setPacAccessType(String pacAccessType) {
        PacAccessType = pacAccessType;
    }

    /**
     * Gets patron group.
     *
     * @return the patron group
     */
    public String getPatronGroup() {
        return PatronGroup;
    }

    /**
     * Sets patron group.
     *
     * @param patronGroup the patron group
     */
    public void setPatronGroup(String patronGroup) {
        PatronGroup = patronGroup;
    }

    /**
     * Gets patron type.
     *
     * @return the patron type
     */
    public String getPatronType() {
        return PatronType;
    }

    /**
     * Sets patron type.
     *
     * @param patronType the patron type
     */
    public void setPatronType(String patronType) {
        PatronType = patronType;
    }

    /**
     * Gets due date.
     *
     * @return the due date
     */
    public String getDueDate() {
        return DueDate;
    }

    /**
     * Sets due date.
     *
     * @param dueDate the due date
     */
    public void setDueDate(String dueDate) {
        DueDate = dueDate;
    }

    /**
     * Gets expiration date.
     *
     * @return the expiration date
     */
    public String getExpirationDate() {
        return ExpirationDate;
    }

    /**
     * Sets expiration date.
     *
     * @param expirationDate the expiration date
     */
    public void setExpirationDate(String expirationDate) {
        ExpirationDate = expirationDate;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public String getStatus() {
        return Status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        Status = status;
    }
}
