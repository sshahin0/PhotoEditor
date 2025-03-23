package com.limsphere.pe.Activities;

import static android.view.View.VISIBLE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.limsphere.pe.R;
import com.limsphere.pe.adapter.ColorAdapter;
import com.limsphere.pe.adapter.RatioAdapter;
import com.limsphere.pe.adapter.StickerTabAdapter;
import com.limsphere.pe.frame.FrameImageView;
import com.limsphere.pe.frame.FramePhotoLayout;
import com.limsphere.pe.gallery.CustomGalleryActivity;
import com.limsphere.pe.model.RatioItem;
import com.limsphere.pe.model.StickerCategory;
import com.limsphere.pe.model.TemplateItem;
import com.limsphere.pe.multitouch.controller.ImageEntity;
import com.limsphere.pe.utils.AdManager;
import com.limsphere.pe.utils.FileUtils;
import com.limsphere.pe.utils.ImageDecoder;
import com.limsphere.pe.utils.ImageUtils;
import com.limsphere.pe.utils.ResultContainer;
import com.limsphere.pe.utils.StickerLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class CollageActivity extends BaseTemplateDetailActivity
        implements FramePhotoLayout.OnQuickActionClickListener, RatioAdapter.OnItemClickListener {
    private static final int REQUEST_SELECT_PHOTO = 1001;
    private static float MAX_SPACE;
    private static float MAX_CORNER;
    private static float DEFAULT_SPACE;
    private static final float MAX_SPACE_PROGRESS = 300.0f;
    private static final float MAX_CORNER_PROGRESS = 200.0f;

    private FrameImageView mSelectedFrameImageView;
    private FramePhotoLayout mFramePhotoLayout;
    private LinearLayout mSpaceLayout;
    private LinearLayout mMainMenuLayout;
    private SeekBar mSpaceBar;
    private SeekBar mCornerBar;
    private float mSpace = DEFAULT_SPACE;
    private float mCorner = 0;
    //Background
    private int mBackgroundColor = Color.BLACK;
    private Bitmap mBackgroundImage;
    private Uri mBackgroundUri = null;
    //Saved instance state
    private Bundle mSavedInstanceState;

    private ImageView back, save;
    private LinearLayout layout, sticker, bgcolor, textBtn, mStickerLayoutView;
    private RecyclerView bgColorRecycler;
    private String[] emojies;
    private String[] colors;

    private RecyclerView mRatioRecycleView;
    //    private LinearLayout mSubMenuParent;
    private RatioAdapter mRatioAdapter;
    private List<RatioItem> mRatioItemList;
    private LinearLayout mLayoutHeaders;

    private ViewPager2 mStickerViewPager;
    private TabLayout mStickerTablayout;
    private StickerTabAdapter mStickerTabAdapter;
    private List<StickerCategory> stickerCategories;

    @Override
    protected boolean isShowingAllTemplates() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MAX_SPACE = ImageUtils.pxFromDp(CollageActivity.this, 30);
        MAX_CORNER = ImageUtils.pxFromDp(CollageActivity.this, 60);
        DEFAULT_SPACE = ImageUtils.pxFromDp(CollageActivity.this, 2);


        //restore old params
        if (savedInstanceState != null) {
            mSpace = savedInstanceState.getFloat("mSpace");
            mCorner = savedInstanceState.getFloat("mCorner");
            mBackgroundColor = savedInstanceState.getInt("mBackgroundColor");
            mBackgroundUri = savedInstanceState.getParcelable("mBackgroundUri");
            mSavedInstanceState = savedInstanceState;
            if (mBackgroundUri != null)
                mBackgroundImage = ImageDecoder.decodeUriToBitmap(this, mBackgroundUri);
        }

        mSpaceBar = (SeekBar) findViewById(R.id.spaceBar);
//        mSubMenuParent = findViewById(R.id.sub_menu_parent);
        mSpaceBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSpace = MAX_SPACE * seekBar.getProgress() / MAX_SPACE_PROGRESS;
                if (mFramePhotoLayout != null)
                    mFramePhotoLayout.setSpace(mSpace, mCorner);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mCornerBar = (SeekBar) findViewById(R.id.cornerBar);
        mCornerBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCorner = MAX_CORNER * seekBar.getProgress() / MAX_CORNER_PROGRESS;
                if (mFramePhotoLayout != null)
                    mFramePhotoLayout.setSpace(mSpace, mCorner);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        back = findViewById(R.id.btnBack);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mRatioRecycleView = findViewById(R.id.ratio_recycle_View);

        mStickerLayoutView = findViewById(R.id.sticker_view_ll);
        mStickerViewPager = findViewById(R.id.viewPager);
        mStickerTablayout = findViewById(R.id.tabLayout);

        save = findViewById(R.id.save);
        save.setOnClickListener(v -> asyncSaveAndShare());

        layout = findViewById(R.id.layout);
        mLayoutHeaders = findViewById(R.id.layout_headers);
        layout.setOnClickListener(v -> {
            showLayoutUI();
        });

        sticker = findViewById(R.id.sticker);
        sticker.setOnClickListener(v -> {
            setUnpressBtn();
            ((ImageView) findViewById(R.id.tabIV2)).setColorFilter(ContextCompat.getColor(CollageActivity.this, R.color.btn_icon_color), android.graphics.PorterDuff.Mode.MULTIPLY);
            ((TextView) findViewById(R.id.tabTxt2)).setTextColor(getResources().getColor(R.color.btn_icon_color));

            hideControls();

            mStickerLayoutView.setVisibility(VISIBLE);
            stickerCategories = StickerLoader.loadStickers(this);
            mStickerTabAdapter = new StickerTabAdapter(this, stickerCategories);
            mStickerViewPager.setAdapter(mStickerTabAdapter);

            // Attach tabs to ViewPager2
            new TabLayoutMediator(mStickerTablayout, mStickerViewPager,
                    (tab, position) -> {
                        // Set the icon for each tab (replace with your own drawable resource)
                        String categoryName = mStickerTabAdapter.getCategoryName(position);

                        // Example: Set a custom image for each tab based on the category name
                        if (categoryName.equals("cat1")) {
                            tab.setIcon(R.drawable.sticker_category_1);  // Replace with actual drawable
                        } else if (categoryName.equals("cat2")) {
                            tab.setIcon(R.drawable.sticker_category_2);  // Replace with actual drawable
                        } else if (categoryName.equals("cat3")) {
                            tab.setIcon(R.drawable.sticker_category_3);  // Replace with actual drawable
                        } else if (categoryName.equals("cat4")) {
                            tab.setIcon(R.drawable.sticker_category_4);  // Replace with actual drawable
                        } else if (categoryName.equals("cat6")) {
                            tab.setIcon(R.drawable.sticker_category_5);  // Replace with actual drawable
                        } else if (categoryName.equals("cat7")) {
                            tab.setIcon(R.drawable.sticker_category_5);  // Replace with actual drawable
                        } else if (categoryName.equals("cat8")) {
                            tab.setIcon(R.drawable.sticker_category_5);  // Replace with actual drawable
                        } else if (categoryName.equals("cat9")) {
                            tab.setIcon(R.drawable.sticker_category_5);  // Replace with actual drawable
                        } else if (categoryName.equals("cat10")) {
                            tab.setIcon(R.drawable.sticker_category_5);  // Replace with actual drawable
                        } else {
                            tab.setIcon(R.drawable.sticker_category_1);   // Default icon
                        }
                    }
            ).attach();

//            startActivityes(null, 0);
        });

        mSpaceLayout = findViewById(R.id.border_layout);
        mMainMenuLayout = findViewById(R.id.main_menu);

        bgColorRecycler = findViewById(R.id.bgColorRecycler);
        bgColorRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        colors = getResources().getStringArray(R.array.color_array);
        ColorAdapter cadapter = new ColorAdapter(CollageActivity.this, colors);
        bgColorRecycler.setAdapter(cadapter);

        bgcolor = findViewById(R.id.bgcolor);
        bgcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnpressBtn();
                ((ImageView) findViewById(R.id.tabIV3)).setColorFilter(ContextCompat.getColor(CollageActivity.this, R.color.btn_icon_color), android.graphics.PorterDuff.Mode.MULTIPLY);
                ((TextView) findViewById(R.id.tabTxt3)).setTextColor(getResources().getColor(R.color.btn_icon_color));

                hideControls();
                bgColorRecycler.setVisibility(VISIBLE);

//                startActivityes(null, 0);
            }
        });

        textBtn = findViewById(R.id.textBtn);
        textBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnpressBtn();
                ((ImageView) findViewById(R.id.tabIV4)).setColorFilter(ContextCompat.getColor(CollageActivity.this, R.color.btn_icon_color), android.graphics.PorterDuff.Mode.MULTIPLY);
                ((TextView) findViewById(R.id.tabTxt4)).setTextColor(getResources().getColor(R.color.btn_icon_color));

                hideControls();
                textButtonClick();
            }
        });
        showLayoutUI();
    }

    private void showBorderUI() {
        mSpaceLayout.setVisibility(VISIBLE);
        ((ImageView) findViewById(R.id.tabIV)).setColorFilter(ContextCompat.getColor(CollageActivity.this, R.color.btn_icon_color), android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.tabTxt)).setTextColor(getResources().getColor(R.color.btn_icon_color));

//        startActivityes(null, 0); called ads
    }

    private void showLayoutUI() {
        setUnpressBtn();
        ((ImageView) findViewById(R.id.tabIV)).setColorFilter(ContextCompat.getColor(CollageActivity.this, R.color.btn_icon_color), android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.tabTxt)).setTextColor(getResources().getColor(R.color.btn_icon_color));

        hideControls();
        ((TextView) findViewById(R.id.tv_header_layout)).setTextColor(getResources().getColor(R.color.btn_icon_color));
        mLayoutHeaders.setVisibility(VISIBLE);
        mTemplateView.setVisibility(VISIBLE);
//        startActivityes(null, 0); called ads
    }

    private void showRatioUI() {
        setUnpressBtn();
        ((TextView) findViewById(R.id.tv_header_ratio)).setTextColor(getResources().getColor(R.color.btn_icon_color));
        ((TextView) findViewById(R.id.tv_header_ratio)).setTextColor(getResources().getColor(R.color.btn_icon_color));

        ((ImageView) findViewById(R.id.tabIV)).setColorFilter(ContextCompat.getColor(CollageActivity.this, R.color.btn_icon_color), android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.tabTxt)).setTextColor(getResources().getColor(R.color.btn_icon_color));

        mLayoutHeaders.setVisibility(VISIBLE);
        mRatioRecycleView.setVisibility(VISIBLE);
        mTemplateView.setVisibility(View.GONE);

        mRatioRecycleView.setLayoutManager(new LinearLayoutManager(CollageActivity.this,
                LinearLayoutManager.HORIZONTAL, false));

        // Create data
        mRatioItemList = new ArrayList<>();
        mRatioItemList.add(new RatioItem("1 : 1", R.drawable.ratio_1_1, RATIO_1_1));
        mRatioItemList.add(new RatioItem("3 : 4", R.drawable.ratio_3_4, RATIO_3_4));
        mRatioItemList.add(new RatioItem("4 : 3", R.drawable.ratio_4_3, RATIO_4_3));
        mRatioItemList.add(new RatioItem("5 : 4", R.drawable.ratio_5_4, RATIO_5_4));
        mRatioItemList.add(new RatioItem("4 : 5", R.drawable.ratio_4_5, RATIO_4_5));
        mRatioItemList.add(new RatioItem("9 : 16", R.drawable.ratio_9_16, RATIO_9_16));
        mRatioItemList.add(new RatioItem("16 : 9", R.drawable.ratio_16_9, RATIO_16_9));
        mRatioItemList.add(new RatioItem("1 : 2", R.drawable.ratio_1_2, RATIO_1_2));
        mRatioItemList.add(new RatioItem("FB", R.drawable.ratio_fb, RATIO_fb));
        mRatioItemList.add(new RatioItem("3 : 2", R.drawable.ratio_3_2, RATIO_3_2));
        mRatioItemList.add(new RatioItem("2 : 3", R.drawable.ratio_2_3, RATIO_2_3));
        mRatioItemList.add(new RatioItem("x", R.drawable.ratio_x, RATIO_3_1));

        // Create and set the adapter
        mRatioAdapter = new RatioAdapter(CollageActivity.this, mRatioItemList, CollageActivity.this);
        mRatioRecycleView.setAdapter(mRatioAdapter);
    }

    void startActivityes(Intent intent, int requestCode) {
        if (!AdManager.isloadMAX) {
            AdManager.adCounter++;
            AdManager.showInterAd(CollageActivity.this, intent, requestCode);
        } else {
            AdManager.adCounter++;
            AdManager.showMaxInterstitial(CollageActivity.this, intent, requestCode);
        }
    }

    void setUnpressBtn() {
        ((ImageView) findViewById(R.id.tabIV)).setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.tabTxt)).setTextColor(Color.WHITE);

        ((ImageView) findViewById(R.id.tabIV2)).setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.tabTxt2)).setTextColor(Color.WHITE);

        ((ImageView) findViewById(R.id.tabIV3)).setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.tabTxt3)).setTextColor(Color.WHITE);

        ((ImageView) findViewById(R.id.tabIV4)).setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.tabTxt4)).setTextColor(Color.WHITE);

        ((TextView) findViewById(R.id.tv_header_layout)).setTextColor(Color.WHITE);
        ((TextView) findViewById(R.id.tv_header_ratio)).setTextColor(Color.WHITE);
        ((TextView) findViewById(R.id.tv_header_border)).setTextColor(Color.WHITE);
    }

    void hideControls() {
        mTemplateView.setVisibility(View.GONE);
        bgColorRecycler.setVisibility(View.GONE);
        mSpaceLayout.setVisibility(View.GONE);
//        stickerRecycler.setVisibility(View.GONE);
        mRatioRecycleView.setVisibility(View.GONE);
        mLayoutHeaders.setVisibility(View.GONE);
        mMainMenuLayout.setVisibility(VISIBLE);
//        mStickerHeaders.setVisibility(View.GONE);
        mStickerLayoutView.setVisibility(View.GONE);

    }

    public void setEmojiesSticker(String name) {
        InputStream inputStream = null;
        try {
            // get input stream
            inputStream = getAssets().open("stickers/" + name);
            // load image as Drawable
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

            File path = new File(Environment.getExternalStorageDirectory() + "/Download/stickers");
            if (!path.isDirectory()) {
                path.mkdirs();
            }
            File mypath = new File(path.getAbsolutePath(), name);

//            FileOutputStream fos = null;
            try {
//                fos = new FileOutputStream(mypath);
                // Use the compress method on the BitMap object to write image to the OutputStream
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(mypath));
                ImageEntity entity = new ImageEntity(Uri.fromFile(mypath), getResources());
                entity.setInitScaleFactor(0.5f);
                entity.setSticker(false);
                entity.load(CollageActivity.this,
                        (mPhotoView.getWidth() - entity.getWidth()) / 2,
                        (mPhotoView.getHeight() - entity.getHeight()) / 2, 0);
                mPhotoView.addImageEntity(entity);
                if (ResultContainer.getInstance().getImageEntities() != null) {
                    ResultContainer.getInstance().getImageEntities().add(entity);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
//                try {
//                    if (fos != null) {
//                        fos.close();
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        } catch (IOException ex) {
            return;
        }
        hideControls();
    }

    public void setBGColor(String color) {
        recycleBackgroundImage();
        mBackgroundColor = Color.parseColor(color);
        mContainerLayout.setBackgroundColor(mBackgroundColor);
        hideControls();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("mSpace", mSpace);
        outState.putFloat("mCornerBar", mCorner);
        outState.putInt("mBackgroundColor", mBackgroundColor);
        outState.putParcelable("mBackgroundUri", mBackgroundUri);
        if (mFramePhotoLayout != null) {
            mFramePhotoLayout.saveInstanceState(outState);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_collage_maker;
    }

    int width = 0, height = 0;

    @Override
    public Bitmap createOutputImage() throws OutOfMemoryError {
        try {
            width = mContainerLayout.getWidth();
            height = mContainerLayout.getHeight();
            Log.e("width * height", width + " * " + height);

            Bitmap template = mFramePhotoLayout.createImage();//viewToBitmap(mContainerLayout, width, height);
            Bitmap result = Bitmap.createBitmap(template.getWidth(), template.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            if (mBackgroundImage != null && !mBackgroundImage.isRecycled()) {
                canvas.drawBitmap(mBackgroundImage, new Rect(0, 0, mBackgroundImage.getWidth(), mBackgroundImage.getHeight()),
                        new Rect(0, 0, result.getWidth(), result.getHeight()), paint);
            } else {
                canvas.drawColor(mBackgroundColor);
            }

            canvas.drawBitmap(template, 0, 0, paint);
            template.recycle();
            template = null;
            Bitmap stickers = mPhotoView.getImage(mOutputScale);
            canvas.drawBitmap(stickers, 0, 0, paint);
            stickers.recycle();
            stickers = null;
            System.gc();
            return result;
        } catch (OutOfMemoryError error) {
            throw error;
        }
    }

    public static Bitmap viewToBitmap(View view, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    protected void buildLayout(TemplateItem item) {
        mFramePhotoLayout = new FramePhotoLayout(this, item.getPhotoItemList());
        mFramePhotoLayout.setQuickActionClickListener(this);
        if (mBackgroundImage != null && !mBackgroundImage.isRecycled()) {
            if (Build.VERSION.SDK_INT >= 16)
                mContainerLayout.setBackground(new BitmapDrawable(getResources(), mBackgroundImage));
            else
                mContainerLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), mBackgroundImage));
        } else {
            mContainerLayout.setBackgroundColor(mBackgroundColor);
        }

        switch (mLayoutRatio) {
            case RATIO_1_1:
                updateRatioParams(1, 1);
                break;
            case RATIO_1_2:
                updateRatioParams(1, 2);
                break;
            case RATIO_2_3:
                //pinterest
                updateRatioParams(2, 3);
                break;
            case RATIO_3_2:
                updateRatioParams(3, 2);
                break;
            case RATIO_3_4:
                updateRatioParams(3, 4);
                break;
            case RATIO_4_3:
                updateRatioParams(4, 3);
                break;
            case RATIO_4_5:
                updateRatioParams(4, 5);
                break;
            case RATIO_5_4:
                updateRatioParams(5, 4);
                break;
            case RATIO_9_16:
                updateRatioParams(9, 16);
                break;
            case RATIO_16_9:
                updateRatioParams(16, 9);
                break;
            case RATIO_3_1:
                // This is for twitter
                updateRatioParams(3, 1);
                break;
            case RATIO_fb:
                updateRatioParams(16, 9);
                break;
        }
        //reset space and corner seek bars
        mSpaceBar.setProgress((int) (MAX_SPACE_PROGRESS * mSpace / MAX_SPACE));
        mCornerBar.setProgress((int) (MAX_CORNER_PROGRESS * mCorner / MAX_CORNER));
    }

    private void updateRatioParams(double ratio1, double ratio2) {
        int viewWidth = mContainerLayout.getWidth();
        int viewHeight = mContainerLayout.getHeight();

        if (viewWidth > viewHeight) {
            viewWidth = (int) ((viewHeight * ratio1) / ratio2);
        } else {
            viewHeight = (int) ((viewWidth * ratio2) / ratio1);
        }

        mOutputScale = ImageUtils.calculateOutputScaleFactor(viewWidth, viewHeight);
        mFramePhotoLayout.build(viewWidth, viewHeight, mOutputScale, mSpace, mCorner);

        if (mSavedInstanceState != null) {
            mFramePhotoLayout.restoreInstanceState(mSavedInstanceState);
            mSavedInstanceState = null;
        }
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(viewWidth, viewHeight);
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mContainerLayout.removeAllViews();
        mContainerLayout.addView(mFramePhotoLayout, params);
        //add sticker view
        mContainerLayout.removeView(mPhotoView);
        mContainerLayout.addView(mPhotoView, params);
    }

    @Override
    public void onEditActionClick(com.limsphere.pe.frame.FrameImageView v) {
        mSelectedFrameImageView = v;
        if (v.getImage() != null && v.getPhotoItem().imagePath != null && v.getPhotoItem().imagePath.length() > 0) {
            Uri uri = Uri.fromFile(new File(v.getPhotoItem().imagePath));
//            requestEditingImage(uri);
        }
    }


    @Override
    public void onChangeActionClick(FrameImageView v) {
        mSelectedFrameImageView = v;
//        requestPhoto();
        Intent mIntent = new Intent(this, CustomGalleryActivity.class);
        mIntent.putExtra(CustomGalleryActivity.KEY_LIMIT_MAX_IMAGE, 1);
        mIntent.putExtra(CustomGalleryActivity.KEY_LIMIT_MIN_IMAGE, 1);
        startActivityForResult(mIntent, CustomGalleryActivity.PICKER_REQUEST_CODE);
    }


    @Override
    protected void resultEditImage(Uri uri) {
        if (mSelectedFrameImageView != null) {
            mSelectedFrameImageView.setImagePath(FileUtils.getPath(this, uri));
        }
    }

    @Override
    protected void resultFromPhotoEditor(Uri image) {
        if (mSelectedFrameImageView != null) {
            mSelectedFrameImageView.setImagePath(FileUtils.getPath(this, image));
        }
    }

    private void recycleBackgroundImage() {
        if (mBackgroundImage != null && !mBackgroundImage.isRecycled()) {
            mBackgroundImage.recycle();
            mBackgroundImage = null;
            System.gc();
        }
    }

    @Override
    protected void resultBackground(Uri uri) {
        recycleBackgroundImage();
        mBackgroundUri = uri;
        mBackgroundImage = ImageDecoder.decodeUriToBitmap(this, uri);
        if (Build.VERSION.SDK_INT >= 16)
            mContainerLayout.setBackground(new BitmapDrawable(getResources(), mBackgroundImage));
        else
            mContainerLayout.setBackgroundDrawable(new BitmapDrawable(getResources(), mBackgroundImage));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_PHOTO && resultCode == RESULT_OK) {
            ArrayList<String> mSelectedImages = data.getStringArrayListExtra("result");
            if (mSelectedFrameImageView != null && mSelectedImages != null && !mSelectedImages.isEmpty()) {
                mSelectedFrameImageView.setImagePath(mSelectedImages.get(0));
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void finish() {
        recycleBackgroundImage();
        if (mFramePhotoLayout != null) {
            mFramePhotoLayout.recycleImages();
        }
        super.finish();
    }


    /**
     * @param itemName
     */
    @Override
    public void onRatioItemClick(int itemName) {
        clickRatio(itemName);
    }

    public void onLayoutTabClicked(View view) {
        showLayoutUI();
    }

    public void onRatioTabClicked(View view) {
        showRatioUI();
    }

    public void onBorderTabClicked(View view) {
        hideControls();
        setUnpressBtn();
        ((TextView) findViewById(R.id.tv_header_border)).setTextColor(getResources().getColor(R.color.btn_icon_color));
        mLayoutHeaders.setVisibility(VISIBLE);
        showBorderUI();
    }
}
