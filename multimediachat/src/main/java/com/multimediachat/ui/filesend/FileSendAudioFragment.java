package com.multimediachat.ui.filesend;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.ImApp;
import com.multimediachat.ui.FileSendTabActivity;

import java.io.File;

@SuppressLint("DefaultLocale")
public class FileSendAudioFragment extends Fragment {

    View fragment_view;
    Context mContext;
    FileSendTabActivity mActivity;
    ImApp mApp;
    ContentResolver cr;
    Resources r;

    private RecyclerView listView;
    private AudioListAdapter listAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.fragement_audio, container, false);
        mContext = getActivity();
        mActivity = (FileSendTabActivity) getActivity();
        mApp = (ImApp) getActivity().getApplication();
        cr = getActivity().getContentResolver();
        r = getActivity().getResources();

        initUI();

        return fragment_view;
    }

    private void initUI() {
        listView = fragment_view.findViewById(R.id.media_view);
        listView.setLayoutManager(new LinearLayoutManager(mContext));
        listAdapter = new AudioListAdapter(mContext, mActivity.mAudioCursor);
        listView.setAdapter(listAdapter);
    }

    public void updateView() {
        if (listAdapter != null) {
            listAdapter.changeCursor(mActivity.mAudioCursor);
            listAdapter.notifyDataSetChanged();
        }
    }

    public AudioListAdapter getAdapter() {
        return listAdapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.changeCursor(mActivity.mAudioCursor);
            listAdapter.notifyDataSetChanged();
        }
    }

    private class AudioListAdapter extends CursorRecyclerViewAdapter<AudioListAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            TextView fileName;
            ImageView checkImageView;

            public MyViewHolder(View view) {
                super(view);
                imageView = (ImageView) view.findViewById(R.id.icon);
                imageView.setImageResource(R.drawable.noaudios);
                fileName = (TextView) view.findViewById(R.id.text);
                checkImageView = view.findViewById(R.id.photo_check);
            }
        }

        public AudioListAdapter(Context mContext, Cursor audioCursor) {
            super(mContext, audioCursor);
            setHasStableIds(true);
            notifyDataSetChanged();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.picker_list_item_layout, parent, false);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Cursor cursor = getCursor();
                    int position = listView.getChildLayoutPosition(view);
                    Boolean state = mActivity.onAlbumSelect(position);
                    ImageView checkImageView = view.findViewById(R.id.photo_check);
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
            return new AudioListAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder viewHolder, Cursor cursor) {
            int dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            String path = cursor.getString(dataColumn);
            viewHolder.fileName.setText(new File(path).getName());
            if (mActivity.mCheckedFiles.indexOf(path) >= 0) {
                viewHolder.checkImageView.setVisibility(View.VISIBLE);
            } else {
                viewHolder.checkImageView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
