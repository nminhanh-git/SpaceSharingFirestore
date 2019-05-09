package com.example.nminhanh.spacesharing.Model;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class UserFavoriteSpace  implements Serializable {
    String id;
    String ownerId;

    String title;
    String address;
    @ServerTimestamp
    Date timeAdded;

    public UserFavoriteSpace() {
    }

    public UserFavoriteSpace(String id, String ownerId, String title, String address) {
        this.id = id;
        this.ownerId = ownerId;
        this.title = title;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Date timeAdded) {
        this.timeAdded = timeAdded;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }
}
