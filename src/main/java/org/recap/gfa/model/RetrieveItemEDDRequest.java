package org.recap.gfa.model;

import java.util.List;

/**
 * Created by sudhishk on 27/1/17.
 */
public class RetrieveItemEDDRequest {

    private List<TtitemEDDRequest> ttitem;

    /**
     * Gets ttitem.
     *
     * @return the ttitem
     */
    public List<TtitemEDDRequest> getTtitem() {
        return ttitem;
    }

    /**
     * Sets ttitem.
     *
     * @param ttitem the ttitem
     */
    public void setTtitem(List<TtitemEDDRequest> ttitem) {
        this.ttitem = ttitem;
    }
}
