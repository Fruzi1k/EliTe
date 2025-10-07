package com.example.elite;

import java.io.Serializable;

public class MediaItem implements Serializable {
    private String fileName;
    private String downloadUrl;
    private String type; // "image" or "video"
    private String uploadedBy;
    private long uploadTime;
    private long fileSize;

    public MediaItem() {
    }

    public MediaItem(String fileName, String downloadUrl, String type, String uploadedBy, long uploadTime, long fileSize) {
        this.fileName = fileName;
        this.downloadUrl = downloadUrl;
        this.type = type;
        this.uploadedBy = uploadedBy;
        this.uploadTime = uploadTime;
        this.fileSize = fileSize;
    }

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    public long getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(long uploadTime) {
        this.uploadTime = uploadTime;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public boolean isImage() {
        return "image".equals(type);
    }

    public boolean isVideo() {
        return "video".equals(type);
    }
}