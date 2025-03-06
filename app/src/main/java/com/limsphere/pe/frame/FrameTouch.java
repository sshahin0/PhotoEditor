package com.limsphere.pe.frame;


import com.limsphere.pe.listener.OnFrameTouchListener;

public abstract class FrameTouch implements OnFrameTouchListener {
	private boolean mImageFrameMoving = false;

	public void setImageFrameMoving(boolean imageFrameMoving) {
		mImageFrameMoving = imageFrameMoving;
	}

	public boolean isImageFrameMoving() {
		return mImageFrameMoving;
	}

}
