package id.ketemu.model;

import id.ketemu.util.Fmt;

import java.time.LocalDateTime;

public class Location {

    private int id;
    private String name;
    private String zone;
    private boolean skkPost;
    private LocalDateTime createdAt;
    private int itemCount;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public boolean isSkkPost() {
        return skkPost;
    }

    public void setSkkPost(boolean skkPost) {
        this.skkPost = skkPost;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAdded() {
        return Fmt.date(createdAt == null ? null : createdAt.toLocalDate());
    }

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }
}
