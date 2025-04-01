package com.limsphere.pe.model;

public class BgPatternImageModel {
    private String imageUrl;
    private String name;
    private int id;
    private boolean isSelected;

    public BgPatternImageModel(String imageUrl) {
        this(imageUrl, "", -1);
    }

    public BgPatternImageModel(String imageUrl, String name, int id) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.id = id;
        this.isSelected = false;
    }

    // Getters and Setters
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
