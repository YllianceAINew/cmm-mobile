package com.multimediachat.ui;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.multimediachat.ui.adapter.carouseltab.TabsAdapter;
import com.multimediachat.ui.adapter.carouseltab.ViewPager;
import com.multimediachat.R;

import java.lang.reflect.Array;
import java.util.ArrayList;


@SuppressLint("SetJavaScriptEnabled")
public class WebActivity extends BaseActivity implements OnClickListener {

	ViewPager mViewPager = null;
	TabsAdapter mTabsAdapter = null;

	TextView mTxtPageTitle = null;

	ArrayList<String> mArrayFileName = new ArrayList<>();

	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.web_layout);

		setActionBarTitle(getString(R.string.help));

		mTxtPageTitle = findViewById(R.id.page_title);

		mViewPager = findViewById(R.id.view_pager);
		mTabsAdapter = new TabsAdapter(this, mViewPager, "Help");
		mTabsAdapter.startWholeTabs();

		mArrayFileName.add("1.htm");
		mArrayFileName.add("2.htm");
		mArrayFileName.add("2.1.htm");
		mArrayFileName.add("2.2.htm");
		mArrayFileName.add("3.htm");
		mArrayFileName.add("3.1.htm");
		mArrayFileName.add("3.2.htm");
		mArrayFileName.add("3.2.1.htm");
		mArrayFileName.add("3.2.2.htm");
		mArrayFileName.add("3.2.3.htm");
		mArrayFileName.add("3.2.4.htm");
		mArrayFileName.add("3.2.5.htm");
        mArrayFileName.add("3.3.htm");
		mArrayFileName.add("3.4.htm");
		mArrayFileName.add("3.5.htm");
		mArrayFileName.add("4.htm");
		mArrayFileName.add("4.1.htm");
		mArrayFileName.add("4.1.1.htm");
		mArrayFileName.add("4.1.2.htm");
		mArrayFileName.add("4.2.htm");
		mArrayFileName.add("4.3.htm");
		mArrayFileName.add("5.htm");
		mArrayFileName.add("5.1.htm");
		mArrayFileName.add("5.2.htm");
		mArrayFileName.add("5.3.htm");
		mArrayFileName.add("6.htm");

		mTabsAdapter.addFragment("index", "index", HelpIndexFragment.class, null, 0, true, -1);

		for (int i = 0; i < mArrayFileName.size(); ++i) {
			String strFilename = mArrayFileName.get(i);
			Bundle argBundle = new Bundle();
			argBundle.putString("filename", strFilename);
			mTabsAdapter.addFragment(strFilename, strFilename, WebViewFragment.class, argBundle, i+1, true, -1);
		}

		mTabsAdapter.finishWholeTabs();
		mViewPager.setOffscreenPageLimit(1);

		mTabsAdapter.setOnPageChangeListener(new TabsAdapter.OnPageChangeListener() {
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageSelected(int position) {
				int[] titleIds = {R.string.help_index,
						R.string.help_index_1,
						R.string.help_index_2,
						R.string.help_index_2_1,
						R.string.help_index_2_2,
						R.string.help_index_3,
						R.string.help_index_3_1,
						R.string.help_index_3_2,
						R.string.help_index_3_2_1,
						R.string.help_index_3_2_2,
						R.string.help_index_3_2_3,
						R.string.help_index_3_2_4,
						R.string.help_index_3_2_5,
						R.string.help_index_3_3,
						R.string.help_index_3_4,
						R.string.help_index_3_5,
						R.string.help_index_4,
						R.string.help_index_4_1,
						R.string.help_index_4_1_1,
						R.string.help_index_4_1_2,
						R.string.help_index_4_2,
						R.string.help_index_4_3,
						R.string.help_index_5,
						R.string.help_index_5_1,
						R.string.help_index_5_2,
						R.string.help_index_5_3,
						R.string.help_index_6};
				mTxtPageTitle.setText(titleIds[position]);
			}

			@Override
			public void onPageScrollStateChanged(int state) {

			}
		});
	}

	@Override
	public void onBackPressed() {
		if (mTabsAdapter.getCurrentPosition() > 0) {
			mTabsAdapter.setCurrentTab(0);
			return;
		}
		super.onBackPressed();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		default:
			super.onClick(v);
			break;
		}
	}
}
