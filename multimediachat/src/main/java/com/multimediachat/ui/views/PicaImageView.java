package com.multimediachat.ui.views;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

public class PicaImageView extends ImageView{
	
	public PicaImageView(Context context) {
		super(context);
	}
	
	public PicaImageView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public PicaImageView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }
	
	@Override
	protected void onDraw(Canvas canvas) {
		try{
			super.onDraw(canvas);
		} catch(OutOfMemoryError ome) {
		} catch(Exception e){
			e.printStackTrace();
		}
	}
}
