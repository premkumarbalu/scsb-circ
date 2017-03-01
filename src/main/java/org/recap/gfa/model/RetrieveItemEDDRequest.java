package org.recap.gfa.model;

import java.util.List;

/**
 * Created by sudhishk on 27/1/17.
 */
public class RetrieveItemEDDRequest {

    private List<TtitemEDDRequest> ttitem;

    public List<TtitemEDDRequest> getTtitem() {
        return ttitem;
    }

    public void setTtitem(List<TtitemEDDRequest> ttitem) {
        this.ttitem = ttitem;
    }
}
