package com.multimediachat.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.multimediachat.R;
import com.multimediachat.app.ImApp;
import com.multimediachat.global.GlobalFunc;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.widget.ImageView;

public class ChatRoomMediaUtil {
	public static int[] sampleImage(Resources r, String filePath, String dstPath) {
		File file = new File(filePath);
		if (file == null || !file.exists())
			return null;

		Bitmap bmp = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Config.RGB_565;
		options.inDither = true;
		try {
			bmp = BitmapFactory.decodeFile(filePath, options);
		} catch (OutOfMemoryError ome) {
			if (bmp != null && !bmp.isRecycled()) {
				bmp.recycle();
				bmp = null;
			}
		} catch (Exception e) {
			if (bmp != null && !bmp.isRecycled()) {
				bmp.recycle();
				bmp = null;
			}
		}

		if (bmp == null)
			return null;

		int rotate = getImageOrientation(filePath);
		Bitmap rotatedBitmap = null;
		if (rotate != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);
			try {
				rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError ome) {
				ome.printStackTrace();
			}
			if (!bmp.isRecycled()) {
				bmp.recycle();
				bmp = null;
			}
		} else {
			rotatedBitmap = bmp;
		}

		if (rotatedBitmap == null)
			return null;

		int imageHeight = r.getDimensionPixelSize(R.dimen.chat_image_height);
		int width = 0, height = 0;

		int vWidth = 0, vHeight = 0;
		if (rotatedBitmap.getWidth() > rotatedBitmap.getHeight()) {
			width = imageHeight;
			height = width * rotatedBitmap.getHeight() / rotatedBitmap.getWidth();
			vWidth = r.getDimensionPixelSize(R.dimen.chat_image_height);
			vHeight = vWidth * rotatedBitmap.getHeight() / rotatedBitmap.getWidth();
		} else {
			height = imageHeight;
			width = height * rotatedBitmap.getWidth() / rotatedBitmap.getHeight();

			vHeight = r.getDimensionPixelSize(R.dimen.chat_image_height);
			vWidth = vHeight * rotatedBitmap.getWidth() / rotatedBitmap.getHeight();
		}

		Bitmap scaledBmp = null;
		if (width != rotatedBitmap.getWidth() || height != rotatedBitmap.getHeight()) {
			scaledBmp = Bitmap.createScaledBitmap(rotatedBitmap, width, height, true);
			if (!rotatedBitmap.isRecycled()) {
				rotatedBitmap.recycle();
				rotatedBitmap = null;
			}
		} else {
			scaledBmp = rotatedBitmap;
		}

		file = new File(dstPath);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		scaledBmp.compress(CompressFormat.JPEG, 96 /* ignored for PNG */, bos);
		byte[] bitmapdata = bos.toByteArray();

		if (scaledBmp != null && !scaledBmp.isRecycled()) {
			scaledBmp.recycle();
			scaledBmp = null;
		}

		// write the bytes in file
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		boolean ret = true;
		try {
			if (fos != null) {
				fos.write(bitmapdata);
			}
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}

		try {
			if (fos != null) {
				fos.flush();
				ret = ret & true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}

		try {
			if (fos != null)
				fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (ret == false)
			return null;

		int[] result = new int[2];
		result[0] = vWidth;
		result[1] = vHeight;

		return result;
	}

	public static int[] resizeImage(Resources r, String filePath, String dstPath, int width, int height) {
		File file = new File(filePath);
		if (file == null || !file.exists())
			return null;

		Bitmap bmp = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Config.RGB_565;
		options.inDither = true;
		try {
			bmp = BitmapFactory.decodeFile(filePath, options);
		} catch (OutOfMemoryError ome) {
			if (bmp != null && !bmp.isRecycled()) {
				bmp.recycle();
				bmp = null;
			}
		} catch (Exception e) {
			if (bmp != null && !bmp.isRecycled()) {
				bmp.recycle();
				bmp = null;
			}
		}

		if (bmp == null)
			return null;

		int rotate = getImageOrientation(filePath);
		Bitmap rotatedBitmap = null;
		if (rotate != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);
			try {
				rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError ome) {
				ome.printStackTrace();
			}
			if (!bmp.isRecycled()) {
				bmp.recycle();
				bmp = null;
			}
		} else {
			rotatedBitmap = bmp;
		}

		if (rotatedBitmap == null)
			return null;

		int imageHeight = r.getDimensionPixelSize(R.dimen.chat_image_height);

		int vWidth = 0, vHeight = 0;
		if (rotatedBitmap.getWidth() > rotatedBitmap.getHeight()) {
			vWidth = r.getDimensionPixelSize(R.dimen.chat_image_height);
			vHeight = vWidth * rotatedBitmap.getHeight() / rotatedBitmap.getWidth();
		} else {
			vHeight = r.getDimensionPixelSize(R.dimen.chat_image_height);
			vWidth = vHeight * rotatedBitmap.getWidth() / rotatedBitmap.getHeight();
		}

		Bitmap scaledBmp = null;
		if (width != rotatedBitmap.getWidth() || height != rotatedBitmap.getHeight()) {
			scaledBmp = Bitmap.createScaledBitmap(rotatedBitmap, width, height, true);
			if (!rotatedBitmap.isRecycled()) {
				rotatedBitmap.recycle();
				rotatedBitmap = null;
			}
		} else {
			scaledBmp = rotatedBitmap;
		}

		file = new File(dstPath);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		scaledBmp.compress(CompressFormat.JPEG, 96 /* ignored for PNG */, bos);
		byte[] bitmapdata = bos.toByteArray();

		if (scaledBmp != null && !scaledBmp.isRecycled()) {
			scaledBmp.recycle();
			scaledBmp = null;
		}

		// write the bytes in file
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		boolean ret = true;
		try {
			if (fos != null) {
				fos.write(bitmapdata);
			}
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}

		try {
			if (fos != null) {
				fos.flush();
				ret = ret & true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}

		try {
			if (fos != null)
				fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (ret == false)
			return null;

		int[] result = new int[2];
		result[0] = vWidth;
		result[1] = vHeight;

		return result;
	}

	public static int[] sampleVideo(Resources r, String filePath, String dstPath) {
		File file = new File(filePath);
		if (file == null || !file.exists())
			return null;

		Bitmap bmp = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Config.RGB_565;
		options.inDither = true;
		try {
			bmp = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
//			bmp = BitmapFactory.decodeFile(filePath, options);
		} catch (OutOfMemoryError ome) {
			if (bmp != null && !bmp.isRecycled()) {
				bmp.recycle();
				bmp = null;
			}
		} catch (Exception e) {
			if (bmp != null && !bmp.isRecycled()) {
				bmp.recycle();
				bmp = null;
			}
		}

		if (bmp == null)
			return null;

		int rotate = getImageOrientation(filePath);
		Bitmap rotatedBitmap = null;
		if (rotate != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);
			try {
				rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
			} catch (Exception e) {
				e.printStackTrace();
			} catch (OutOfMemoryError ome) {
				ome.printStackTrace();
			}
			if (!bmp.isRecycled()) {
				bmp.recycle();
				bmp = null;
			}
		} else {
			rotatedBitmap = bmp;
		}

		if (rotatedBitmap == null)
			return null;

		int imageHeight = r.getDimensionPixelSize(R.dimen.chat_image_height);
		int width = 0, height = 0;

		int vWidth = 0, vHeight = 0;
		if (rotatedBitmap.getWidth() > rotatedBitmap.getHeight()) {
			width = imageHeight;
			height = width * rotatedBitmap.getHeight() / rotatedBitmap.getWidth();
			vWidth = r.getDimensionPixelSize(R.dimen.chat_image_height);
			vHeight = vWidth * rotatedBitmap.getHeight() / rotatedBitmap.getWidth();
		} else {
			height = imageHeight;
			width = height * rotatedBitmap.getWidth() / rotatedBitmap.getHeight();

			vHeight = r.getDimensionPixelSize(R.dimen.chat_image_height);
			vWidth = vHeight * rotatedBitmap.getWidth() / rotatedBitmap.getHeight();
		}

		Bitmap scaledBmp = null;
		if (width != rotatedBitmap.getWidth() || height != rotatedBitmap.getHeight()) {
			scaledBmp = Bitmap.createScaledBitmap(rotatedBitmap, width, height, true);
			if (!rotatedBitmap.isRecycled()) {
				rotatedBitmap.recycle();
				rotatedBitmap = null;
			}
		} else {
			scaledBmp = rotatedBitmap;
		}

		file = new File(dstPath);

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		scaledBmp.compress(CompressFormat.JPEG, 96 /* ignored for PNG */, bos);
		byte[] bitmapdata = bos.toByteArray();

		if (scaledBmp != null && !scaledBmp.isRecycled()) {
			scaledBmp.recycle();
			scaledBmp = null;
		}

		// write the bytes in file
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(file);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		boolean ret = true;
		try {
			if (fos != null) {
				fos.write(bitmapdata);
			}
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}

		try {
			if (fos != null) {
				fos.flush();
				ret = ret & true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}

		try {
			if (fos != null)
				fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (ret == false)
			return null;

		int[] result = new int[2];
		result[0] = vWidth;
		result[1] = vHeight;

		return result;
	}

	public static boolean compressImage(String filePath, String destPath, int nPercent) {
		File file = new File(filePath);
		if (file == null || !file.exists())
			return false;

		if (nPercent < 1 || nPercent > 100)
			return false;

		Bitmap bmp = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = false;
		options.inPreferredConfig = Config.RGB_565;
		options.inDither = true;
		try {
			bmp = BitmapFactory.decodeFile(filePath, options);
		} catch (OutOfMemoryError ome) {
			if (bmp != null && !bmp.isRecycled()) {
				bmp.recycle();
				bmp = null;
			}
		} catch (Exception e) {
			if (bmp != null && !bmp.isRecycled()) {
				bmp.recycle();
				bmp = null;
			}
		}

		if (bmp == null)
			return false;

		int rotate = getImageOrientation(filePath);
		Bitmap rotatedBitmap = null;
		if (rotate != 0) {
			Matrix matrix = new Matrix();
			matrix.postRotate(rotate);
			try {
				rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, false);
			} catch (OutOfMemoryError ome) {
				ome.printStackTrace();
			} finally {
				if (!bmp.isRecycled()) {
					bmp.recycle();
					bmp = null;
				}
			}
		} else {
			rotatedBitmap = bmp;
		}

		if (rotatedBitmap == null)
			return false;

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		rotatedBitmap.compress(CompressFormat.JPEG, nPercent, bos);
		byte[] bitmapdata = bos.toByteArray();
		if (rotatedBitmap != null && !rotatedBitmap.isRecycled()) {
			rotatedBitmap.recycle();
			rotatedBitmap = null;
		}

		File destFile = new File(destPath);
		// write the bytes in file
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(destFile);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		boolean ret = true;
		try {
			if (fos != null) {
				fos.write(bitmapdata);
			}
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}

		try {
			if (fos != null) {
				fos.flush();
				ret = ret & true;
			}
		} catch (IOException e) {
			e.printStackTrace();
			ret = false;
		}

		try {
			if (fos != null)
				fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret != false;

	}

	public static void drawBitmapToThumbnailAsync(final ImApp mApp, final Resources r, final ImageView imageView,
												  Bitmap bm, final boolean isLeft) {
		new AsyncTask<Bitmap, Void, Bitmap>() {
			@Override
			protected void onPreExecute() {
			}

			@Override
			protected void onPostExecute(Bitmap result) {
				super.onPreExecute();

				if (result != null)
					imageView.setImageBitmap(result);
			}

			@Override
			protected Bitmap doInBackground(Bitmap... params) {
				Bitmap bm = params[0];
				if (bm == null)
					return null;

				Bitmap mask = null;

				Drawable drawable = isLeft ? r.getDrawable(R.drawable.chat_from_img_bg)
						: r.getDrawable(R.drawable.chat_to_img_bg);
				mask = GlobalFunc.get_ninepatch(drawable, bm.getWidth(), bm.getHeight(), r);

				Bitmap result = Bitmap.createBitmap(bm.getWidth(), bm.getHeight(), Config.ARGB_8888);
				Canvas mCanvas = new Canvas(result);
				Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
				mCanvas.drawBitmap(bm, 0, 0, null);
				paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.MULTIPLY));
				//mCanvas.drawBitmap(mask, 0, 0, paint);
				paint.setXfermode(null);
				if (mask != null && !mask.isRecycled()) {
					mask.recycle();
					mask = null;
				}
				return result;
			}
		}.execute(bm);
	}

	public static int getImageOrientation(String imagePath) {
		int rotate = 0;
		try {

			File imageFile = new File(imagePath);
			ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
			int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rotate;
	}
}
