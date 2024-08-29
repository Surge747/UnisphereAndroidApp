package com.example.mackenz;

public class Reply {
    private String id;
    private String Content;
    private String By;

    public Reply() {
        // Default constructor required for calls to DataSnapshot.getValue(Reply.class)
    }

    public Reply(String id, String By, String Content) {
        this.id = id;
        this.By = By;
        this.Content = Content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String Content) {
        this.Content = Content;
    }

    public String getBy() {
        return By;
    }

    public void setBy(String By) {
        this.By = By;
    }
}
