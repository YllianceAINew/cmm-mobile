package com.multimediachat.ui;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.multimediachat.R;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.views.QuickActionPopup;
import com.multimediachat.util.BitmapUtil;
import com.multimediachat.util.PrefUtil.mPref;

import java.io.File;

public class MyQRCodeActivity extends BaseActivity {
    private static final int ID_CHANGE_STYLE = 1;
    private static final int ID_SAVE_TO_PHONE = 2;
    private static final int ID_SCAN_QR_CODE = 3;

    QuickActionPopup quickActionPopup = null;

    ImageView mImvCode;
    Bitmap bmMyQRCode;
    View mDescriptionView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_qrcode_activity);
        setActionBarTitle(getString(R.string.str_my_qrcode));

        initUI();

        String address = getIntent().getStringExtra("address");
        if ( address == null )
            address = mPref.getString("username", "") + "@" + GlobalConstrants.server_domain;

        GlobalFunc.showQRCode(address, mImvCode);
    }

    private void initUI() {
        mDescriptionView = findViewById(R.id.description);
        if ( getIntent().getBooleanExtra("isFriend", false) ) {
            mDescriptionView.setVisibility(View.GONE);
            setActionBarTitle(getResources().getString(R.string.str_friend_qrcode));
        } else
            addImageButton(R.drawable.btn_view_more, R.id.btn_more, POS_RIGHT);

        mImvCode = (ImageView) findViewById(R.id.codeImg);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(Menu.NONE, 1, 0, getString(R.string.save_to_phone));

        return true;
    }

    private void SaveQRCode()
    {
        int res = 0;
        File destFile = new File(Environment.getExternalStorageDirectory() + "/DCIM");
        File[] children = destFile.listFiles();
        if (children == null) {
            destFile.mkdirs();
        }
        String destPath = Environment.getExternalStorageDirectory() + "/DCIM/"
                + String.valueOf(System.currentTimeMillis()) + ".jpg";

        BitmapUtil.writeBmpToFile(bmMyQRCode, destPath);
        destFile = new File(destPath);
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "image");
            values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());

            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DESCRIPTION, "image description");
            values.put(MediaStore.MediaColumns.DATA, destFile.getAbsolutePath());
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null)
                res = 1;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (res == 1) {
            GlobalFunc.showToast(MyQRCodeActivity.this, R.string.save_photo_success, false);
        } else {
            GlobalFunc.showToast(MyQRCodeActivity.this, R.string.save_photo_failed, false);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                SaveQRCode();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btn_more:
                openOptionsMenu();
                break;
            default:
                super.onClick(v);
                break;
        }
    }
}
