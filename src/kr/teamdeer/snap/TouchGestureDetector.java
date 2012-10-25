package kr.teamdeer.snap;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

public class TouchGestureDetector {

	private GestureDetector mDetector = null;
	private ScaleGestureDetector mScaleDetector = null;
	private OnGenericGestureListener mGenericListener = null;
	private OnScaleGestureListener mScaleListener = null;
	
	public static interface OnFinishedListener {
		public abstract void onFinished(MotionEvent e);
	}
	
	public static class OnGenericGestureListener
	extends GestureDetector.SimpleOnGestureListener
	implements OnFinishedListener {
		@Override
		public void onFinished(MotionEvent e) {}
	}
	
	public static class OnScaleGestureListener
	extends ScaleGestureDetector.SimpleOnScaleGestureListener
	implements OnFinishedListener {
		@Override
		public void onFinished(MotionEvent e) {}
	}
	
	public TouchGestureDetector(
			Context context,
			OnGenericGestureListener glistener,
			OnScaleGestureListener slistener) {
		mGenericListener = glistener;
		if (mGenericListener != null)
			mDetector = new GestureDetector(context, mGenericListener);
		mScaleListener = slistener;
		if (mScaleListener != null)
			mScaleDetector = new ScaleGestureDetector(context, mScaleListener);
	}
	
	public boolean onTouchEvent(MotionEvent ev) {
		boolean gd = mDetector != null ?
					mDetector.onTouchEvent(ev) :
					false;
		boolean sgd = mScaleDetector != null ?
					mScaleDetector.onTouchEvent(ev) :
					false;
		if (ev.getAction() == MotionEvent.ACTION_UP) {
				if (mGenericListener != null) mGenericListener.onFinished(ev);
				if (mScaleListener != null) mScaleListener.onFinished(ev);
		}
		return gd || sgd;
	}

}
