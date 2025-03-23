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
    private final List<String> imageUrls;

    public StickerGridAdapter(Context context, List<String> imageUrls) {
        this.context = context;
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public StickerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sticker, parent, false);
        return new StickerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StickerViewHolder holder, int position) {
        Glide.with(context).load(imageUrls.get(position)).into(holder.stickerImage);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class StickerViewHolder extends RecyclerView.ViewHolder {
        ImageView stickerImage;

        public StickerViewHolder(View itemView) {
            super(itemView);
            stickerImage = itemView.findViewById(R.id.stickerImage);
        }
    }
}
