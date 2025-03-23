package com.limsphere.pe.model;

import java.util.List;

public class StickerCategory {
    private String categoryName;
    private List<String> imageUrls;

    public StickerCategory(String categoryName, List<String> imageUrls) {
        this.categoryName = categoryName;
        this.imageUrls = imageUrls;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }
}
