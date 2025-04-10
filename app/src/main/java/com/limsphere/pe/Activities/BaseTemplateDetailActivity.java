package com.limsphere.pe.Activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.limsphere.pe.ShareActivity;
import com.limsphere.pe.R;
import com.limsphere.pe.utils.Utils;
import com.limsphere.pe.adapter.HorizontalPreviewTemplateAdapter;
import com.limsphere.pe.appconfig.Logger;
import com.limsphere.pe.appconfig.AppConstant;
import com.limsphere.pe.model.TemplateItem;
import com.limsphere.pe.multitouch.controller.ImageEntity;
import com.limsphere.pe.multitouch.controller.MultiTouchEntity;
import com.limsphere.pe.multitouch.controller.TextEntity;
import com.limsphere.pe.multitouch.custom.OnDoubleClickListener;
import com.limsphere.pe.multitouch.custom.PhotoView;
import com.limsphere.pe.quickaction.QuickAction;
import com.limsphere.pe.quickaction.QuickActionItem;
import com.limsphere.pe.template.PhotoItem;
import com.limsphere.pe.utils.AdManager;
import com.limsphere.pe.utils.DateTimeUtils;
import com.limsphere.pe.utils.ImageUtils;
import com.limsphere.pe.utils.ResultContainer;
import com.limsphere.pe.utils.TemplateImageUtils;
import com.limsphere.pe.utils.collagelayout.FrameImageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;


public abstract class BaseTemplateDetailActivity extends BasePhotoActivity implements HorizontalPreviewTemplateAdapter.OnPreviewTemplateClickListener, OnDoubleClickListener {
    private static final String TAG = BaseTemplateDetailActivity.class.getSimpleName();
    private static final String PREF_NAME = "templateDetailPref";
    private static final String RATIO_KEY = "ratio";
    public static final int RATIO_1_1 = 0;
    public static final int RATIO_3_1 = 1;
    public static final int RATIO_3_4 = 2;
    public static final int RATIO_4_3 = 3;
    public static final int RATIO_5_4 = 4;
    public static final int RATIO_4_5 = 5;
    public static final int RATIO_16_9 = 6;
    public static final int RATIO_9_16 = 7;
    public static final int RATIO_1_2 = 8;
    public static final int RATIO_fb = 9;
    public static final int RATIO_3_2 = 10;
    public static final int RATIO_2_3 = 11;
    //action id
    private static final int ID_EDIT = 1;
    private static final int ID_DELETE = 2;
    private static final int ID_CANCEL = 3;

    protected RelativeLayout mContainerLayout;
    protected RecyclerView mTemplateView;
    public PhotoView mPhotoView;
    protected float mOutputScale = 1;
    protected View mAddImageView;
    protected Animation mAnimation;
    protected int mItemType = AppConstant.NORMAL_IMAGE_ITEM;
    protected TemplateItem mSelectedTemplateItem;
    protected ArrayList<TemplateItem> mTemplateItemList = new ArrayList<>();
    private int mImageInTemplateCount = 0;

    protected HorizontalPreviewTemplateAdapter mTemplateAdapter;
    protected List<String> mSelectedPhotoPaths = new ArrayList<>();
    private Dialog mRatioDialog;
    private SharedPreferences mPref;
    protected int mLayoutRatio = RATIO_1_1;
    private ImageEntity mSelectedEntity = null;
    private QuickAction mTextQuickAction;
    private QuickAction mStickerQuickAction;
    protected SharedPreferences mPreferences;
    private boolean mIsFrameImage = true;
    private boolean mClickedShareButton = false;

    //abstract methods
    protected abstract int getLayoutId();

    protected abstract void buildLayout(TemplateItem templateItem);

    protected abstract Bitmap createOutputImage();

    protected boolean isShowingAllTemplates() {
        return true;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG, "onCreate, savedInstanceState=" + savedInstanceState);
        setContentView(getLayoutId());
        mPref = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        mLayoutRatio = mPref.getInt(RATIO_KEY, RATIO_1_1);
        mImageInTemplateCount = getIntent().getIntExtra(ThumbListActivity.EXTRA_IMAGE_IN_TEMPLATE_COUNT, 0);
        mIsFrameImage = getIntent().getBooleanExtra(ThumbListActivity.EXTRA_IS_FRAME_IMAGE, true);
        final int selectedItemIndex = getIntent().getIntExtra(ThumbListActivity.EXTRA_SELECTED_TEMPLATE_INDEX, 0);
        final ArrayList<String> extraImagePaths = getIntent().getStringArrayListExtra(ThumbListActivity.EXTRA_IMAGE_PATHS);
        //pref
        mPreferences = getSharedPreferences(AppConstant.PREF_NAME, Context.MODE_PRIVATE);
        mContainerLayout = findViewById(R.id.containerLayout);
        mTemplateView = findViewById(R.id.collage_template_rv);
        mPhotoView = new PhotoView(this);
        mPhotoView.setOnDoubleClickListener(this);
        createQuickAction();

        mAnimation = AnimationUtils.loadAnimation(this, R.anim.slide_in_bottom);

        //loading data
        if (savedInstanceState != null) {
            mClickedShareButton = savedInstanceState.getBoolean("mClickedShareButton", false);
            final int idx = savedInstanceState.getInt("mSelectedTemplateItemIndex", 0);
            mImageInTemplateCount = savedInstanceState.getInt("mImageInTemplateCount", 0);
            mIsFrameImage = savedInstanceState.getBoolean("mIsFrameImage", false);
            loadFrameImages(mIsFrameImage);
            Logger.d(TAG, "onCreate, mTemplateItemList size=" + mTemplateItemList.size() + ", selected idx=" + idx + ", mImageInTemplateCount=" + mImageInTemplateCount);
            if (idx < mTemplateItemList.size() && idx >= 0)
                mSelectedTemplateItem = mTemplateItemList.get(idx);
            if (mSelectedTemplateItem != null) {
                ArrayList<String> imagePaths = savedInstanceState.getStringArrayList("photoItemImagePaths");
                if (imagePaths != null) {
                    int size = Math.min(imagePaths.size(), mSelectedTemplateItem.getPhotoItemList().size());
                    for (int i = 0; i < size; i++)
                        mSelectedTemplateItem.getPhotoItemList().get(i).imagePath = imagePaths.get(i);
                }
            }
            ArrayList<MultiTouchEntity> entities = savedInstanceState.getParcelableArrayList("mPhotoViewImageEntities");
            if (entities != null) {
                mPhotoView.setImageEntities(entities);
            }
        } else {
            loadFrameImages(mIsFrameImage);
            mSelectedTemplateItem = mTemplateItemList.get(selectedItemIndex);
            mSelectedTemplateItem.setSelected(true);
            if (extraImagePaths != null) {
                int size = Math.min(extraImagePaths.size(), mSelectedTemplateItem.getPhotoItemList().size());
                for (int i = 0; i < size; i++)
                    mSelectedTemplateItem.getPhotoItemList().get(i).imagePath = extraImagePaths.get(i);
            }
        }

        mTemplateAdapter = new HorizontalPreviewTemplateAdapter(BaseTemplateDetailActivity.this, mTemplateItemList, this, mIsFrameImage);
        //Show templates
        mTemplateView.setHasFixedSize(true);
        mTemplateView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        mTemplateView.setAdapter(mTemplateAdapter);
        //Create after initializing
        mContainerLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mOutputScale = ImageUtils.calculateOutputScaleFactor(mContainerLayout.getWidth(), mContainerLayout.getHeight());
                buildLayout(mSelectedTemplateItem);
                // remove listener
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    mContainerLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    mContainerLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            }
        });

        //Scroll to selected item
        if (mTemplateItemList != null && selectedItemIndex >= 0 && selectedItemIndex < mTemplateItemList.size()) {
            mTemplateView.scrollToPosition(selectedItemIndex);
        }

        if (!AdManager.isloadMAX) {
            //admob
            AdManager.initAd(BaseTemplateDetailActivity.this);
            AdManager.loadInterAd(BaseTemplateDetailActivity.this);
        } else {
            //MAX + Fb Ads
            AdManager.initMAX(BaseTemplateDetailActivity.this);
            AdManager.maxInterstital(BaseTemplateDetailActivity.this);
        }
    }

    private void loadFrameImages(boolean isFrameImage) {
        ArrayList<TemplateItem> mAllTemplateItemList = new ArrayList<>();
        if (!isFrameImage) {
            mAllTemplateItemList.addAll(TemplateImageUtils.loadTemplates());
        } else {
            mAllTemplateItemList.addAll(FrameImageUtils.loadFrameImages(this));
        }

        mTemplateItemList = new ArrayList<>();
        if (mImageInTemplateCount > 0) {
            for (TemplateItem item : mAllTemplateItemList)
                if (item.getPhotoItemList().size() == mImageInTemplateCount) {
                    mTemplateItemList.add(item);
                }
        } else {
            mTemplateItemList.addAll(mAllTemplateItemList);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        int idx = mTemplateItemList.indexOf(mSelectedTemplateItem);
        if (idx < 0) idx = 0;
        Logger.d(TAG, "onSaveInstanceState, idx=" + idx);
        outState.putInt("mSelectedTemplateItemIndex", idx);
        //saved all image path of template item
        ArrayList<String> imagePaths = new ArrayList<>();
        for (PhotoItem item : mSelectedTemplateItem.getPhotoItemList()) {
            if (item.imagePath == null) item.imagePath = "";
            imagePaths.add(item.imagePath);
        }
        outState.putStringArrayList("photoItemImagePaths", imagePaths);
        outState.putParcelableArrayList("mPhotoViewImageEntities", mPhotoView.getImageEntities());
        outState.putInt("mImageInTemplateCount", mImageInTemplateCount);
        outState.putBoolean("mIsFrameImage", mIsFrameImage);
        outState.putBoolean("mClickedShareButton", mClickedShareButton);
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d("PhotoCollageFragment.onPause",
                "onPause: width=" + mPhotoView.getWidth() + ", height = "
                        + mPhotoView.getHeight());
        mPhotoView.unloadImages();
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d("PhotoCollageFragment.onResume",
                "onResume: width=" + mPhotoView.getWidth() + ", height = "
                        + mPhotoView.getHeight());
        mPhotoView.loadImages(this);
        mPhotoView.invalidate();
        if (mClickedShareButton) {
            mClickedShareButton = false;

        }
    }

    private void createQuickAction() {
        QuickActionItem deleteItem = new QuickActionItem(ID_DELETE, getString(R.string.delete), getResources().getDrawable(R.drawable.menu_delete));
        QuickActionItem cancelItem = new QuickActionItem(ID_CANCEL, getString(R.string.cancel), getResources().getDrawable(R.drawable.menu_cancel));

        //create QuickAction. Use QuickAction.VERTICAL or QuickAction.HORIZONTAL param to define layout
        //orientation
        mTextQuickAction = new QuickAction(this, QuickAction.HORIZONTAL);
        mStickerQuickAction = new QuickAction(this, QuickAction.HORIZONTAL);
        //add action items into QuickAction
        mTextQuickAction.addActionItem(deleteItem);
        mTextQuickAction.addActionItem(cancelItem);
        mStickerQuickAction.addActionItem(deleteItem);
        mStickerQuickAction.addActionItem(cancelItem);
        //Set listener for action item clicked
        mTextQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                QuickActionItem quickActionItem = mTextQuickAction.getActionItem(pos);
                mTextQuickAction.dismiss();
                //here we can filter which action item was clicked with pos or actionId parameter
                if (actionId == ID_DELETE) {
                    mPhotoView.removeImageEntity(mSelectedEntity);
                } else if (actionId == ID_CANCEL) {

                }
            }
        });
        //Set listener for action item clicked
        mStickerQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                QuickActionItem quickActionItem = mStickerQuickAction.getActionItem(pos);
                mStickerQuickAction.dismiss();
                //here we can filter which action item was clicked with pos or actionId parameter
                if (actionId == ID_DELETE) {
                    mPhotoView.removeImageEntity(mSelectedEntity);
                } else if (actionId == ID_CANCEL) {

                }
            }
        });
        //set listnener for on dismiss event, this listener will be called only if QuickAction dialog was dismissed
        //by clicking the area outside the dialog.
        mTextQuickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
            @Override
            public void onDismiss() {

            }
        });

    }

    @Override
    public void onPhotoViewDoubleClick(PhotoView view, MultiTouchEntity entity) {
        mSelectedEntity = (ImageEntity) entity;
        if (mSelectedEntity instanceof TextEntity) {
            mTextQuickAction.show(view, (int) mSelectedEntity.getCenterX(), (int) mSelectedEntity.getCenterY());
        } else {
            mStickerQuickAction.show(view, (int) mSelectedEntity.getCenterX(), (int) mSelectedEntity.getCenterY());
        }
    }

    @Override
    public void onBackgroundDoubleClick() {

    }

    @Override
    public void onPreviewTemplateClick(TemplateItem item) {
        mSelectedTemplateItem.setSelected(false);
        for (int idx = 0; idx < mSelectedTemplateItem.getPhotoItemList().size(); idx++) {
            PhotoItem photoItem = mSelectedTemplateItem.getPhotoItemList().get(idx);
            if (photoItem.imagePath != null && photoItem.imagePath.length() > 0) {
                if (idx < mSelectedPhotoPaths.size()) {
                    mSelectedPhotoPaths.add(idx, photoItem.imagePath);
                } else {
                    mSelectedPhotoPaths.add(photoItem.imagePath);
                }
            }
        }

        final int size = Math.min(mSelectedPhotoPaths.size(), item.getPhotoItemList().size());
        for (int idx = 0; idx < size; idx++) {
            PhotoItem photoItem = item.getPhotoItemList().get(idx);
            if (photoItem.imagePath == null || photoItem.imagePath.length() < 1) {
                photoItem.imagePath = mSelectedPhotoPaths.get(idx);
            }
        }

        mSelectedTemplateItem = item;
        mSelectedTemplateItem.setSelected(true);
        mTemplateAdapter.notifyDataSetChanged();

        buildLayout(item);

    }


    public void clickRatio(int itemName){
        mPref.edit().putInt(RATIO_KEY, itemName).commit();
        mLayoutRatio = itemName;
        buildLayout(mSelectedTemplateItem);
//        if (mRatioDialog == null) {
//            AlertDialog.Builder builder = new AlertDialog.Builder(this);
//            String[] layoutRatioName = new String[]{getString(R.string.photo_editor_square), getString(R.string.fit),
//                    getString(R.string.golden_ratio),};
//
//            builder.setTitle(R.string.select_ratio);
//            builder.setSingleChoiceItems(layoutRatioName, mPref.getInt(RATIO_KEY, 0),
//                    new DialogInterface.OnClickListener() {
//
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            mPref.edit().putInt(RATIO_KEY, which).commit();
//                            mLayoutRatio = which;
//                            dialog.dismiss();
//                            buildLayout(mSelectedTemplateItem);
//                        }
//                    });
//            mRatioDialog = builder.create();
//        }
//        mRatioDialog.show();
    }


    public void asyncSaveAndShare() {
        AsyncTask<Void, Void, File> task = new AsyncTask<Void, Void, File>() {
            Dialog dialog;
            String errMsg;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                dialog = ProgressDialog.show(BaseTemplateDetailActivity.this, getString(R.string.app_name), getString(R.string.creating));
            }

            @Override
            protected File doInBackground(Void... params) {
                try {
                    Bitmap image = createOutputImage();
                    String fileName = DateTimeUtils.getCurrentDateTime().replaceAll(":", "-").concat(".png");
                    File collageFolder = new File(ImageUtils.OUTPUT_COLLAGE_FOLDER);
                    if (!collageFolder.exists()) {
                        collageFolder.mkdirs();
                    }
                    File photoFile = new File(collageFolder, fileName);
                    image.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(photoFile));
                    Utils.mediaScanner(BaseTemplateDetailActivity.this, ImageUtils.OUTPUT_COLLAGE_FOLDER+"/", fileName);
                    return photoFile;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    errMsg = ex.getMessage();
                } catch (OutOfMemoryError err) {
                    err.printStackTrace();
                    errMsg = err.getMessage();
                }
                return null;
            }

            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);
                try {
                    dialog.dismiss();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                if (file != null) {
                    Intent intent = new Intent(BaseTemplateDetailActivity.this, ShareActivity.class);
                    intent.putExtra("path", file.getAbsolutePath());
                    intent.putExtra("isCreation", false);

                    if (!AdManager.isloadMAX) {
                        AdManager.adCounter = 5;
                        AdManager.showInterAd(BaseTemplateDetailActivity.this, intent,0);
                    } else {
                        AdManager.adCounter = 5;
                        AdManager.showMaxInterstitial(BaseTemplateDetailActivity.this, intent,0);
                    }
                    Toast.makeText(BaseTemplateDetailActivity.this, "Saved Successfully...", Toast.LENGTH_LONG).show();
                } else if (errMsg != null) {
                    Toast.makeText(BaseTemplateDetailActivity.this, errMsg, Toast.LENGTH_LONG).show();
                }
                //log
                Bundle bundle = new Bundle();
                if (mIsFrameImage) {
                    String[] layoutRatioName = new String[]{"square", "fit", "golden"};
                    String ratio = "";
                    if (mLayoutRatio < layoutRatioName.length)
                        ratio = layoutRatioName[mLayoutRatio];
                } else {

                }

            }
        };
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public int[] calculateThumbnailSize(int imageWidth, int imageHeight) {
        int[] size = new int[2];
        float ratioWidth = ((float) imageWidth) / getPhotoViewWidth();
        float ratioHeight = ((float) imageHeight) / getPhotoViewHeight();
        float ratio = Math.max(ratioWidth, ratioHeight);
        if (ratio == ratioWidth) {
            size[0] = getPhotoViewWidth();
            size[1] = (int) (imageHeight / ratio);
        } else {
            size[0] = (int) (imageWidth / ratio);
            size[1] = getPhotoViewHeight();
        }

        return size;
    }

    private int getPhotoViewWidth() {
        return mContainerLayout.getWidth();
    }

    private int getPhotoViewHeight() {
        return mContainerLayout.getHeight();
    }

    public void textButtonClick(){
        mItemType = AppConstant.TEXT_ITEM;
        addTextItem();
    }

    @Override
    public void resultStickers(Uri[] uri) {
        super.resultPickMultipleImages(uri);
        final int size = uri.length;

        for (int idx = 0; idx < size; idx++) {
            float angle = (float) (idx * Math.PI / 20);

            ImageEntity entity = new ImageEntity(uri[idx], getResources());
            entity.setInitScaleFactor(0.25);
            entity.load(this,
                    (mPhotoView.getWidth() - entity.getWidth()) / 2,
                    (mPhotoView.getHeight() - entity.getHeight()) / 2, angle);
            mPhotoView.addImageEntity(entity);
            if (ResultContainer.getInstance().getImageEntities() != null) {
                ResultContainer.getInstance().getImageEntities().add(entity);
            }
        }
    }

    @Override
    protected void resultAddTextItem(String text, int color, String fontPath) {
        final TextEntity entity = new TextEntity(text, getResources());
        entity.setTextColor(color);
        entity.setTypefacePath(fontPath, BaseTemplateDetailActivity.this);
        entity.load(this,
                (mPhotoView.getWidth() - entity.getWidth()) / 2,
                (mPhotoView.getHeight() - entity.getHeight()) / 2);
        entity.setSticker(false);
        entity.setDrawImageBorder(true);
        mPhotoView.addImageEntity(entity);
        if (ResultContainer.getInstance().getImageEntities() != null) {
            ResultContainer.getInstance().getImageEntities().add(entity);
        }
    }

    @Override
    protected void resultEditTextItem(String text, int color, String fontPath) {
        if (mSelectedEntity instanceof TextEntity) {
            TextEntity textEntity = (TextEntity) mSelectedEntity;
            textEntity.setTextColor(color);
            textEntity.setTypefacePath(fontPath, BaseTemplateDetailActivity.this);
            textEntity.setText(text);
        }
    }

}
