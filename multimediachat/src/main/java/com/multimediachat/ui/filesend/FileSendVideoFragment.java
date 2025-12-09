package com.multimediachat.ui.filesend;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.ImApp;
import com.multimediachat.ui.FileSendTabActivity;
import com.multimediachat.util.Utilities;

@SuppressLint("DefaultLocale")
public class FileSendVideoFragment extends Fragment {

    View fragment_view;
    Context mContext;
    FileSendTabActivity mActivity;
    ImApp mApp;
    ContentResolver cr;
    Resources r;

    public static int itemWidth = 100;

    private RecyclerView listView;
    private static VideoListAdapter listAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.fragement_video, container, false);
        mContext = getActivity();
        mActivity = (FileSendTabActivity) getActivity();
        mApp = (ImApp) getActivity().getApplication();
        cr = getActivity().getContentResolver();
        r = getActivity().getResources();

        initUI();

        return fragment_view;
    }

    private void initUI() {
        itemWidth = (Utilities.getScreenWidth(mContext) - AndroidUtility.dp(4)) / 3;
        listView = fragment_view.findViewById(R.id.media_view);
        listView.setLayoutManager(new GridLayoutManager(mContext, 3));
        listAdapter = new VideoListAdapter(mContext, mActivity.mVideoCursor);
        listView.setAdapter(listAdapter);
    }

    public void updateView() {
        if (listAdapter != null) {
            listAdapter.changeCursor(mActivity.mVideoCursor);
            listAdapter.notifyDataSetChanged();
        }
    }

    public VideoListAdapter getAdapter() {
        return listAdapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class VideoListAdapter extends CursorRecyclerViewAdapter<VideoListAdapter.MyViewHolder> {
        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            View checkImageView;

            public MyViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.media_photo_image);
                checkImageView = view.findViewById(R.id.photo_check_frame);
            }
        }

        public VideoListAdapter(Context mContext, Cursor videoCursor) {
            super(mContext, videoCursor);
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
                    Cursor cursor = getCursor();
                    int position = listView.getChildLayoutPosition(view);
                    Boolean state = mActivity.onAlbumSelect(position);
                    View checkImageView = view.findViewById(R.id.photo_check_frame);
                    if (state) {
                        if (cursor.moveToPosition(position)) {
                            int dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
                            String path = cursor.getString(dataColumn);
                            if (mActivity.mCheckedFiles.indexOf(path) >= 0) {
                                checkImageView.setVisibility(View.VISIBLE);
                            } else {
                                checkImageView.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
            });
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder viewHolder, Cursor cursor) {
            int dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            String path = cursor.getString(dataColumn);
            Glide.with(mContext).setDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).format(DecodeFormat.PREFER_RGB_565).placeholder(R.drawable.novideos_centercrop).error(R.drawable.novideos_centercrop)).load(path).into(viewHolder.imageView);
            if (mActivity.mCheckedFiles.indexOf(path) >= 0) {
                viewHolder.checkImageView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.checkImageView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
