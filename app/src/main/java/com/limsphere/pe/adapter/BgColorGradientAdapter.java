package com.limsphere.pe.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.limsphere.pe.R;

import java.util.List;

public class BgColorGradientAdapter extends RecyclerView.Adapter<BgColorGradientAdapter.ColorViewHolder> {

    public static final int TYPE_SOLID_COLOR = 0;
    public static final int TYPE_GRADIENT = 1;

    private List<Object> colorItems;
    private Context context;
    private OnColorClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnColorClickListener {
        void onSolidColorClick(String colorCode);

        void onGradientColorClick(String startColor, String endColor);
    }

    public BgColorGradientAdapter(Context context, List<Object> colorItems, OnColorClickListener listener) {
        this.context = context;
        this.colorItems = colorItems;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        Object item = colorItems.get(position);
        if (item instanceof Integer) {
            return TYPE_SOLID_COLOR;
        } else if (item instanceof int[]) {
            return TYPE_GRADIENT;
        }
        return super.getItemViewType(position);
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_collage_bg_color, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        int viewType = getItemViewType(position);
        Object item = colorItems.get(position);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);

        if (viewType == TYPE_SOLID_COLOR) {
            // Handle solid color
            int color = (Integer) item;
            String colorCode = String.format("#%06X", (0xFFFFFF & color));
            drawable.setColor(color);
            holder.itemView.setOnClickListener(v -> {
                updateSelectedPosition(position);
                if (listener != null) {
                    listener.onSolidColorClick(colorCode);
                }
            });
        } else {
            // Handle gradient
            int[] gradientColors = (int[]) item;
            String startColor = String.format("#%06X", (0xFFFFFF & gradientColors[0]));
            String endColor = String.format("#%06X", (0xFFFFFF & gradientColors[1]));

            drawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
            drawable.setColors(gradientColors);

            holder.itemView.setOnClickListener(v -> {
                updateSelectedPosition(position);
                if (listener != null) {
                    listener.onGradientColorClick(startColor, endColor);
                }
            });
        }

        // Apply selection state
        if (position == selectedPosition) {
            drawable.setStroke(8, context.getResources().getColor(R.color.btn_icon_color));
        } else {
            drawable.setStroke(0, Color.TRANSPARENT);
        }

        holder.colorView.setBackground(drawable);
    }

    private void updateSelectedPosition(int newPosition) {
        int previousPosition = selectedPosition;
        selectedPosition = newPosition;
        if (previousPosition != RecyclerView.NO_POSITION) {
            notifyItemChanged(previousPosition);
        }
        notifyItemChanged(selectedPosition);
    }

    @Override
    public int getItemCount() {
        return colorItems.size();
    }

    public static class ColorViewHolder extends RecyclerView.ViewHolder {
        View colorView;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.color_view);
        }
    }
}