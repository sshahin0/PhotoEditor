package com.limsphere.pe.utils;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class CustomGalleryPicker {
    public static final String KEY_LIMIT_MAX_IMAGE = "KEY_LIMIT_MAX_IMAGE";
    public static final String KEY_LIMIT_MIN_IMAGE = "KEY_LIMIT_MIN_IMAGE";

    private final AppCompatActivity activity;
    private int limitImageMax = 15;
    private int limitImageMin = 2;
    private GalleryResultCallback callback;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;

    public CustomGalleryPicker(AppCompatActivity activity, GalleryResultCallback callback) {
        this.activity = activity;
        this.callback = callback;

        this.pickMultipleMedia = activity.registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(limitImageMax),
                uris -> {
                    if (uris != null && !uris.isEmpty()) {
                        // Check if selection exceeds the limit
                        if (uris.size() > limitImageMax) {
                            Toast.makeText(activity, "You can select a maximum of " + limitImageMax + " images", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Check if selection is below the minimum
                        if (uris.size() < limitImageMin) {
                            Toast.makeText(activity, "You need to select at least " + limitImageMin + " images", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        ArrayList<String> filePaths = new ArrayList<>();
                        for (Uri uri : uris) {
                            String path = getFilePathFromUri(uri);
                            if (path != null) {
                                filePaths.add(path);
                            } else {
                                filePaths.add(uri.toString());
                            }
                        }
                        if (callback != null) {
                            callback.onGalleryResult(filePaths);
                        }
                    } else {
                        Toast.makeText(activity, "No images selected", Toast.LENGTH_SHORT).show();
                        if (callback != null) {
                            callback.onGalleryCanceled();
                        }
                    }
                });
    }

    public void setLimits(int max, int min) {
        this.limitImageMax = max;
        this.limitImageMin = min;

        if (this.limitImageMin > this.limitImageMax || this.limitImageMin < 0) {
            throw new IllegalArgumentException("Invalid image limits");
        }
    }

    public void launch() {
        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private String getFilePathFromUri(Uri uri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
            }
            cursor.close();
        }

        if (filePath == null) {
            filePath = uri.getPath();
        }

        return filePath;
    }

    public interface GalleryResultCallback {
        void onGalleryResult(ArrayList<String> filePaths);

        void onGalleryCanceled();
    }
}