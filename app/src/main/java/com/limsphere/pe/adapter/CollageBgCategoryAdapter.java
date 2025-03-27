package com.limsphere.pe.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.limsphere.pe.R;

import java.util.List;

public class CollageBgCategoryAdapter extends RecyclerView.Adapter<CollageBgCategoryAdapter.CategoryViewHolder> {

    private List<Integer> images;
    private Context context;
    private OnCategoryClickListener listener;
    private int selectedPosition = 0; // Default selection

    public interface OnCategoryClickListener {
        void onCategoryClick(int position);
    }

    public CollageBgCategoryAdapter(Context context, List<Integer> images, OnCategoryClickListener listener) {
        this.context = context;
        this.images = images;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_collage_bg_category, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        holder.categoryImage.setImageResource(images.get(position));

        float radiusInPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                5, // 10dp
                context.getResources().getDisplayMetrics()
        );

        // Create a dynamic blue border for selected category
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(radiusInPx);

        if (position == selectedPosition) {
            drawable.setStroke(8, context.getResources().getColor(R.color.btn_icon_color)); // Blue border when selected
        } else {
            drawable.setStroke(0, Color.TRANSPARENT);
        }

        holder.itemView.setBackground(drawable);

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            listener.onCategoryClick(position);
        });
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class CategoryViewHolder extends RecyclerView.ViewHolder {
        ImageView categoryImage;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryImage = itemView.findViewById(R.id.categoryImage);
        }
    }
}