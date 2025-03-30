package com.limsphere.pe.colorpicker;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.limsphere.pe.R;

public class ColorPickerViewParent extends LinearLayout implements ColorPickerView.OnColorChangedListener, View.OnClickListener {

    private ColorPickerView mColorPicker;
    private ImageView mOldColor;
    private ImageView mNewColor;
    private OnColorChangedListener mListener;
    private int oldColor;
    private int newColor;

    public interface OnColorChangedListener {
        void onColorChanged(int color);

        void onCrossPressed();
    }

    public ColorPickerViewParent(Context context) {
        this(context, null);
    }

    public ColorPickerViewParent(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorPickerViewParent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        setOrientation(VERTICAL);
        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.view_color_picker, this, true);

        mColorPicker = layout.findViewById(R.id.color_picker_view);
        mOldColor = layout.findViewById(R.id.old_color_panel);
        mNewColor = layout.findViewById(R.id.new_color_panel);

        ((LinearLayout) mOldColor.getParent()).setPadding(Math.round(mColorPicker.getDrawingOffset()), 0, Math.round(mColorPicker.getDrawingOffset()), 0);

        mOldColor.setOnClickListener(this);
        mNewColor.setOnClickListener(this);
        mColorPicker.setOnColorChangedListener(this);
    }

    public void setOldColor(int color) {
        oldColor = color;
        mColorPicker.setColor(color, true);
    }

    public int getColor() {
        return mColorPicker.getColor();
    }

    @Override
    public void onColorChanged(int color) {
        newColor = color;
    }

    public void setOnColorChangedListener(OnColorChangedListener listener) {
        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.new_color_panel && mListener != null) {
            mListener.onColorChanged(newColor);
        } else if (v.getId() == R.id.old_color_panel && mListener != null) {
            mListener.onCrossPressed();
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            oldColor = bundle.getInt("old_color");
            newColor = bundle.getInt("new_color");
            mColorPicker.setColor(newColor, true);
            super.onRestoreInstanceState(bundle.getParcelable("super_state"));
        } else {
            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("super_state", super.onSaveInstanceState());
        bundle.putInt("old_color", oldColor);
        bundle.putInt("new_color", newColor);
        return bundle;
    }
}
