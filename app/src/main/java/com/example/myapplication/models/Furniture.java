package com.example.myapplication.models;

public class Furniture {
    private String name;
    private String category;
    private String image;
    private double price;
    private String description;
    private String modelUrl;

    // Empty constructor required for Firestore
    public Furniture() { }

    public Furniture(String name, String category, String image, double price, String description, String modelUrl) {
        this.name = name;
        this.category = category;
        this.image = image;
        this.price = price;
        this.description = description;
        this.modelUrl = modelUrl;
    }

    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getImage() { return image; }
    public double getPrice() { return price; }
    public String getDescription() { return description; }

    public String getModelUrl() {
        return modelUrl;
    }

    public void setModelUrl(String modelUrl) {
        this.modelUrl = modelUrl;
    }
}
