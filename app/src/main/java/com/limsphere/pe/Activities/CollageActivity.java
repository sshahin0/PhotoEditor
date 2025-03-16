package com.limsphere.pe.Activities;

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
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.limsphere.pe.adapter.ColorAdapter;
import com.limsphere.pe.R;
import com.limsphere.pe.adapter.RatioAdapter;
import com.limsphere.pe.adapter.StickerAdapter;
import com.limsphere.pe.gallery.CustomGalleryActivity;
import com.limsphere.pe.frame.FrameImageView;
import com.limsphere.pe.frame.FramePhotoLayout;
import com.limsphere.pe.model.RatioItem;
import com.limsphere.pe.model.TemplateItem;
import com.limsphere.pe.multitouch.controller.ImageEntity;
import com.limsphere.pe.utils.AdManager;
import com.limsphere.pe.utils.FileUtils;
import com.limsphere.pe.utils.ImageDecoder;
import com.limsphere.pe.utils.ImageUtils;
import com.limsphere.pe.utils.ResultContainer;

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
    private LinearLayout mRatioLayout;
    private LinearLayout mMainMenuLayout;
    private ImageView mMenuBackBtn;
    private ImageView mMenuBackLayoutBtn;
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
    private LinearLayout ratio;
    private LinearLayout layout, sticker, adjust, bgcolor, textBtn;
    private LinearLayout templateLayout;
    private RecyclerView stickerRecycler, bgColorRecycler;
    private String[] emojies;
    private String[] colors;

    private RecyclerView mRatioRecycleView;
    private RatioAdapter mRatioAdapter;
    private List<RatioItem> mRatioItemList;

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

        ratio = findViewById(R.id.ratio);
        ratio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                clickRatio();

                mRatioLayout.setVisibility(View.VISIBLE);
                mMainMenuLayout.setVisibility(View.GONE);

                mRatioRecycleView = findViewById(R.id.ratio_recycle_View);
                mRatioRecycleView.setLayoutManager(new LinearLayoutManager(CollageActivity.this,
                        LinearLayoutManager.HORIZONTAL, false));

                // Create data
                mRatioItemList = new ArrayList<>();
                mRatioItemList.add(new RatioItem("3 : 4", R.drawable.ratio_34, RATIO_3_4)); // Replace with actual image resource
                mRatioItemList.add(new RatioItem("5 : 4", R.drawable.ratio_54, RATIO_5_4));
                mRatioItemList.add(new RatioItem("16 : 9", R.drawable.ratio_169, RATIO_16_9));
                mRatioItemList.add(new RatioItem("9 : 16", R.drawable.ratio_916, RATIO_9_16));
                mRatioItemList.add(new RatioItem("FB", R.drawable.ratio_fb, RATIO_fb));
                mRatioItemList.add(new RatioItem("Insta", R.drawable.ratio_instagram, RATIO_insta));
                mRatioItemList.add(new RatioItem("pInt", R.drawable.ratio_pinterest, RATIO_pInt));

                // Create and set the adapter
                mRatioAdapter = new RatioAdapter(mRatioItemList, CollageActivity.this);
                mRatioRecycleView.setAdapter(mRatioAdapter);
            }
        });
        mMenuBackBtn = findViewById(R.id.menu_back);
        mMenuBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRatioLayout.setVisibility(View.GONE);
                mMainMenuLayout.setVisibility(View.VISIBLE);
            }
        });

        save = findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                asyncSaveAndShare();
            }
        });

        templateLayout = findViewById(R.id.templateLayout);
        layout = findViewById(R.id.layout);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnpressBtn();
                ((ImageView) findViewById(R.id.tabIV)).setColorFilter(ContextCompat.getColor(CollageActivity.this, R.color.btn_icon_color), android.graphics.PorterDuff.Mode.MULTIPLY);
                ((TextView) findViewById(R.id.tabTxt)).setTextColor(getResources().getColor(R.color.btn_icon_color));

                hideControls();
                mMainMenuLayout.setVisibility(View.GONE);
                templateLayout.setVisibility(View.VISIBLE);

                startActivityes(null, 0);
            }
        });

        mMenuBackLayoutBtn = findViewById(R.id.menu_back_layout);
        mMenuBackLayoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                templateLayout.setVisibility(View.GONE);
                mMainMenuLayout.setVisibility(View.VISIBLE);
            }
        });

        sticker = findViewById(R.id.sticker);
        sticker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnpressBtn();
                ((ImageView) findViewById(R.id.tabIV2)).setColorFilter(ContextCompat.getColor(CollageActivity.this, R.color.btn_icon_color), android.graphics.PorterDuff.Mode.MULTIPLY);
                ((TextView) findViewById(R.id.tabTxt2)).setTextColor(getResources().getColor(R.color.btn_icon_color));

                hideControls();
                stickerRecycler.setVisibility(View.VISIBLE);

                startActivityes(null, 0);
            }
        });

        mSpaceLayout = findViewById(R.id.spaceLayout);
        mRatioLayout = findViewById(R.id.ratio_layout);
        mMainMenuLayout = findViewById(R.id.main_menu);
        adjust = findViewById(R.id.adjust);
        adjust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnpressBtn();
                ((ImageView) findViewById(R.id.tabIV1)).setColorFilter(ContextCompat.getColor(CollageActivity.this, R.color.btn_icon_color), android.graphics.PorterDuff.Mode.MULTIPLY);
                ((TextView) findViewById(R.id.tabTxt1)).setTextColor(getResources().getColor(R.color.btn_icon_color));

                hideControls();
                mSpaceLayout.setVisibility(View.VISIBLE);

                startActivityes(null, 0);
            }
        });

        stickerRecycler = findViewById(R.id.stickerRecycler);
        stickerRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        try {
            emojies = getAssets().list("stickers");
        } catch (IOException e) {
            e.printStackTrace();
        }
        StickerAdapter adapter = new StickerAdapter(CollageActivity.this, emojies);
        stickerRecycler.setAdapter(adapter);

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
                bgColorRecycler.setVisibility(View.VISIBLE);

                startActivityes(null, 0);
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

        ((ImageView) findViewById(R.id.tabIV1)).setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.tabTxt1)).setTextColor(Color.WHITE);

        ((ImageView) findViewById(R.id.tabIV2)).setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.tabTxt2)).setTextColor(Color.WHITE);

        ((ImageView) findViewById(R.id.tabIV3)).setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.tabTxt3)).setTextColor(Color.WHITE);

        ((ImageView) findViewById(R.id.tabIV4)).setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY);
        ((TextView) findViewById(R.id.tabTxt4)).setTextColor(Color.WHITE);
    }

    void hideControls() {
        templateLayout.setVisibility(View.GONE);
        bgColorRecycler.setVisibility(View.GONE);
        mSpaceLayout.setVisibility(View.GONE);
        mRatioLayout.setVisibility(View.GONE);
        stickerRecycler.setVisibility(View.GONE);
        mMainMenuLayout.setVisibility(View.VISIBLE);
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

        int viewWidth = mContainerLayout.getWidth();
        int viewHeight = mContainerLayout.getHeight();
        if (mLayoutRatio == RATIO_SQUARE) {
            if (viewWidth > viewHeight) {
                viewWidth = viewHeight;
            } else {
                viewHeight = viewWidth;
            }
        } else if (mLayoutRatio == RATIO_GOLDEN) {
            final double goldenRatio = 1.61803398875;
            if (viewWidth <= viewHeight) {
                if (viewWidth * goldenRatio >= viewHeight) {
                    viewWidth = (int) (viewHeight / goldenRatio);
                } else {
                    viewHeight = (int) (viewWidth * goldenRatio);
                }
            } else if (viewHeight <= viewWidth) {
                if (viewHeight * goldenRatio >= viewWidth) {
                    viewHeight = (int) (viewWidth / goldenRatio);
                } else {
                    viewWidth = (int) (viewHeight * goldenRatio);
                }
            }
        } else if (mLayoutRatio == RATIO_3_4) {
            if (viewWidth * 3 > viewHeight * 4) {
                // If the width is too wide, adjust the width based on the height
                viewWidth = viewHeight * 4 / 3;
            } else {
                // If the height is too tall, adjust the height based on the width
                viewHeight = viewWidth * 3 / 4;
            }
        } else if (mLayoutRatio == RATIO_5_4) {
            if (viewWidth * 4 > viewHeight * 5) {
                // If the width is too wide, adjust the width based on the height
                viewWidth = viewHeight * 5 / 4;
            } else {
                // If the height is too tall, adjust the height based on the width
                viewHeight = viewWidth * 4 / 5;
            }
        } else if (mLayoutRatio == RATIO_16_9) {
            if (viewWidth * 9 > viewHeight * 16) {
                // If the width is too wide, adjust the width based on the height
                viewWidth = viewHeight * 16 / 9;
            } else {
                // If the height is too tall, adjust the height based on the width
                viewHeight = viewWidth * 9 / 16;
            }
        } else if (mLayoutRatio == RATIO_9_16) {
            if (viewHeight * 9 > viewWidth * 16) {
                // If the height is too tall, adjust the height based on the width
                viewHeight = viewWidth * 16 / 9;
            } else {
                // If the width is too wide, adjust the width based on the height
                viewWidth = viewHeight * 9 / 16;
            }
        } else if (mLayoutRatio == RATIO_fb) {
            if (viewWidth * 1 > viewHeight * 1.91) {
                // If the width is too wide, adjust the width based on the height
                viewWidth = (int) (viewHeight * 1.91);
            } else {
                // If the height is too tall, adjust the height based on the width
                viewHeight = (int) (viewWidth / 1.91);
            }
        } else if (mLayoutRatio == RATIO_insta) {
            if (viewWidth > viewHeight) {
                viewWidth = viewHeight;
            } else {
                viewHeight = viewWidth;
            }
        } else if (mLayoutRatio == RATIO_pInt) {
            if (viewWidth * 3 > viewHeight * 2) {
                // If the width is too wide, adjust the width based on the height
                viewWidth = viewHeight * 2 / 3;
            } else {
                // If the height is too tall, adjust the height based on the width
                viewHeight = viewWidth * 3 / 2;
            }
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
        //reset space and corner seek bars
        mSpaceBar.setProgress((int) (MAX_SPACE_PROGRESS * mSpace / MAX_SPACE));
        mCornerBar.setProgress((int) (MAX_CORNER_PROGRESS * mCorner / MAX_CORNER));
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
}
