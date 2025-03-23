package com.limsphere.pe.adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.limsphere.pe.R;
import com.limsphere.pe.model.TemplateItem;
import java.util.ArrayList;

public class HorizontalPreviewTemplateAdapter extends RecyclerView.Adapter<HorizontalPreviewTemplateAdapter.PreviewTemplateViewHolder> {
    String ASSET_PREFIX = "assets://";
    public static class PreviewTemplateViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;

        PreviewTemplateViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.imageView);
        }
    }

    public static interface OnPreviewTemplateClickListener {
        void onPreviewTemplateClick(TemplateItem item);
    }

    private ArrayList<TemplateItem> mTemplateItems;
    private OnPreviewTemplateClickListener mListener;
    boolean isPip;
    private Context mContext;

    public HorizontalPreviewTemplateAdapter(Context context, ArrayList<TemplateItem> items, OnPreviewTemplateClickListener listener, boolean isPip) {
        mTemplateItems = items;
        mListener = listener;
        this.isPip = isPip;
        this.mContext = context;
    }

    @Override
    public PreviewTemplateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (isPip) {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preview_template_hor, parent, false);
        }else {
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_preview_pip_hor, parent, false);
        }
        return new PreviewTemplateViewHolder(v);
    }

    @Override
    public void onBindViewHolder(PreviewTemplateViewHolder holder, final int position) {
        String file = mTemplateItems.get(position).getPreview().substring(ASSET_PREFIX.length());
        Glide.with(holder.mImageView.getContext()).load(Uri.parse("file:///android_asset/".concat(file))).into(holder.mImageView);
        if (mTemplateItems.get(position).isSelected()) {
            holder.mImageView.setBackgroundColor(mContext.getResources().getColor(R.color.btn_icon_color));
        } else {
            holder.mImageView.setBackgroundColor(mContext.getResources().getColor(R.color.sub_menu_bg_color));
        }

        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onPreviewTemplateClick(mTemplateItems.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTemplateItems.size();
    }
}
