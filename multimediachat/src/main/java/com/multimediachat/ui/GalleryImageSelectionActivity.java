/*
 * added by JHK(2019.11.07)
 * select images from Gallery in order to setting profile image
 */

package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.app.Dialog;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.MediaController;

import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.filesend.CursorRecyclerViewAdapter;
import com.multimediachat.ui.filesend.FileInfo;
import com.multimediachat.util.Utilities;
import java.io.File;
import java.util.ArrayList;

@SuppressLint("DefaultLocale")
public class GalleryImageSelectionActivity extends BaseActivity {
    Context context;
    public static Cursor mImageCursor = null;
    private final int ERROR_FILE_NO_EXIST = 102;
    private static int REQUEST_CHOOSE_GALLERY_IMAGE = 1;
    public static int itemWidth = 100;
    private RecyclerView listView;
    public ImageListAdapter listAdapter;
    public ArrayList<FileInfo> storageInfos = new ArrayList<FileInfo>();


    private static final String[] projectionFiles = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.DATE_ADDED,
            MediaStore.Files.FileColumns.MEDIA_TYPE,
            MediaStore.Files.FileColumns.MIME_TYPE,
            MediaStore.Files.FileColumns.TITLE
    };
    private String userAddr = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context = this;
        setContentView(R.layout.fragement_image);
        setActionBarTitle(getString(R.string.choose_profilephoto));

        loadGalleryAlbums(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
        loadPhoneAndExternalStorage();
        initUI();
        Intent i  = getIntent();
        userAddr = i.getStringExtra("userAddress");
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
    private void initUI() {

        itemWidth = (Utilities.getScreenWidth(this) - AndroidUtility.dp(4)) / 3;
        listView = findViewById(R.id.media_view);
        listView.setLayoutManager(new GridLayoutManager(this, 3));
        listAdapter = new ImageListAdapter(this, mImageCursor);
        listView.setAdapter(listAdapter);

    }

    public void updateView() {
        if (listAdapter != null) {
            listAdapter.changeCursor(mImageCursor);
            listAdapter.notifyDataSetChanged();
        }
    }

    public ImageListAdapter getAdapter() {
        return listAdapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ImageListAdapter extends CursorRecyclerViewAdapter<ImageListAdapter.MyViewHolder> {
        class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ImageView photoCheck;

            MyViewHolder(View view) {
                super(view);
                imageView = view.findViewById(R.id.media_photo_image);
                photoCheck = view.findViewById(R.id.photo_check);
                photoCheck.setVisibility(View.GONE);
            }
        }

        ImageListAdapter(Context context, Cursor audioCursor) {
            super(context, audioCursor);
            setHasStableIds(true);
            notifyDataSetChanged();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.file_send_album_layout, parent, false);
            itemView.setLayoutParams(new GridView.LayoutParams(itemWidth, itemWidth));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = listView.getChildLayoutPosition(view);
                    MediaController.PhotoEntry selectedPhoto = getPhotoEntry(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, position);
                    File file = new File(selectedPhoto.path);
                    if (!file.exists()) {
                        showErrorDialog(ERROR_FILE_NO_EXIST);
                        selectedPhoto = null;
                    }
                    if (selectedPhoto == null)
                        return;

                    String selectedPhotoPath = selectedPhoto.path;
                    Intent intent = new Intent(context, GalleryImageOpenActivity.class);
                    intent.putExtra("samplePath", selectedPhotoPath);
                    intent.putExtra("userAddr", userAddr);
                    startActivityForResult(intent, REQUEST_CHOOSE_GALLERY_IMAGE);
                }
            });
            return new MyViewHolder(itemView);
        }

        MediaController.PhotoEntry getPhotoEntry(int mediaType, int pos) {
            int imageId = 0;
            String path = "";
            long dateTaken = 0;
            MediaController.PhotoEntry photoEntry = null;

            if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE) {
                mImageCursor.moveToPosition(pos);
                int dataColumn = mImageCursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                path = mImageCursor.getString(dataColumn);
                photoEntry = new MediaController.PhotoEntry(imageId, imageId, dateTaken, path, 0, MediaController.MediaType.PHOTO);
            }
            return photoEntry;
        }

        private void showErrorDialog(int res) {
            // custom dialog
            final Dialog dlg = GlobalFunc.createDialog(context, R.layout.msgdialog, true);

            TextView title = dlg.findViewById(R.id.msgtitle);
            title.setText(R.string.information);

            TextView content = dlg.findViewById(R.id.msgcontent);

           if (res == ERROR_FILE_NO_EXIST)
                content.setText(R.string.error_file_no_exist);

            Button dlg_btn_ok = dlg.findViewById(R.id.btn_ok);
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

            if (context != null) {
                dlg.show();
            }
        }

        @Override
        public void onBindViewHolder(MyViewHolder viewHolder, Cursor cursor) {
            int dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            String path = cursor.getString(dataColumn);
            Glide.with(context).setDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).format(DecodeFormat.PREFER_RGB_565).placeholder(R.drawable.nophotos_centercrop).error(R.drawable.nophotos_centercrop)).load(path).into(viewHolder.imageView);
        }
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
            } else {
                if (mImageCursor != null) {
                    mImageCursor.close();
                    mImageCursor = null;
                }
                String selection = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
                Uri queryUri = MediaStore.Files.getContentUri("external");
                mImageCursor = ImApp.applicationContext.getContentResolver().query(queryUri, projectionFiles, selection, null, MediaStore.Files.FileColumns.DATE_ADDED + " DESC");
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mImageCursor != null) {
            mImageCursor.close();
            mImageCursor = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHOOSE_GALLERY_IMAGE)
            finish();
    }
}
