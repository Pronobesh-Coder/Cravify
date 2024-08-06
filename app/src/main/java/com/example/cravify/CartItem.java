package com.example.cravify;

public class CartItem {
    private String name;
    private String type;
    private int quantity;
    private double price;

    // No-argument constructor required for Firestore
    public CartItem() {
    }

    public CartItem(String name, String type, int quantity, double price) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }
}
