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

public class BgGradientAdapter extends RecyclerView.Adapter<BgGradientAdapter.GradientViewHolder> {

    private List<int[]> gradientList;
    private Context context;
    private int selectedPosition = RecyclerView.NO_POSITION;
    private BgColorAdapter.OnColorClickListener listener;

    public BgGradientAdapter(Context context, List<int[]> gradientList, BgColorAdapter.OnColorClickListener listener) {
        this.context = context;
        this.gradientList = gradientList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GradientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_collage_bg_color, parent, false);
        return new GradientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GradientViewHolder holder, int position) {
//        String colorCode = String.valueOf(gradientList.get(position));
        int[] gradientColors = gradientList.get(position);
        String startColor = "";
        String endColor = "";
        for (int i = 0; i < gradientColors.length; i++) {
            if (i == 0) {
                startColor = String.format("#%06X", (0xFFFFFF & gradientColors[0]));
            } else {
                endColor = String.format("#%06X", (0xFFFFFF & gradientColors[1]));
            }
        }

        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, gradientColors);
        drawable.setShape(GradientDrawable.OVAL); // Circular shape

        if (position == selectedPosition) {
            drawable.setStroke(8, context.getResources().getColor(R.color.btn_icon_color)); // Blue stroke when selected
        } else {
            drawable.setStroke(0, Color.TRANSPARENT);
        }

        holder.colorView.setBackground(drawable);

        String finalStartColor = startColor;
        String finalEndColor = endColor;
        holder.itemView.setOnClickListener(v -> {
            int previousPosition = selectedPosition;
            selectedPosition = position;
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);
            if (listener != null) {
                listener.onGradientColorClick(finalStartColor, finalEndColor);
            }
        });
    }

    @Override
    public int getItemCount() {
        return gradientList.size();
    }

    public static class GradientViewHolder extends RecyclerView.ViewHolder {
        View colorView;

        public GradientViewHolder(@NonNull View itemView) {
            super(itemView);
            colorView = itemView.findViewById(R.id.color_view);
        }
    }
}
