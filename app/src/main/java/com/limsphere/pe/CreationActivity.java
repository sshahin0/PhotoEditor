package com.limsphere.pe;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.limsphere.pe.adapter.MyCreationAdapter;
import com.limsphere.pe.model.MyAlbumMediaFile;
import com.limsphere.pe.utils.AdManager;
import com.limsphere.pe.utils.ImageUtils;

import org.apache.commons.io.comparator.LastModifiedFileComparator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class CreationActivity extends AppCompatActivity {

    private File[] files;
    private RecyclerView creation_list;
    private RecyclerView.LayoutManager layoutManager;

    ImageView btnBack;
    LinearLayout emptyStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_creation);

        btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        emptyStatus = findViewById(R.id.emptyStatus);

        LinearLayout adContainer = findViewById(R.id.banner_container);

        if (!AdManager.isloadMAX) {
            //admob
            AdManager.initAd(CreationActivity.this);
            AdManager.loadBannerAd(CreationActivity.this, adContainer);
            AdManager.loadInterAd(CreationActivity.this);
        } else {
            //MAX + Fb Ads
            AdManager.initMAX(CreationActivity.this);
            AdManager.maxBanner(CreationActivity.this, adContainer);
            AdManager.maxInterstital(CreationActivity.this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setRecycler();
    }

    MyCreationAdapter myCreationAdapter;

    private void setRecycler() {
        myCreationAdapter = new MyCreationAdapter(this, getMyCreation());

        creation_list = (RecyclerView) findViewById(R.id.creation_list);
        creation_list.setHasFixedSize(true);
        layoutManager = new GridLayoutManager(CreationActivity.this, 2);
        creation_list.setLayoutManager(layoutManager);
        creation_list.setItemAnimator(new DefaultItemAnimator());
        creation_list.setAdapter(myCreationAdapter);
        myCreationAdapter.notifyDataSetChanged();
    }

    private ArrayList<MyAlbumMediaFile> getMyCreation() {
        ArrayList<MyAlbumMediaFile> mediaList = new ArrayList<>();
        MyAlbumMediaFile f;
        String path = ImageUtils.OUTPUT_COLLAGE_FOLDER;
        File targetPath = new File(path);

        files = targetPath.listFiles();

        if (files != null && files.length != 0) {
            emptyStatus.setVisibility(View.GONE);
            try {
                Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_REVERSE);

                for (int i = 0; i < files.length; i++) {
                    File file = files[i];
                    f = new MyAlbumMediaFile(file);
                    mediaList.add(f);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            emptyStatus.setVisibility(View.VISIBLE);
        }
        return mediaList;
    }

}
