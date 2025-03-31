package com.limsphere.pe.model;

public class BgPatternImageModel {
    private String imageUrl;

    public BgPatternImageModel() {
    } // Needed for Firebase

    public BgPatternImageModel(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
