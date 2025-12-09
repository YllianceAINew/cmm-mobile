/*
 * This is the source code of Telegram for Android v. 1.4.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.GlideApp;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.MediaController;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.global.GlobalVariable;
import com.multimediachat.global.ErrorManager;
import com.multimediachat.util.EncDecDes;
import com.multimediachat.util.ImageLoaderUtil;
import com.multimediachat.util.Utilities;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PhotoPickerActivity extends BaseActivity implements NotificationCenter.NotificationCenterDelegate, OnClickListener {
    public final static int TYPE_NONE = 0;
    public final static int TYPE_CHATROOM = 1;
    public final static int TYPE_EDITPOST = 2;
    public final static int TYPE_PHOTOCROP = 3;
    public final static int TYPE_SETBG = 4;
    public final static int TYPE_QRCODE = 5;

    public interface PhotoPickerActivityDelegate {
        void didSelectPhotos(ArrayList<String> photos);

        void startPhotoSelectActivity();
    }

    private final int REQUEST_SHOW_IMAGE = 1016;
    public static final int MAX_SELECT_COUNT = 9;

    private ArrayList<MediaController.AlbumEntry> albumsSorted = null;
    public static HashMap<Integer, MediaController.PhotoEntry> selectedPhotos = new HashMap<Integer, MediaController.PhotoEntry>();
    private Integer cameraAlbumId = null;
    private boolean loading = false;
    public static MediaController.AlbumEntry selectedAlbum = null;

    private GridView listView;
    private ListAdapter listAdapter;
    private View progressView;
    private TextView emptyView;
    private View doneButton;
    private TextView doneButtonTextView;
    private TextView doneButtonBadgeTextView;
    private Spinner spinnerFolders;
    private int itemWidth = 100;
    private boolean photoClicked = false;

    TextView mBtnSend;
    int classGuid;
    boolean isSetBackground = false;
    boolean isSelectPhotoOnly = false;
    boolean isChooseOnly = false;
    String mChatId = null;	//only used when this activity start from chat room
    int mType = 0;
    public static boolean m_isVideoSelected = false;

    public static CheckBox btn_fullimage;

    private MainProgress mProgressDlg;

    @SuppressLint("WrongViewCast")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_picker_layout);
        setActionBarTitle(getString(R.string.choose_photo));
        addTextButton(getString(R.string.send), R.id.btnSend, POS_RIGHT);
        classGuid = ImApp.getInstance().generateClassGuid();

        mProgressDlg = new MainProgress(this);
        mProgressDlg.setMessage("");

        itemWidth = (Utilities.getScreenWidth(this) - AndroidUtility.dp(4) * 4) / 3;

        loading = true;
        selectedPhotos.clear();
        selectedAlbum = null;
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.albumsDidLoaded);
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.closeChats);

        isSetBackground = getIntent().getBooleanExtra("setbackground", false);
        isSelectPhotoOnly = getIntent().getBooleanExtra("selectphotoonly", false);
        isChooseOnly = getIntent().getBooleanExtra("chooseonly", false);
        mChatId = getIntent().getStringExtra("chatid");
        mType = getIntent().getIntExtra("type", 0);

        spinnerFolders = (Spinner)findViewById(R.id.spinner_folders);
        spinnerFolders.setDropDownWidth(getWindowManager().getDefaultDisplay().getWidth());
        mBtnSend = (TextView) findViewById(R.id.btnSend);
        mBtnSend.setEnabled(false);

        if ( isSetBackground ) {
            mBtnSend.setVisibility(View.GONE);
            setActionBarTitle(getString(R.string.images));
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        emptyView = (TextView) findViewById(R.id.searchEmptyView);
//		emptyView.setText(getString(R.string.NoPhotos));

        listView = (GridView) findViewById(R.id.media_grid);
        listView.setColumnWidth(itemWidth);
        progressView = findViewById(R.id.progressLayout);

        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        btn_fullimage = (CheckBox) findViewById(R.id.btn_fullimage);

        if ( mType == TYPE_EDITPOST )
            btn_fullimage.setVisibility(View.GONE);

        doneButton = findViewById(R.id.done_button);
        if ( isSetBackground )
        {
            btn_fullimage.setVisibility(View.GONE);
            doneButton.setVisibility(View.GONE);
        }

        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//				sendSelectedPhotos();
                photoClicked = true;

                Intent intent = new Intent(PhotoPickerActivity.this, ShowImageActivity.class);
                intent.putExtra("selectedIndex", 0);
                intent.putExtra("bSelectPhotos", true);
                intent.putExtra("isPreview", true);
                intent.putExtra("type", mType);
                intent.putExtra("isSetBackground", isSetBackground);
                intent.putExtra("fullimage", btn_fullimage.isChecked());
                startActivityForResult(intent, REQUEST_SHOW_IMAGE);
            }
        });

        cancelButton.setText(getString(R.string.cancel));
        doneButtonTextView = (TextView) doneButton.findViewById(R.id.done_button_text);
        doneButtonBadgeTextView = (TextView) doneButton.findViewById(R.id.done_button_badge);

        listAdapter = new ListAdapter(this);
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (selectedAlbum == null) {
                    if (i < 0 || i >= albumsSorted.size()) {
                        return;
                    }
                    selectedAlbum = albumsSorted.get(i);
                } else {
                    if (i < 0 || i >= selectedAlbum.photos.size()) {
                        return;
                    }

                    if (photoClicked)
                        return;

                    MediaController.PhotoEntry photoEntry = selectedAlbum.photos.get(i);

                    if ( mType == TYPE_SETBG )
                    {
                        photoClicked = true;

                        Intent intent = new Intent(PhotoPickerActivity.this, WallPaperPreviewActivity.class);
                        intent.putExtra("imageIndex", i);
                        intent.putExtra("type", WallPaperPreviewActivity.TYPE_CHOOSE_PHOTO);
                        startActivityForResult(intent, REQUEST_SHOW_IMAGE);
                    } else {
                        if ( isSetBackground || (mType == TYPE_EDITPOST && photoEntry.mediaType == MediaController.MediaType.VIDEO) ) {
                            if ( selectedPhotos.size() > 0 && photoEntry.mediaType == MediaController.MediaType.VIDEO )	//video preview not available while selecting photos
                                return;

                            photoClicked = true;

                            Intent intent = new Intent(PhotoPickerActivity.this, ShowImageActivity.class);
                            intent.putExtra("selectedIndex", i);
                            intent.putExtra("bSelectPhotos", true);
                            intent.putExtra("isSetBackground", isSetBackground);
                            intent.putExtra("type", mType);
                            intent.putExtra("fullimage", btn_fullimage.isChecked());

                            if ( mType == TYPE_EDITPOST && photoEntry.mediaType == MediaController.MediaType.VIDEO )
                                intent.putExtra("noscroll", true);	//show video preview only

                            startActivityForResult(intent, REQUEST_SHOW_IMAGE);
                        } else {
                            onCheck(view, i);
                        }
                    }
                }
            }
        });
        if (loading && (albumsSorted == null || albumsSorted != null && albumsSorted.isEmpty())) {
            progressView.setVisibility(View.VISIBLE);
            listView.setEmptyView(null);
        } else {
            progressView.setVisibility(View.GONE);
            listView.setEmptyView(emptyView);
        }
        updateSelectedCount();
        MediaController.loadGalleryPhotosAlbums(classGuid, isSetBackground || isSelectPhotoOnly);
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.albumsDidLoaded);
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.closeChats);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        photoClicked = false;
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
        updateSelectedCount();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SHOW_IMAGE) {
            if (resultCode == RESULT_OK) {
                sendSelectedPhotos();
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.albumsDidLoaded) {
            int guid = (Integer) args[0];
            if (classGuid == guid) {
                albumsSorted = (ArrayList<MediaController.AlbumEntry>) args[1];
                selectedAlbum = albumsSorted.get(0);
                if (args[2] != null) {
                    cameraAlbumId = (Integer) args[2];
                }
                if (progressView != null) {
                    progressView.setVisibility(View.GONE);
                }
                if (listView != null && listView.getEmptyView() == null) {
                    listView.setEmptyView(emptyView);
                }
                spinnerFolders.setAdapter(new FoldersArrayAdapter(this));
                if (listAdapter != null) {
                    listAdapter.notifyDataSetChanged();
                }
                spinnerFolders.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (position < 0 || position >= albumsSorted.size()) {
                            return;
                        }
                        selectedAlbum = albumsSorted.get(position);
                        listAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                loading = false;
            }
        } else if (id == NotificationCenter.closeChats) {
            // removeSelfFromStack();
        }
    }

    public void setDelegate(PhotoPickerActivityDelegate delegate) {
    }

    private void postNotification(ArrayList<String> photos, boolean isPhotos)
    {
        int notification = 0;

        switch (mType)
        {
            case TYPE_QRCODE:
                notification = NotificationCenter.qrcode_photos_compressed;
                break;

            case TYPE_CHATROOM:
                if ( isPhotos )
                    notification = NotificationCenter.chat_photos_compressed;
                else
                    notification = NotificationCenter.chat_video_compressed;
                break;

            case TYPE_EDITPOST:
                if ( isPhotos )
                    notification = NotificationCenter.editpost_photos_compressed;
                else
                    notification = NotificationCenter.editpost_videos_compressed;
                break;

            case TYPE_PHOTOCROP:
                notification = NotificationCenter.photocrop_photos_compressed;
                break;

            case TYPE_SETBG:
                notification = NotificationCenter.setbg_photos_compressed;
                break;
        }

        if ( notification != 0 )
            NotificationCenter.getInstance().postNotificationName(notification, photos);
    }

    private void sendSelectedPhotos() {
        for (HashMap.Entry<Integer, MediaController.PhotoEntry> entry : selectedPhotos.entrySet()) {
            MediaController.PhotoEntry photoEntry = entry.getValue();
            try {
                File file = new File(photoEntry.path);

                if (!file.exists()) {
                    ErrorManager.showErrorForFileNotFound();
                    return;
                }

                if (file.length() > GlobalConstrants.FILE_SIZE_LIMIT_BEFORE_COMPRESS_IN_BYTES) {
                    ErrorManager.showErrorForFileSizeBeforeCompress();
                    return;
                }
            } catch (Exception e) {
                ErrorManager.showErrorForProcessFile();
                return;
            }
        }

        mProgressDlg.show();

        AsyncTask task = new AsyncTask<Void, Void, Boolean>(){

            @Override
            protected Boolean doInBackground(Void... voids) {
                if (selectedPhotos.isEmpty()) {
                    return false;
                }

                if ( isSetBackground )
                {
                    final ArrayList<String> photos = new ArrayList<String>();
                    for (HashMap.Entry<Integer, MediaController.PhotoEntry> entry : selectedPhotos.entrySet()) {
                        MediaController.PhotoEntry photoEntry = entry.getValue();
                        if (photoEntry.path != null) {
                            if ( !isChooseOnly ) {
                                GlobalFunc.makeChatDir();
                                String chatPath = GlobalConstrants.LOCAL_PATH + GlobalConstrants.CHAT_DIR_NAME + "/" + GlobalVariable.account_id + "/";

                                if ( mType == TYPE_PHOTOCROP )
                                    chatPath = GlobalConstrants.LOCAL_PATH + GlobalConstrants.OTHER_DIR_NAME + "/" + GlobalConstrants.AVATAR_DIR_NAME + "/";
                                else {
                                    if (mChatId != null) {
                                        chatPath += mChatId + "/";
                                        File fileChat = new File(chatPath);
                                        if (!fileChat.exists())
                                            fileChat.mkdirs();
                                    }
                                }

                                String samplePath = chatPath + EncDecDes.getInstance().generateFileName("imageresized_" + System.currentTimeMillis());

//								if (BitmapUtil.makePostBitmap(photoEntry.path, samplePath, PhotoPickerActivity.this) == true) {
//								if (ImageLoaderUtil.compressImage(photoEntry.path, samplePath, 80)) {
                                try {
                                    FileUtils.copyFile(new File(photoEntry.path), new File(samplePath));
//									DebugConfig.error("*****", String.format("Compress Image1:%d->%d", new File(photoEntry.path).length(), new File(samplePath).length()));
                                    photoEntry.path = samplePath;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            photos.add(0, photoEntry.path);
                        }
                    }
                    // delegate.didSelectPhotos(photos);
                    if ( photos.size() > 0 )  {
                        postNotification(photos, true);
                    }
                }
                else
                {
                    if ( mType == TYPE_EDITPOST )
                    {
                        boolean isVideoSelected = false;
                        final ArrayList<String> photos = new ArrayList<String>();
                        for (HashMap.Entry<Integer, MediaController.PhotoEntry> entry : selectedPhotos.entrySet()) {
                            MediaController.PhotoEntry photoEntry = entry.getValue();
                            if (photoEntry.path != null) {
/*
						if (!btn_fullimage.isChecked() && photoEntry.mediaType == MediaController.MediaType.PHOTO) {
							String samplePath = GlobalConstrants.IMAGE_DIR_PATH + EncDecDes.getInstance().generateFileName("imageresized_" + System.currentTimeMillis());
							int width = getWindowManager().getDefaultDisplay().getWidth();
							int height = getWindowManager().getDefaultDisplay().getHeight();
							ChatRoomMediaUtil.resizeImage(getResources(), photoEntry.path, samplePath, width, height);
							photoEntry.path = samplePath;
						}
*/
                                String postPath = GlobalConstrants.LOCAL_PATH+GlobalConstrants.OTHER_DIR_NAME+"/"+GlobalConstrants.POSTS_DIR_NAME+"/";

                                if ( photoEntry.mediaType == MediaController.MediaType.PHOTO ) {
                                    String samplePath = postPath + EncDecDes.getInstance().generateFileName("imageresized_" + System.currentTimeMillis());

//                                    if (BitmapUtil.makePostBitmap(photoEntry.path, samplePath, PhotoPickerActivity.this) == true) {
                                    if (ImageLoaderUtil.compressImage(photoEntry.path, samplePath)) {
                                        DebugConfig.error("*****", String.format("Compress Image2:%d->%d", new File(photoEntry.path).length(), new File(samplePath).length()));
                                        photoEntry.path = samplePath;
                                    }
                                } else if ( photoEntry.mediaType == MediaController.MediaType.VIDEO ) {
                                    isVideoSelected = true;

                                    String samplePath = postPath + EncDecDes.getInstance().generateFileName("videocompressed_" + System.currentTimeMillis());

                                    try {
                                        boolean isCompressSuccess = com.yovenny.videocompress.MediaController.getInstance().convertVideo(photoEntry.path, samplePath);

                                        if ( isCompressSuccess )
                                        {
                                            DebugConfig.error("*****", String.format("Compress Success:%d->%d", new File(photoEntry.path).length(), new File(samplePath).length()));
                                            photoEntry.path = samplePath;
                                        }
                                    } catch ( Exception e ) {
                                        e.printStackTrace();
                                    }
                                }

                                File f = new File(photoEntry.path);

                                if ( f.length() > GlobalConstrants.FILE_SIZE_LIMIT_IN_BYTES) {
                                    ImApp.RunOnUIThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            ErrorManager.showErrorForFileSize();
                                        }
                                    });

                                    if ( photoEntry.path.startsWith(postPath) )
                                        f.delete();
                                } else
                                    photos.add(0, photoEntry.path);
                            }
                        }
                        // delegate.didSelectPhotos(photos);
                        if (photos.size() > 0) {
                            if (isVideoSelected)
                                postNotification(photos, false);
                            else
                                postNotification(photos, true);
                        }
                    }
                    else
                    {
                        final ArrayList<MediaController.PhotoEntry> photos = new ArrayList<MediaController.PhotoEntry>();
                        for (HashMap.Entry<Integer, MediaController.PhotoEntry> entry : selectedPhotos.entrySet()) {
                            MediaController.PhotoEntry photoEntry = entry.getValue();
                            if (photoEntry.path != null) {
                                if ( !btn_fullimage.isChecked() && photoEntry.mediaType == MediaController.MediaType.PHOTO ) {
                                    GlobalFunc.makeChatDir();
                                    String chatPath = GlobalConstrants.LOCAL_PATH+GlobalConstrants.CHAT_DIR_NAME+"/"+ GlobalVariable.account_id+"/";
                                    if ( mChatId != null )
                                    {
                                        chatPath += mChatId + "/";
                                        File fileChat = new File(chatPath);
                                        if ( !fileChat.exists() )
                                            fileChat.mkdirs();
                                    }

                                    String samplePath = chatPath + EncDecDes.getInstance().generateFileName("imageresized_" + System.currentTimeMillis());

//									if (BitmapUtil.makePostBitmap(photoEntry.path, samplePath, PhotoPickerActivity.this) == true) {
                                    if (ImageLoaderUtil.compressImage(photoEntry.path, samplePath)) {
                                        DebugConfig.error("*****", String.format("Compress Image3:%d->%d", new File(photoEntry.path).length(), new File(samplePath).length()));
                                        photoEntry.path = samplePath;
                                    }
                                } else if ( photoEntry.mediaType == MediaController.MediaType.VIDEO ) {
                                    GlobalFunc.makeChatDir();
                                    String chatPath = GlobalConstrants.LOCAL_PATH+GlobalConstrants.CHAT_DIR_NAME+"/"+ GlobalVariable.account_id+"/";
                                    if ( mChatId != null )
                                    {
                                        chatPath += mChatId + "/";
                                        File fileChat = new File(chatPath);
                                        if ( !fileChat.exists() )
                                            fileChat.mkdirs();
                                    }

                                    String samplePath = chatPath + EncDecDes.getInstance().generateFileName("videocompressed_" + System.currentTimeMillis() );

                                    try {
                                        boolean isCompressSuccess = com.yovenny.videocompress.MediaController.getInstance().convertVideo(photoEntry.path, samplePath);

                                        if ( isCompressSuccess )
                                        {
                                            DebugConfig.error("*****", String.format("Compress Success:%d->%d", new File(photoEntry.path).length(), new File(samplePath).length()));
                                            photoEntry.path = samplePath;
                                        }
                                    } catch ( Exception e ) {
                                        e.printStackTrace();
                                    }
                                }

                                photos.add(0, photoEntry);
                            }
                        }
                        // delegate.didSelectPhotos(photos);
                        if (photos.size() > 0) {
                            if ( mType == TYPE_CHATROOM ) {
                                Intent intent = new Intent();
                                intent.putExtra("data", photos);
                                setResult(RESULT_OK, intent);
                            }
                        }
                    }
                }

                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                mProgressDlg.dismiss();
                if ( result )
                    finish();
            }
        }.execute();

        mProgressDlg.addAsyncTask(task);
    }

    private void updateSelectedCount() {
        if (selectedPhotos.isEmpty()) {
            doneButtonTextView.setTextColor(0xff999999);
            doneButtonBadgeTextView.setText("");
            doneButton.setEnabled(false);

            if ( mType == TYPE_EDITPOST )
                mBtnSend.setText(getString(R.string.str_done));
            else
                mBtnSend.setText(getString(R.string.send));
            mBtnSend.setEnabled(false);
        } else {
            doneButtonTextView.setTextColor(0xffffffff);
            doneButtonBadgeTextView.setText("(" + selectedPhotos.size() + ")");
            doneButton.setEnabled(true);

            if ( mType == TYPE_EDITPOST )
                mBtnSend.setText(getString(R.string.str_done)+"("+selectedPhotos.size()+"/"+MAX_SELECT_COUNT+")");
            else
                mBtnSend.setText(getString(R.string.send)+"("+selectedPhotos.size()+"/"+MAX_SELECT_COUNT+")");
            mBtnSend.setEnabled(true);
        }
    }

    private void updateSelectedPhoto(View view, MediaController.PhotoEntry photoEntry) {
        ImageView checkImageView = (ImageView) view.findViewById(R.id.photo_check);
        if (selectedPhotos.containsKey(photoEntry.imageId)) {
            checkImageView.setImageResource(R.drawable.circlecheckbox_on);
        } else {
            checkImageView.setImageResource(R.drawable.circlecheckbox_off);
        }
    }

    private class FoldersArrayAdapter extends BaseAdapter
    {
        Context mContext;

        public FoldersArrayAdapter(Context context)
        {
            mContext = context;
        }

        @Override
        public int getCount() {
            return albumsSorted.size();
        }

        @Override
        public Object getItem(int position) {
            MediaController.AlbumEntry albumEntry = (MediaController.AlbumEntry)albumsSorted.get(position);

            return albumEntry.bucketName;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            ViewHolder1 viewHolder = null;
            if ( convertView == null ) {
                viewHolder = new ViewHolder1();
                LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = li.inflate(R.layout.profile_folder_item, parent, false);
                viewHolder.text_name = (TextView) convertView.findViewById(R.id.text_name);
                viewHolder.text_count = (TextView) convertView.findViewById(R.id.text_count);
                viewHolder.image_folder = (ImageView) convertView.findViewById(R.id.image_folder);
                viewHolder.image_video_icon = (ImageView) convertView.findViewById(R.id.image_video_icon);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder1)convertView.getTag();
            }

            MediaController.AlbumEntry albumEntry = (MediaController.AlbumEntry)albumsSorted.get(position);
            viewHolder.text_name.setText(albumEntry.bucketName);

            if ( position == 0 ) {
                viewHolder.text_count.setVisibility(View.GONE);
            }
            else {
                viewHolder.text_count.setText(String.valueOf(albumEntry.photos.size()));
                viewHolder.text_count.setVisibility(View.VISIBLE);
            }

            if ( albumEntry.coverPhoto == null || albumEntry.coverPhoto.mediaType == MediaController.MediaType.PHOTO )
                viewHolder.image_video_icon.setVisibility(View.GONE);
            else
                viewHolder.image_video_icon.setVisibility(View.VISIBLE);

            if ( albumEntry.coverPhoto == null )
                viewHolder.image_folder.setImageDrawable(null);
            else {
                ImageLoaderUtil.loadImageFile(PhotoPickerActivity.this, albumEntry.coverPhoto.path, viewHolder.image_folder);
            }

            if ( position == spinnerFolders.getSelectedItemPosition() )
            {
                convertView.setBackgroundColor(getResources().getColor(R.color.dropdown_sel_color));
            }
            else
            {
                convertView.setBackgroundColor(getResources().getColor(R.color.dropdown_bg_color));
            }

            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder2 viewHolder = null;
            if ( convertView == null ) {
                viewHolder = new ViewHolder2();
                LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = li.inflate(R.layout.spinner_folder_item, parent, false);
                viewHolder.text_name = (TextView) convertView.findViewById(R.id.text_name);

                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder2)convertView.getTag();
            }

            MediaController.AlbumEntry albumEntry = (MediaController.AlbumEntry)albumsSorted.get(position);
            viewHolder.text_name.setText(albumEntry.bucketName);

            return convertView;
        }

        private class ViewHolder1
        {
            TextView text_name;
            TextView text_count;
            ImageView image_folder;
            ImageView image_video_icon;
        }
        private class ViewHolder2
        {
            TextView text_name;
        }
    }

    private void onCheck(View v, int i)
    {
        MediaController.PhotoEntry photoEntry = selectedAlbum.photos.get(i);
        if (selectedPhotos.containsKey(photoEntry.imageId)) {
            selectedPhotos.remove(photoEntry.imageId);
            if (selectedPhotos.size() == 0)
                m_isVideoSelected = false;
        } else {
            if (selectedPhotos.size() >= MAX_SELECT_COUNT) {
                GlobalFunc.showErrorMessageToast(this, 106, false);
                return;
            }
            if (mType == TYPE_EDITPOST && photoEntry.mediaType == MediaController.MediaType.VIDEO && selectedPhotos.size() > 0) //not able to select video after select photos
                return;

            if (m_isVideoSelected)
                return;

            if (mType == TYPE_EDITPOST && photoEntry.mediaType == MediaController.MediaType.VIDEO)
                m_isVideoSelected = true;

            selectedPhotos.put(photoEntry.imageId, photoEntry);
        }
        updateSelectedPhoto(v, photoEntry);
        updateSelectedCount();
        if (isSetBackground && selectedPhotos.size() > 0) {
            sendSelectedPhotos();
        }
    }

    private class ListAdapter extends BaseAdapter {
        private Context mContext;
        int externalStoreDirLen = 0;

        public ListAdapter(Context context) {
            mContext = context;
            externalStoreDirLen = Environment.getExternalStorageDirectory().getAbsolutePath().length();
        }

        @Override
        public boolean areAllItemsEnabled() {
            return true;
        }

        @Override
        public boolean isEnabled(int i) {
            return true;
        }

        @Override
        public int getCount() {
            if (selectedAlbum != null) {
                return selectedAlbum.photos.size();
            }
            return 0;
        }

        @Override
        public Object getItem(int i) {
            return selectedAlbum.photos.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder = null;

            if (view == null) {
                LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = li.inflate(R.layout.photo_picker_photo_layout, viewGroup, false);
                view.setLayoutParams(new GridView.LayoutParams(itemWidth, itemWidth));
                viewHolder = new ViewHolder();
                viewHolder.checkImageView = view.findViewById(R.id.photo_check_frame);
                viewHolder.imageView = (ImageView) view.findViewById(R.id.media_photo_image);
                viewHolder.durationView = (TextView) view.findViewById(R.id.media_video_duration);
                viewHolder.video_overlay = view.findViewById(R.id.lyt_video);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }

            MediaController.PhotoEntry photoEntry = selectedAlbum.photos.get(i);

            if (isSetBackground || (mType == TYPE_EDITPOST && photoEntry.mediaType == MediaController.MediaType.VIDEO)) {
                viewHolder.checkImageView.setVisibility(View.GONE);
//				viewHolder.checkImageView.setOnClickListener(null);
            } else {
                viewHolder.checkImageView.setVisibility(View.VISIBLE);

//				viewHolder.checkImageView.setOnClickListener(new View.OnClickListener() {
//					@Override
//					public void onClick(View v) {
//						onCheck((View)v.getParent(), i);
//					}
//				});
            }

//			imageView.setTag(i);
//			view.setTag(i);

            if (photoEntry.path != null) {
//				viewHolder.imageView.setImageDrawable(null);
                if (photoEntry.mediaType == MediaController.MediaType.VIDEO)
                    viewHolder.video_overlay.setVisibility(View.VISIBLE);
                else
                    viewHolder.video_overlay.setVisibility(View.GONE);

                ImageLoaderUtil.loadImageFile(PhotoPickerActivity.this, photoEntry.path, viewHolder.imageView);
            } else {
                viewHolder.imageView.setImageResource(R.drawable.nophotos);
                viewHolder.video_overlay.setVisibility(View.GONE);
            }
            updateSelectedPhoto(view, photoEntry);

            return view;
        }

        @Override
        public int getItemViewType(int i) {
            if (selectedAlbum != null) {
                return 1;
            }
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            if (selectedAlbum != null) {
                return selectedAlbum.photos.isEmpty();
            }
            return true;
        }

        private class ViewHolder
        {
            View checkImageView;
            ImageView imageView;
            TextView durationView;
            View video_overlay;
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btnSend:
                sendSelectedPhotos();
                break;

            default:
                super.onClick(v);
                break;
        }
    }
}