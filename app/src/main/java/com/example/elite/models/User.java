package com.example.elite.models;

import java.io.Serializable;

public class User implements Serializable {
    private String uid;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private String phone;
    private String position; // должность (раньше было role)

    public User() {
    }

    public User(String uid, String email, String password, String position, String firstName, String lastName) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.position = position;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(String uid, String email, String password, String position, String firstName, String lastName, String phone) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.position = position;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
    }

    // Getters
    public String getUid() {
        return uid;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPosition() {
        return position;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhone() {
        return phone;
    }




    // Setters
    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    // Helper methods
    public String getFullName() {
        StringBuilder fullName = new StringBuilder();
        if (firstName != null && !firstName.trim().isEmpty()) {
            fullName.append(firstName);
        }
        if (lastName != null && !lastName.trim().isEmpty()) {
            if (fullName.length() > 0) fullName.append(" ");
            fullName.append(lastName);
        }
        return fullName.length() > 0 ? fullName.toString() : "Пользователь";
    }

    public boolean isDirector() {
        return "director".equalsIgnoreCase(position);
    }

    public boolean isWorker() {
        return "worker".equalsIgnoreCase(position);
    }
}
