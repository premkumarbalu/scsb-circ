package org.recap.gfa.model;

import java.util.List;

/**
 * Created by sudhishk on 27/1/17.
 */
public class RetrieveItemEDDRequest {

    private List<TtitemEDDResponse> ttitem;

    /**
     * Gets ttitem.
     *
     * @return the ttitem
     */
    public List<TtitemEDDResponse> getTtitem() {
        return ttitem;
    }

    /**
     * Sets ttitem.
     *
     * @param ttitem the ttitem
     */
    public void setTtitem(List<TtitemEDDResponse> ttitem) {
        this.ttitem = ttitem;
    }
}
