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
import com.limsphere.pe.adapter.BgColorGradientAdapter;
import com.limsphere.pe.adapter.CollageBgCategoryAdapter;
import com.limsphere.pe.colorpicker.ColorPickerViewParent;
import com.limsphere.pe.utils.ColorUtils;
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

    public BackgroundManager(Context context, RelativeLayout containerLayout, LinearLayout bgColorView, RecyclerView bgColorRecycler, RecyclerView bgCatRecycler, ColorPickerViewParent colorChooser) {
        this.context = context;
        this.containerLayout = containerLayout;
        this.bgColorView = bgColorView;
        this.bgColorRecycler = bgColorRecycler;
        this.bgCatRecycler = bgCatRecycler;
        this.colorChooser = colorChooser;
    }

    public void setupBackgroundOptions(BackgroundGalleryListener galleryListener, BgColorGradientAdapter.OnColorClickListener colorClickListener) {
        bgColorRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
        bgCatRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));

        List<Integer> categoryImages = Arrays.asList(R.drawable.bg_gallery_cat, R.drawable.bg_solid_cat, R.drawable.bg_gradient_cat, R.drawable.bg_color_chooser_cat);

        CollageBgCategoryAdapter categoryAdapter = new CollageBgCategoryAdapter(context, categoryImages, position -> {
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

    private void loadColorPicker() {
        bgColorView.setVisibility(View.GONE); // Hide color list
        colorChooser.setVisibility(View.VISIBLE);

        colorChooser.setOnColorChangedListener(new ColorPickerViewParent.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                setSolidColor(color);
                hideColorPicker();
                bgColorView.setVisibility(View.VISIBLE);
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

    private void loadSolidColors(BgColorGradientAdapter.OnColorClickListener colorClickListener) {
        List<Object> colorItems = new ArrayList<>(ColorUtils.loadSolidColors(context));
        showColorList(colorItems, colorClickListener);
    }

    private void showColorList(List<Object> colorItems, BgColorGradientAdapter.OnColorClickListener colorClickListener) {
        BgColorGradientAdapter adapter = new BgColorGradientAdapter(context, colorItems, colorClickListener);

        bgColorRecycler.setAdapter(adapter);
    }

    private void loadGradientColors(BgColorGradientAdapter.OnColorClickListener colorClickListener) {
        List<Object> colorItems = new ArrayList<>(ColorUtils.loadGradientColors(context));
        showColorList(colorItems, colorClickListener);
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

        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{startColorValue, endColorValue});

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