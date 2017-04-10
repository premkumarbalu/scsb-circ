package org.recap.gfa.model;

import java.util.List;

/**
 * Created by sudhishk on 27/1/17.
 */
public class RetrieveItemRequest {

    private List<TtitemRequest> ttitem;

    /**
     * Gets ttitem.
     *
     * @return the ttitem
     */
    public List<TtitemRequest> getTtitem() {
        return ttitem;
    }

    /**
     * Sets ttitem.
     *
     * @param ttitem the ttitem
     */
    public void setTtitem(List<TtitemRequest> ttitem) {
        this.ttitem = ttitem;
    }
}
