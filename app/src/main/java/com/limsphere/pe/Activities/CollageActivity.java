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
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.limsphere.pe.adapter.BgColorAdapter;
import com.limsphere.pe.adapter.BgGradientAdapter;
import com.limsphere.pe.adapter.CollageBgCategoryAdapter;
import com.limsphere.pe.adapter.RatioAdapter;
import com.limsphere.pe.adapter.StickerTabAdapter;
import com.limsphere.pe.frame.FrameImageView;
import com.limsphere.pe.frame.FramePhotoLayout;
import com.limsphere.pe.gallery.CustomGalleryActivity;
import com.limsphere.pe.model.RatioItem;
import com.limsphere.pe.model.StickerCategory;
import com.limsphere.pe.model.TemplateItem;
import com.limsphere.pe.utils.AdManager;
import com.limsphere.pe.utils.FileUtils;
import com.limsphere.pe.utils.ImageDecoder;
import com.limsphere.pe.utils.ImageUtils;
import com.limsphere.pe.utils.StickerLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CollageActivity extends BaseTemplateDetailActivity
        implements FramePhotoLayout.OnQuickActionClickListener, RatioAdapter.OnItemClickListener, BgColorAdapter.OnColorClickListener {
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
    private LinearLayout layout, sticker, bgcolor, textBtn, mStickerLayoutView, mBgColorView;
    private RecyclerView mBgColorRecycler, mBgCatRecycler;
    private BgGradientAdapter gradientColorAdapter;


    private RecyclerView mRatioRecycleView;
    //    private LinearLayout mSubMenuParent;
    private RatioAdapter mRatioAdapter;
    private List<RatioItem> mRatioItemList;
    private LinearLayout mLayoutHeaders;

    private ViewPager2 mStickerViewPager;
    private TabLayout mStickerTablayout;
    private StickerTabAdapter mStickerTabAdapter;
    private List<StickerCategory> stickerCategories;
    private BgColorAdapter solidColorAdapter;

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

            mStickerViewPager.setClipToPadding(false);
            mStickerViewPager.setClipChildren(false);
            mStickerViewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);
            mStickerViewPager.setPageTransformer((page, position) -> {
                float scale = Math.max(0.85f, 1 - Math.abs(position)); // Adjust scaling
                page.setScaleY(scale);
                page.setTranslationX(-position * page.getWidth() * 0.1f); // Adjust translation if needed
            });
            mStickerViewPager.setAdapter(mStickerTabAdapter);

            // Attach tabs to ViewPager2
            new TabLayoutMediator(mStickerTablayout, mStickerViewPager,
                    (tab, position) -> {
                        // Set the icon for each tab (replace with your own drawable resource)
                        String categoryName = mStickerTabAdapter.getCategoryName(position);

                        // Example: Set a custom image for each tab based on the category name
                        if (categoryName.equals("activity")) {
                            tab.setIcon(R.drawable.sticker_category_1);  // Replace with actual drawable
                        } else if (categoryName.equals("birthday")) {
                            tab.setIcon(R.drawable.sticker_category_2);  // Replace with actual drawable
                        } else if (categoryName.equals("celebration")) {
                            tab.setIcon(R.drawable.sticker_category_3);  // Replace with actual drawable
                        } else if (categoryName.equals("comic")) {
                            tab.setIcon(R.drawable.sticker_category_4);  // Replace with actual drawable
                        } else if (categoryName.equals("emoji")) {
                            tab.setIcon(R.drawable.sticker_category_5);  // Replace with actual drawable
                        } else if (categoryName.equals("emotion")) {
                            tab.setIcon(R.drawable.sticker_category_5);  // Replace with actual drawable
                        } else if (categoryName.equals("food")) {
                            tab.setIcon(R.drawable.sticker_category_5);  // Replace with actual drawable
                        } else if (categoryName.equals("love")) {
                            tab.setIcon(R.drawable.sticker_category_5);  // Replace with actual drawable
                        } else if (categoryName.equals("romance")) {
                            tab.setIcon(R.drawable.sticker_category_5);  // Replace with actual drawable
                        } else if (categoryName.equals("accessories")) {
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

        mBgColorView = findViewById(R.id.collage_bg_ll);
        mBgColorRecycler = findViewById(R.id.collage_bg_colors_rv);
        mBgCatRecycler = findViewById(R.id.collage_bg_cat_rv);
        mBgColorRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        // Setup category RecyclerView
        mBgCatRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Integer> categoryImages = Arrays.asList(R.drawable.bg_solid_cat, R.drawable.bg_gradient_cat);

        CollageBgCategoryAdapter categoryAdapter = new CollageBgCategoryAdapter(this, categoryImages, position -> {
            if (position == 0) {
                loadSolidColors();
            } else {
                loadGradientColors();
            }
        });

        mBgCatRecycler.setAdapter(categoryAdapter);

        // Setup default color RecyclerView
//        mBgColorRecycler.setLayoutManager(new GridLayoutManager(this, 4));
        loadSolidColors();
//        List<Integer> colors = new ArrayList<>();
//        // Convert Hex Strings to Color Integers
//        for (String color : mBgSolidColorsStrings) {
//            colors.add(Color.parseColor(color));
//        }
//
//        ColorAdapter adapter = new ColorAdapter(this, colors);
//        bgColorRecycler.setAdapter(adapter);

        bgcolor = findViewById(R.id.bgcolor);
        bgcolor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnpressBtn();
                ((ImageView) findViewById(R.id.tabIV3)).setColorFilter(ContextCompat.getColor(CollageActivity.this, R.color.btn_icon_color), android.graphics.PorterDuff.Mode.MULTIPLY);
                ((TextView) findViewById(R.id.tabTxt3)).setTextColor(getResources().getColor(R.color.btn_icon_color));

                hideControls();
                mBgColorView.setVisibility(VISIBLE);

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

    private void loadSolidColors() {
        List<Integer> colors = new ArrayList<>();
        for (String color : getResources().getStringArray(R.array.solid_color_list)) {
            colors.add(Color.parseColor(color));
        }

        solidColorAdapter = new BgColorAdapter(this, colors, this);
        mBgColorRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        mBgColorRecycler.setAdapter(solidColorAdapter);
    }

    private void loadGradientColors() {
        List<int[]> gradients = new ArrayList<>();
        for (String gradient : getResources().getStringArray(R.array.gradient_list)) {
            String[] colors = gradient.split(",");
            int[] gradientColors = {Color.parseColor(colors[0]), Color.parseColor(colors[1])};
            gradients.add(gradientColors);
        }

        gradientColorAdapter = new BgGradientAdapter(this, gradients, this);
        mBgColorRecycler.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        mBgColorRecycler.setAdapter(gradientColorAdapter);
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
        mBgColorView.setVisibility(View.GONE);
        mSpaceLayout.setVisibility(View.GONE);
        mRatioRecycleView.setVisibility(View.GONE);
        mLayoutHeaders.setVisibility(View.GONE);
        mMainMenuLayout.setVisibility(VISIBLE);
        mStickerLayoutView.setVisibility(View.GONE);
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

    /**
     * @param colorCode
     */
    @Override
    public void onSolidColorClick(String colorCode) {
        recycleBackgroundImage();
        mBackgroundColor = Color.parseColor(colorCode);
        mContainerLayout.setBackgroundColor(mBackgroundColor);
//        hideControls();
    }

    /**
     * @param startColor
     * @param endColor
     */
    @Override
    public void onGradientColorClick(String startColor, String endColor) {
        recycleBackgroundImage();

        int startColor1 = Color.parseColor(startColor);
        int endColor1 = Color.parseColor(endColor);

//        int[] colors = {Color.parseColor("#1E90FF"), Color.parseColor("#87CEFA")};
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{startColor1, endColor1}
        );

        Bitmap largeIcon = BitmapFactory.decodeResource(getResources(), R.drawable.bg_gradient_cat);

//        mBackgroundImage = drawableToBitmap(gradientDrawable);
        mContainerLayout.setBackground(gradientDrawable);

        mBackgroundImage = gradientDrawableToBitmap(gradientDrawable, 500,500);
    }

    // Method to convert GradientDrawable to Bitmap
    private Bitmap gradientDrawableToBitmap(GradientDrawable drawable, int width, int height) {
        // Create a Bitmap with specified width and height
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        // Create a Canvas and set the Bitmap to it
        Canvas canvas = new Canvas(bitmap);
        // Set bounds for the drawable
        drawable.setBounds(0, 0, width, height);
        // Draw the drawable onto the canvas
        drawable.draw(canvas);
        return bitmap;
    }
}
