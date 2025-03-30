package com.limsphere.pe.Activities.managers;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.limsphere.pe.R;
import com.limsphere.pe.adapter.StickerTabAdapter;
import com.limsphere.pe.model.StickerCategory;
import com.limsphere.pe.utils.StickerLoader;

import java.util.List;

public class StickerManager {
    private final Context context;
    private View stickerLayoutView;
    private ViewPager2 stickerViewPager;
    private TabLayout stickerTabLayout;
    private StickerTabAdapter stickerTabAdapter;
    private List<StickerCategory> stickerCategories;

    public StickerManager(Context context) {
        this.context = context;
    }

    public void initializeViews(View stickerLayoutView, ViewPager2 stickerViewPager, TabLayout stickerTabLayout) {
        this.stickerLayoutView = stickerLayoutView;
        this.stickerViewPager = stickerViewPager;
        this.stickerTabLayout = stickerTabLayout;
    }

    public void setupStickerUI() {
        setupStickerViewPager();
        showStickerUI();
    }

    private void setupStickerViewPager() {
        stickerCategories = StickerLoader.loadStickers(context);
        stickerTabAdapter = new StickerTabAdapter((FragmentActivity) context, stickerCategories);

        stickerViewPager.setClipToPadding(false);
        stickerViewPager.setClipChildren(false);
        stickerViewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        stickerViewPager.setPageTransformer((page, position) -> {
            float scale = Math.max(0.85f, 1 - Math.abs(position));
            page.setScaleY(scale);
            page.setTranslationX(-position * page.getWidth() * 0.1f);
        });

        stickerViewPager.setAdapter(stickerTabAdapter);

        new TabLayoutMediator(stickerTabLayout, stickerViewPager, (tab, position) -> {
            String categoryName = stickerTabAdapter.getCategoryName(position);
            tab.setIcon(getStickerCategoryIcon(categoryName));
        }).attach();
    }

    private int getStickerCategoryIcon(String categoryName) {
        switch (categoryName) {
            case "activity":
                return R.drawable.sticker_category_1;
            case "birthday":
                return R.drawable.sticker_category_2;
            case "celebration":
                return R.drawable.sticker_category_3;
            case "comic":
                return R.drawable.sticker_category_4;
            default:
                return R.drawable.sticker_category_5;
        }
    }

    public void showStickerUI() {
        stickerLayoutView.setVisibility(View.VISIBLE);
    }

    public void hideStickerUI() {
        if (stickerLayoutView != null) {
            stickerLayoutView.setVisibility(View.GONE);
        }
    }
}