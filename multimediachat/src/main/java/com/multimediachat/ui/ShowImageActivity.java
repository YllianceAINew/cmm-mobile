package com.multimediachat.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import com.multimediachat.R;
import com.multimediachat.app.MediaController;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.util.ImageLoaderUtil;
import com.multimediachat.ui.views.CustomViewPager;
import com.multimediachat.ui.views.HorizontalListView;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShowImageActivity extends BaseActivity implements OnClickListener {
    String path;
    // ImageView imgView;
    Bitmap bitmap = null;
    int externalStoreDirLen = 0;
    ProgressBar progressBar;
    ViewPager pager;
    boolean bSelectPhotos;
    public static int selectedIndex = 0;
    private List<String> filePathList;
    private long chatId;

    public ArrayList<MediaController.PhotoEntry> m_selectedPhotos_origin;
    public boolean m_isPreview;
//	private View doneButton;
//	private TextView doneButtonTextView;
//	private TextView doneButtonBadgeTextView;

    private ImagePagerAdapter imagePagerAdapter;

    private CheckBox btnSelect;
    private TextView btnOK;
    private CheckBox btn_fullimage;

    private boolean isSetBackground;
    private int mType;

    private HorizontalListView image_list;
    private HorizontalAdapter mHorizontalAdapter;

    boolean isNoScroll;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showimage_activity);

        setActionBarTitle(getString(R.string.preview));
        addTextButton(getString(R.string.ok), R.id.btnOK, POS_RIGHT);

        progressBar = (ProgressBar) findViewById(R.id.progressLoading);
        btnOK = (TextView)findViewById(R.id.btnOK);

        btnSelect = (CheckBox) findViewById(R.id.btn_select);
        btn_fullimage = (CheckBox) findViewById(R.id.btn_fullimage);

        if ( getIntent().getBooleanExtra("fullimage", false) )
            btn_fullimage.setChecked(true);

        btn_fullimage.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if ( bSelectPhotos )
                {
                    PhotoPickerActivity.btn_fullimage.setChecked(btn_fullimage.isChecked());
                }
            }
        });

        btnSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MediaController.PhotoEntry photoEntry;
                if ( !m_isPreview )
                    photoEntry = PhotoPickerActivity.selectedAlbum.photos.get(selectedIndex);
                else
                    photoEntry = m_selectedPhotos_origin.get(selectedIndex);
                if (PhotoPickerActivity.selectedPhotos.containsKey(photoEntry.imageId)) {
                    if ( !btnSelect.isChecked() ) {
                        PhotoPickerActivity.selectedPhotos.remove(photoEntry.imageId);
                        updateSelectedPhoto();
                        updateSelectedCount();
                    }
                } else {
                    if ( btnSelect.isChecked() )
                    {
                        if (PhotoPickerActivity.selectedPhotos.size() >= PhotoPickerActivity.MAX_SELECT_COUNT) {
                            btnSelect.setChecked(false);
                            GlobalFunc.showErrorMessageToast(ShowImageActivity.this, 106, false);
                            return;
                        }

                        PhotoPickerActivity.selectedPhotos.put(photoEntry.imageId, photoEntry);
                        updateSelectedPhoto();
                        updateSelectedCount();
                    }
                }
            }
        });

        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }

        filePathList = new ArrayList<String>();

        externalStoreDirLen = Environment.getExternalStorageDirectory().getAbsolutePath().length();
        bSelectPhotos = intent.getBooleanExtra("bSelectPhotos", false);
        isSetBackground = intent.getBooleanExtra("isSetBackground", false);
        mType = intent.getIntExtra("type", 0);
        isNoScroll = intent.getBooleanExtra("noscroll", false);

        if ( mType == PhotoPickerActivity.TYPE_EDITPOST )
            btn_fullimage.setVisibility(View.GONE);

        if ( isSetBackground || isNoScroll )
            findViewById(R.id.show_image_bottom).setVisibility(View.GONE);

        if (!bSelectPhotos) {
            findViewById(R.id.show_image_bottom).setVisibility(View.GONE);

            chatId = intent.getLongExtra("chatId", 0);
            String selectedFilePath = intent.getStringExtra("filePath");
            if (chatId == 0) {
                finish();
                return;
            }

            Cursor cursor = null;
            try {
                Uri uri = Imps.Messages.getContentUriByThreadId(chatId);
                cursor = getContentResolver().query(uri, new String[] { Imps.Messages.BODY, Imps.Messages.DATE },
                        Imps.Messages.MIME_TYPE + " LIKE 'image/%'", null, "date");
                while (cursor.moveToNext()) {
                    JSONObject obj = new JSONObject(cursor.getString(0));
                    String filePath = obj.getString(GlobalConstrants.MESSAGE_PARAM_CONTENT);

                    if (filePath != null) {
                        if (selectedFilePath != null && selectedFilePath.equals(filePath)) {
                            selectedIndex = filePathList.size();
                        }

                        File file = new File(filePath);
                        if (file == null || !file.exists()) {
                            if (Uri.parse(filePath).getScheme() != null) {
                                filePath = GlobalFunc.getRealPathFromURI(this, Uri.parse(filePath));
                            }
                        }
                        filePathList.add(filePath);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                    cursor = null;
                }
            }

            if (filePathList.size() == 0) {
                finish();
                return;
            }

        } else {
            selectedIndex = intent.getIntExtra("selectedIndex", 0);
            m_isPreview = intent.getBooleanExtra("isPreview", false);

            if ( m_isPreview )
            {
                m_selectedPhotos_origin = new ArrayList<MediaController.PhotoEntry>();
                for (HashMap.Entry<Integer, MediaController.PhotoEntry> entry : PhotoPickerActivity.selectedPhotos.entrySet()) {
                    MediaController.PhotoEntry photoEntry = entry.getValue();
                    if (photoEntry.path != null) {
                        m_selectedPhotos_origin.add(0, photoEntry);
                    }
                }
            }

            updateSelectedCount();
        }

        pager = (ViewPager) findViewById(R.id.pager);

        if ( isNoScroll )
            ((CustomViewPager)pager).setPagingEnabled(false);

        imagePagerAdapter = new ImagePagerAdapter();
        pager.setAdapter(imagePagerAdapter);

        pager.setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageSelected(int arg0) {
                selectedIndex = arg0;
                if (bSelectPhotos) {
                    updateSelectedPhoto();
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });

        image_list = (HorizontalListView)findViewById(R.id.image_list);
        mHorizontalAdapter = new HorizontalAdapter(this);
        image_list.setAdapter(mHorizontalAdapter);

        if (bSelectPhotos && m_isPreview) {
            image_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    pager.setCurrentItem(i);
                }
            });
        }

        if (bSelectPhotos) {
            updateSelectedCount();
        } else {
            findViewById(R.id.btnOK).setVisibility(View.GONE);

        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (bSelectPhotos) {
            if (selectedIndex == 0) {
                updateSelectedPhoto();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (pager != null && selectedIndex >= 0) {
            if (imagePagerAdapter.getCount() > selectedIndex) {
                if (pager.getCurrentItem() != selectedIndex)
                    pager.setCurrentItem(selectedIndex);
            }
        }
    }

    private String convertFilePathToOriginal(String filePath) {
        return Environment.getExternalStorageDirectory() + filePath.substring(19);
    }

    public byte[] bitmapToByteArray(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    private void updateSelectedCount() {
        if ( isSetBackground )
            return;

        if (PhotoPickerActivity.selectedPhotos.isEmpty()) {
/*
			doneButtonTextView.setTextColor(0xff999999);
			doneButtonTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.selectphoto_small_grey, 0, 0, 0);
			doneButtonBadgeTextView.setVisibility(View.GONE);
			doneButton.setEnabled(false);
*/
            if ( mType == PhotoPickerActivity.TYPE_EDITPOST )
                btnOK.setText(getString(R.string.str_done));
            else
                btnOK.setText(getString(R.string.send));
            btnOK.setEnabled(true);
        } else {
/*
			doneButtonTextView.setTextColor(0xffffffff);
			doneButtonTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
			doneButtonBadgeTextView.setVisibility(View.VISIBLE);
			doneButtonBadgeTextView.setText("" + PhotoPickerActivity.selectedPhotos.size());
			doneButton.setEnabled(true);
*/
            if ( mType == PhotoPickerActivity.TYPE_EDITPOST )
                btnOK.setText(getString(R.string.str_done)+"("+PhotoPickerActivity.selectedPhotos.size()+"/"+PhotoPickerActivity.MAX_SELECT_COUNT+")");
            else
                btnOK.setText(getString(R.string.send)+"("+PhotoPickerActivity.selectedPhotos.size()+"/"+PhotoPickerActivity.MAX_SELECT_COUNT+")");
            btnOK.setEnabled(true);
        }
    }

    private ImageView selectedImageView = null;

    private void rotateImage() {
        if (selectedImageView != null) {

            String filePath = null;
            try {
                filePath = convertFilePathToOriginal(filePathList.get(selectedIndex));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (filePath != null) {

            }

            final RotateAnimation rotateAnim = new RotateAnimation(0.0f, 90, RotateAnimation.RELATIVE_TO_SELF, 0.5f,
                    RotateAnimation.RELATIVE_TO_SELF, 0.5f);

            rotateAnim.setDuration(0);
            rotateAnim.setFillAfter(true);
            selectedImageView.startAnimation(rotateAnim);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btn_back:
                setResult(RESULT_CANCELED);
                finish();
                break;
            case R.id.btnOK:
                if ( isSetBackground )
                {
                    MediaController.PhotoEntry photoEntry = PhotoPickerActivity.selectedAlbum.photos.get(selectedIndex);
                    if (PhotoPickerActivity.selectedPhotos.containsKey(photoEntry.imageId)) {
                    } else {
                        PhotoPickerActivity.selectedPhotos.put(photoEntry.imageId, photoEntry);
                    }
                    setResult(RESULT_OK);
                    finish();
                    return;
                }
                if ( bSelectPhotos ) {
/*
					MediaController.PhotoEntry photoEntry = PhotoPickerActivity.selectedAlbum.photos.get(selectedIndex);
					if (PhotoPickerActivity.selectedPhotos.containsKey(photoEntry.imageId)) {
						PhotoPickerActivity.selectedPhotos.cii_remove(photoEntry.imageId);
					} else {
						if (PhotoPickerActivity.selectedPhotos.size() > PhotoPickerActivity.MAX_SELECT_COUNT)
							return;
						PhotoPickerActivity.selectedPhotos.put(photoEntry.imageId, photoEntry);
					}
					updateSelectedPhoto();
					updateSelectedCount();
*/

                    if ( PhotoPickerActivity.selectedPhotos.isEmpty() )
                    {
                        MediaController.PhotoEntry photoEntry = PhotoPickerActivity.selectedAlbum.photos.get(selectedIndex);
                        PhotoPickerActivity.selectedPhotos.put(photoEntry.imageId, photoEntry);
                    }

                    setResult(RESULT_OK);
                    finish();
                }
                else
                {
                    setResult(RESULT_OK);
                    finish();
                }
                break;
            default:
                super.onClick(view);
                break;
        }
    }

    public static boolean isIntentAvailable(Context context, Intent intent) {
        final PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    protected void showThumbnail(String mimeType, String body) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            // set a general mime type not specific
            if (mimeType != null) {
                intent.setDataAndType(Uri.parse(body), mimeType);
            }

            if (isIntentAvailable(ShowImageActivity.this, intent)) {
                startActivity(intent);
            } else {
                GlobalFunc.showToast(ShowImageActivity.this, R.string.unknown_file_format, true);
            }
        } catch (Exception e) {
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
            if (bSelectPhotos) {
                if ( !m_isPreview ) {
                    if (PhotoPickerActivity.selectedAlbum != null) {
                        return PhotoPickerActivity.selectedAlbum.photos.size();
                    }
                    return 0;
                }

                return m_selectedPhotos_origin.size();
            } else
                return filePathList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View imageLayout = inflater.inflate(R.layout.item_pager_image, view, false);
            assert imageLayout != null;
            ImageView imageView = (ImageView) imageLayout.findViewById(R.id.image);
            final ProgressBar spinner = (ProgressBar) imageLayout.findViewById(R.id.loading);

            if (bSelectPhotos) {
                final MediaController.PhotoEntry photoEntry = (!m_isPreview ? PhotoPickerActivity.selectedAlbum.photos.get(position) : m_selectedPhotos_origin.get(position));

                if (photoEntry.path != null) {
                    if ( photoEntry.mediaType == MediaController.MediaType.VIDEO )
                    {
                        imageView.setVisibility(View.VISIBLE);
                        ImageView videoPlayIcon = (ImageView) imageLayout.findViewById(R.id.video_play_icon);
                        videoPlayIcon.setVisibility(View.VISIBLE);

                        spinner.setVisibility(View.VISIBLE);
                        ImageLoaderUtil.loadImageUrlAsBitmap(ShowImageActivity.this, photoEntry.path, imageView, new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                spinner.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                spinner.setVisibility(View.GONE);
                                return false;
                            }
                        });

                        imageView.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                File file = new File(photoEntry.path);
                                final Handler handler = new Handler();

                                MediaScannerConnection.scanFile(ShowImageActivity.this, new String[] { file.toString() }, null,
                                        new MediaScannerConnection.OnScanCompletedListener() {
                                            public void onScanCompleted(String path, final Uri uri) {
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        if (uri != null)
                                                            showThumbnail("video/*", uri.toString());
                                                    }
                                                });
                                            }
                                        });

                            }
                        });
                    }
                    else
                    {
                        spinner.setVisibility(View.VISIBLE);
                        ImageLoaderUtil.loadImageUrlAsBitmap(ShowImageActivity.this, photoEntry.path, imageView, new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                                spinner.setVisibility(View.GONE);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                                spinner.setVisibility(View.GONE);
                                return false;
                            }
                        });
                    }
                } else {
                    imageView.setImageResource(R.drawable.nophotos);
                }
            } else {
                selectedImageView = imageView;
                String filePath = filePathList.get(position);
                spinner.setVisibility(View.VISIBLE);
                ImageLoaderUtil.loadImageUrlAsBitmap(ShowImageActivity.this, filePath, imageView, new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object o, Target<Bitmap> target, boolean b) {
                        spinner.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap bitmap, Object o, Target<Bitmap> target, DataSource dataSource, boolean b) {
                        spinner.setVisibility(View.GONE);
                        return false;
                    }
                });
            }

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateSelectedPhoto() {
        MediaController.PhotoEntry photoEntry = ( m_isPreview ? m_selectedPhotos_origin.get(selectedIndex) : PhotoPickerActivity.selectedAlbum.photos.get(selectedIndex) );
        if (PhotoPickerActivity.selectedPhotos.containsKey(photoEntry.imageId)) {
            btnSelect.setChecked(true);
        } else {
            btnSelect.setChecked(false);
        }

        if ( mType != PhotoPickerActivity.TYPE_EDITPOST ) {
            if (photoEntry.mediaType == MediaController.MediaType.VIDEO)
                btn_fullimage.setVisibility(View.GONE);
            else
                btn_fullimage.setVisibility(View.VISIBLE);
        }

        if ( mType == PhotoPickerActivity.TYPE_EDITPOST )
        {
            if ( photoEntry.mediaType == MediaController.MediaType.VIDEO )
            {
                btnSelect.setVisibility(View.GONE);
            }
            else
            {
                btnSelect.setVisibility(View.VISIBLE);
            }
        }
        else
        {
            btnSelect.setVisibility(View.VISIBLE);
        }

        mHorizontalAdapter.notifyDataSetChanged();
    }

    public class HorizontalAdapter extends ArrayAdapter<MediaController.PhotoEntry>
    {
        public HorizontalAdapter(Context _context) {
            super(_context, 0);
        }

        @Override
        public int getCount() {
            if (bSelectPhotos && m_isPreview)
                return m_selectedPhotos_origin.size();

            return PhotoPickerActivity.selectedPhotos.size();
        }

        @Override
        public MediaController.PhotoEntry getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if ( convertView == null )
            {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.show_image_list_item, null);

                viewHolder = new ViewHolder();
                viewHolder.imageView = (ImageView)convertView.findViewById(R.id.imageView);
                viewHolder.videoIcon = (ImageView)convertView.findViewById(R.id.videoIcon);
                viewHolder.photo_check = (ImageView)convertView.findViewById(R.id.photo_check);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder)convertView.getTag();
            }

            MediaController.PhotoEntry photoEntry;

            if (bSelectPhotos && m_isPreview)
                photoEntry = m_selectedPhotos_origin.get(position);
            else
                photoEntry = (new ArrayList<MediaController.PhotoEntry>(PhotoPickerActivity.selectedPhotos.values())).get(position);

            ImageLoaderUtil.loadImageFile(ShowImageActivity.this, photoEntry.path, viewHolder.imageView);

            if ( photoEntry.mediaType == MediaController.MediaType.VIDEO )
                viewHolder.videoIcon.setVisibility(View.VISIBLE);
            else
                viewHolder.videoIcon.setVisibility(View.GONE);

            if (PhotoPickerActivity.selectedPhotos.containsKey(photoEntry.imageId)) {
                viewHolder.photo_check.setImageResource(R.drawable.circlecheckbox_on);
            } else {
                viewHolder.photo_check.setImageResource(R.drawable.circlecheckbox_off);
            }

            return convertView;
        }

        private class ViewHolder
        {
            ImageView imageView;
            ImageView videoIcon;
            ImageView photo_check;
        }
    }
}