package com.example.nminhanh.spacesharing.Model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {
    String id;
    String senderId;
    String messageImage;
    String message;
    long timeAdded;
    boolean isRead;

    public Message() {
    }

    public Message(String senderId, String messageImage, String message, long timeAdded, boolean isRead) {
        this.senderId = senderId;
        this.messageImage = messageImage;
        this.message = message;
        this.timeAdded = timeAdded;
        this.isRead = isRead;
    }

    public Message(String senderId, String messageImage, String message, boolean isRead) {
        this.senderId = senderId;
        this.messageImage = messageImage;
        this.message = message;
        this.isRead = isRead;
    }

    public Message(String senderId, String message, boolean isRead) {
        this.senderId = senderId;
        this.message = message;
        this.isRead = isRead;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessageImage() {
        return messageImage;
    }

    public void setMessageImage(String messageImage) {
        this.messageImage = messageImage;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getTimeAdded() {
        return timeAdded;
    }

    public void setTimeAdded(long timeAdded) {
        this.timeAdded = timeAdded;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
}
