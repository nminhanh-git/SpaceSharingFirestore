package com.example.nminhanh.spacesharing.Model;

import com.google.firebase.firestore.ServerTimestamp;

import java.io.Serializable;
import java.util.Date;

public class UserFavoriteSpace  implements Serializable {
    String id;

    @ServerTimestamp
    Date timeAdded;

    public UserFavoriteSpace() {
    }

    public UserFavoriteSpace(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }



    public Date getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(Date timeAdded) {
        this.timeAdded = timeAdded;
    }
}
