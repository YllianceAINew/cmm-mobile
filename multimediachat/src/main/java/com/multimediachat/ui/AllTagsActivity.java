package com.multimediachat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.multimediachat.R;

import java.util.ArrayList;
import java.util.HashMap;

import com.multimediachat.app.DatabaseUtils;

/**
 * Created by jack on 12/8/2017.
 */

public class AllTagsActivity extends BaseActivity {
    private static final int REQUEST_TAG = 1;
    ListView mListView;
    ListViewAdapter mListAdapter;
    HashMap<String,ArrayList<String>> mAllTags;
    ArrayList<String> mAllTagNames;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initUI();
    }

    private void initUI()
    {
        setContentView(R.layout.listview_activity);
        setActionBarTitle(getString(R.string.all_tags));
        addImageButton(R.drawable.plus_outline_ellipse, R.id.btn_plus, POS_RIGHT);

        mAllTags = DatabaseUtils.getAllTags(cr);
        mAllTagNames = new ArrayList<String>(mAllTags.keySet());

        mListView = (ListView)findViewById(R.id.listview);
        mListAdapter = new ListViewAdapter();
        mListView.setAdapter(mListAdapter);
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId())
        {
            case R.id.btn_plus:
                intent = new Intent(AllTagsActivity.this, NewTagActivity.class);
                startActivityForResult(intent, REQUEST_TAG);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private class ListViewAdapter extends BaseAdapter
    {
        @Override
        public int getCount() {
            return mAllTagNames.size();
        }

        @Override
        public Object getItem(int position) {
            return mAllTagNames.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;

            if ( convertView == null )
            {
                convertView = AllTagsActivity.this.getLayoutInflater().inflate(R.layout.all_tags_list_item, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.txt_name = (TextView)convertView.findViewById(R.id.txt_name);
                viewHolder.txt_count = (TextView)convertView.findViewById(R.id.txt_count);
                convertView.setTag(viewHolder);
            }
            else
            {
                viewHolder = (ViewHolder)convertView.getTag();
            }

            final String tag =  mAllTagNames.get(position);
            final ArrayList<String> tagusers = mAllTags.get(tag);

            int count = tagusers != null ? tagusers.size() : 0;

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(AllTagsActivity.this, NewTagActivity.class);
                    intent.putExtra("isEdit", true);
                    intent.putExtra("tagname", tag);
                    intent.putExtra("tagusers", tagusers);
                    startActivityForResult(intent, REQUEST_TAG);
                }
            });

            viewHolder.txt_name.setText(tag);
            viewHolder.txt_count.setText(String.valueOf(count));

            return convertView;
        }

        private class ViewHolder
        {
            TextView txt_name;
            TextView txt_count;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ( requestCode == REQUEST_TAG )
        {
            if ( resultCode == RESULT_OK )
            {
                mAllTags = DatabaseUtils.getAllTags(cr);
                mAllTagNames = new ArrayList<String>(mAllTags.keySet());
                mListAdapter.notifyDataSetChanged();
            }
        }
    }
}