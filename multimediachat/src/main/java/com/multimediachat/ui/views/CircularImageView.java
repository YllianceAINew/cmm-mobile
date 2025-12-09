package com.multimediachat.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;

import com.multimediachat.R;

public class CircularImageView extends PicaImageView
{
	public static class RoundedDrawable extends Drawable
	{

		protected final BitmapShader bitmapShader;
		protected final RectF mBitmapRect;
		protected final RectF mRect = new RectF();
		protected final Paint paint = new Paint();
		protected final Paint paintBorder = new Paint();
		protected int borderWidth = 3;

		public void draw(Canvas canvas)
		{
/*
			canvas.drawCircle(mRect.width() / 2.0F, mRect.height() / 2.0F, mRect.width() / 2.0F, paint);
//			canvas.drawRoundRect(mRect, 15F, 15F, paint);
*/
			float circleCenter = (mRect.width()-(borderWidth * 2)) / 2.0F;

			// circleCenter is the x or y of the view's center
			// radius is the radius in pixels of the cirle to be drawn
			// paint contains the shader that will texture the shape
			canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter, paint);
			canvas.drawCircle(circleCenter + borderWidth, circleCenter + borderWidth, circleCenter, paintBorder);
		}

		public int getOpacity()
		{
			return -3;
		}

		protected void onBoundsChange(Rect rect)
		{
			super.onBoundsChange(rect);
			mRect.set(0.0F, 0.0F, rect.width(), rect.height());
			Matrix matrix = new Matrix();
			matrix.setRectToRect(mBitmapRect, mRect, android.graphics.Matrix.ScaleToFit.FILL);
			if (rect.width() < 200)
				borderWidth = 1;
			else
				borderWidth = 10;
			/*borderWidth = rect.width()/60;
			if (borderWidth  == 0)
				borderWidth = 1;*/
			paintBorder.setStrokeWidth(borderWidth);
			bitmapShader.setLocalMatrix(matrix);
		}

		public void setAlpha(int i)
		{
			paint.setAlpha(i);
		}

		public void setColorFilter(ColorFilter colorfilter)
		{
			paint.setColorFilter(colorfilter);
		}

		public RoundedDrawable(Bitmap bitmap)
		{
			bitmapShader = new BitmapShader(bitmap, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP);
			mBitmapRect = new RectF(0.0F, 0.0F, bitmap.getWidth(), bitmap.getHeight());
			paint.setAntiAlias(true);
			paint.setShader(bitmapShader);

			paintBorder.setColor(Color.rgb(0xbb, 0xbb, 0xbb));
			paintBorder.setAntiAlias(true);
//			paintBorder.setShadowLayer(4.0f, 0.0f, 2.0f, Color.BLACK);
			paintBorder.setStrokeWidth(borderWidth);
			paintBorder.setStyle(Paint.Style.STROKE);
		}
	}


	public CircularImageView(Context context)
	{
		super(context);
	}

	public CircularImageView(Context context, AttributeSet attributeset)
	{
		super(context, attributeset);
	}

	public CircularImageView(Context context, AttributeSet attributeset, int i)
	{
		super(context, attributeset, i);
	}

	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
	}

	public void setBorderColor(int i)
	{
	}

	public void setBorderWidth(int width)
	{
	}

	public void setImageBitmap(Bitmap bitmap)
	{
		super.setImageDrawable(new RoundedDrawable(bitmap));
	}

	public void setImageDrawable(Drawable drawable)
	{
		if (drawable != null && (drawable instanceof RoundedDrawable))
		{
			super.setImageDrawable(drawable);
			return;
		}
		if (drawable != null && (drawable instanceof BitmapDrawable))
		{
			Bitmap bitmap = ((BitmapDrawable)drawable).getBitmap();
			if (bitmap == null || bitmap.isRecycled())
			{
				return;
			} else
			{
				super.setImageDrawable(new RoundedDrawable(bitmap));
				return;
			}
		} else
		{
			super.setImageDrawable(drawable);
			return;
		}
	}

	public void setImageResource(int i)
	{
		setImageDrawable(getResources().getDrawable(i));
	}

}