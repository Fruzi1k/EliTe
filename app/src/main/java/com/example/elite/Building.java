package com.example.elite;

import java.io.Serializable;

public class Building implements Serializable {
    private String id;
    private String name; // Project name
    private String code; // Postal code
    private String city;
    private String street; // Street address
    private String googleMapsUrl; // Google Maps URL

    public Building() {
    }

    public Building(String id, String name, String city, String street, String code, String googleMapsUrl) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.street = street;
        this.code = code;
        this.googleMapsUrl = googleMapsUrl;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getGoogleMapsUrl() {
        return googleMapsUrl;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public void setGoogleMapsUrl(String googleMapsUrl) {
        this.googleMapsUrl = googleMapsUrl;
    }

    // Helper method to get full address for copying
    public String getFullAddress() {
        StringBuilder fullAddress = new StringBuilder();
        
        if (street != null && !street.trim().isEmpty()) {
            fullAddress.append(street);
        }
        
        if (city != null && !city.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(city);
        }
        
        if (code != null && !code.trim().isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(" ");
            fullAddress.append(code);
        }
        
        return fullAddress.toString();
    }

    // Helper method to extract coordinates from Google Maps URL
    public boolean hasValidGoogleMapsUrl() {
        return googleMapsUrl != null && !googleMapsUrl.trim().isEmpty() && 
               (googleMapsUrl.contains("maps.google.") || 
                googleMapsUrl.contains("goo.gl/maps") ||
                googleMapsUrl.contains("maps.app.goo.gl") ||
                googleMapsUrl.contains("google.com/maps"));
    }
}
