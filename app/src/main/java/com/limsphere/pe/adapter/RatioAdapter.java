package com.limsphere.pe.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.limsphere.pe.R;
import com.limsphere.pe.model.RatioItem;

import java.util.List;

public class RatioAdapter extends RecyclerView.Adapter<RatioAdapter.ItemViewHolder> {

    private List<RatioItem> itemList;

    public RatioAdapter(List<RatioItem> itemList) {
        this.itemList = itemList;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ratio_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        RatioItem currentItem = itemList.get(position);
        holder.titleTextView.setText(currentItem.getTitle());
        holder.imageView.setImageResource(currentItem.getImageResourceId());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView;
        ImageView imageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.item_title);
            imageView = itemView.findViewById(R.id.item_image);
        }
    }
}
