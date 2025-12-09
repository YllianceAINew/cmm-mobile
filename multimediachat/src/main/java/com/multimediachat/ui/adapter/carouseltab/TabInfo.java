package com.multimediachat.ui.adapter.carouseltab;

import android.app.Fragment;
import android.os.Bundle;

public class TabInfo {
	public final CharSequence tag;
    public final CharSequence tabLabel;
    public final Class<? extends Fragment> clss;
    public final Bundle args;
    public TabPref tabPref;
    public final Fragment obj;
    public final int tabIcon;

    public TabInfo(CharSequence _tag, CharSequence _tabLabel, int _tabIcon, Class<? extends Fragment> _class, Bundle _args,
                   TabPref _tabPref) {
    	tag = _tag;
    	tabLabel = _tabLabel;
        tabIcon = _tabIcon;
        clss = _class;
        args = _args;
        tabPref = _tabPref;
        obj = null;
    }
    public TabInfo(CharSequence _tag, CharSequence _tabLabel, int _tabIcon, Class<? extends Fragment> _class, Bundle _args,
                   TabPref _tabPref, Fragment _obj) {
    	tag = _tag;
    	tabLabel = _tabLabel;
        tabIcon = _tabIcon;
        clss = _class;
        args = _args;
        tabPref = _tabPref;
        obj = _obj;
    }
}
