package com.multimediachat.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.multimediachat.R;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.ImApp;
import com.multimediachat.global.GlobalFunc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.widget.ImageView;

public class SampleImageBackgroundLoader {
	private final Map<String, Bitmap> mCache = new HashMap<String, Bitmap>();
	private final LinkedList<String> mCacheController = new LinkedList<String>();
	private ExecutorService mThreadPool;
	private final Map<ImageView, String> mImageViews = Collections
			.synchronizedMap(new WeakHashMap<ImageView, String>());

	public static int MAX_CACHE_SIZE = 30;
	public int THREAD_POOL_SIZE = 3;

	public static BitmapFactory.Options bmOptions = new BitmapFactory.Options();
	private Context mContext;
	Handler handler = new Handler();
	int imageHeight;

	ImApp mApp;

	/**
	 * Constructor
	 */
	public SampleImageBackgroundLoader(Context aContext) {
		mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		mContext = aContext;
		bmOptions.inDither = false;
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
		imageHeight = mContext.getResources().getDimensionPixelSize(R.dimen.chat_image_height);
		mApp = (ImApp) ((Activity) mContext).getApplication();
	}

	/**
	 * Clears all instance data and stops running threads
	 */
	public void Reset() {
		ExecutorService oldThreadPool = mThreadPool;
		mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		oldThreadPool.shutdownNow();

		mCacheController.clear();
		mCache.clear();
		mImageViews.clear();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void loadBitmap(final String filePath, final ImageView imageView, Bitmap placeholder, boolean isLeft) {
		mImageViews.put(imageView, filePath);
		Bitmap bm = getBitmapFromCache(filePath);
		if (bm != null) {
			imageView.setImageBitmap(bm);

			if (android.os.Build.VERSION.SDK_INT >= 16)
				imageView.setBackground(null);
			else
				imageView.setBackgroundDrawable(null);
		} else {
			queueJob(filePath, imageView, placeholder, isLeft);
		}
	}

	public Bitmap getBitmapFromCache(String filePath) {
		if (mCache.containsKey(filePath)) {
			return mCache.get(filePath);
		}

		return null;
	}

	private synchronized void putBitmapInCache(String filePath, Bitmap _bm) {
		int chacheControllerSize = mCacheController.size();
		if (chacheControllerSize > MAX_CACHE_SIZE) {
			List<String> tmpList = mCacheController.subList(0, MAX_CACHE_SIZE / 2);
			for (int i = 0; i < tmpList.size(); i++) {
				String tmpUri = tmpList.get(i);
				if (tmpUri != null) {
					Bitmap tmpBm = mCache.get(tmpUri);
					if (tmpBm != null && !tmpBm.isRecycled()) {
						tmpBm.recycle();
						tmpBm = null;
					}
					mCache.remove(tmpUri);
				}
			}
			tmpList.clear();
		}
		mCacheController.addLast(filePath);
		mCache.put(filePath, _bm);
	}

	private void queueJob(final String filePath, final ImageView imageView, final Bitmap placeholder,
			final boolean isLeft) {
		PhotoToLoad p = new PhotoToLoad(filePath, imageView, placeholder, isLeft);
		mThreadPool.submit(new PhotosLoader(p));
	}

	// Task for the queue
	private class PhotoToLoad {
		public String filePath;
		public ImageView imageView;
		public Bitmap placeHolder;
		public boolean isLeft;

		public PhotoToLoad(String _filePath, ImageView i, Bitmap bm, boolean _isLeft) {
			filePath = _filePath;
			imageView = i;
			placeHolder = bm;
			isLeft = _isLeft;
		}
	}

	class PhotosLoader implements Runnable {
		PhotoToLoad photoToLoad;

		PhotosLoader(PhotoToLoad photoToLoad) {
			this.photoToLoad = photoToLoad;
		}

		@Override
		public void run() {
			try {

				if (imageViewReused(photoToLoad))
					return;
				Bitmap bmp = getThumbnail(photoToLoad.filePath, photoToLoad.isLeft);
				if (imageViewReused(photoToLoad))
					return;
				BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad, photoToLoad.placeHolder);
				handler.post(bd);

			} catch (Throwable th) {
				th.printStackTrace();
			}
		}
	}

	boolean imageViewReused(PhotoToLoad photoToLoad) {
		String tag = mImageViews.get(photoToLoad.imageView);
		// Check url is already exist in imageViews MAP
		return tag == null || !tag.equals(photoToLoad.filePath);
	}

	class BitmapDisplayer implements Runnable {
		Bitmap bitmap;
		PhotoToLoad photoToLoad;
		Bitmap placeHolder;

		public BitmapDisplayer(Bitmap b, PhotoToLoad p, Bitmap _placeHolder) {
			bitmap = b;
			photoToLoad = p;
			placeHolder = _placeHolder;
		}

		@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
		public void run() {
			if (imageViewReused(photoToLoad))
				return;

			if (bitmap != null) {
				photoToLoad.imageView.setImageBitmap(bitmap);
				if (android.os.Build.VERSION.SDK_INT >= 16)
					photoToLoad.imageView.setBackground(null);
				else
					photoToLoad.imageView.setBackgroundDrawable(null);
			}
		}
	}

	private Bitmap getThumbnail(String filePath, boolean isLeft) {
		Bitmap bm = null;

		File file = new File(filePath);
		if (file == null || !file.exists()) {
			return null;
		}

		InputStream istr = null;
		try {
			istr = new FileInputStream(filePath);
			bm = BitmapFactory.decodeStream(istr, null, bmOptions);
			istr.close();
		} catch (Exception e) {
			DebugConfig.error("SampleImageLoader", "error=" + e.toString());
		} finally {
			if (istr != null) {
				try {
					istr.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		}
		Bitmap bmScaled = null;

		if (bm != null) {
			int width, height;

			if (bm.getWidth() > bm.getHeight()) {
				width = imageHeight;
				height = width * bm.getHeight() / bm.getWidth();
			} else {
				height = imageHeight;
				width = height * bm.getWidth() / bm.getHeight();
			}

			if (width != bm.getWidth() || height != bm.getHeight()) {
				bmScaled = Bitmap.createScaledBitmap(bm, width, height, true);
				if (!bm.isRecycled()) {
					bm.recycle();
					bm = null;
				}
			} else
				bmScaled = bm;
		}

		Bitmap result = null;
		if (bmScaled != null) {
			Bitmap mask = null;

			Drawable drawable = isLeft ? mContext.getResources().getDrawable(R.drawable.chat_from_img_bg)
					: mContext.getResources().getDrawable(R.drawable.chat_to_img_bg);
			mask = GlobalFunc.get_ninepatch(drawable, bmScaled.getWidth(), bmScaled.getHeight(),
					mContext.getResources());

			result = Bitmap.createBitmap(bmScaled.getWidth(), bmScaled.getHeight(), Config.ARGB_8888);
			Canvas mCanvas = new Canvas(result);
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			mCanvas.drawBitmap(bmScaled, 0, 0, null);
			paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
			mCanvas.drawBitmap(mask, 0, 0, paint);
			paint.setXfermode(null);
			if (mask != null && !mask.isRecycled()) {
				mask.recycle();
				mask = null;
			}

			if (!bmScaled.isRecycled()) {
				bmScaled.recycle();
				bmScaled = null;
			}
		}
		if (result != null)
			putBitmapInCache(filePath, result);

		return result;
	}

	public void recycleCache() {
		Iterator<String> iterator;
		iterator = mCacheController.iterator();
		while (iterator.hasNext()) {
			String uri = iterator.next();
			Bitmap bm = mCache.get(uri);
			if (bm != null && !bm.isRecycled()) {
				bm.recycle();
				bm = null;
				mCache.remove(uri);
			}
		}
	}
}
