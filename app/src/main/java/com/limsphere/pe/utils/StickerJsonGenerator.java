package com.limsphere.pe.utils;

import android.content.Context;
import android.content.res.AssetManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class StickerJsonGenerator {

    public static String generateStickersJson(Context context) throws IOException, JSONException {
        JSONObject stickersJson = new JSONObject();
        JSONObject stickersObject = new JSONObject();

        try {
            AssetManager assetManager = context.getAssets();
            String stickersFolder = "stickers";

            // List all category folders inside "stickers"
            String[] categories = assetManager.list(stickersFolder);
            if (categories != null) {
                for (String category : categories) {
                    String categoryPath = stickersFolder + "/" + category;
                    String[] images = assetManager.list(categoryPath);

                    if (images != null) {
                        JSONArray imageUrls = new JSONArray();
                        for (String image : images) {
                            String imageUrl = "file:///android_asset/" + categoryPath + "/" + image;
                            imageUrls.put(imageUrl);
                        }
                        stickersObject.put(category, imageUrls);
                    }
                }
            }
            stickersJson.put("stickers", stickersObject);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return stickersJson.toString();
    }
}
