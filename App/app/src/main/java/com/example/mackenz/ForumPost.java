package com.example.mackenz;

import java.util.ArrayList;
import java.util.List;

public class ForumPost {
    private String id;
    private String Title;
    private String By;
    private String Content;
    private List<Reply> Replies;

    public ForumPost() {
        Replies = new ArrayList<>();
    }

    public ForumPost(String id, String Title, String By, String Content) {
        this.id = id;
        this.Title = Title;
        this.By = By;
        this.Content = Content;
        this.Replies = new ArrayList<>();
    }

    public <E> ForumPost(Object o, String title, String content, String username, ArrayList<E> es) {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String Title) {
        this.Title = Title;
    }

    public String getBy() {
        return By;
    }

    public void setBy(String By) {
        this.By = By;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String Content) {
        this.Content = Content;
    }

    public List<Reply> getReplies() {
        return Replies;
    }

    public void setReplies(List<Reply> Replies) {
        this.Replies = Replies;
    }
}
