package com.example.elite;

public class Building {
    private String id;
    private String name; // Project name
    private String code; // Postal code
    private String city;
    private String street; // Street address
    private String googleMapsUrl; // Google Maps URL instead of lat/lng
    private double lat; // Keep for backward compatibility
    private double lng; // Keep for backward compatibility

    public Building() {
    }

    public Building(String id, String name, String city, String street, String code, String googleMapsUrl, double lat, double lng) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.street = street;
        this.code = code;
        this.googleMapsUrl = googleMapsUrl;
        this.lat = lat;
        this.lng = lng;
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

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
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

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setLng(double lng) {
        this.lng = lng;
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
