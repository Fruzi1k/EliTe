package com.example.elite;

public class User {
    private String uid;
    private String email;
    private String password;
    private String role;
    private String firstName;
    private String lastName;
    private String phone;
    private String position; // должность

    public User() {
    }

    public User(String uid, String email, String password, String role) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public User(String uid, String email, String password, String role, String firstName, String lastName, String phone, String position) {
        this.uid = uid;
        this.email = email;
        this.password = password;
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.position = position;
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

    public String getRole() {
        return role;
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

    public String getPosition() {
        return position;
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

    public void setRole(String role) {
        this.role = role;
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

    public void setPosition(String position) {
        this.position = position;
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
        return "director".equalsIgnoreCase(role);
    }

    public boolean isWorker() {
        return "worker".equalsIgnoreCase(role);
    }
}
