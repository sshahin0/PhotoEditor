package com.limsphere.pe.frame;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.limsphere.pe.appconfig.Logger;
import com.limsphere.pe.quickaction.QuickAction;
import com.limsphere.pe.template.PhotoItem;
import com.limsphere.pe.utils.ImageDecoder;
import com.limsphere.pe.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class FramePhotoLayout extends RelativeLayout implements FrameImageView.OnImageClickListener {
    private static final String TAG = FramePhotoLayout.class.getSimpleName();

    public interface OnQuickActionClickListener {
        void onEditActionClick(FrameImageView v);

        void onChangeActionClick(FrameImageView v);
    }

    OnDragListener mOnDragListener = new OnDragListener() {
        @Override
        public boolean onDrag(View v, DragEvent event) {
            int dragEvent = event.getAction();

            switch (dragEvent) {
                case DragEvent.ACTION_DRAG_ENTERED:
                    Logger.i("Drag Event", "Entered: x=" + event.getX() + ", y=" + event.getY());
                    break;

                case DragEvent.ACTION_DRAG_EXITED:
                    Logger.i("Drag Event", "Exited: x=" + event.getX() + ", y=" + event.getY());
                    break;

                case DragEvent.ACTION_DROP:
                    Logger.i("Drag Event", "Dropped: x=" + event.getX() + ", y=" + event.getY());
                    FrameImageView target = (FrameImageView) v;
                    FrameImageView selectedView = getSelectedFrameImageView(target, event);
                    if (selectedView != null) {
                        target = selectedView;
                        FrameImageView dragged = (FrameImageView) event.getLocalState();
                        if (target.getPhotoItem() != null && dragged.getPhotoItem() != null) {
                            String targetPath = target.getPhotoItem().imagePath;
                            String draggedPath = dragged.getPhotoItem().imagePath;
                            if (targetPath == null) targetPath = "";
                            if (draggedPath == null) draggedPath = "";
                            if (!targetPath.equals(draggedPath))
                                target.swapImage(dragged);
                        }
                    }
                    break;
            }

            return true;
        }
    };

    //action id
    private static final int ID_EDIT = 1;
    private static final int ID_CHANGE = 2;
    private static final int ID_DELETE = 3;
    private static final int ID_CANCEL = 4;

    private QuickAction mQuickAction;

    private List<PhotoItem> mPhotoItems;
    private List<FrameImageView> mItemImageViews;
    private int mViewWidth, mViewHeight;
    private float mOutputScaleRatio = 1;
    private OnQuickActionClickListener mQuickActionClickListener;
    private float mCurrentZoom = 1.0f;
    private float mMinZoom = 1.0f;
    private float mMaxZoom = 0.30f;
    private float mPivotX = 0.5f; // Default pivot point (center)
    private float mPivotY = 0.5f; // Default pivot point (center)

    public FramePhotoLayout(Context context, List<PhotoItem> photoItems) {
        super(context);
        mItemImageViews = new ArrayList<>();
        setLayerType(LAYER_TYPE_HARDWARE, null);
        mPhotoItems = photoItems;
    }

    private FrameImageView getSelectedFrameImageView(FrameImageView target, DragEvent event) {
        Logger.d(TAG, "getSelectedFrameImageView");
        FrameImageView dragged = (FrameImageView) event.getLocalState();
        int leftMargin = (int) (mViewWidth * target.getPhotoItem().bound.left);
        int topMargin = (int) (mViewHeight * target.getPhotoItem().bound.top);
        final float globalX = leftMargin + event.getX();
        final float globalY = topMargin + event.getY();
        for (int idx = mItemImageViews.size() - 1; idx >= 0; idx--) {
            FrameImageView view = mItemImageViews.get(idx);
            float x = globalX - mViewWidth * view.getPhotoItem().bound.left;
            float y = globalY - mViewHeight * view.getPhotoItem().bound.top;
            if (view.isSelected(x, y)) {
                if (view == dragged) {
                    return null;
                } else {
                    return view;
                }
            }
        }
        return null;
    }

    public void saveInstanceState(Bundle outState) {
        if (mItemImageViews != null)
            for (FrameImageView view : mItemImageViews)
                view.saveInstanceState(outState);
    }

    public void restoreInstanceState(Bundle savedInstanceState) {
        if (mItemImageViews != null)
            for (FrameImageView view : mItemImageViews)
                view.restoreInstanceState(savedInstanceState);
    }

    public void setQuickActionClickListener(OnQuickActionClickListener quickActionClickListener) {
        mQuickActionClickListener = quickActionClickListener;
    }


    private boolean isNotLargeThan1Gb() {
        ImageUtils.MemoryInfo memoryInfo = ImageUtils.getMemoryInfo(getContext());
        if (memoryInfo.totalMem > 0 && (memoryInfo.totalMem / 1048576.0 <= 1024)) {
            return true;
        } else {
            return false;
        }
    }

    public void build(final int viewWidth, final int viewHeight, final float outputScaleRatio, final float space, final float corner) {
        if (viewWidth < 1 || viewHeight < 1) {
            return;
        }
        //add children views
        mViewWidth = viewWidth;
        mViewHeight = viewHeight;
        mOutputScaleRatio = outputScaleRatio;
        mItemImageViews.clear();
        //A circle view always is on top
        if (mPhotoItems.size() > 4 || isNotLargeThan1Gb()) {
            ImageDecoder.SAMPLER_SIZE = 256;
        } else {
            ImageDecoder.SAMPLER_SIZE = 512;
        }
        Logger.d(TAG, "build, SAMPLER_SIZE = " + ImageDecoder.SAMPLER_SIZE);
        for (PhotoItem item : mPhotoItems) {
            FrameImageView imageView = addPhotoItemView(item, mOutputScaleRatio, space, corner);
            mItemImageViews.add(imageView);
        }
    }

    public void build(final int viewWidth, final int viewHeight, final float outputScaleRatio) {
        build(viewWidth, viewHeight, outputScaleRatio, 0, 0);
    }

    public void setSpace(float space, float corner) {
        for (FrameImageView img : mItemImageViews)
            img.setSpace(space, corner);
    }

    // Add this method to update the zoom level for the entire layout
    public void setZoomLevel(float progress) {
        // Convert progress (0-100) to zoom level (mMinZoom - mMaxZoom)
        mCurrentZoom = mMinZoom + (progress / 100f) * (mMaxZoom - mMinZoom);

        // Apply zoom to the entire layout
        setScaleX(mCurrentZoom);
        setScaleY(mCurrentZoom);

        // Set pivot point to center (for smoother zoom)
        setPivotX(getWidth() * 0.5f);
        setPivotY(getHeight() * 0.5f);

        requestLayout();
        invalidate();
    }

    // Optionally add method to set pivot point
    public void setZoomPivot(float pivotX, float pivotY) {
        mPivotX = pivotX;
        mPivotY = pivotY;
        setPivotX(getWidth() * mPivotX);
        setPivotY(getHeight() * mPivotY);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        // Store original dimensions
        mViewWidth = width;
        mViewHeight = height;

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private FrameImageView addPhotoItemView(PhotoItem item, float outputScaleRatio, final float space, final float corner) {
        final FrameImageView imageView = new FrameImageView(getContext(), item);
        int leftMargin = (int) (mViewWidth * item.bound.left);
        int topMargin = (int) (mViewHeight * item.bound.top);
        int frameWidth = 0, frameHeight = 0;
        if (item.bound.right == 1) {
            frameWidth = mViewWidth - leftMargin;
        } else {
            frameWidth = (int) (mViewWidth * item.bound.width() + 0.5f);
        }

        if (item.bound.bottom == 1) {
            frameHeight = mViewHeight - topMargin;
        } else {
            frameHeight = (int) (mViewHeight * item.bound.height() + 0.5f);
        }

        imageView.init(frameWidth, frameHeight, outputScaleRatio, space, corner);
        imageView.setOnImageClickListener(this);
        if (mPhotoItems.size() > 1)
            imageView.setOnDragListener(mOnDragListener);

        LayoutParams params = new LayoutParams(frameWidth, frameHeight);
        params.leftMargin = leftMargin;
        params.topMargin = topMargin;
        imageView.setOriginalLayoutParams(params);
        addView(imageView, params);
        return imageView;
    }

    public Bitmap createImage() throws OutOfMemoryError {
        try {
            Bitmap template = Bitmap.createBitmap((int) (mOutputScaleRatio * mViewWidth), (int) (mOutputScaleRatio * mViewHeight), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(template);
            canvas.scale(mOutputScaleRatio, mOutputScaleRatio);
            for (FrameImageView view : mItemImageViews)
                if (view.getImage() != null && !view.getImage().isRecycled()) {
                    final int left = (int) (view.getLeft() * mOutputScaleRatio);
                    final int top = (int) (view.getTop() * mOutputScaleRatio);
                    final int width = (int) (view.getWidth() * mOutputScaleRatio);
                    final int height = (int) (view.getHeight() * mOutputScaleRatio);
                    //draw image
                    canvas.saveLayer(left, top, left + width, top + height, new Paint(), Canvas.ALL_SAVE_FLAG);
                    canvas.translate(left, top);
                    canvas.clipRect(0, 0, width, height);
                    view.drawOutputImage(canvas);
                    canvas.restore();
                }

            return template;
        } catch (OutOfMemoryError error) {
            throw error;
        }
    }


    public void recycleImages() {
        Logger.d(TAG, "recycleImages");
        for (FrameImageView view : mItemImageViews) {
            view.recycleImage();
        }
        System.gc();
    }

    @Override
    public void onLongClickImage(FrameImageView v) {
        if (mPhotoItems.size() > 1) {
            v.setTag("x=" + v.getPhotoItem().x + ",y=" + v.getPhotoItem().y + ",path=" + v.getPhotoItem().imagePath);
            ClipData.Item item = new ClipData.Item((CharSequence) v.getTag());
            String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
            ClipData dragData = new ClipData(v.getTag().toString(), mimeTypes, item);
            DragShadowBuilder myShadow = new DragShadowBuilder(v);
            v.startDrag(dragData, myShadow, v, 0);
        }
    }

}
