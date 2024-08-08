package com.example.cravify;

public class MenuItem {
    private String name;
    private String description;
    private String type;
    private double price;
    private String imageUrl;


    public MenuItem(String name, String description, String type, double price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.price = price;
        this.imageUrl = imageUrl;

    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
