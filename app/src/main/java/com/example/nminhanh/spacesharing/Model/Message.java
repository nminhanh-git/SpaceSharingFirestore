package com.example.nminhanh.spacesharing.Model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Message {
    String senderId;
    String messageImage;
    String message;
    long timeAdded;

    public Message() {
    }

    public Message(String senderId, String messageImage, String message, long timeAdded) {
        this.senderId = senderId;
        this.messageImage = messageImage;
        this.message = message;
        this.timeAdded = timeAdded;
    }

    public Message(String senderId, String messageImage, String message) {
        this.senderId = senderId;
        this.messageImage = messageImage;
        this.message = message;
    }

    public Message(String senderId, String message) {
        this.senderId = senderId;
        this.message = message;
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
}
