package org.recap.model.deAccession;

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

    public List<Integer> getBibIds() {
        return bibIds;
    }

    public void setBibIds(List<Integer> bibIds) {
        this.bibIds = bibIds;
    }

    public List<Integer> getHoldingsIds() {
        return holdingsIds;
    }

    public void setHoldingsIds(List<Integer> holdingsIds) {
        this.holdingsIds = holdingsIds;
    }

    public List<Integer> getItemIds() {
        return itemIds;
    }

    public void setItemIds(List<Integer> itemIds) {
        this.itemIds = itemIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
