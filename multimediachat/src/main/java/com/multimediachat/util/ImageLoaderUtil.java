package com.multimediachat.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.GlideApp;
import com.multimediachat.app.im.engine.ContactList;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.util.connection.PicaApiUtility;
import com.multimediachat.util.datamodel.ContactItem;

import java.io.File;
import java.io.IOException;

/**
 * Created by jack on 1/16/2018.
 */

public class ImageLoaderUtil {

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

    public static boolean compressImage(String srcPath, String dstPath)
    {
        return compressImage(srcPath, dstPath, GlobalConstrants.JPEG_QUALITY);
    }

    public static boolean compressImage(String srcPath, String dstPath, int nPercent)
    {
        File file = new File(srcPath);
        if (file == null || !file.exists())
            return false;

        Bitmap bmp = null;

        try {
            bmp = BitmapFactory.decodeFile(srcPath);
        } catch (Exception e) {
            if (bmp != null && !bmp.isRecycled()) {
                bmp.recycle();
                bmp = null;
            }
        }

        if (bmp == null)
            return false;

        Bitmap res = null;

        int rotate = getImageOrientation(srcPath);

        if ( rotate !=  0 ) {
            try {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotate);

                res = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            res = bmp;
        }

        if ( res == null )
            return false;

        return BitmapUtil.writeBmpToFile(res, dstPath, nPercent);
    }

    public static void loadMyAvatarImage(final Context context, final ImageView imageView) {
        File file = new File(GlobalConstrants.USER_PHOTO_PATH);

        if ( !file.exists() ) {
            imageView.setImageResource(R.drawable.profilephoto);
        } else
        {
            GlideApp.with(context).load(file).signature(new ObjectKey(String.valueOf(file.lastModified()))).placeholder(R.drawable.profilephoto).error(R.drawable.profilephoto).into(imageView);
        }
    }

    public static void loadAvatarImage(final Context context, String username, final ImageView imageView, final int sizeType) {
        if ( !username.contains("@") )
            username = username + "@" + GlobalConstrants.server_domain;

        String filePath = PicaApiUtility.getAvatarImageUrl(username, sizeType);
        String hash = DatabaseUtils.getHash(context.getContentResolver(), username);

        if ( hash == null || hash.isEmpty() ) {
            GlideApp
                    .with(context)
                    .load(filePath)
                    .placeholder(R.drawable.profilephoto)
                    .error(R.drawable.profilephoto)
                    .into(imageView);
        } else {
            GlideApp.with(context).load(filePath).signature(new ObjectKey(hash)).placeholder(R.drawable.profilephoto).error(R.drawable.profilephoto).into(imageView);
        }
    }

    public static void loadImageUri(Context context, Uri uri, ImageView imageView)
    {
        File file = new File(uri.getPath());
        GlideApp.with(context).load(file).signature(new ObjectKey(String.valueOf(file.lastModified()))).into(imageView);
    }

    public static void loadImageUrlAsBitmap(Context context, String fileUrl, ImageView imageView, final RequestListener listener){
        GlideApp.with(context).asBitmap().load(fileUrl).listener(listener).into(imageView);
    }

    public static void loadImageFile(Context context, String filePath, ImageView imageView)
    {
        File file = new File(filePath);
        GlideApp.with(context).load(file).signature(new ObjectKey(String.valueOf(file.lastModified()))).into(imageView);
    }

    public static void loadImageUrl(Context context, String fileUrl, ImageView imageView)
    {
        GlideApp.with(context).load(fileUrl).into(imageView);
    }
}
