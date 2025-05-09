package com.limsphere.pe.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.limsphere.pe.R;
import com.limsphere.pe.adapter.TemplateAdapter;
import com.limsphere.pe.adapter.TemplateViewHolder;
import com.limsphere.pe.model.TemplateItem;
import com.limsphere.pe.quickaction.QuickAction;
import com.limsphere.pe.quickaction.QuickActionItem;
import com.limsphere.pe.template.PhotoItem;
import com.limsphere.pe.utils.AdManager;
import com.limsphere.pe.utils.CustomGalleryPicker;
import com.limsphere.pe.utils.TemplateImageUtils;
import com.limsphere.pe.utils.collagelayout.FrameImageUtils;
import com.tonicartos.superslim.LayoutManager;

import java.util.ArrayList;

public class ThumbListActivity extends BaseFragmentActivity implements TemplateViewHolder.OnTemplateItemClickListener {
    private class ViewHolder {
        private final RecyclerView mRecyclerView;

        public ViewHolder(RecyclerView recyclerView) {
            mRecyclerView = recyclerView;
        }

        public void initViews(LayoutManager lm) {
            mRecyclerView.setLayoutManager(lm);
        }

        public void scrollToPosition(int position) {
            mRecyclerView.scrollToPosition(position);
        }

        public void setAdapter(RecyclerView.Adapter<?> adapter) {
            mRecyclerView.setAdapter(adapter);
        }

        public void smoothScrollToPosition(int position) {
            mRecyclerView.smoothScrollToPosition(position);
        }
    }

    public static final String EXTRA_IMAGE_PATHS = "imagePaths";
    public static final String EXTRA_IMAGE_IN_TEMPLATE_COUNT = "imageInTemplateCount";
    public static final String EXTRA_SELECTED_TEMPLATE_INDEX = "selectedTemplateIndex";
    public static final String EXTRA_IS_FRAME_IMAGE = "frameImage";

    private ViewHolder mViews;
    private TemplateAdapter mAdapter;
    private int mHeaderDisplay;
    private boolean mAreMarginsFixed;
    private CustomGalleryPicker customGalleryPicker;

    //Template views
    private ArrayList<TemplateItem> mTemplateItemList = new ArrayList<TemplateItem>();
    private ArrayList<TemplateItem> mAllTemplateItemList = new ArrayList<TemplateItem>();
    private boolean mFrameImages = true;

    //Frame filter Quick Action
    private QuickAction mQuickAction;
    private TextView mFilterView;
    private int mImageInTemplateCount = 0;
    private int mSelectedTemplateIndex = 0;
    private ImageView back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thumb_list);

        mHeaderDisplay = getResources().getInteger(R.integer.default_header_display);
        mAreMarginsFixed = getResources().getBoolean(R.bool.default_margins_fixed);

        mViews = new ViewHolder((RecyclerView) findViewById(R.id.recycler_view));
        mViews.initViews(new LayoutManager(this));

        // Initialize gallery picker
        customGalleryPicker = new CustomGalleryPicker(this, new CustomGalleryPicker.GalleryResultCallback() {
            @Override
            public void onGalleryResult(ArrayList<String> filePaths) {
                handleGalleryResult(filePaths);
            }

            @Override
            public void onGalleryCanceled() {
                Toast.makeText(ThumbListActivity.this, "Image selection canceled", Toast.LENGTH_SHORT).show();
            }
        });

        // Get frame images flag from intent
        mFrameImages = getIntent().getBooleanExtra("is_frame_images", true);
        
        //show templates
        if (mFrameImages) {
            loadFrameImages(false);
        } else {
            loadFrameImages(true);
        }

        mAdapter = new TemplateAdapter(this, mHeaderDisplay, mTemplateItemList, this, mFrameImages);
        mAdapter.setMarginsFixed(mAreMarginsFixed);
        mAdapter.setHeaderDisplay(mHeaderDisplay);
        mViews.setAdapter(mAdapter);

        //Frame count filter
        createFilterQuickAction();

        back = findViewById(R.id.btnBack);
        back.setOnClickListener(v -> onBackPressed());

        mFilterView = findViewById(R.id.frameCountView);
        mFilterView.setOnClickListener(v -> mQuickAction.show(mFilterView));

        LinearLayout adContainer = findViewById(R.id.banner_container);
        if (!AdManager.isloadMAX) {
            //admob
            AdManager.initAd(ThumbListActivity.this);
            AdManager.loadBannerAd(ThumbListActivity.this, adContainer);
            AdManager.loadInterAd(ThumbListActivity.this);
        } else {
            //MAX + Fb Ads
            AdManager.initMAX(ThumbListActivity.this);
            AdManager.maxBanner(ThumbListActivity.this, adContainer);
            AdManager.maxInterstital(ThumbListActivity.this);
        }
    }

    private void handleGalleryResult(ArrayList<String> filePaths) {
        try {
            final TemplateItem selectedTemplateItem = mTemplateItemList.get(mSelectedTemplateIndex);
            int itemSize = selectedTemplateItem.getPhotoItemList().size();
            int size = Math.min(itemSize, filePaths.size());

            for (int idx = 0; idx < size; idx++) {
                selectedTemplateItem.getPhotoItemList().get(idx).imagePath = filePaths.get(idx);
            }

            Intent intent = null;
            if (mFrameImages) {
                intent = new Intent(this, CollageActivity.class);
            } else {
                intent = new Intent(this, PIPActivity.class);
            }

            intent.putExtra(EXTRA_IMAGE_IN_TEMPLATE_COUNT, selectedTemplateItem.getPhotoItemList().size());
            intent.putExtra(EXTRA_IS_FRAME_IMAGE, mFrameImages);

            if (mImageInTemplateCount == 0) {
                ArrayList<TemplateItem> tmp = new ArrayList<>();
                for (TemplateItem item : mTemplateItemList)
                    if (item.getPhotoItemList().size() == selectedTemplateItem.getPhotoItemList().size()) {
                        tmp.add(item);
                    }
                intent.putExtra(EXTRA_SELECTED_TEMPLATE_INDEX, tmp.indexOf(selectedTemplateItem));
            } else {
                intent.putExtra(EXTRA_SELECTED_TEMPLATE_INDEX, mSelectedTemplateIndex);
            }

            ArrayList<String> imagePaths = new ArrayList<>();
            for (PhotoItem item : selectedTemplateItem.getPhotoItemList()) {
                if (item.imagePath == null) item.imagePath = "";
                imagePaths.add(item.imagePath);
            }
            intent.putExtra(EXTRA_IMAGE_PATHS, imagePaths);

            startActivityes(intent, 0);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadFrameImages(boolean template) {
        mAllTemplateItemList.clear();
        if (template) {
            mAllTemplateItemList.addAll(TemplateImageUtils.loadTemplates());
        } else {
            mAllTemplateItemList.addAll(FrameImageUtils.loadFrameImages(this));
        }
        mTemplateItemList.clear();
        if (mImageInTemplateCount > 0) {
            for (TemplateItem item : mAllTemplateItemList)
                if (item.getPhotoItemList().size() == mImageInTemplateCount) {
                    mTemplateItemList.add(item);
                }
        } else {
            mTemplateItemList.addAll(mAllTemplateItemList);
        }
    }

    private void createFilterQuickAction() {
        //create QuickAction. Use QuickAction.VERTICAL or QuickAction.HORIZONTAL param to define layout
        //orientation
        mQuickAction = new QuickAction(this, QuickAction.VERTICAL);
        mQuickAction.setPopupBackgroundColor(getResources().getColor(R.color.dark_gray));
        //add action items into QuickAction
        String[] filterTexts = getResources().getStringArray(R.array.frame_count);
        if (mFrameImages) {
            for (int idx = 0; idx < filterTexts.length; idx++) {
                QuickActionItem item = new QuickActionItem(idx, filterTexts[idx]);
                mQuickAction.addActionItem(item);
            }
        } else {
            for (int idx = 0; idx < 4; idx++) {
                QuickActionItem item = new QuickActionItem(idx, filterTexts[idx]);
                mQuickAction.addActionItem(item);
            }
        }
        //Set listener for action item clicked
        mQuickAction.setOnActionItemClickListener(new QuickAction.OnActionItemClickListener() {
            @Override
            public void onItemClick(QuickAction source, int pos, int actionId) {
                QuickActionItem quickActionItem = mQuickAction.getActionItem(pos);
                mQuickAction.dismiss();
                //here we can filter which action item was clicked with pos or actionId parameter
                mFilterView.setText(quickActionItem.getTitle());
                if (quickActionItem.getActionId() == 0) {
                    mTemplateItemList.clear();
                    mTemplateItemList.addAll(mAllTemplateItemList);
                    mImageInTemplateCount = 0;
                } else {
                    mTemplateItemList.clear();
                    mImageInTemplateCount = quickActionItem.getActionId();
                    for (TemplateItem item : mAllTemplateItemList)
                        if (item.getPhotoItemList().size() == quickActionItem.getActionId()) {
                            mTemplateItemList.add(item);
                        }
                }
                mAdapter.setData(mTemplateItemList);
            }
        });

        //set listener for on dismiss event
        mQuickAction.setOnDismissListener(new QuickAction.OnDismissListener() {
            @Override
            public void onDismiss() {
                //for dismiss
            }
        });
    }

    @Override
    public void onTemplateItemClick(TemplateItem templateItem) {
        mSelectedTemplateIndex = mTemplateItemList.indexOf(templateItem);
        customGalleryPicker.setLimits(templateItem.getPhotoItemList().size(), templateItem.getPhotoItemList().size());
        customGalleryPicker.launch();
    }

    void startActivityes(Intent intent, int requestCode) {
        if (!AdManager.isloadMAX) {
            AdManager.adCounter++;
            AdManager.showInterAd(ThumbListActivity.this, intent, requestCode);
        } else {
            AdManager.adCounter++;
            AdManager.showMaxInterstitial(ThumbListActivity.this, intent, requestCode);
        }
    }
}