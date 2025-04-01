package com.limsphere.pe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.limsphere.pe.R;
import com.limsphere.pe.model.BgPatternImageModel;

import java.util.List;

public class BgPatternAdapter extends RecyclerView.Adapter<BgPatternAdapter.PatternViewHolder> {
    private List<BgPatternImageModel> patternList;
    private int selectedPosition = -1;
    private OnPatternClickListener listener;

    public interface OnPatternClickListener {
        void onPatternClick(String url);
    }

    public BgPatternAdapter(List<BgPatternImageModel> patternList, OnPatternClickListener listener) {
        this.patternList = patternList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PatternViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_bg_pattern_image, parent, false);
        return new PatternViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PatternViewHolder holder, int position) {
        BgPatternImageModel model = patternList.get(position);

        // Load image using your preferred image loading library (Glide/Picasso)
        Glide.with(holder.itemView.getContext()).load(model.getImageUrl()).apply(RequestOptions.circleCropTransform()).into(holder.patternImage);

        // Show/hide selection indicator
        holder.selectionIndicator.setVisibility(position == selectedPosition ? View.VISIBLE : View.INVISIBLE);

        holder.itemView.setOnClickListener(v -> {
            int previousSelected = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(previousSelected);
            notifyItemChanged(selectedPosition);

            if (listener != null) {
                listener.onPatternClick(model.getImageUrl());
            }
        });
    }

    @Override
    public int getItemCount() {
        return patternList.size();
    }

    public static class PatternViewHolder extends RecyclerView.ViewHolder {
        ImageView patternImage;
        ImageView selectionIndicator;

        public PatternViewHolder(@NonNull View itemView) {
            super(itemView);
            patternImage = itemView.findViewById(R.id.patternImage);
            selectionIndicator = itemView.findViewById(R.id.selectionIndicator);
        }
    }

    public void setSelectedPosition(int position) {
        selectedPosition = position;
        notifyDataSetChanged();
    }
}