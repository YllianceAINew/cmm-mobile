package com.multimediachat.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.multimediachat.R;
import com.multimediachat.app.MediaController;
import com.multimediachat.util.ImageLoaderUtil;

public class WallPaperPreviewActivity extends BaseActivity {
    public static final int TYPE_WALLPAPER = 1;
    public static final int TYPE_CHOOSE_PHOTO = 2;
    public static final int TYPE_CAMERA = 3;

    private int mImageIndex = 0;
    private int fromType = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI()
    {
        setContentView(-1);
        setActionBarTitle(getString(R.string.preview));

        fromType = getIntent().getIntExtra("type", 0);

        if ( fromType == TYPE_WALLPAPER ) {
            mImageIndex = getIntent().getIntExtra("imageIndex", 0);
            updateBackground(mImageIndex);
        } else if ( fromType == TYPE_CHOOSE_PHOTO ) {
            mImageIndex = getIntent().getIntExtra("imageIndex", 0);
            MediaController.PhotoEntry photoEntry = PhotoPickerActivity.selectedAlbum.photos.get(mImageIndex);
            updateBackground(photoEntry.path);
        } else if ( fromType == TYPE_CAMERA ) {
            String path = getIntent().getStringExtra("path");
            updateBackground(path);
        }
    }

    private void updateBackground(int index)
    {
        ImageView viewBackground = (ImageView)findViewById(R.id.backgroundImageView);

        viewBackground.setImageResource(getResources().getIdentifier(String.format("back%02d", index+1), "drawable", getPackageName()));
    }

    private void updateBackground(String path)
    {
        ImageView viewBackground = (ImageView)findViewById(R.id.backgroundImageView);

        ImageLoaderUtil.loadImageUrl(this, path, viewBackground);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_ok:
                if ( fromType == TYPE_WALLPAPER ) {
                    Intent data = new Intent();
                    data.putExtra("imageIndex", mImageIndex);
                    setResult(RESULT_OK, data);
                    finish();
                } else if ( fromType == TYPE_CHOOSE_PHOTO )
                {
                    MediaController.PhotoEntry photoEntry = PhotoPickerActivity.selectedAlbum.photos.get(mImageIndex);
                    if (PhotoPickerActivity.selectedPhotos.containsKey(photoEntry.imageId)) {
                    } else {
                        PhotoPickerActivity.selectedPhotos.put(photoEntry.imageId, photoEntry);
                    }
                    setResult(RESULT_OK);
                    finish();
                } else if ( fromType == TYPE_CAMERA )
                {
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            default:
                super.onClick(v);
                break;
        }
    }
}
