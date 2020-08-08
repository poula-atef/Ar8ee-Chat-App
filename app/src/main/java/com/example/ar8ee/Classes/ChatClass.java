package com.example.ar8ee.Classes;

public class ChatClass {
    private String sender,recever,message,seen;

    public ChatClass() {
    }

    public ChatClass(String sender, String recever, String message, String seen) {
        this.sender = sender;
        this.recever = recever;
        this.message = message;
        this.seen = seen;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getRecever() {
        return recever;
    }

    public void setRecever(String recever) {
        this.recever = recever;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }
}
