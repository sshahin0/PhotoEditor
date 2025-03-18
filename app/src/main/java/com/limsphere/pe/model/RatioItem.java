package com.limsphere.pe.model;

public class RatioItem {
    private String title;
    private int imageResourceId;
    private int ratioKey;
    private boolean isSelected;
    private int calculatedWidth = -1;  // Store calculated width
    private int calculatedHeight = -1; // Store calculated height

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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getCalculatedWidth() {
        return calculatedWidth;
    }

    public void setCalculatedWidth(int width) {
        this.calculatedWidth = width;
    }

    public int getCalculatedHeight() {
        return calculatedHeight;
    }

    public void setCalculatedHeight(int height) {
        this.calculatedHeight = height;
    }
}
