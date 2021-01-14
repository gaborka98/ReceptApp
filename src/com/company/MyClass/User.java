package com.company.MyClass;

import java.util.HashMap;

public class User {
    private String username;
    private String hash;
    private String email;
    private Boolean isModerator;
    private int storageId;

    public User(String username, String hash, String email, Boolean isModerator) {
        this.username = username;
        this.hash = hash;
        this.email = email;
        this.isModerator = isModerator;
        this.storageId = -1;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setModerator(Boolean moderator) {
        isModerator = moderator;
    }

    public Boolean getModerator() {
        return isModerator;
    }

    public int getStorageId() {
        return storageId;
    }

    public void setStorageId(int storageId) {
        this.storageId = storageId;
    }
}
