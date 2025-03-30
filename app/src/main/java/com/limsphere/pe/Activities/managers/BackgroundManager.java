package com.limsphere.pe.Activities.managers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.limsphere.pe.R;
import com.limsphere.pe.adapter.BgColorAdapter;
import com.limsphere.pe.adapter.BgGradientAdapter;
import com.limsphere.pe.adapter.CollageBgCategoryAdapter;
import com.limsphere.pe.colorpicker.ColorPickerViewParent;
import com.limsphere.pe.utils.ImageDecoder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BackgroundManager {
    public interface BackgroundGalleryListener {
        void onGalleryRequested();
    }

    private final Context context;
    private final RelativeLayout containerLayout;
    private final LinearLayout bgColorView;
    private final RecyclerView bgColorRecycler;
    private final RecyclerView bgCatRecycler;
    private final ColorPickerViewParent colorChooser;

    private int backgroundColor = Color.BLACK;
    private Bitmap backgroundImage;
    private Uri backgroundUri = null;

    public BackgroundManager(Context context,
                             RelativeLayout containerLayout,
                             LinearLayout bgColorView,
                             RecyclerView bgColorRecycler,
                             RecyclerView bgCatRecycler,
                             ColorPickerViewParent colorChooser) {
        this.context = context;
        this.containerLayout = containerLayout;
        this.bgColorView = bgColorView;
        this.bgColorRecycler = bgColorRecycler;
        this.bgCatRecycler = bgCatRecycler;
        this.colorChooser = colorChooser;
    }

    public void setupBackgroundOptions(BackgroundGalleryListener galleryListener,
                                       BgColorAdapter.OnColorClickListener colorClickListener) {
        bgColorRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        bgCatRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        List<Integer> categoryImages = Arrays.asList(
                R.drawable.bg_gallery_cat,
                R.drawable.bg_solid_cat,
                R.drawable.bg_gradient_cat,
                R.drawable.bg_color_chooser_cat);

        CollageBgCategoryAdapter categoryAdapter = new CollageBgCategoryAdapter(
                context,
                categoryImages,
                position -> {
                    switch (position) {
                        case 0:
                            galleryListener.onGalleryRequested();
                            break;
                        case 1:
                            loadSolidColors(colorClickListener);
                            break;
                        case 2:
                            loadGradientColors(colorClickListener);
                            break;
                        case 3:
                            loadColorPicker();
                            break;
                    }
                });

        bgCatRecycler.setAdapter(categoryAdapter);
        loadSolidColors(colorClickListener);
    }

    private void handleBackgroundCategorySelection(int position,
                                                   BackgroundGalleryListener galleryListener,
                                                   BgColorAdapter.OnColorClickListener colorClickListener) {
        switch (position) {
            case 0:
                galleryListener.onGalleryRequested();
                break;
            case 1:
                loadSolidColors(colorClickListener);
                break;
            case 2:
                loadGradientColors(colorClickListener);
                break;
            case 3:
                loadColorPicker();
                break;
        }
    }

    private void loadColorPicker() {
        bgColorView.setVisibility(View.GONE); // Hide color list
        colorChooser.setVisibility(View.VISIBLE); // Show color picker

        // Reset color picker to current background color
//        colorChooser.setColor(backgroundColor);

        colorChooser.setOnColorChangedListener(new ColorPickerViewParent.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                setSolidColor(color);
            }

            @Override
            public void onCrossPressed() {
                hideColorPicker();
                bgColorView.setVisibility(View.VISIBLE); // Show color list again
            }
        });
    }

    public void setBackgroundFromUri(Uri uri) {
        recycleBackgroundImage();
        backgroundUri = uri;
        backgroundImage = ImageDecoder.decodeUriToBitmap(context, uri);
        containerLayout.setBackground(new BitmapDrawable(context.getResources(), backgroundImage));
    }

    private void loadSolidColors(BgColorAdapter.OnColorClickListener colorClickListener) {
        List<Integer> colors = new ArrayList<>();
        for (String color : context.getResources().getStringArray(R.array.solid_color_list)) {
            colors.add(Color.parseColor(color));
        }

        BgColorAdapter solidColorAdapter = new BgColorAdapter(context, colors, colorClickListener);
        bgColorRecycler.setAdapter(solidColorAdapter);
    }

    private void loadGradientColors(BgColorAdapter.OnColorClickListener colorClickListener) {
        List<int[]> gradients = new ArrayList<>();
        for (String gradient : context.getResources().getStringArray(R.array.gradient_list)) {
            String[] colors = gradient.split(",");
            int[] gradientColors = {Color.parseColor(colors[0]), Color.parseColor(colors[1])};
            gradients.add(gradientColors);
        }

        BgGradientAdapter gradientColorAdapter = new BgGradientAdapter(context, gradients, colorClickListener);
        bgColorRecycler.setAdapter(gradientColorAdapter);
    }

    public void setSolidColor(int color) {
        recycleBackgroundImage();
        backgroundColor = color;
        containerLayout.setBackgroundColor(backgroundColor);
    }

    public void setGradientColor(String startColor, String endColor) {
        recycleBackgroundImage();

        int startColorValue = Color.parseColor(startColor);
        int endColorValue = Color.parseColor(endColor);

        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{startColorValue, endColorValue});

        containerLayout.setBackground(gradientDrawable);
        backgroundImage = gradientDrawableToBitmap(gradientDrawable, 500, 500);
    }

    private Bitmap gradientDrawableToBitmap(GradientDrawable drawable, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        android.graphics.Canvas canvas = new android.graphics.Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    public void recycleBackgroundImage() {
        if (backgroundImage != null && !backgroundImage.isRecycled()) {
            backgroundImage.recycle();
            backgroundImage = null;
            System.gc();
        }
    }

    public void hideBackgroundUI() {
        colorChooser.setVisibility(View.GONE);
        bgColorView.setVisibility(View.GONE);
    }

    public void showBackgroundUI() {
        bgColorView.setVisibility(View.VISIBLE);
    }

    public void hideColorPicker() {
        colorChooser.setVisibility(View.GONE);
        colorChooser.setOnColorChangedListener(null); // Remove listener
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public Bitmap getBackgroundImage() {
        return backgroundImage;
    }

    public Uri getBackgroundUri() {
        return backgroundUri;
    }
}