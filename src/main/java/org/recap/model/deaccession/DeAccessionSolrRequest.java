package org.recap.model.deaccession;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajeshbabuk on 15/2/17.
 */
public class DeAccessionSolrRequest {

    private List<Integer> bibIds = new ArrayList<>();
    private List<Integer> holdingsIds = new ArrayList<>();
    private List<Integer> itemIds = new ArrayList<>();
    private String status;

    /**
     * Gets bib ids.
     *
     * @return the bib ids
     */
    public List<Integer> getBibIds() {
        return bibIds;
    }

    /**
     * Sets bib ids.
     *
     * @param bibIds the bib ids
     */
    public void setBibIds(List<Integer> bibIds) {
        this.bibIds = bibIds;
    }

    /**
     * Gets holdings ids.
     *
     * @return the holdings ids
     */
    public List<Integer> getHoldingsIds() {
        return holdingsIds;
    }

    /**
     * Sets holdings ids.
     *
     * @param holdingsIds the holdings ids
     */
    public void setHoldingsIds(List<Integer> holdingsIds) {
        this.holdingsIds = holdingsIds;
    }

    /**
     * Gets item ids.
     *
     * @return the item ids
     */
    public List<Integer> getItemIds() {
        return itemIds;
    }

    /**
     * Sets item ids.
     *
     * @param itemIds the item ids
     */
    public void setItemIds(List<Integer> itemIds) {
        this.itemIds = itemIds;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets status.
     *
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }
}
