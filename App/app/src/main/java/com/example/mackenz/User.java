package com.example.mackenz;

import java.util.List;

public class User {
    private String username;
    private List<String> coursesTaken;
    private String password;
    private String profilePicUrl;

    // Default constructor required for calls to DataSnapshot.getValue(User.class)
    public User() {
    }

    public User(String username, List<String> coursesTaken, String password, String profilePicUrl) {
        this.username = username;
        this.coursesTaken = coursesTaken;
        this.password = password;
        this.profilePicUrl = profilePicUrl;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getCoursesTaken() {
        return coursesTaken;
    }

    public String getPassword() {
        return password;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }
}
