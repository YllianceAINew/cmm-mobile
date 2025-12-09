package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.multimediachat.app.ImApp;
import com.multimediachat.global.GlobalFunc;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.request.RequestOptions;
import com.multimediachat.R;
import com.multimediachat.app.MediaController;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.ui.adapter.carouseltab.TabsAdapter;
import com.multimediachat.ui.adapter.carouseltab.ViewPager;
import com.multimediachat.ui.filesend.FileInfo;
import com.multimediachat.ui.filesend.FileManagerFragment;
import com.multimediachat.ui.filesend.FileSendAudioFragment;
import com.multimediachat.ui.filesend.FileSendImageFragment;
import com.multimediachat.ui.filesend.FileSendVideoFragment;
import com.multimediachat.util.Utilities;

import java.io.File;
import java.util.ArrayList;

@SuppressLint("DefaultLocale")
public class FileSendTabActivity extends BaseActivity implements NotificationCenter.NotificationCenterDelegate, View.OnClickListener, TabsAdapter.OnPageChangeListener {
    private ViewPager mViewPager;
    private static TabsAdapter mTabsAdapter;
    private static final String TAG_IMAGE_TABS = "tab_image";
    private static final String TAG_VIDEO_TABS = "tab_video";
    private static final String TAG_AUDIO_TABS = "tab_audio";
    private static final String TAG_FILE_TABS = "tab_file";

    public static final int TAB_INDEX_IMAGE = 0;
    public static final int TAB_INDEX_VIDEO = 1;
    public static final int TAB_INDEX_AUDIO = 2;
    public static final int TAB_INDEX_FILE = 3;

    private final int ERROR_FILE_SIZE_TOO_LARGE = 100;
    private final int ERROR_FILE_TYPE_WRONG = 101;
    private final int ERROR_FILE_NO_EXIST = 102;

    public static ArrayList<MediaController.PhotoEntry> selectedPhotos;
    public ArrayList<MediaController.PhotoEntry> mixedPhotos = new ArrayList<>();

    public static Cursor mImageCursor = null;
    public static Cursor mVideoCursor = null;
    public static Cursor mAudioCursor = null;

    public static int mImageSelPos = -1;
    public static int mVideoSelPos = -1;
    public static int mAudioSelPos = -1;
    public static int mFileSelPos = -1;

    public int classGuid;
    public boolean loading = false;
    private static final int MEDIA_TYPE_ALL = -1;
    private int mediaType = MEDIA_TYPE_ALL;
    Bundle argBundle;
    public static Button mBtnSelected;
    public static Button mBtnSend;
    private int MAXLIMIT = 10000;
    public ArrayList<String> mCheckedFiles = new ArrayList<>();

    public FileSendTabActivity mActivity;

    public ArrayList<String> folders = new ArrayList<String>();
    public ArrayList<FileInfo> fileInfos = new ArrayList<FileInfo>();
    public ArrayList<FileInfo> storageInfos = new ArrayList<FileInfo>();

    private SelectedItemAdapter mSelectedItemAdapter;
    TextView mTxtSelectedTitle;
    ListView mListSelectedItem;
    Dialog mSelectedDlg;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mActivity = this;

        initActionBar();
        initUI();
        initTabView();
    }

    private void initActionBar() {
        setContentView(R.layout.activity_file_send_tab);
        setActionBarTitle(getString(R.string.create_filse_send));
    }

    private void initUI() {
//        findViewById(R.id.btnViewMore).setVisibility(View.GONE);
        classGuid = ImApp.getInstance().generateClassGuid();

        selectedPhotos = new ArrayList<>();
        NotificationCenter.getInstance().addObserver(this, NotificationCenter.albumsDidLoaded);

        mSelectedItemAdapter = new SelectedItemAdapter(this);

        mBtnSend = (Button) findViewById(R.id.btn_send);
        mBtnSend.setOnClickListener(this);

        mBtnSelected = (Button) findViewById(R.id.btn_selected);
        mBtnSelected.setOnClickListener(this);

        mediaType = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

        loadGalleryAlbums(mediaType);
        loadPhoneAndExternalStorage();
        updateControlButton();
    }

    private void loadPhoneAndExternalStorage() {
        /*if  (GlobalFunc.getProductModel().startsWith(GlobalConstrants.MODEL_NAME_ARIRANG)){
             *//*Read internal and external storage using StorageManager*//*
            StorageManager mStorageManager = (StorageManager) getSystemService(Context.STORAGE_SERVICE);
            List<StorageVolume> storageVolumeList = null;
            storageVolumeList = mStorageManager.getStorageVolumes();

            if(storageVolumeList != null){
                for (StorageVolume volume : storageVolumeList) {
                    if (volume != null && volume.getState().equals(Environment.MEDIA_MOUNTED)) {
                        FileInfo info = new FileInfo();
                        Parcel parcel = Parcel.obtain();
                        volume.writeToParcel(parcel, 1);
                        parcel.setDataPosition(0);
                        String id = parcel.readString();
                        int storageid = parcel.readInt();
                        info.storageId = storageVolumeList.indexOf(volume);
                        info.absPath =parcel.readString();
                        info.fileName = parcel.readString();
                        info.storageType = parcel.readInt();

                        StatFs stat = new StatFs(info.absPath);
                        long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getAvailableBlocks();
                        long bytesTotal = (long)stat.getBlockSize() *(long)stat.getBlockCount();
                        info.lastModified = String.format("%s %s %s %s", getString(R.string.file_manager_total), Utilities.formatFileSize(bytesTotal),
                                getString(R.string.file_manager_available), Utilities.formatFileSize(bytesAvailable));

                        storageInfos.add(info);
                    }
                }
            }
        } else {*/
        /////             Read only phone internal storage     ////////////
        FileInfo info = new FileInfo();
        info.storageType = 1;
        info.absPath = Environment.getExternalStorageDirectory().getPath();
        info.fileName = getString(R.string.file_manager_primary);
        info.storageId = 0;
        StatFs stat = new StatFs(info.absPath);
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        long bytesTotal = (long) stat.getBlockSize() * (long) stat.getBlockCount();
        info.lastModified = String.format("%s %s %s %s", getString(R.string.file_manager_total), Utilities.formatFileSize(bytesTotal),
                getString(R.string.file_manager_available), Utilities.formatFileSize(bytesAvailable));
        storageInfos.add(info);
        //}
    }

    private void initTabView() {
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTabsAdapter = new TabsAdapter(this, mViewPager, "File Send");

        argBundle = new Bundle();
        mTabsAdapter.startWholeTabs();
        mTabsAdapter.addFragment(TAG_IMAGE_TABS, getResources().getString(R.string.tab_label_image), FileSendImageFragment.class, argBundle, TAB_INDEX_IMAGE, true, R.drawable.tab_picture_n);
        mTabsAdapter.addFragment(TAG_VIDEO_TABS, getResources().getString(R.string.tab_label_video), FileSendVideoFragment.class, argBundle, TAB_INDEX_VIDEO, true, R.drawable.tab_video_n);
        mTabsAdapter.addFragment(TAG_AUDIO_TABS, getResources().getString(R.string.tab_label_audio), FileSendAudioFragment.class, argBundle, TAB_INDEX_AUDIO, true, R.drawable.tab_music_n);
        mTabsAdapter.addFragment(TAG_FILE_TABS, getResources().getString(R.string.tab_label_file), FileManagerFragment.class, argBundle, TAB_INDEX_FILE, true, R.drawable.tab_folder_n);

        mTabsAdapter.finishWholeTabs();
        mViewPager.setOffscreenPageLimit(1);

        mTabsAdapter.setOnPageChangeListener(this);
        mTabIndex = TAB_INDEX_IMAGE;
    }

    /*start : setup tab view*/
    @Override
    public void onStart() {
        super.onStart();
        if (mDuringSwipe || mUserTabClick) {
            mDuringSwipe = false;
            mUserTabClick = false;
        }
    }

    @Override
    protected void onDestroy() {
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.albumsDidLoaded);

        if (mImageCursor != null) {
            mImageCursor.close();
            mImageCursor = null;
        }

        if (mVideoCursor != null) {
            mVideoCursor.close();
            mVideoCursor = null;
        }

        if (mAudioCursor != null) {
            mAudioCursor.close();
            mAudioCursor = null;
        }

        //mScanner.cancel();
        super.onDestroy();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void didReceivedNotification(int id, Object... args) {
        if (id == NotificationCenter.albumsDidLoaded) {
            int guid = (Integer) args[0];
            if (classGuid == guid) {
                fileInfos = (ArrayList<FileInfo>) args[1];
                mFileFragment.hideLoadingDialog();
                mFileFragment.viewFolderList();
                loading = true;
            }
        }
    }

    static int mTabIndex;
    FileSendImageFragment mImageFragment;
    FileSendVideoFragment mVideoFragment;
    FileSendAudioFragment mAudioFragment;
    FileManagerFragment mFileFragment;
    private boolean mDuringSwipe = false;
    boolean mUserTabClick = false;

    private static final String[] projectionFiles = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE
    };

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        mTabIndex = position;
        mImageSelPos = mVideoSelPos = mAudioSelPos = mFileSelPos = -1;

        if (mTabIndex == TAB_INDEX_IMAGE) {
            mediaType = MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
            loadGalleryAlbums(mediaType);
            initImageFragementView();
        } else if (mTabIndex == TAB_INDEX_VIDEO) {
            mediaType = MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
            loadGalleryAlbums(mediaType);
            initVideoFragementView();
        } else if (mTabIndex == TAB_INDEX_AUDIO) {
            mediaType = MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;
            loadGalleryAlbums(mediaType);
            initAudioFragementView();
        } else {
            mediaType = MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
            loadGalleryAlbums(mediaType);
            if (!loading) {
                fileInfos = storageInfos;
                mFileFragment.mCurrentFolder = "/";
                mFileFragment.viewFolderList();
                loading = true;
            } else
                initFileFragementView();
        }

        updateControlButton();
        mTabsAdapter.getFragmentTag(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public void loadGalleryAlbums(final int loadType) {
        try {
            if (loadType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                if (mImageCursor != null) {
                    mImageCursor.close();
                    mImageCursor = null;
                }
                String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + loadType;
                Uri queryUri = MediaStore.Files.getContentUri("external");
                mImageCursor = ImApp.applicationContext.getContentResolver().query(queryUri, projectionFiles, selection, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
            } else if (loadType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
                if (mVideoCursor != null) {
                    mVideoCursor.close();
                    mVideoCursor = null;
                }
                String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + loadType;
                Uri queryUri = MediaStore.Files.getContentUri("external");
                mVideoCursor = ImApp.applicationContext.getContentResolver().query(queryUri, projectionFiles, selection, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
            } else if (loadType == MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO) {
                if (mAudioCursor != null) {
                    mAudioCursor.close();
                    mAudioCursor = null;
                }
                String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + loadType;
                Uri queryUri = MediaStore.Files.getContentUri("external");
                mAudioCursor = ImApp.applicationContext.getContentResolver().query(queryUri, projectionFiles, selection, null, MediaStore.Files.FileColumns.DISPLAY_NAME + " DESC");
            } else {
                if (mImageCursor != null) {
                    mImageCursor.close();
                    mImageCursor = null;
                }

                if (mVideoCursor != null) {
                    mVideoCursor.close();
                    mVideoCursor = null;
                }

                if (mAudioCursor != null) {
                    mAudioCursor.close();
                    mAudioCursor = null;
                }

                String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                Uri queryUri = MediaStore.Files.getContentUri("external");
                mImageCursor = ImApp.applicationContext.getContentResolver().query(queryUri, projectionFiles, selection, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");

                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
                queryUri = MediaStore.Files.getContentUri("external");
                mVideoCursor = ImApp.applicationContext.getContentResolver().query(queryUri, projectionFiles, selection, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");

                selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO;
                queryUri = MediaStore.Files.getContentUri("external");
                mAudioCursor = ImApp.applicationContext.getContentResolver().query(queryUri, projectionFiles, selection, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
            }
        } catch (Exception e) {
        } finally {

        }
    }

    public MediaController.PhotoEntry getPhotoEntry(int mediaType, int pos) {
        int imageId = 0;
        String path = "";
        long dateTaken = 0;
        MediaController.PhotoEntry photoEntry = null;

        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
            mImageCursor.moveToPosition(pos);
            int dataColumn = mImageCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            path = mImageCursor.getString(dataColumn);
            photoEntry = new MediaController.PhotoEntry(imageId, imageId, dateTaken, path, 0, MediaController.MediaType.PHOTO);
        } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            mVideoCursor.moveToPosition(pos);
            int dataColumn = mVideoCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            path = mVideoCursor.getString(dataColumn);
            photoEntry = new MediaController.PhotoEntry(imageId, imageId, dateTaken, path, 0, MediaController.MediaType.VIDEO);
        } else if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO) {
            mAudioCursor.moveToPosition(pos);
            int dataColumn = mAudioCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            path = mAudioCursor.getString(dataColumn);
            photoEntry = new MediaController.PhotoEntry(imageId, imageId, dateTaken, path, 0, MediaController.MediaType.AUDIO);
        } else {
            FileInfo info = mActivity.fileInfos.get(pos);
            if (info.fileType == 0)
                photoEntry = new MediaController.PhotoEntry(imageId, imageId, dateTaken, info.absPath, 0, MediaController.MediaType.AUDIO);
            else if (info.fileType == 1)
                photoEntry = new MediaController.PhotoEntry(imageId, imageId, dateTaken, info.absPath, 0, MediaController.MediaType.VIDEO);
            else if (info.fileType == 2)
                photoEntry = new MediaController.PhotoEntry(imageId, imageId, dateTaken, info.absPath, 0, MediaController.MediaType.PHOTO);
        }

        return photoEntry;
    }

    private void showErrorDialog(int res) {
        // custom dialog
        final Dialog dlg = GlobalFunc.createDialog(this, R.layout.msgdialog, true);

        TextView title = (TextView) dlg.findViewById(R.id.msgtitle);
        title.setText(R.string.information);

        TextView content = (TextView) dlg.findViewById(R.id.msgcontent);

        if (res == ERROR_FILE_TYPE_WRONG)
            content.setText(R.string.error_file_type);
        else if (res == ERROR_FILE_NO_EXIST)
            content.setText(R.string.error_file_no_exist);

        Button dlg_btn_ok = (Button) dlg.findViewById(R.id.btn_ok);
        dlg_btn_ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    dlg.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        dlg.setCanceledOnTouchOutside(false);

        if (this != null && !this.isFinishing()) {
            dlg.show();
        }
    }

    private boolean isExistsPhotoEntry(ArrayList<MediaController.PhotoEntry> orgPeArr, MediaController.PhotoEntry cmpPe) {
        for (MediaController.PhotoEntry orgPe : orgPeArr) {
            if (orgPe.path.equals(cmpPe.path))
                return true;
        }
        return false;
    }

    private int getIndexInArray(ArrayList<MediaController.PhotoEntry> orgPeArr, MediaController.PhotoEntry cmpPe) {
        int pos = 0;
        for (MediaController.PhotoEntry orgPe : orgPeArr) {
            if (orgPe.path.equals(cmpPe.path))
                return pos;
            pos++;
        }
        return 0;
    }

    public Boolean onAlbumSelect(int position) {

        MediaController.PhotoEntry selectedPhoto = getPhotoEntry(mediaType, position);
        File fileSend = new File(selectedPhoto.path);
        if (fileSend != null && fileSend.exists()) {
            if (fileSend.length() == 0) {
                showErrorDialog(ERROR_FILE_TYPE_WRONG);
                selectedPhoto = null;
            }
        } else {
            showErrorDialog(ERROR_FILE_NO_EXIST);
            selectedPhoto = null;
        }
        if (selectedPhoto == null)
            return false;

        if (!mCheckedFiles.contains(selectedPhoto.path)) {
            if (mCheckedFiles.size() < 9) {
                selectedPhotos.add(selectedPhoto);
                mCheckedFiles.add(selectedPhoto.path);
            } else {
                showErrorDialog();
                return false;
            }
        } else {
            selectedPhotos.remove(getIndexInArray(selectedPhotos, selectedPhoto));
            mCheckedFiles.remove(selectedPhoto.path);
        }

        updateSelectedCount();
        updateControlButton();
        return true;
    }

    private void showErrorDialog() {
        if (this != null && !this.isFinishing()) {
            final Dialog dlg = GlobalFunc.createDialog(this, R.layout.msgdialog, true);

            TextView title = (TextView) dlg.findViewById(R.id.msgtitle);
            title.setText(R.string.information);

            TextView content = (TextView) dlg.findViewById(R.id.msgcontent);
            content.setText(R.string.file_choose_limit);

            Button dlg_btn_ok = (Button) dlg.findViewById(R.id.btn_ok);
            dlg_btn_ok.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    try {
                        dlg.dismiss();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            dlg.setCanceledOnTouchOutside(false);
            dlg.show();
        }
    }

    private Fragment getFragmentAt(int position) {
        String curFragTag = mTabsAdapter.getFragmentTag(position);
        if (curFragTag.equals(TAG_IMAGE_TABS))
            return mImageFragment;
        else if (curFragTag.equals(TAG_VIDEO_TABS))
            return mVideoFragment;
        else if (curFragTag.equals(TAG_AUDIO_TABS))
            return mAudioFragment;
        else if (curFragTag.equals(TAG_FILE_TABS))
            return mFileFragment;
        else {
            throw new IllegalStateException("Unknown fragment index: " + position);
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof FileSendImageFragment)
            mImageFragment = (FileSendImageFragment) fragment;
        else if (fragment instanceof FileSendVideoFragment)
            mVideoFragment = (FileSendVideoFragment) fragment;
        else if (fragment instanceof FileSendAudioFragment)
            mAudioFragment = (FileSendAudioFragment) fragment;
        else if (fragment instanceof FileManagerFragment)
            mFileFragment = (FileManagerFragment) fragment;
    }

    private void sendSelectedPhotos() {
        if (selectedPhotos.isEmpty()) {
            return;
        }

        if (selectedPhotos.size() > 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.chat_photos_and_videos_selected, selectedPhotos);
                }
            }).start();
        }
        finish();
    }

    public void updateControlButton() {
        if (selectedPhotos.isEmpty()) {
            mBtnSelected.setEnabled(false);
            mBtnSend.setEnabled(false);
        } else {
            mBtnSelected.setEnabled(true);
            mBtnSend.setEnabled(true);
        }
    }

    public void initImageFragementView() {
        FileSendImageFragment managerFragment = (FileSendImageFragment) (mActivity.getFragmentAt(TAB_INDEX_IMAGE));
        if (managerFragment != null)
            managerFragment.updateView();
    }

    public void initVideoFragementView() {
        FileSendVideoFragment managerFragment = (FileSendVideoFragment) (mActivity.getFragmentAt(TAB_INDEX_VIDEO));
        if (managerFragment != null)
            managerFragment.updateView();
    }

    public void initAudioFragementView() {
        FileSendAudioFragment managerFragment = (FileSendAudioFragment) (mActivity.getFragmentAt(TAB_INDEX_AUDIO));
        if (managerFragment != null)
            managerFragment.updateView();
    }

    public void initFileFragementView() {
        FileManagerFragment managerFragment = (FileManagerFragment) (mActivity.getFragmentAt(TAB_INDEX_FILE));
        if (managerFragment != null)
            managerFragment.viewFolderList();
    }

    /*start : choose MediaFileStream*/

    public void updateSelectedCount() {
        if (selectedPhotos.isEmpty()) {
            mBtnSelected.setText(getString(R.string.select));
            mBtnSelected.setEnabled(false);
            mBtnSend.setEnabled(false);
            return;
        }
        mBtnSelected.setEnabled(true);
        int selectedCount = selectedPhotos.size();
        if (selectedCount > 0) {
            mBtnSelected.setText(getString(R.string.select) + "(" + selectedCount + ")");
            mBtnSend.setEnabled(true);
        } else {
            mBtnSend.setEnabled(false);
            mBtnSelected.setText(getString(R.string.select));
        }
    }

    public class SelectedItemAdapter extends BaseAdapter {
        private Context mContext;

        public SelectedItemAdapter(Context _context) {
            mContext = _context;
        }

        public boolean containsItem(MediaController.PhotoEntry _item) {
            if (mixedPhotos.contains(_item))
                return true;
            else
                return false;
        }

        public boolean removeItem(MediaController.PhotoEntry _item, MediaController.MediaType mediaType) {
            int i = 0;
            if (mixedPhotos.remove(_item)) {
                if (selectedPhotos.remove(_item)) {
                    ;
                }
                mCheckedFiles.remove(_item.path);
                if (mixedPhotos.size() == 0)
                    mSelectedDlg.dismiss();
                if (mediaType == MediaController.MediaType.PHOTO) {
                    int count = mImageCursor.getCount();
                    for (i = 0; i < count; i++)
                        if (getPhotoEntry(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, i).path.equals(_item.path))
                            break;
                    mCheckedFiles.remove(_item.path);
                } else if (mediaType == MediaController.MediaType.VIDEO) {
                    int count = mVideoCursor.getCount();
                    for (i = 0; i < count; i++)
                        if (getPhotoEntry(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO, i).path.equals(_item.path))
                            break;
                    mCheckedFiles.remove(_item.path);
                } else if (mediaType == MediaController.MediaType.AUDIO) {
                    int count = mAudioCursor != null ? mAudioCursor.getCount() : 0;
                    for (i = 0; i < count; i++)
                        if (getPhotoEntry(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO, i).path.equals(_item.path))
                            break;
                    mCheckedFiles.remove(_item.path);
                }
                notifyDataSetChanged();
                initImageFragementView();
                initAudioFragementView();
                initVideoFragementView();
                initFileFragementView();
                updateSelectedCount();
            }
            return false;
        }

        public void removeAll() {
            mixedPhotos.clear();
            selectedPhotos.clear();
            mCheckedFiles = new ArrayList<>();
            notifyDataSetChanged();
        }

        public ArrayList<MediaController.PhotoEntry> getAllItem() {
            return mixedPhotos;
        }

        @Override
        public int getCount() {
            int count = 0;
            if (mixedPhotos != null)
                count = mixedPhotos.size();
            return count;
        }

        @Override
        public MediaController.PhotoEntry getItem(int position) {
            if (mixedPhotos != null)
                return mixedPhotos.get(position);
            return null;
        }

        @Override
        public long getItemId(int position) {
            if (mixedPhotos != null)
                return mixedPhotos.get(position).hashCode();
            return 0;
        }

        @Override
        public View getView(final int position, View view, ViewGroup viewGroup) {
            SelectedItemAdapter.ViewHolder viewHolder;
            final MediaController.PhotoEntry photoEntry = getItem(position);

            LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = li.inflate(R.layout.select_list_item, viewGroup, false);
            viewHolder = new SelectedItemAdapter.ViewHolder();
            viewHolder.imgIcon = (ImageView) view.findViewById(R.id.img_select);
            viewHolder.txtName = (TextView) view.findViewById(R.id.txt_name);
            viewHolder.txtInfo = (TextView) view.findViewById(R.id.txt_info);
            viewHolder.iconDelete = view.findViewById(R.id.icon_delete);

            if (photoEntry.mediaType == MediaController.MediaType.PHOTO)
                Glide.with(mContext)
                        .setDefaultRequestOptions(new RequestOptions()
                                .format(DecodeFormat.PREFER_RGB_565)
                                .placeholder(R.drawable.download_image)
                                .error(R.drawable.download_image))
                        .load(photoEntry.path)
                        .into(viewHolder.imgIcon);
            else if (photoEntry.mediaType == MediaController.MediaType.VIDEO)
                Glide.with(mContext)
                        .setDefaultRequestOptions(new RequestOptions()
                                .format(DecodeFormat.PREFER_RGB_565)
                                .placeholder(R.drawable.download_video)
                                .error(R.drawable.download_video))
                        .load(photoEntry.path)
                        .into(viewHolder.imgIcon);
            else if (photoEntry.mediaType == MediaController.MediaType.AUDIO)
                viewHolder.imgIcon.setImageResource(R.drawable.download_audio);

            if (photoEntry != null) {
                File file = new File(photoEntry.path);
                viewHolder.txtName.setText(file.getName());
                viewHolder.txtName.setSelected(true);
                viewHolder.txtInfo.setText(Utilities.formatFileSize(file.length()));
            }

            viewHolder.iconDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeItem(photoEntry, photoEntry.mediaType);
                }
            });

            return view;
        }

        @Override
        public boolean isEnabled(int position) {
            return false;
        }

        private class ViewHolder {
            ImageView imgIcon;
            TextView txtName;
            ImageView iconDelete;
            TextView txtInfo;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                if (GlobalFunc.checkStorageFreeSpace(this)) {
                    sendSelectedPhotos();
                }
                break;
            case R.id.btn_selected:
                mSelectedDlg = new Dialog(this);
                mSelectedDlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
                mSelectedDlg.setContentView(R.layout.custom_selected_item_dialog);
                mSelectedDlg.setCanceledOnTouchOutside(true);
                mSelectedDlg.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mSelectedDlg.findViewById(R.id.btn_delete).setOnClickListener(this);
                mTxtSelectedTitle = (TextView) mSelectedDlg.findViewById(R.id.txt_select_title);
                mListSelectedItem = (ListView) mSelectedDlg.findViewById(R.id.list_selected_user);
                mixedPhotos = new ArrayList<>();
                for (MediaController.PhotoEntry mp : selectedPhotos)
                    mixedPhotos.add(mp);
                mListSelectedItem.setAdapter(mSelectedItemAdapter);
                mSelectedDlg.show();
                break;

            case R.id.btn_delete:
                mSelectedDlg.dismiss();
                //listAdapter.notifyDataSetChanged();
                mSelectedItemAdapter.removeAll();
                selectedPhotos.clear();
                mCheckedFiles = new ArrayList<>();
                initImageFragementView();
                initVideoFragementView();
                initAudioFragementView();
                initFileFragementView();
                updateSelectedCount();
                break;

            default:
                super.onClick(v);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (mTabIndex == TAB_INDEX_FILE && mFileFragment != null) {
            String path = mFileFragment.mCurrentFolder;
            File file = new File(path);
            if (file != null && !path.equals("/")) {
                if (path.equals(storageInfos.get(mFileFragment.mCurrentStorage).absPath)) {
                    fileInfos = storageInfos;
                    mFileFragment.mCurrentFolder = "/";
                    mFileFragment.viewFolderList();
                } else {
                    path = file.getParent();
                    mFileFragment.mCurrentFolder = path;
                    loading = false;
                    mFileFragment.showLoadingDialog();
                    MediaController.getInstance().loadFolderDirs(mActivity.classGuid, path);
                }
                return;
            }
        }
        super.onBackPressed();
    }

}

