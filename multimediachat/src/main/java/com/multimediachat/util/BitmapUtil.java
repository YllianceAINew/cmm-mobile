package com.multimediachat.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.codec.digest.DigestUtils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import com.multimediachat.app.DebugConfig;
import com.multimediachat.global.GlobalConstrants;

public class BitmapUtil {
	
	
	public static String hashBitmap(Bitmap bmp){
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] byteArray = stream.toByteArray();
		String hash = DigestUtils.md5Hex(byteArray);
		return hash;
	}
	
	public static String hashBitmap(byte[] bytes){
		if ( bytes == null )
			return null;
		
		String hash = DigestUtils.md5Hex(bytes);
		return hash;
	}
	
	public static Bitmap resizeBitmap(Bitmap bmp, int width, int height) {
		Bitmap result = null;
		if ( bmp != null ) {
			if ( bmp.getWidth() != width || bmp.getHeight() != height ) {
				result = Bitmap.createScaledBitmap(bmp, width, height, true);
			}
			else{
				result = bmp;
			}
		}
		else{
			result = null;
		}
		
		return result;
	}
	
	public static Bitmap getBitmapFromFile(String filePath) {
		Bitmap res = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		
		try{
			res = BitmapFactory.decodeFile(filePath, options);
		}catch(Exception e){
			e.printStackTrace();
		}catch(OutOfMemoryError ome) {
			ome.printStackTrace();
		}
		return res;
	}
	
	public static Bitmap makePhotoCoverBmp(Bitmap bitmap) {
		Bitmap res = null;
		if ( bitmap == null )
			return null;
		
		if ( bitmap.getWidth() > GlobalConstrants.PHOTO_COVER_SIZE || bitmap.getHeight() > GlobalConstrants.PHOTO_COVER_SIZE ) {
			int width, height;
			if ( bitmap.getWidth() >= bitmap.getHeight() ) {
				width = GlobalConstrants.PHOTO_COVER_SIZE;
				height = width * bitmap.getHeight() / bitmap.getWidth();
			}
			else{
				height = GlobalConstrants.PHOTO_COVER_SIZE;
				width = height * bitmap.getWidth() / bitmap.getHeight();
			}
			try{
				res = Bitmap.createScaledBitmap(bitmap, width, height, true);
			}catch(Exception e){
				e.printStackTrace();
			}
			return res;
		}
		else{
			return bitmap;
		}
	}
	
	public static byte[] convertBmpToBytes(Bitmap bm) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		byte[] bytes = stream.toByteArray();

		try {
			stream.close();
		}catch(Exception e) {
			e.printStackTrace();
		}

		return bytes;
	}

	public static boolean writeBmpToFile(Bitmap bm, String filePath) {
		return writeBmpToFile(bm, filePath, 100);
	}

	public static boolean writeBmpToFile(Bitmap bm, String filePath, int nPercent) {
		File file = new File(filePath);
		try{
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			bm.compress(CompressFormat.JPEG, nPercent, bos);
			byte[] bitmapdata = bos.toByteArray();
			//write the bytes in file
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bitmapdata);
			fos.flush();
			fos.close();
			fos = null;
			return true;
		}catch(Exception e){
			DebugConfig.error("BitmapUtil", "writeBmpToFile",  e);
		}
		return false;
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

	public static boolean makePostBitmap(String srcPath, String dstPath, Context context) {
		Bitmap bitmap = null;
		Bitmap res = null;

		bitmap = getBitmapFromFile(srcPath);

		if ( bitmap == null )
			return false;

		int screenWidth = Utilities.getScreenWidth(context);
		int screenHeight = Utilities.getScreenHeight(context);

		int rotate = getImageOrientation(srcPath);

		if ( rotate !=  0 ) {
			try {
				Matrix matrix = new Matrix();
				matrix.postRotate(rotate);

				res = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			res = bitmap;
		}

		if ( res == null )
			return false;

		if ( res.getWidth() > screenWidth || res.getHeight() > screenHeight ) {
			int width, height;
			if ( (float)res.getWidth()/screenWidth >= (float)res.getHeight()/screenHeight ) {
				width = screenWidth;
				height = width * res.getHeight() / res.getWidth();
			}
			else{
				height = screenHeight;
				width = height * res.getWidth() / res.getHeight();
			}

			try {
				res = Bitmap.createScaledBitmap(res, width, height, true);
			} catch (Exception e) {
				e.printStackTrace();
			}

			if ( writeBmpToFile(res, dstPath) == true );
				return true;
		}

		return false;
	}

	public static Bitmap mergeToPin(Bitmap back, Bitmap front) {
		Bitmap result = Bitmap.createBitmap(back.getWidth(), back.getHeight(), back.getConfig());
		Canvas canvas = new Canvas(result);
		int widthBack = back.getWidth();
		int widthFront = front.getWidth();
		int heightBack = back.getHeight();
		int heightFront = front.getHeight();
		float moveX = (widthBack - widthFront) / 2;
		float moveY = (heightBack - heightFront) / 2;
		canvas.drawBitmap(back, 0f, 0f, null);
		canvas.drawBitmap(front, moveX, moveY, null);
		return result;
	}
}
