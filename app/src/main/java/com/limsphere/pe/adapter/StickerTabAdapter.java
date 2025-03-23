package com.limsphere.pe.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.limsphere.pe.Activities.fragment.StickerCategoryFragment;
import com.limsphere.pe.model.StickerCategory;

import java.util.List;

public class StickerTabAdapter extends FragmentStateAdapter {
    private final List<StickerCategory> stickerCategories;

    public StickerTabAdapter(@NonNull FragmentActivity fragmentActivity, List<StickerCategory> stickerCategories) {
        super(fragmentActivity);
        this.stickerCategories = stickerCategories;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        StickerCategory category = stickerCategories.get(position);
        return StickerCategoryFragment.newInstance(category.getCategoryName(), category.getImageUrls());
    }

    @Override
    public int getItemCount() {
        return stickerCategories.size();
    }

    public String getCategoryName(int position) {
        return stickerCategories.get(position).getCategoryName();
    }
}
