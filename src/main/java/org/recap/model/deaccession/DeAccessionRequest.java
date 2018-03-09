package org.recap.model.deaccession;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenchulakshmig on 11/10/16.
 */
public class DeAccessionRequest {

    private List<DeAccessionItem> deAccessionItems = new ArrayList<>();
    private String username;
    private String notes;

    /**
     * Gets de accession items.
     *
     * @return the de accession items
     */
    public List<DeAccessionItem> getDeAccessionItems() {
        return deAccessionItems;
    }

    /**
     * Sets de accession items.
     *
     * @param deAccessionItems the de accession items
     */
    public void setDeAccessionItems(List<DeAccessionItem> deAccessionItems) {
        this.deAccessionItems = deAccessionItems;
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets username.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets notes.
     *
     * @return the notes
     */
    public String getNotes() {
        return notes;
    }

    /**
     * Sets notes.
     *
     * @param notes the notes
     */
    public void setNotes(String notes) {
        this.notes = notes;
    }
}
