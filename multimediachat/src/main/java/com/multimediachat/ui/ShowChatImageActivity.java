package com.multimediachat.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.multimediachat.R;
import com.multimediachat.app.DebugConfig;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by jack on 1/2/2018.
 */

public class ShowChatImageActivity extends Activity {
    private ImagePagerAdapter imagePagerAdapter;
    ViewPager pager;
    ArrayList<String>   filePathList;
    private long chatId;
    public int selectedIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        initUI();
    }

    private void initData()
    {
        Intent intent = getIntent();
        int externalStoreDirLen = Environment.getExternalStorageDirectory().getAbsolutePath().length();
        filePathList = new ArrayList<String>();

        chatId = intent.getLongExtra("chatId", 0);
        String selectedFilePath = intent.getStringExtra("filePath");
        if (chatId == 0) {
            finish();
            return;
        }

        filePathList.add("file:///mnt/sdcard/" + selectedFilePath.substring(externalStoreDirLen));

//        Cursor cursor = null;
//        try {
//            Uri uri = Imps.Messages.getContentUriByThreadId(chatId);
//            cursor = getContentResolver().query(uri, new String[] { Imps.Messages.BODY, Imps.Messages.DATE },
//                    Imps.Messages.MIME_TYPE + " LIKE 'image/%'", null, "date");
//            while (cursor.moveToNext()) {
//                String filePath = cursor.getString(0);
//                if (filePath != null) {
//                    if (selectedFilePath != null && selectedFilePath.equals(filePath)) {
//                        selectedIndex = filePathList.size();
//                    }
//
//                    File file = new File(filePath);
//                    if (file != null && file.exists()) {
//                        try {
//                            filePath = "file:///mnt/sdcard/"
//                                    + file.getAbsolutePath().substring(externalStoreDirLen);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        if (Uri.parse(filePath).getScheme() != null) {
//                            String tmp = GlobalFunc.getRealPathFromURI(this, Uri.parse(filePath));
//                            try {
//                                filePath = "file:///mnt/sdcard/" + tmp.substring(externalStoreDirLen);
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                    filePathList.add(filePath);
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            if (cursor != null) {
//                cursor.close();
//                cursor = null;
//            }
//        }

        if (filePathList.size() == 0) {
            finish();
            return;
        }

			/*
			 * File file = new File(filePath); if ( file != null &&
			 * file.exists() ) { try{ filePath= "file:///mnt/sdcard/" +
			 * file.getAbsolutePath().substring(externalStoreDirLen);
			 * }catch(Exception e){ e.printStackTrace(); } } else{ if (
			 * Uri.parse(filePath).getScheme() != null ) { String tmp =
			 * GlobalFunc.getRealPathFromURI( this, Uri.parse(filePath) ); try{
			 * filePath = "file:///mnt/sdcard/" +
			 * tmp.substring(externalStoreDirLen); }catch(Exception e){
			 * e.printStackTrace(); } } }
			 */
    }

    private void initUI()
    {
        setContentView(R.layout.show_chat_image_activity);

        pager = (ViewPager) findViewById(R.id.pager);

        imagePagerAdapter = new ImagePagerAdapter();
        pager.setAdapter(imagePagerAdapter);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                selectedIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        BaseActivity.isActive = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        BaseActivity.isActive = true;

        if (pager != null && selectedIndex >= 0) {
            if (imagePagerAdapter.getCount() > selectedIndex) {
                if (pager.getCurrentItem() != selectedIndex)
                    pager.setCurrentItem(selectedIndex);
            }
        }
    }

    private class ImagePagerAdapter extends PagerAdapter {
        private LayoutInflater inflater;

        ImagePagerAdapter() {
            inflater = getLayoutInflater();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return filePathList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
            Glide.with(ShowChatImageActivity.this).load(filePathList.get(position)).into(imageView);
            view.addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view.equals(object);
        }

        @Override
        public void restoreState(Parcelable state, ClassLoader loader) {
        }

        @Override
        public Parcelable saveState() {
            return null;
        }
    }

}
