package com.multimediachat.ui.sideindexers;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListAdapter;
import android.widget.ListView;

public class IndexableListView extends ListView {
	
	private boolean mIsFastScrollEnabled = false;
	private IndexScroller mScroller = null;
	//private GestureDetector mGestureDetector = null;
	
	public IndexableListView(Context context) {
		super(context);
	}
	
	public IndexableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public IndexableListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}
	
	@Override
	public boolean isFastScrollEnabled() {
		return mIsFastScrollEnabled;
	}
	
	@Override
	public void setFastScrollEnabled(boolean enabled) {
		mIsFastScrollEnabled = enabled;
		if (mIsFastScrollEnabled) {
			if (mScroller == null) {
				mScroller = new IndexScroller(getContext(), this);
				mScroller.show();
			}
		} 
		// To hide automatically
		/*else {
			if (mScroller != null) {
				mScroller.hide();
				mScroller = null;
			}
		}*/
	}
	
	public void hideMyScroller() {
		if (mScroller != null) {
			mScroller.hide();
		}
	}
	
	public void showMyScroller() {
		if (mScroller != null) {
			mScroller.show();
		}
	}
	
	@Override
	public void draw(Canvas canvas) {
		super.draw(canvas);
		if (mScroller != null) {
			mScroller.draw(canvas);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (mScroller != null && mScroller.onTouchEvent(event)) {
			return true;
		}
		/*if (mGestureDetector == null) {
			mGestureDetector = new GestureDetector(getContext(),
					new GestureDetector.SimpleOnGestureListener() {
						@Override
						public boolean onFling(MotionEvent event1, MotionEvent event2,
								float velocityX, float velocityY) {
							mScroller.show();
							return super.onFling(event1, event2, velocityX, velocityY);
						}
					});
		}
		mGestureDetector.onTouchEvent(event);*/
		
		return super.onTouchEvent(event);
	}
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent event) {
		return false;
	}
	
	@Override
	public void setAdapter(ListAdapter adapter) {
		super.setAdapter(adapter);
		if (mScroller != null) {
			mScroller.setAdapter(adapter);
		}
	}
	
	public void setSections(Object[] sections) {
		if ( mScroller != null ) {
			mScroller.setSections(sections);
		}
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		if (mScroller != null) {
			mScroller.onSizeChanged(w, h, oldw, oldh);
		}
	}
}