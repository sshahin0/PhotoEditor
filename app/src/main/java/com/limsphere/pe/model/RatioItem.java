package com.limsphere.pe.model;

public class RatioItem {
    private String title;
    private int imageResourceId;
    private int ratioKey;

    public RatioItem(String title, int imageResourceId, int ratioKey) {
        this.title = title;
        this.imageResourceId = imageResourceId;
        this.ratioKey = ratioKey;
    }

    public int getRatioKey() {
        return ratioKey;
    }

    public void setRatioKey(int ratioKey) {
        this.ratioKey = ratioKey;
    }

    public String getTitle() {
        return title;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }
}
