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

import java.util.List;

public class StickerGridAdapter extends RecyclerView.Adapter<StickerGridAdapter.StickerViewHolder> {

    private final Context context;
    private final List<String> stickerUrls;
    private final OnStickerClickListener listener;

    // Interface for click events
    public interface OnStickerClickListener {
        void onStickerClick(String imageUrl);
    }

    public StickerGridAdapter(Context context, List<String> stickerUrls, OnStickerClickListener listener) {
        this.context = context;
        this.stickerUrls = stickerUrls;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sticker, parent, false);
        return new StickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {
        String imageUrl = stickerUrls.get(position);

        // Load sticker image
        Glide.with(context)
                .load(imageUrl)
                .into(holder.stickerImage);

        // Handle click
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onStickerClick(imageUrl);
            }
        });
    }

    @Override
    public int getItemCount() {
        return stickerUrls.size();
    }

    public static class StickerViewHolder extends RecyclerView.ViewHolder {
        ImageView stickerImage;

        public StickerViewHolder(@NonNull View itemView) {
            super(itemView);
            stickerImage = itemView.findViewById(R.id.stickerImage);
        }
    }
}
