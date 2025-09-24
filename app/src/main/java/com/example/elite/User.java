package com.example.elite;

public class User {
    private  String uid;
    private  String email;

    private String password;
    private  String role;

    public User() {
    }

    public User(String uid, String email, String password, String role) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.role = role;

    }

    public String getPassword() {
        return password;
    }

    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }
}
