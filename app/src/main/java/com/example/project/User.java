package com.example.project;

import java.util.List;

public class User {
    private String id;
    private String name;
    private String role;
    private String address;

    public User() {
    }

    public User(String id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.address = "";
    }

    public User(String id, String name, String role, String address) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.address = address;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}