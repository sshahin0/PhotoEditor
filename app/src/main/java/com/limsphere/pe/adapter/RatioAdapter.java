package com.limsphere.pe.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

import com.limsphere.pe.R;
import com.limsphere.pe.model.RatioItem;

import java.util.List;

public class RatioAdapter extends RecyclerView.Adapter<RatioAdapter.ItemViewHolder> {

    private List<RatioItem> itemList;
    private OnItemClickListener mItemClickListener;
    private Context mContext;

    public RatioAdapter(Context context, List<RatioItem> itemList, OnItemClickListener onClickListener) {
        this.itemList = itemList;
        this.mItemClickListener = onClickListener;
        this.mContext = context;
    }

    public interface OnItemClickListener {
        void onRatioItemClick(int ratioItem);
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ratio_item, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        RatioItem currentItem = itemList.get(position);

        int imageResId = currentItem.getImageResourceId();

        // Load the drawable image
        Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), imageResId);
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();

        // Calculate aspect ratio
        float aspectRatio = (float) originalWidth / originalHeight;

        holder.imageView.post(() -> {
            int parentWidth = ((View) holder.imageView.getParent()).getWidth(); // Get parent width
            int targetHeight = (int) (parentWidth / aspectRatio); // Calculate height dynamically

            // Apply calculated dimensions
            ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
            params.width = parentWidth;
            params.height = targetHeight;
            holder.imageView.setLayoutParams(params);
        });

        // Adjust ImageView height dynamically
//        ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();
//        params.height = (int) (holder.imageView.getWidth() / aspectRatio);
//        holder.imageView.setLayoutParams(params);

        ViewGroup.LayoutParams params = holder.imageView.getLayoutParams();

//        switch (currentItem.getRatioKey()) {
//            case BaseTemplateDetailActivity.RATIO_1_1:
//                params.width = 120;
//                params.height = 120;
//                break;
//            case BaseTemplateDetailActivity.RATIO_1_2:
//                params.width = 60;
//                params.height = 120;
//                break;
//            case BaseTemplateDetailActivity.RATIO_2_3:
//                params.width = 80;
//                params.height = 120;
//                break;
//            case BaseTemplateDetailActivity.RATIO_3_2:
//                params.width = 120;
//                params.height = 80;
//                break;
//            case BaseTemplateDetailActivity.RATIO_3_4:
//                params.width = 90;
//                params.height = 120;
//                break;
//            case BaseTemplateDetailActivity.RATIO_4_3:
//                params.width = 120;
//                params.height = 90;
//                break;
//            case BaseTemplateDetailActivity.RATIO_4_5:
//                params.width = 96;
//                params.height = 120;
//                break;
//            case BaseTemplateDetailActivity.RATIO_5_4:
//                params.width = 120;
//                params.height = 96;
//                break;
//            case BaseTemplateDetailActivity.RATIO_9_16:
//                params.width = 200;
//                params.height = 50;
//                break;
//            case BaseTemplateDetailActivity.RATIO_16_9:
//                params.width = 200;
//                params.height = 50;
//                break;
//            default:
//                params.width = 80;
//                params.height = 80;
//        }
//        holder.imageView.setLayoutParams(params);

//        holder.titleTextView.setText(currentItem.getTitle());
        holder.imageView.setImageResource(currentItem.getImageResourceId());
        holder.imageView.setOnClickListener(v -> {
            if (mItemClickListener != null) {
                mItemClickListener.onRatioItemClick(currentItem.getRatioKey());  // Notify activity when an item is clicked
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        //        TextView titleTextView;
        ImageView imageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
//            titleTextView = itemView.findViewById(R.id.item_title);
            imageView = itemView.findViewById(R.id.item_image);
        }
    }
}
