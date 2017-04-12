package org.recap.gfa.model;

/**
 * Created by sudhishk on 8/12/16.
 */
public class GFARetrieveItemResponse {

    private RetrieveItem retrieveItem;
    private boolean success;
    private String screnMessage;


    /**
     * Gets retrieve item.
     *
     * @return the retrieve item
     */
    public RetrieveItem getRetrieveItem() {
        return retrieveItem;
    }

    /**
     * Sets retrieve item.
     *
     * @param retrieveItem the retrieve item
     */
    public void setRetrieveItem(RetrieveItem retrieveItem) {
        this.retrieveItem = retrieveItem;
    }

    /**
     * Is success boolean.
     *
     * @return the boolean
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Sets success.
     *
     * @param success the success
     */
    public void setSuccess(boolean success) {
        this.success = success;
    }

    /**
     * Gets scren message.
     *
     * @return the scren message
     */
    public String getScrenMessage() {
        return screnMessage;
    }

    /**
     * Sets scren message.
     *
     * @param screnMessage the scren message
     */
    public void setScrenMessage(String screnMessage) {
        this.screnMessage = screnMessage;
    }
}
