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
import java.util.Collections;

public class CustomGalleryPicker {
    public static final String KEY_LIMIT_MAX_IMAGE = "KEY_LIMIT_MAX_IMAGE";
    public static final String KEY_LIMIT_MIN_IMAGE = "KEY_LIMIT_MIN_IMAGE";

    private final AppCompatActivity activity;
    private int limitImageMax = 15;
    private int limitImageMin = 2;
    private GalleryResultCallback callback;
    private boolean isMultipleSelection = true;

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultipleMedia;
    private final ActivityResultLauncher<PickVisualMediaRequest> pickSingleMedia;

    public CustomGalleryPicker(AppCompatActivity activity, GalleryResultCallback callback) {
        this.activity = activity;
        this.callback = callback;

        // Multiple media picker
        this.pickMultipleMedia = activity.registerForActivityResult(
                new ActivityResultContracts.PickMultipleVisualMedia(limitImageMax),
                uris -> handleMediaResult(uris));

        // Single media picker
        this.pickSingleMedia = activity.registerForActivityResult(
                new ActivityResultContracts.PickVisualMedia(),
                uri -> {
                    if (uri != null) {
                        handleMediaResult(Collections.singletonList(uri));
                    } else {
                        if (callback != null) {
                            callback.onGalleryCanceled();
                        }
                    }
                });
    }

    private void handleMediaResult(java.util.List<Uri> uris) {
        if (uris == null || uris.isEmpty()) {
            Toast.makeText(activity, "No images selected", Toast.LENGTH_SHORT).show();
            if (callback != null) {
                callback.onGalleryCanceled();
            }
            return;
        }

        // Check if selection exceeds the limit (for multiple selection only)
        if (isMultipleSelection && uris.size() > limitImageMax) {
            Toast.makeText(activity, "You can select a maximum of " + limitImageMax + " images", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if selection is below the minimum (for multiple selection only)
        if (isMultipleSelection && uris.size() < limitImageMin) {
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
    }

    public void setLimits(int max, int min) {
        this.limitImageMax = max;
        this.limitImageMin = min;

        if (this.limitImageMin > this.limitImageMax || this.limitImageMin < 0) {
            throw new IllegalArgumentException("Invalid image limits");
        }
    }

    public void launchSingle() {
        this.isMultipleSelection = false;
        pickSingleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public void launchMultiple() {
        this.isMultipleSelection = true;
        pickMultipleMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    public void launch() {
        if (limitImageMax > 1) {
            launchMultiple();
        } else {
            launchSingle();
        }
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