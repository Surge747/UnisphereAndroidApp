package com.example.mackenz;

public class Message {
    private String text;
    private boolean isUser; // True if this message is sent by the user, false if received

    public Message(String text, boolean isUser) {
        this.text = text;
        this.isUser = isUser;
    }

    public static Message get(int adapterPosition) {
        return null;
    }

    public String getText() {
        return text;
    }

    public boolean isUser() {
        return isUser;
    }
}

