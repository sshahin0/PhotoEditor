package com.limsphere.pe.Activities.fragment;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.limsphere.pe.Activities.CollageActivity;
import com.limsphere.pe.R;
import com.limsphere.pe.adapter.StickerGridAdapter;
import com.limsphere.pe.multitouch.controller.ImageEntity;
import com.limsphere.pe.multitouch.custom.PhotoView;
import com.limsphere.pe.utils.ResultContainer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StickerCategoryFragment extends Fragment implements StickerGridAdapter.OnStickerClickListener {

    private List<String> mStickerUrls;

    public static StickerCategoryFragment newInstance(String categoryName, List<String> stickerUrls) {
        StickerCategoryFragment fragment = new StickerCategoryFragment();
        Bundle args = new Bundle();
        args.putStringArrayList("stickerUrls", new ArrayList<>(stickerUrls));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStickerUrls = getArguments().getStringArrayList("stickerUrls");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sticker_category, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewStickers);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 6));

        StickerGridAdapter adapter = new StickerGridAdapter(getContext(), mStickerUrls, this);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onStickerClick(String imageUrl) {
        setEmojiesSticker(getActivity(), imageUrl, ((CollageActivity) getActivity()).mPhotoView);
    }

    public void setEmojiesSticker(FragmentActivity activity, String stickerUrl, PhotoView photoView) {
        InputStream inputStream;
        try {
            File file = new File(stickerUrl);
            inputStream = activity.getAssets().open("stickers/" + stickerUrl.split("stickers/")[1]);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            File path = new File(activity.getExternalFilesDir(null) + "/Download/stickers");
            if (!path.isDirectory()) {
                path.mkdirs();
            }
            File mypath = new File(path.getAbsolutePath(), file.getName());
            try {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(mypath));
                ImageEntity entity = new ImageEntity(Uri.fromFile(mypath), getResources());
                entity.setInitScaleFactor(0.5f);
                entity.setSticker(false);
                entity.load(activity,
                        (photoView.getWidth() - entity.getWidth()) / 2,
                        (photoView.getHeight() - entity.getHeight()) / 2, 0);
                photoView.addImageEntity(entity);
                if (ResultContainer.getInstance().getImageEntities() != null) {
                    ResultContainer.getInstance().getImageEntities().add(entity);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
