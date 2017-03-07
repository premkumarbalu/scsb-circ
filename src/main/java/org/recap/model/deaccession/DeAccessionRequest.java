package org.recap.model.deaccession;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchulakshmig on 11/10/16.
 */
public class DeAccessionRequest {

    private List<DeAccessionItem> deAccessionItems = new ArrayList<>();
    private String username;

    public List<DeAccessionItem> getDeAccessionItems() {
        return deAccessionItems;
    }

    public void setDeAccessionItems(List<DeAccessionItem> deAccessionItems) {
        this.deAccessionItems = deAccessionItems;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
