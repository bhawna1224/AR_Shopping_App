package com.example.myapplication.models;

public class Furniture {
    private String name;
    private String category;
    private String image;
    private double price;
    private String description;

    // Empty constructor required for Firestore
    public Furniture() { }

    public Furniture(String name, String category, String image, double price, String description) {
        this.name = name;
        this.category = category;
        this.image = image;
        this.price = price;
        this.description = description;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getImage() { return image; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }
}
