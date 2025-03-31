package com.limsphere.pe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.limsphere.pe.R;
import com.limsphere.pe.model.BgPatternImageModel;

import java.util.List;

public class BgPatternImageAdapter extends RecyclerView.Adapter<com.limsphere.pe.adapter.BgPatternImageAdapter.ViewHolder> {

    private final Context context;
    private final List<BgPatternImageModel> imageList;
    private final OnImageClickListener onImageClickListener;

    // Interface for handling item clicks
    public interface OnImageClickListener {
        void onPatternImageClick(BgPatternImageModel BgPatternImageModel, int position);
    }

    public BgPatternImageAdapter(Context context, List<BgPatternImageModel> imageList, OnImageClickListener onImageClickListener) {
        this.context = context;
        this.imageList = imageList;
        this.onImageClickListener = onImageClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_bg_pattern_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        BgPatternImageModel imageModel = imageList.get(position);

        // Load image using Glide
        Glide.with(context).load(imageModel.getImageUrl()).into(holder.imageView);

        // Handle click event
        holder.imageView.setOnClickListener(v -> {
            if (onImageClickListener != null) {
                onImageClickListener.onPatternImageClick(imageModel, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }
}