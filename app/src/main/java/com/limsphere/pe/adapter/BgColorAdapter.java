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

public class BgColorAdapter extends RecyclerView.Adapter<BgColorAdapter.ColorViewHolder> {

    private List<Integer> colorList;
    private Context mContext;
    private OnColorClickListener listener;
    private int selectedPosition = RecyclerView.NO_POSITION;

    public interface OnColorClickListener {
        void onSolidColorClick(String colorCode);

        void onGradientColorClick(String startColor, String endColor);
    }

    public BgColorAdapter(Context context, List<Integer> colorList, OnColorClickListener listener) {
        this.mContext = context;
        this.colorList = colorList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_collage_bg_color, parent, false);
        return new ColorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
        String colorCode = String.format("#%06X", (0xFFFFFF & colorList.get(position)));
        int color = colorList.get(position);

        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL); // Make it a circle
        drawable.setColor(color);

        if (position == selectedPosition) {
            drawable.setStroke(8, this.mContext.getResources().getColor(R.color.btn_icon_color)); // Blue stroke if selected
        } else {
            drawable.setStroke(0, Color.TRANSPARENT);
        }

        holder.colorView.setBackground(drawable);

        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onSolidColorClick(colorCode);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colorList.size();
    }

    public static class ColorViewHolder extends RecyclerView.ViewHolder {
        View colorView;

        public ColorViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.color_view);
        }
    }
}


