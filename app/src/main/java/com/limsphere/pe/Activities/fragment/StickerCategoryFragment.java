package com.limsphere.pe.Activities.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.limsphere.pe.R;
import com.limsphere.pe.adapter.StickerGridAdapter;

import java.util.ArrayList;
import java.util.List;

public class StickerCategoryFragment extends Fragment {
    private static final String ARG_CATEGORY_NAME = "category_name";
    private static final String ARG_IMAGE_URLS = "image_urls";

    public static StickerCategoryFragment newInstance(String categoryName, List<String> imageUrls) {
        StickerCategoryFragment fragment = new StickerCategoryFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_NAME, categoryName);
        args.putStringArrayList(ARG_IMAGE_URLS, new ArrayList<>(imageUrls));
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sticker_category, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewStickers);

        if (getArguments() != null) {
            List<String> imageUrls = getArguments().getStringArrayList(ARG_IMAGE_URLS);
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
            recyclerView.setAdapter(new StickerGridAdapter(getContext(), imageUrls));
        }

        return view;
    }
}
