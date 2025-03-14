package com.limsphere.pe.model;

public class RatioItem {
    private String title;
    private int imageResourceId;

    public RatioItem(String title, int imageResourceId) {
        this.title = title;
        this.imageResourceId = imageResourceId;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}
