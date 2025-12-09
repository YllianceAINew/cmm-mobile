/*
 * added by JHK(2019.11.07)
 * open selected photo from Gallery in order to setting profile image
 */
package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.multimediachat.R;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.util.ChatRoomMediaUtil;

import java.io.File;

@SuppressLint("DefaultLocale")
public class GalleryImageOpenActivity extends Activity implements View.OnClickListener{
    Context context;
    private String imagePath = "";
    private String userAddress = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.gallery_image_open);
        initUI();
    }
    private void initUI() {
        ImageView imageView = findViewById(R.id.gallery_image);
        Button btnOk = findViewById(R.id.btn_done);
        btnOk.setOnClickListener(this);
        Button btnCancel = findViewById(R.id.btn_discard);
        btnCancel.setOnClickListener(this);
        Intent i = getIntent();
        imagePath = i.getStringExtra("samplePath");
        userAddress = i.getStringExtra("userAddr");
        if (!imagePath.isEmpty()) {
            File image = new File(imagePath);
            if (image.exists())
                imageView.setImageBitmap(BitmapFactory.decodeFile(imagePath));
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_discard:
                break;
            case R.id.btn_done:
                String samplePath, avatarPath = null;
                avatarPath = context.getFilesDir().getAbsolutePath() + "/" + GlobalConstrants.AVATAR_DIR_NAME + "/";
                File avatarDir = new File(avatarPath);
                if (!avatarDir.exists())
                    avatarDir.mkdirs();
                samplePath = avatarPath + "thumbnail_" + System.currentTimeMillis() + ".jpg";
                ChatRoomMediaUtil.sampleImage(getResources(), imagePath, samplePath);
                Imps.updateContactsInDb(getContentResolver(), userAddress, samplePath);
                break;
            default:
                break;
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
