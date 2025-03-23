package com.limsphere.pe.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.limsphere.pe.model.StickerCategory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StickerLoader {

    public static List<StickerCategory> loadStickers(Context context) {
        List<StickerCategory> stickerCategories = new ArrayList<>();
        try {
            AssetManager assetManager = context.getAssets();
            String stickersFolder = "stickers";
            String[] categories = assetManager.list(stickersFolder);

            if (categories != null) {
                for (String category : categories) {
                    String categoryPath = stickersFolder + "/" + category;
                    String[] images = assetManager.list(categoryPath);

                    if (images != null) {
                        List<String> imageUrls = new ArrayList<>();
                        for (String image : images) {
                            imageUrls.add("file:///android_asset/" + categoryPath + "/" + image);
                        }
                        stickerCategories.add(new StickerCategory(category, imageUrls));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stickerCategories;
    }
}
