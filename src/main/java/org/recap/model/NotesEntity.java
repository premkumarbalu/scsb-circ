package org.recap.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by rajeshbabuk on 26/10/16.
 */

@Entity
@Table(name = "notes_t", schema = "recap", catalog = "")
public class NotesEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "NOTES_ID")
    private Integer notesId;

    @Column(name = "NOTES")
    private String notes;

    @Column(name = "ITEM_ID")
    private Integer itemId;

    @Column(name = "REQUEST_ID")
    private Integer requestId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "REQUEST_ID", insertable = false, updatable = false)
    private RequestItemEntity requestItemEntity;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ITEM_ID", referencedColumnName = "ITEM_ID", insertable = false, updatable = false)
    private ItemEntity itemEntity;

    public Integer getNotesId() {
        return notesId;
    }

    public void setNotesId(Integer notesId) {
        this.notesId = notesId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Integer getItemId() {
        return itemId;
    }

    public void setItemId(Integer itemId) {
        this.itemId = itemId;
    }

    public Integer getRequestId() {
        return requestId;
    }

    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    public RequestItemEntity getRequestItemEntity() {
        return requestItemEntity;
    }

    public void setRequestItemEntity(RequestItemEntity requestItemEntity) {
        this.requestItemEntity = requestItemEntity;
    }

    public ItemEntity getItemEntity() {
        return itemEntity;
    }

    public void setItemEntity(ItemEntity itemEntity) {
        this.itemEntity = itemEntity;
    }
}
