package com.multimediachat.ui.adapter.carouseltab;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.multimediachat.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class TabsAdapter extends FragmentPagerAdapter
        implements ViewPager.OnPageChangeListener {

    public interface OnPageChangeListener {

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        public void onPageSelected(int position);

        public void onPageScrollStateChanged(int state);
    }

    private TabsAdapter.OnPageChangeListener mOnPageChangeListener;

    public void setOnPageChangeListener(TabsAdapter.OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    private static final int TAB_INDICATOR_HEIGHT = 0; // dp
    private static final int PAGE_MARGIN_WIDTH = 8;    // px
    public static final String PREF_POS = "#pos:";
    public static final String PREF_FROZEN = "#frozen:";
    public static final String PREF_SHOW = "#show:";

    private static HashMap<String, TabsAdapter> mThis = new HashMap<String, TabsAdapter>();
    private String mTag;

    private final Context mContext;
    private final ViewPager mViewPager;
    private final TabBar mPagerTabStrip;
    private ArrayList<TabInfo> mTabsWholeTabs = new ArrayList<TabInfo>();
    private ArrayList<TabInfo> mTabs = new ArrayList<TabInfo>();

    private SharedPreferences mSharedPref;
    private boolean mInAddingFragments = false;
    private int mCurPos = 0;
    private View mLoadingContainer;

    public TabsAdapter(Activity activity, ViewPager pager, String tag) {
        super(activity.getFragmentManager());
        mTag = tag;
        mSharedPref = activity.getSharedPreferences("carousel_tabs_pref_" + tag, Context.MODE_PRIVATE);
        mContext = activity;
        mViewPager = pager;
        mViewPager.setAdapter(this);
        mViewPager.setOnPageChangeListener(this);
        mViewPager.setPageMargin(PAGE_MARGIN_WIDTH);
        int color = ThemeColor.getColor(ThemeColor.PAGE_MARGIN_COLOR);
        mViewPager.setPageMarginDrawable(new ColorDrawable(color));
        mPagerTabStrip = (TabBar) mViewPager.findViewById(R.id.pager_tab_strip);
        mThis.put(tag, this);
    }

    public static TabsAdapter getInstance(String tag) {
        return mThis.get(tag);
    }

    public String getTag() {
        return mTag;
    }


    public void showEmptyLoading(boolean bShow) {
        if (bShow)
            mLoadingContainer.setVisibility(View.VISIBLE);
        else
            mLoadingContainer.setVisibility(View.INVISIBLE);
    }

    public int getCurrentPosition() {
        return mCurPos;
    }

    public String getCurrentTabTag() {
        String tag = mTabs.get(mCurPos).tag.toString();
        return tag;
    }

    public TabInfo getTabInfoByTag(CharSequence tag) {
        for (int i = 0; i < mTabs.size(); i++) {
            TabInfo tabInfo = mTabs.get(i);
            if (tabInfo.tag == tag)
                return tabInfo;
        }
        return null;
    }

    public int getTabPos(CharSequence tag) {
        TabInfo tinfo = getTabInfoByTag(tag);
        if (tinfo != null)
            return tinfo.tabPref.nPos;
        else {
            String defValue = makePrefValue(new TabPref(0, false, true));
            String prefStr = mSharedPref.getString(tag.toString(), defValue);
            TabPref tabPref = parsePrefValue(prefStr);
            return tabPref.nPos;
        }
    }

    public boolean getTabShow(CharSequence tag) {
        TabInfo tinfo = getTabInfoByTag(tag);
        if (tinfo != null)
            return tinfo.tabPref.bShow;
        else
            return false;
    }

    public int getPositionByTag(CharSequence tag) {
        for (int i = 0; i < mTabs.size(); i++) {
            if (mTabs.get(i).tag.equals(tag)) {
                return i;
            }
        }
        return -1;
    }

    public String getTagByPosition(int position) {
        return mTabs.get(position).tag.toString();
    }

    public void setCurrentTab(int position) {
        mViewPager.setCurrentItem(position, false);
        mCurPos = position;

        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageSelected(position);
    }

    public void setTabStripOnLongClickListener(View.OnLongClickListener l) {
        mPagerTabStrip.setOnLongClickListener(l);
    }

    public void updateCurrentTab(int position) {
    }

    public static String makePrefValue(TabPref tp) {
        return (new String(PREF_POS + tp.nPos + PREF_FROZEN + (tp.bFrozen ? true : false) + PREF_SHOW + (tp.bShow ? true : false)));
    }

    public static TabPref parsePrefValue(String s) {
        int ind1, ind2;
        ind1 = s.indexOf(PREF_POS) + PREF_POS.length();
        ind2 = s.indexOf(PREF_FROZEN);
        int pos = Integer.parseInt(s.substring(ind1, ind2));

        ind1 = s.indexOf(PREF_FROZEN) + PREF_FROZEN.length();
        ind2 = s.indexOf(PREF_SHOW);
        boolean frozen = Boolean.parseBoolean(s.substring(ind1, ind2));

        ind1 = s.indexOf(PREF_SHOW) + PREF_SHOW.length();
        ind2 = s.length();
        boolean show = Boolean.parseBoolean(s.substring(ind1, ind2));

        return (new TabPref(pos, frozen, show));
    }

    private void sortByPreferencePos(ArrayList<TabInfo> alist) {
        Collections.sort(alist, new Comparator<TabInfo>() {
            public int compare(TabInfo ti1, TabInfo ti2) {
                int i = ti1.tabPref.nPos;
                int j = ti2.tabPref.nPos;
                if (i > j)
                    return 1;
                else if (i < j)
                    return -1;
                return 0;
            }
        });
    }

    public void setSharedPreference(String key, String value) {
        SharedPreferences.Editor editor = mSharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public ArrayList<TabInfo> getWholeTabs() {
        return mTabsWholeTabs;
    }

    public void startWholeTabs() {
        if (mInAddingFragments)
            return;
        mTabsWholeTabs.clear();
        mInAddingFragments = true;
    }

    public void finishWholeTabs() {
        if (!mInAddingFragments)
            return;
        mInAddingFragments = false;
        updateDataAndPrefFromWholeTabs();
    }

    public void updateDataAndPrefFromWholeTabs() {
        mTabs.clear();
        // modify position values.
        sortByPreferencePos(mTabsWholeTabs);
        for (int i = 0; i < mTabsWholeTabs.size(); i++) {
            TabInfo ti = mTabsWholeTabs.get(i);
            if (ti.tabPref.nPos != i) {
                ti.tabPref.nPos = i;
            }
            setSharedPreference(ti.tag.toString(), makePrefValue(ti.tabPref));
            // set real tab.
            if (ti.tabPref.bShow)
                mTabs.add(ti);
        }

        mViewPager.setAdapter(this);
        notifyDataSetChanged();
    }

    @SuppressWarnings("synthetic-access")
    public void addFragment(CharSequence fragmentTag, CharSequence tabLabel,
                            Class<? extends Fragment> className, Bundle args,
                            int pos, boolean frozen, int iconId) {

        addFragment(fragmentTag, tabLabel, className, args, pos, frozen, iconId, null);
    }

    public void addFragment(CharSequence fragmentTag, CharSequence tabLabel,
                            Class<? extends Fragment> className, Bundle args,
                            int pos, boolean frozen, int iconId, Fragment frag) {
        if (!mInAddingFragments)
            startWholeTabs();
        String defValue = makePrefValue(new TabPref(pos, frozen, true));
        String prefStr = mSharedPref.getString(fragmentTag.toString(), defValue);
        TabPref tabPref = parsePrefValue(prefStr);

        TabInfo tabInfo = new TabInfo(fragmentTag, tabLabel, iconId, className, args, tabPref, frag);
        mTabsWholeTabs.add(tabInfo);
    }

    @Override
    public Fragment getItem(int position) {
        TabInfo tabInfo = mTabs.get(position);
        if (tabInfo.obj != null)
            return tabInfo.obj;
        final Fragment frag = Fragment.instantiate(mContext,
                tabInfo.clss.getName(), tabInfo.args);
        return frag;
    }

    @Override
    public String getFragmentTag(int position) {
        return mTabs.get(position).tag.toString();
    }

    @Override
    public int getCount() {
        return mTabs.size();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }

    @Override
    public void onPageSelected(int position) {
        mViewPager.setCurrentItem(position);
        mCurPos = position;
        ((Activity) mContext).invalidateOptionsMenu();

        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageSelected(position);

        if (mPagerTabStrip != null) {
            if (mPagerTabStrip.mPageListener != null)
                mPagerTabStrip.mPageListener.onPageSelected(position);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            updateCurrentTab(mCurPos);
        }
        if (mOnPageChangeListener != null)
            mOnPageChangeListener.onPageScrollStateChanged(state);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTabs.get(position).tabLabel;
    }

    public int getPageIconId(int position) {
        return mTabs.get(position).tabIcon;
    }

    public TabBar getTabBar() {
        return mPagerTabStrip;
    }

    public View getTab(int position) {
        View view = null;
        if (position >= 0 && position < mPagerTabStrip.getTabCount()) {
            view = mPagerTabStrip.getTabItem(position);
        }
        return view;
    }
}
