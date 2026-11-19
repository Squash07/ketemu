package id.ketemu.model;

import id.ketemu.util.Fmt;

import java.time.LocalDateTime;

public class WatchlistEntry {

    private int userId;
    private int itemId;
    private String note;
    private LocalDateTime createdAt;
    private Item item;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public String getSaved() {
        return Fmt.ago(createdAt);
    }
}
