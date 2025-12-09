package com.multimediachat.ui;

import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.multimediachat.R;

import java.util.ArrayList;

public class HelpIndexFragment extends Fragment implements View.OnClickListener {

    View mFragmentView = null;
    WebActivity mActivity = null;
    ArrayList<Integer> mIds = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mFragmentView = inflater.inflate(R.layout.fragment_help_index, container, false);
        mActivity = (WebActivity) getActivity();

        mIds = new ArrayList<>();
        mIds.add(R.id.index_1);
        mIds.add(R.id.index_2);
        mIds.add(R.id.index_2_1);
        mIds.add(R.id.index_2_2);
        mIds.add(R.id.index_3);
        mIds.add(R.id.index_3_1);
        mIds.add(R.id.index_3_2);
        mIds.add(R.id.index_3_2_1);
        mIds.add(R.id.index_3_2_2);
        mIds.add(R.id.index_3_2_3);
        mIds.add(R.id.index_3_2_4);
        mIds.add(R.id.index_3_2_5);
        mIds.add(R.id.index_3_3);
        mIds.add(R.id.index_3_4);
        mIds.add(R.id.index_3_5);
        mIds.add(R.id.index_4);
        mIds.add(R.id.index_4_1);
        mIds.add(R.id.index_4_1_1);
        mIds.add(R.id.index_4_1_2);
        mIds.add(R.id.index_4_2);
        mIds.add(R.id.index_4_3);
        mIds.add(R.id.index_5);
        mIds.add(R.id.index_5_1);
        mIds.add(R.id.index_5_2);
        mIds.add(R.id.index_5_3);
        mIds.add(R.id.index_6);

        for (int i = 0; i < mIds.size(); ++i)
            mFragmentView.findViewById(mIds.get(i)).setOnClickListener(this);

        return mFragmentView;
    }

    @Override
    public void onClick(View view) {
        int pos = mIds.indexOf(view.getId());
        if (pos >= 0)
            mActivity.mTabsAdapter.setCurrentTab(pos+1);
    }
}
