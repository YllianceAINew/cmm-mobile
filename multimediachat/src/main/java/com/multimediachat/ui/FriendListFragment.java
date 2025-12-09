package com.multimediachat.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.ImApp;
import com.multimediachat.ui.sideindexers.IndexableListView;
import com.multimediachat.ui.views.FriendListFilterView;
import com.multimediachat.ui.views.FriendListFilterView.ContactListListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class FriendListFragment extends Fragment implements ContactListListener, View.OnClickListener {
	static int FRIEND_LIST_LOADER_ID = 4470;
	FriendListFilterView mFilterView = null;
	View fragmentView = null;
	ImApp mApp;
	Resources r;

	private EditText searchEditText;

	private TextView mTxtSelTag = null;
	private RecyclerView mListTags = null;
	private TagListAdapter mListTagsAdapter = null;
	HashMap<String, ArrayList<String>> mAllTags;
	ArrayList<String> mAllTagNames;

	View lyt_search;
	Animation slideInTopToBottom, slideOutBottomToTop;

	public static int mListTouchPosition[] = new int[2];

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mApp = (ImApp) getActivity().getApplication();
		fragmentView = inflater.inflate(R.layout.friend_list_fragment, container, false);

		r = getResources();

		mFilterView = fragmentView.findViewById(R.id.contactFilterView);

		mTxtSelTag = fragmentView.findViewById(R.id.txt_selected_tag);

		mAllTags = new HashMap<>();
		mAllTags.putAll(DatabaseUtils.getAllTags(getContext().getContentResolver()));
		mAllTagNames = new ArrayList<String>(mAllTags.keySet());
		mAllTagNames.add(0, getString(R.string.tags_list_all));

		mListTags = fragmentView.findViewById(R.id.list_tags);
		mListTags.setLayoutManager(new LinearLayoutManager(getContext()));
		mListTagsAdapter = new TagListAdapter();
		mListTags.setAdapter(mListTagsAdapter);

		mFilterView.setMode(0);
		mFilterView.setListener(this);
		mFilterView.setLoaderManager(getLoaderManager(), FRIEND_LIST_LOADER_ID);

		mFilterView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(getActivity().findViewById(R.id.edit_search_bar).getWindowToken(), 0);
				/*if ( lyt_search.getVisibility()==View.VISIBLE ) {
					showHideSearch();
				}*/
				return false;
			}
		});

		lyt_search = fragmentView.findViewById(R.id.lyt_search);
		slideInTopToBottom = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_top_to_bottom);
		slideOutBottomToTop = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_bottom_to_top);
		slideInTopToBottom.setDuration(200);
		slideOutBottomToTop.setDuration(200);

		searchEditText = (EditText)fragmentView.findViewById(R.id.txtSearch);
		showFriendList();
		searchEditText.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

			}

			@Override
			public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
				mFilterView.showList(searchEditText.getText().toString());
			}

			@Override
			public void afterTextChanged(Editable editable) {

			}
		});
		fragmentView.findViewById(R.id.btnClose).setOnClickListener(this);

		fragmentView.findViewById(R.id.lyt_tags).setOnClickListener(this);
		fragmentView.findViewById(R.id.lyt_tags_list).setOnClickListener(this);

		View emptyLayout = fragmentView.findViewById(R.id.emptyLayout);

		IndexableListView indexableListView = mFilterView.findViewById(R.id.filteredList);
		indexableListView.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				mListTouchPosition[0] = (int)event.getX();
				mListTouchPosition[1] = (int)event.getY();
				/*if ( lyt_search.getVisibility()==View.VISIBLE ) {
					showHideSearch();
				}
				if ( mListTags.getVisibility()==View.VISIBLE ) {
					showHideTagsList();
				}*/
				return false;
			}
		});

		((AbsListView) mFilterView.findViewById(R.id.filteredList)).setEmptyView(emptyLayout);

		return fragmentView;
	}

	@Override
	public void onResume() {

		if (mAllTags == null)
			mAllTags = new HashMap<>();
		else
			mAllTags.clear();

		mAllTags.putAll(DatabaseUtils.getAllTags(getContext().getContentResolver()));

		mAllTagNames.clear();
		mAllTagNames = new ArrayList<String>(mAllTags.keySet());
		Collections.sort(mAllTagNames, new Comparator<String>() {
			@Override
			public int compare(String s, String t1) {
				return s.compareTo(t1);
			}
		});
		mAllTagNames.add(0, getString(R.string.tags_list_all));

		mListTagsAdapter.notifyDataSetChanged();

		if (mAllTagNames.indexOf(mTxtSelTag.getText().toString()) < 0) {
			mTxtSelTag.setText(getString(R.string.tags_list_all));
			mFilterView.filterListByTag(null);
		}

		super.onResume();
	}

	@Override
	public void setTitle(String aTxt) {
	}


	public void showFriendList() {
		if (mFilterView != null)
			mFilterView.showList(searchEditText.getText().toString());
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
			case R.id.btnClose:
				searchEditText.setText("");
				break;
			case R.id.lyt_tags:
				Intent intent = new Intent(getActivity(), AllTagsActivity.class);
				startActivity(intent);
				break;
			case R.id.lyt_tags_list:
				showHideTagsList();
				break;
		}
	}

	public boolean showHideTagsList()
	{
		if ( (slideOutBottomToTop.hasStarted() && !slideOutBottomToTop.hasEnded()) || (slideInTopToBottom.hasStarted() && !slideInTopToBottom.hasEnded()) )
			return false;

		if ( mListTags.getVisibility()==View.VISIBLE )
		{
			mListTags.startAnimation(slideOutBottomToTop);
			slideOutBottomToTop.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					mListTags.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}
			});
		}
		else
		{
			mListTags.startAnimation(slideInTopToBottom);
			mListTags.setVisibility(View.VISIBLE);
		}
		return true;
	}

	public void showHideSearch()
	{
		if ( (slideOutBottomToTop.hasStarted() && !slideOutBottomToTop.hasEnded()) || (slideInTopToBottom.hasStarted() && !slideInTopToBottom.hasEnded()) )
			return;

		if ( lyt_search.getVisibility()==View.VISIBLE )
		{
			View view = getActivity().getCurrentFocus();
			if (view != null) {
				InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
			}

			lyt_search.startAnimation(slideOutBottomToTop);
			slideOutBottomToTop.setAnimationListener(new Animation.AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					lyt_search.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}
			});
		}
		else
		{
			lyt_search.startAnimation(slideInTopToBottom);
			lyt_search.setVisibility(View.VISIBLE);
			searchEditText.requestFocus();
			AndroidUtility.showKeyboard(searchEditText);
		}
	}

	private class TagListAdapter extends RecyclerView.Adapter<TagListAdapter.TagHolder> {

		@NonNull
		@Override
		public TagHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
			View itemView = LayoutInflater.from(viewGroup.getContext())
					.inflate(R.layout.tags_list_item, viewGroup, false);

			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (showHideTagsList()) {
						int pos = mListTags.getChildLayoutPosition(view);
						String strTag = mAllTagNames.get(pos);
						mTxtSelTag.setText(strTag);
						if (strTag.equals(getString(R.string.tags_list_all))) {
							mFilterView.filterListByTag(null);
						} else {
							mFilterView.filterListByTag(strTag);
						}
					}
				}
			});

			return new TagHolder(itemView);
		}

		@Override
		public void onBindViewHolder(@NonNull TagHolder tagHolder, int i) {
			tagHolder.mTxtTagName.setText(mAllTagNames.get(i));
		}

		@Override
		public int getItemCount() {
			return mAllTagNames.size();
		}

		class TagHolder extends RecyclerView.ViewHolder {
			TextView mTxtTagName = null;

			public TagHolder(@NonNull View itemView) {
				super(itemView);
				mTxtTagName = itemView.findViewById(R.id.txt_name);
			}
		}
	}
}