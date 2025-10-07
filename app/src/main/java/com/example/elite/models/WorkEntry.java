package com.example.elite.models;

import java.io.Serializable;
import java.util.Date;

public class WorkEntry implements Serializable {
    private String id;
    private String userId;
    private String userName;
    private String userPosition;
    private String projectId;
    private String projectName;
    private Date workDate;
    private double hoursWorked;
    private String description;
    private Date createdAt;
    private Date updatedAt;

    public WorkEntry() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    public WorkEntry(String userId, String userName, String userPosition, 
                    String projectId, String projectName, Date workDate, 
                    double hoursWorked, String description) {
        this();
        this.userId = userId;
        this.userName = userName;
        this.userPosition = userPosition;
        this.projectId = projectId;
        this.projectName = projectName;
        this.workDate = workDate;
        this.hoursWorked = hoursWorked;
        this.description = description;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPosition() {
        return userPosition;
    }

    public void setUserPosition(String userPosition) {
        this.userPosition = userPosition;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public Date getWorkDate() {
        return workDate;
    }

    public void setWorkDate(Date workDate) {
        this.workDate = workDate;
    }

    public double getHoursWorked() {
        return hoursWorked;
    }

    public void setHoursWorked(double hoursWorked) {
        this.hoursWorked = hoursWorked;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}