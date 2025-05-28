package com.example.myapplication.models;

public class Furniture {
    private String name;
    private String category;
    private String image;
    private double price;
    private String description;
    private String modelUrl;
    private int count;

    // Empty constructor required for Firestore
    public Furniture() { }

    public Furniture(String name, double price, String image, String description, String category, String modelUrl, int count) {
        this.name = name;
        this.price = price;
        this.image = image;
        this.description = description;
        this.category = category;
        this.modelUrl = modelUrl;
        this.count = count;
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
    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
