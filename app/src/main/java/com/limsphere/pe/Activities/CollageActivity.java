package com.limsphere.pe.Activities;

import static android.view.View.VISIBLE;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
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
import com.limsphere.pe.colorpicker.ColorPickerDialog;
import com.limsphere.pe.colorpicker.ColorPickerViewParent;
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

public class CollageActivity extends BaseTemplateDetailActivity implements
        FramePhotoLayout.OnQuickActionClickListener,
        RatioAdapter.OnItemClickListener,
        BgColorAdapter.OnColorClickListener,
        ColorPickerDialog.OnColorChangedListener {

    // Constants
    private static final int REQUEST_SELECT_PHOTO = 1001;
    private static final float MAX_SPACE_PROGRESS = 300.0f;
    private static final float MAX_CORNER_PROGRESS = 200.0f;

    // Layout dimensions
    private float MAX_SPACE;
    private float MAX_CORNER;
    private float DEFAULT_SPACE;

    // UI Components
    private FrameImageView mSelectedFrameImageView;
    private FramePhotoLayout mFramePhotoLayout;
    private LinearLayout mSpaceLayout;
    private LinearLayout mMainMenuLayout;
    private SeekBar mSpaceBar;
    private SeekBar mCornerBar;

    // Background properties
    private int mBackgroundColor = Color.BLACK;
    private Bitmap mBackgroundImage;
    private Uri mBackgroundUri = null;

    // State management
    private Bundle mSavedInstanceState;

    // UI Views
    private ImageView back, save;
    private LinearLayout layout, sticker, bgcolor, textBtn, mStickerLayoutView, mBgColorView;
    private ColorPickerViewParent mColorChooser;
    private RecyclerView mBgColorRecycler, mBgCatRecycler;
    private BgGradientAdapter gradientColorAdapter;
    private RecyclerView mRatioRecycleView;
    private RatioAdapter mRatioAdapter;
    private List<RatioItem> mRatioItemList;
    private LinearLayout mLayoutHeaders;
    private ViewPager2 mStickerViewPager;
    private TabLayout mStickerTablayout;
    private StickerTabAdapter mStickerTabAdapter;
    private List<StickerCategory> stickerCategories;
    private BgColorAdapter solidColorAdapter;

    // Layout parameters
    private float mSpace;
    private float mCorner = 0;
    private int width = 0, height = 0;

    // ActivityResult launchers
    private final ActivityResultLauncher<androidx.activity.result.PickVisualMediaRequest> mPickMediaLauncher =
            registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), this::handleMediaPickResult);

    @Override
    protected boolean isShowingAllTemplates() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeDimensions();
        restoreInstanceState(savedInstanceState);
        initializeViews();
        setupSeekBars();
        setupButtons();
        setupBackgroundOptions();
        showLayoutUI();
    }

    private void initializeDimensions() {
        MAX_SPACE = ImageUtils.pxFromDp(this, 30);
        MAX_CORNER = ImageUtils.pxFromDp(this, 60);
        DEFAULT_SPACE = ImageUtils.pxFromDp(this, 2);
        mSpace = DEFAULT_SPACE;
    }

    private void restoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSpace = savedInstanceState.getFloat("mSpace");
            mCorner = savedInstanceState.getFloat("mCorner");
            mBackgroundColor = savedInstanceState.getInt("mBackgroundColor");
            mBackgroundUri = savedInstanceState.getParcelable("mBackgroundUri");
            mSavedInstanceState = savedInstanceState;
            if (mBackgroundUri != null) {
                mBackgroundImage = ImageDecoder.decodeUriToBitmap(this, mBackgroundUri);
            }
        }
    }

    private void initializeViews() {
        mSpaceBar = findViewById(R.id.spaceBar);
        mCornerBar = findViewById(R.id.cornerBar);
        mColorChooser = findViewById(R.id.color_picker_view_parent);
        back = findViewById(R.id.btnBack);
        save = findViewById(R.id.save);
        mRatioRecycleView = findViewById(R.id.ratio_recycle_View);
        mStickerLayoutView = findViewById(R.id.sticker_view_ll);
        mStickerViewPager = findViewById(R.id.viewPager);
        mStickerTablayout = findViewById(R.id.tabLayout);
        layout = findViewById(R.id.layout);
        mLayoutHeaders = findViewById(R.id.layout_headers);
        sticker = findViewById(R.id.sticker);
        mSpaceLayout = findViewById(R.id.border_layout);
        mMainMenuLayout = findViewById(R.id.main_menu);
        mBgColorView = findViewById(R.id.collage_bg_ll);
        mBgColorRecycler = findViewById(R.id.collage_bg_colors_rv);
        mBgCatRecycler = findViewById(R.id.collage_bg_cat_rv);
        bgcolor = findViewById(R.id.bgcolor);
        textBtn = findViewById(R.id.textBtn);
    }

    private void setupSeekBars() {
        mSpaceBar.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSpace = MAX_SPACE * progress / MAX_SPACE_PROGRESS;
                updateFrameLayoutSpacing();
            }
        });

        mCornerBar.setOnSeekBarChangeListener(new SimpleSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mCorner = MAX_CORNER * progress / MAX_CORNER_PROGRESS;
                updateFrameLayoutSpacing();
            }
        });
    }

    private void updateFrameLayoutSpacing() {
        if (mFramePhotoLayout != null) {
            mFramePhotoLayout.setSpace(mSpace, mCorner);
        }
    }

    private void setupButtons() {
        back.setOnClickListener(v -> onBackPressed());
        save.setOnClickListener(v -> asyncSaveAndShare());

        layout.setOnClickListener(v -> showLayoutUI());
        sticker.setOnClickListener(v -> showStickerUI());
        bgcolor.setOnClickListener(v -> showBackgroundColorUI());
        textBtn.setOnClickListener(v -> showTextUI());
    }

    private void setupBackgroundOptions() {
        mBgColorRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mBgCatRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        List<Integer> categoryImages = Arrays.asList(
                R.drawable.bg_gallery_cat,
                R.drawable.bg_solid_cat,
                R.drawable.bg_gradient_cat,
                R.drawable.bg_color_chooser_cat);

        CollageBgCategoryAdapter categoryAdapter = new CollageBgCategoryAdapter(
                this,
                categoryImages,
                this::handleBackgroundCategorySelection);

        mBgCatRecycler.setAdapter(categoryAdapter);
        loadSolidColors();
    }

    private void handleBackgroundCategorySelection(int position) {
        switch (position) {
            case 0:
                loadGalleryForBg();
                break;
            case 1:
                loadSolidColors();
                break;
            case 2:
                loadGradientColors();
                break;
            case 3:
                loadColorPicker();
                break;
        }
    }

    private void loadColorPicker() {
        hideControls();
        mColorChooser.setVisibility(VISIBLE);
        mColorChooser.setOnColorChangedListener(new ColorPickerViewParent.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                recycleBackgroundImage();
                mBackgroundColor = color;
                mContainerLayout.setBackgroundColor(mBackgroundColor);
                mBgColorView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCrossPressed() {
                hideControls();
                mBgColorView.setVisibility(VISIBLE);
            }
        });
    }

    private void loadGalleryForBg() {
        mPickMediaLauncher.launch(new androidx.activity.result.PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void handleMediaPickResult(Uri uri) {
        if (uri != null) {
            recycleBackgroundImage();
            mBackgroundImage = ImageDecoder.decodeUriToBitmap(this, uri);
            mContainerLayout.setBackground(new BitmapDrawable(getResources(), mBackgroundImage));
        }
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
        setTabPressed(R.id.tabIV, R.id.tabTxt);
    }

    private void showLayoutUI() {
        setUnpressAllButtons();
        setTabPressed(R.id.tabIV, R.id.tabTxt);
        hideControls();
        setHeaderPressed(R.id.tv_header_layout);
        mLayoutHeaders.setVisibility(VISIBLE);
        mTemplateView.setVisibility(VISIBLE);
    }

    private void showStickerUI() {
        setUnpressAllButtons();
        setTabPressed(R.id.tabIV2, R.id.tabTxt2);
        hideControls();
        mStickerLayoutView.setVisibility(VISIBLE);

        setupStickerViewPager();
    }

    private void setupStickerViewPager() {
        stickerCategories = StickerLoader.loadStickers(this);
        mStickerTabAdapter = new StickerTabAdapter(this, stickerCategories);

        mStickerViewPager.setClipToPadding(false);
        mStickerViewPager.setClipChildren(false);
        mStickerViewPager.getChildAt(0).setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

        mStickerViewPager.setPageTransformer((page, position) -> {
            float scale = Math.max(0.85f, 1 - Math.abs(position));
            page.setScaleY(scale);
            page.setTranslationX(-position * page.getWidth() * 0.1f);
        });

        mStickerViewPager.setAdapter(mStickerTabAdapter);

        new TabLayoutMediator(mStickerTablayout, mStickerViewPager, (tab, position) -> {
            String categoryName = mStickerTabAdapter.getCategoryName(position);
            tab.setIcon(getStickerCategoryIcon(categoryName));
        }).attach();
    }

    private int getStickerCategoryIcon(String categoryName) {
        switch (categoryName) {
            case "activity":
                return R.drawable.sticker_category_1;
            case "birthday":
                return R.drawable.sticker_category_2;
            case "celebration":
                return R.drawable.sticker_category_3;
            case "comic":
                return R.drawable.sticker_category_4;
            default:
                return R.drawable.sticker_category_5;
        }
    }

    private void showBackgroundColorUI() {
        setUnpressAllButtons();
        setTabPressed(R.id.tabIV3, R.id.tabTxt3);
        hideControls();
        mBgColorView.setVisibility(VISIBLE);
    }

    private void showTextUI() {
        setUnpressAllButtons();
        setTabPressed(R.id.tabIV4, R.id.tabTxt4);
        hideControls();
        textButtonClick();
    }

    private void showRatioUI() {
        setUnpressAllButtons();
        setHeaderPressed(R.id.tv_header_ratio);
        setTabPressed(R.id.tabIV, R.id.tabTxt);

        mLayoutHeaders.setVisibility(VISIBLE);
        mRatioRecycleView.setVisibility(VISIBLE);
        mTemplateView.setVisibility(View.GONE);

        setupRatioRecyclerView();
    }

    private void setupRatioRecyclerView() {
        mRatioRecycleView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        mRatioItemList = createRatioItems();
        mRatioAdapter = new RatioAdapter(this, mRatioItemList, this);
        mRatioRecycleView.setAdapter(mRatioAdapter);
    }

    private List<RatioItem> createRatioItems() {
        List<RatioItem> items = new ArrayList<>();
        items.add(new RatioItem("1 : 1", R.drawable.ratio_1_1, RATIO_1_1));
        items.add(new RatioItem("3 : 4", R.drawable.ratio_3_4, RATIO_3_4));
        items.add(new RatioItem("4 : 3", R.drawable.ratio_4_3, RATIO_4_3));
        items.add(new RatioItem("5 : 4", R.drawable.ratio_5_4, RATIO_5_4));
        items.add(new RatioItem("4 : 5", R.drawable.ratio_4_5, RATIO_4_5));
        items.add(new RatioItem("9 : 16", R.drawable.ratio_9_16, RATIO_9_16));
        items.add(new RatioItem("16 : 9", R.drawable.ratio_16_9, RATIO_16_9));
        items.add(new RatioItem("1 : 2", R.drawable.ratio_1_2, RATIO_1_2));
        items.add(new RatioItem("FB", R.drawable.ratio_fb, RATIO_fb));
        items.add(new RatioItem("3 : 2", R.drawable.ratio_3_2, RATIO_3_2));
        items.add(new RatioItem("2 : 3", R.drawable.ratio_2_3, RATIO_2_3));
        items.add(new RatioItem("x", R.drawable.ratio_x, RATIO_3_1));
        return items;
    }

    private void startActivityWithAds(Intent intent, int requestCode) {
        if (!AdManager.isloadMAX) {
            AdManager.adCounter++;
            AdManager.showInterAd(this, intent, requestCode);
        } else {
            AdManager.adCounter++;
            AdManager.showMaxInterstitial(this, intent, requestCode);
        }
    }

    private void setUnpressAllButtons() {
        // Reset all tab buttons
        setTabUnpressed(R.id.tabIV, R.id.tabTxt);
        setTabUnpressed(R.id.tabIV2, R.id.tabTxt2);
        setTabUnpressed(R.id.tabIV3, R.id.tabTxt3);
        setTabUnpressed(R.id.tabIV4, R.id.tabTxt4);

        // Reset all headers
        setHeaderUnpressed(R.id.tv_header_layout);
        setHeaderUnpressed(R.id.tv_header_ratio);
        setHeaderUnpressed(R.id.tv_header_border);
    }

    private void setTabPressed(int imageViewId, int textViewId) {
        ((ImageView) findViewById(imageViewId)).setColorFilter(
                ContextCompat.getColor(this, R.color.btn_icon_color),
                android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(textViewId)).setTextColor(getResources().getColor(R.color.btn_icon_color));
    }

    private void setTabUnpressed(int imageViewId, int textViewId) {
        ((ImageView) findViewById(imageViewId)).setColorFilter(Color.WHITE,
                android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(textViewId)).setTextColor(Color.WHITE);
    }

    private void setHeaderPressed(int textViewId) {
        ((TextView) findViewById(textViewId)).setTextColor(getResources().getColor(R.color.btn_icon_color));
    }

    private void setHeaderUnpressed(int textViewId) {
        ((TextView) findViewById(textViewId)).setTextColor(Color.WHITE);
    }

    private void hideControls() {
        mTemplateView.setVisibility(View.GONE);
        mBgColorView.setVisibility(View.GONE);
        mSpaceLayout.setVisibility(View.GONE);
        mRatioRecycleView.setVisibility(View.GONE);
        mLayoutHeaders.setVisibility(View.GONE);
        mMainMenuLayout.setVisibility(VISIBLE);
        mStickerLayoutView.setVisibility(View.GONE);
        mColorChooser.setVisibility(View.GONE);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
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

    @Override
    public Bitmap createOutputImage() throws OutOfMemoryError {
        try {
            width = mContainerLayout.getWidth();
            height = mContainerLayout.getHeight();
            Log.d("CollageActivity", "Creating output image with dimensions: " + width + "x" + height);

            Bitmap template = mFramePhotoLayout.createImage();
            Bitmap result = Bitmap.createBitmap(template.getWidth(), template.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            // Draw background
            if (mBackgroundImage != null && !mBackgroundImage.isRecycled()) {
                canvas.drawBitmap(mBackgroundImage,
                        new Rect(0, 0, mBackgroundImage.getWidth(), mBackgroundImage.getHeight()),
                        new Rect(0, 0, result.getWidth(), result.getHeight()),
                        paint);
            } else {
                canvas.drawColor(mBackgroundColor);
            }

            // Draw template and stickers
            canvas.drawBitmap(template, 0, 0, paint);
            Bitmap stickers = mPhotoView.getImage(mOutputScale);
            canvas.drawBitmap(stickers, 0, 0, paint);

            // Clean up
            template.recycle();
            stickers.recycle();
            System.gc();

            return result;
        } catch (OutOfMemoryError error) {
            Log.e("CollageActivity", "Out of memory error while creating output image");
            throw error;
        }
    }

    @Override
    protected void buildLayout(TemplateItem item) {
        mFramePhotoLayout = new FramePhotoLayout(this, item.getPhotoItemList());
        mFramePhotoLayout.setQuickActionClickListener(this);

        // Set background
        if (mBackgroundImage != null && !mBackgroundImage.isRecycled()) {
            mContainerLayout.setBackground(new BitmapDrawable(getResources(), mBackgroundImage));
        } else {
            mContainerLayout.setBackgroundColor(mBackgroundColor);
        }

        // Apply ratio
        applyLayoutRatio();

        // Reset seek bars
        mSpaceBar.setProgress((int) (MAX_SPACE_PROGRESS * mSpace / MAX_SPACE));
        mCornerBar.setProgress((int) (MAX_CORNER_PROGRESS * mCorner / MAX_CORNER));
    }

    private void applyLayoutRatio() {
        switch (mLayoutRatio) {
            case RATIO_1_1:
                updateRatioParams(1, 1);
                break;
            case RATIO_1_2:
                updateRatioParams(1, 2);
                break;
            case RATIO_2_3:
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
                updateRatioParams(3, 1);
                break;
            case RATIO_fb:
                updateRatioParams(16, 9);
                break;
        }
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
        mContainerLayout.removeView(mPhotoView);
        mContainerLayout.addView(mPhotoView, params);
    }

    @Override
    public void onEditActionClick(FrameImageView v) {
        mSelectedFrameImageView = v;
        if (v.getImage() != null && v.getPhotoItem().imagePath != null && !v.getPhotoItem().imagePath.isEmpty()) {
            Uri uri = Uri.fromFile(new File(v.getPhotoItem().imagePath));
            // requestEditingImage(uri);
        }
    }

    @Override
    public void onChangeActionClick(FrameImageView v) {
        mSelectedFrameImageView = v;
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
        mContainerLayout.setBackground(new BitmapDrawable(getResources(), mBackgroundImage));
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
        setUnpressAllButtons();
        setHeaderPressed(R.id.tv_header_border);
        mLayoutHeaders.setVisibility(VISIBLE);
        showBorderUI();
    }

    @Override
    public void onSolidColorClick(String colorCode) {
        recycleBackgroundImage();
        mBackgroundColor = Color.parseColor(colorCode);
        mContainerLayout.setBackgroundColor(mBackgroundColor);
    }

    @Override
    public void onGradientColorClick(String startColor, String endColor) {
        recycleBackgroundImage();

        int startColorValue = Color.parseColor(startColor);
        int endColorValue = Color.parseColor(endColor);

        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{startColorValue, endColorValue});

        mContainerLayout.setBackground(gradientDrawable);
        mBackgroundImage = gradientDrawableToBitmap(gradientDrawable, 500, 500);
    }

    private Bitmap gradientDrawableToBitmap(GradientDrawable drawable, int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    @Override
    public void onColorChanged(int color) {
        recycleBackgroundImage();
        mBackgroundColor = color;
        mContainerLayout.setBackgroundColor(mBackgroundColor);
    }

    // Helper class for simplified SeekBar listeners
    private abstract static class SimpleSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}