package com.multimediachat.ui.filesend;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.multimediachat.R;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.MediaController;
import com.multimediachat.ui.FileSendTabActivity;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


@SuppressLint("DefaultLocale")
public class FileManagerFragment extends Fragment {
    View fragment_view;
    Context mContext;
    FileSendTabActivity mActivity;
    ImApp mApp;
    ContentResolver cr;
    Resources r;

    private View progressView;
    private RecyclerView fileListView;

    private FileListAdapter listAdapter = null;
    /*start file browser*/
    public String mCurrentFolder = null;
    public int mCurrentStorage = -1;

    private View parentView;
    private ImageView parentArrow;
    private RecyclerView parentList;
    private FoldersArrayAdapter parentAdpater;
    private ArrayList<String> parentFolders = new ArrayList<String>();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragment_view = inflater.inflate(R.layout.fragement_file, container, false);
        mContext = getActivity();
        mActivity = (FileSendTabActivity) getActivity();
        mApp = (ImApp) getActivity().getApplication();
        cr = getActivity().getContentResolver();
        r = getActivity().getResources();

        initUI();

        return fragment_view;
    }

    private void initUI() {
        progressView = fragment_view.findViewById(R.id.progressLayout);

        fileListView = fragment_view.findViewById(R.id.file_view);
        fileListView.setLayoutManager(new LinearLayoutManager(mContext));
        listAdapter = new FileListAdapter(mContext, mActivity.fileInfos);
        fileListView.setAdapter(listAdapter);
        fileListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (parentList.getVisibility() == View.VISIBLE) {
                    parentList.setVisibility(View.GONE);
                    parentArrow.setImageResource(R.drawable.spinner_arrow_down);
                }
                return false;
            }
        });

        parentView = fragment_view.findViewById(R.id.parent_view);
        parentArrow = fragment_view.findViewById(R.id.parent_view_arrow);
        fragment_view.findViewById(R.id.parent_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (parentFolders.size() == 1)
                    return;
                if (parentList.getVisibility() == View.VISIBLE) {
                    parentList.setVisibility(View.GONE);
                    parentArrow.setImageResource(R.drawable.spinner_arrow_down);
                } else {
                    parentAdpater.notifyDataSetChanged();
                    parentList.setVisibility(View.VISIBLE);
                    parentArrow.setImageResource(R.drawable.spinner_arrow_up);
                }
            }
        });
        parentList = fragment_view.findViewById(R.id.parent_list);
        parentList.setLayoutManager(new LinearLayoutManager(mContext));
        parentAdpater = new FoldersArrayAdapter(mContext, null);
        parentList.setAdapter(parentAdpater);
    }

    public void showLoadingDialog() {
        progressView.setVisibility(View.VISIBLE);
    }

    public void hideLoadingDialog() {
        progressView.setVisibility(View.GONE);
    }

    public void setParentPath(String strPath) {
        parentFolders.clear();
        parentFolders.add(getString(R.string.file_manager_main));
        if (mCurrentStorage >= 0 && !strPath.equals("/")) {
            FileInfo storage = mActivity.storageInfos.get(mCurrentStorage);
            parentFolders.add(storage.fileName);
            if (strPath.length() > storage.absPath.length()) {
                String path = strPath.substring(storage.absPath.length() + 1);
                if (!path.equals("")) {
                    String[] splits = path.split("/");
                    Collections.addAll(parentFolders, splits);
                }
            }
        }
        TextView txtView = fragment_view.findViewById(R.id.text_name);
        txtView.setText(parentFolders.get(parentFolders.size() - 1));
        if (parentFolders.size() == 1)
            parentArrow.setVisibility(View.GONE);
        else
            parentArrow.setVisibility(View.VISIBLE);
    }

    public void viewFolderList() {
        if (listAdapter != null) {
            setParentPath(mCurrentFolder);
            listAdapter.userList = mActivity.fileInfos;
            listAdapter.notifyDataSetChanged();
            parentAdpater.list = parentFolders;
            parentAdpater.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class FileListAdapter extends RecyclerView.Adapter<FileListAdapter.MyViewHolder> {
        private List<FileInfo> userList;
        private final Context mContext;

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView fileName, fileDate;
            ImageView imageView, checkImageView;

            MyViewHolder(View view) {
                super(view);
                fileName = view.findViewById(R.id.file_name);
                fileDate = view.findViewById(R.id.file_date);
                imageView = view.findViewById(R.id.icon);
                checkImageView = view.findViewById(R.id.photo_check);
            }
        }

        FileListAdapter(Context mContext, List<FileInfo> list) {
            this.mContext = mContext;
            this.userList = list;
            notifyDataSetChanged();
        }

        @Override
        public FileListAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.folder_list_item_layout, parent, false);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = fileListView.getChildLayoutPosition(view);
                    FileInfo selItem = userList.get(position);
                    if (selItem.storageType != 2)
                        mCurrentStorage = selItem.storageId;
                    File file = new File(selItem.absPath);
                    if (file.isDirectory()) {
                        mCurrentFolder = selItem.absPath;
                        MediaController.getInstance().loadFolderDirs(mActivity.classGuid, mCurrentFolder);
                        showLoadingDialog();
                    } else {
                        Boolean state = mActivity.onAlbumSelect(position);
                        ImageView checkImageView = view.findViewById(R.id.photo_check);
                        if (state) {
                            if (mActivity.mCheckedFiles.indexOf(mActivity.fileInfos.get(position).absPath) >= 0) {
                                checkImageView.setVisibility(View.VISIBLE);
                            } else {
                                checkImageView.setVisibility(View.INVISIBLE);
                            }
                        }
                    }
                }
            });
            return new FileListAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(FileListAdapter.MyViewHolder holder, int position) {
            FileInfo info = userList.get(position);

            if (info.absPath != null) {
                holder.fileName.setText(info.fileName);
                holder.fileName.setSelected(true);
                if (info.storageType == 0) {
                    holder.imageView.setImageResource(R.drawable.home_sdcard);
                    holder.checkImageView.setVisibility(View.INVISIBLE);
                    holder.fileDate.setText(info.lastModified);
                } else if (info.storageType == 1) {
                    holder.imageView.setImageResource(R.drawable.home_phone);
                    holder.checkImageView.setVisibility(View.INVISIBLE);
                    holder.fileDate.setText(info.lastModified);
                } else if (info.isFolder) {
                    holder.imageView.setImageResource(R.drawable.ic_launcher_folder);
                    holder.fileDate.setVisibility(View.VISIBLE);
                    holder.fileDate.setText(info.lastModified);
                    holder.fileDate.setSelected(true);
                    holder.checkImageView.setVisibility(View.INVISIBLE);
                } else {
                    holder.fileDate.setText(info.lastModified + "  " + info.length);

                    try {
                        if (info.fileType == 0) {
                            holder.imageView.setImageResource(R.drawable.noaudios);
                        } else if (info.fileType == 1) {
                            Glide.with(mContext).setDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).format(DecodeFormat.PREFER_RGB_565).placeholder(R.drawable.novideos_centercrop).error(R.drawable.novideos_centercrop)).load(info.absPath).into(holder.imageView);
                        } else if (info.fileType == 2) {
                            Glide.with(mContext).setDefaultRequestOptions(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.NONE).format(DecodeFormat.PREFER_RGB_565).placeholder(R.drawable.nophotos_centercrop).error(R.drawable.nophotos_centercrop)).load(info.absPath).into(holder.imageView);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (mActivity.mCheckedFiles.contains(info.absPath)) {
                        holder.checkImageView.setVisibility(View.VISIBLE);
                    } else {
                        holder.checkImageView.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }

        @Override
        public int getItemCount() {
            if (userList != null)
                return userList.size();
            return 0;
        }
    }

    private class FoldersArrayAdapter extends RecyclerView.Adapter<FoldersArrayAdapter.MyViewHolder> {
        private ArrayList<String> list;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView text_name;
            TextView text_padding;
            ImageView image_folder;

            public MyViewHolder(View view) {
                super(view);
                image_folder = (ImageView) view.findViewById(R.id.image_folder);
                text_name = (TextView) view.findViewById(R.id.text_name);
                text_padding = (TextView) view.findViewById(R.id.text_padding);
            }
        }

        public FoldersArrayAdapter(Context mContext, ArrayList<String> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.folder_item, parent, false);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parentList.setVisibility(View.GONE);
                    parentArrow.setImageResource(R.drawable.spinner_arrow_down);
                    int position = fileListView.getChildLayoutPosition(view);
                    if (position == 0) {
                        mActivity.fileInfos = mActivity.storageInfos;
                        mCurrentFolder = "/";
                        viewFolderList();
                    } else if (position == 1) {
                        FileInfo storage = mActivity.storageInfos.get(mCurrentStorage);
                        mCurrentFolder = storage.absPath;
                        MediaController.getInstance().loadFolderDirs(mActivity.classGuid, mCurrentFolder);
                    } else {
                        FileInfo storage = mActivity.storageInfos.get(mCurrentStorage);
                        String path = storage.absPath + "/";
                        for (int i = 2; i <= position; i++)
                            path += list.get(i) + "/";
                        mActivity.loading = false;
                        showLoadingDialog();
                        mCurrentFolder = path;
                        MediaController.getInstance().loadFolderDirs(mActivity.classGuid, mCurrentFolder);
                    }
                }
            });
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            if (position == 0)
                holder.image_folder.setImageResource(R.drawable.ic_home);
            else if (position == 1) {
                if (mActivity.storageInfos.get(mCurrentStorage).storageType == 0)
                    holder.image_folder.setImageResource(R.drawable.nav_sdcard);
                else
                    holder.image_folder.setImageResource(R.drawable.nav_phone);
            } else
                holder.image_folder.setImageResource(R.drawable.dropdown_icon_folder);
            holder.text_name.setText(list.get(position));
            holder.text_name.setSelected(true);
            String strPad = "";
            for (int i = 0; i < position; i++)
                strPad += " ";
            holder.text_padding.setText(strPad);
        }

        @Override
        public int getItemCount() {
            if (list != null)
                return list.size() - 1;
            return 0;
        }
    }

    public static String formatDate(Date date) {
        DateFormat dateformat = new SimpleDateFormat("yyyy/MM/dd hh:mm");
        return dateformat.format(date);
    }

}
