package org.recap.gfa.model;

/**
 * Created by sudhishk on 8/12/16.
 */
public class GFAEddItemResponse {


    private boolean success;
    private String screnMessage;
    private RetrieveItemEDDRequest retrieveEDD;


    /**
     * Gets retrieve edd.
     *
     * @return the retrieve edd
     */
    public RetrieveItemEDDRequest getRetrieveEDD() {
        return retrieveEDD;
    }

    /**
     * Sets retrieve edd.
     *
     * @param retrieveEDD the retrieve edd
     */
    public void setRetrieveEDD(RetrieveItemEDDRequest retrieveEDD) {
        this.retrieveEDD = retrieveEDD;
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
