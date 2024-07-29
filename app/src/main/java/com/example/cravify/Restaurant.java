package com.example.cravify;

public class Restaurant {
    private String Name;
    private String Cuisine;
    private String ImageUrl;
    private String Address; // New field

    // Required empty constructor
    public Restaurant() {
    }

    public Restaurant(String name, String cuisine, String imageUrl, String address) {
        this.Name = name;
        this.Cuisine = cuisine;
        this.ImageUrl = imageUrl;
        this.Address = address;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getCuisine() {
        return Cuisine;
    }

    public void setCuisine(String cuisine) {
        Cuisine = cuisine;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }
}
