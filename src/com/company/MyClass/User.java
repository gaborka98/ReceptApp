package com.company.MyClass;

import java.util.HashMap;

public class User {
    private int id;
    private String username;
    private String hash;
    private String email;
    private Boolean isModerator;
    private int storageId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public User(String username, String hash, String email, Boolean isModerator){
        this.id = -1;
        this.username = username;
        this.hash = hash;
        this.email = email;
        this.isModerator = isModerator;
        this.storageId = -1;
    }

    public User(int id, String username, String hash, String email, Boolean isModerator) {
        this.id = id;
        this.username = username;
        this.hash = hash;
        this.email = email;
        this.isModerator = isModerator;
        this.storageId = -1;
    }

    public User(int id, String username, String hash, String email, Boolean isModerator, int storageId) {
        this.id = id;
        this.username = username;
        this.hash = hash;
        this.email = email;
        this.isModerator = isModerator;
        this.storageId = storageId;
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
