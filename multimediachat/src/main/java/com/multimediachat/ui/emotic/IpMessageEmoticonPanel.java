package com.multimediachat.ui.emotic;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.multimediachat.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class IpMessageEmoticonPanel extends LinearLayout {

    private Handler mHandler;
    private Context mContext;
    private ViewPager mScrollLayout;
    private ViewPagerAdapter viewPagerAdapter;
    private Button mDelEmoticon;
    private Button mSpace;
    private Button mEnter;
    private Button mDelEmoticonPlus;
    private Button mSpacePlus;
    private Button mEnterPlus;

    private int mOrientation = 0;
    private EditEmoticonListener mListener;

    private int[] mColumnArray;

    private DelEmoticonThread mDelEmoticonThread;
    private Object mObject = new Object();
    private boolean mNeedQuickDelete = false;

    private static int emoticPagerPosition;

    public IpMessageEmoticonPanel(Context context) {
        super(context);
        mContext = context;
    }

    public IpMessageEmoticonPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        try {
            PackageManager pm = getContext().getPackageManager();
            pm.getPackageInfo("com.android.calendar", PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
        }

        mColumnArray = getResources().getIntArray(R.array.share_column);

        mScrollLayout = (ViewPager) findViewById(R.id.emoticon_panel_zone);
        mTemplateTab = (RadioButton) findViewById(R.id.smiley_panel_template_btn);
        mDefaultTab = (RadioButton) findViewById(R.id.smiley_panel_default_btn);
        mDefaultplusTab = (RadioButton) findViewById(R.id.smiley_panel_defaultplus_btn);
        mDefaultName = getResources().getStringArray(R.array.default_emoticon_name);
        int[] source = getResources().getIntArray(R.array.defaultplus_emoticon_name);
        mDefaultplusName = new String[source.length];
        for (int i = 0; i < source.length; i++)
            mDefaultplusName[i] = new String(new int[]{source[i]}, 0, 1);
        OnClickListener panelClickListener = new OnClickListener() {
            public void onClick(View v) {
                int clickedId = v.getId();
                if (R.id.template_panel == clickedId) {
                    mScrollLayout.setCurrentItem(0);
                } else if (R.id.default_panel == clickedId) {
                    mScrollLayout.setCurrentItem(1);
                } else if (R.id.defaultplus_panel == clickedId) {
                    mScrollLayout.setCurrentItem(2);
                }
            }
        };
        LinearLayout templatePanel = (LinearLayout) findViewById(R.id.template_panel);
        LinearLayout defaultPanel = (LinearLayout) findViewById(R.id.default_panel);
        LinearLayout defaultplusPanel = (LinearLayout) findViewById(R.id.defaultplus_panel);
        templatePanel.setOnClickListener(panelClickListener);
        defaultPanel.setOnClickListener(panelClickListener);
        defaultplusPanel.setOnClickListener(panelClickListener);

        mTemplateTab.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mDefaultTab.setChecked(false);
                    mDefaultplusTab.setChecked(false);
                    mScrollLayout.setCurrentItem(0);
                }
            }
        });

        mDefaultTab.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mTemplateTab.setChecked(false);
                    mDefaultplusTab.setChecked(false);
                    mScrollLayout.setCurrentItem(1);
                }
            }
        });

        mDefaultplusTab.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mTemplateTab.setChecked(false);
                    mDefaultTab.setChecked(false);
                    mScrollLayout.setCurrentItem(2);
                }
            }
        });

        viewPagerAdapter = new ViewPagerAdapter();
        mScrollLayout.setAdapter(viewPagerAdapter);
        mScrollLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                emoticPagerPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (emoticPagerPosition == 0) {
                        mTemplateTab.setChecked(true);
                        mDefaultTab.setChecked(false);
                        mDefaultplusTab.setChecked(false);
                    } else if (emoticPagerPosition == 1) {
                        mTemplateTab.setChecked(false);
                        mDefaultTab.setChecked(true);
                        mDefaultplusTab.setChecked(false);
                    } else if (emoticPagerPosition == 2) {
                        mTemplateTab.setChecked(false);
                        mDefaultTab.setChecked(false);
                        mDefaultplusTab.setChecked(true);
                    }
                }
            }
        });

        mScrollLayout.setCurrentItem(1);
    }

    public interface EditEmoticonListener {

        int addEmoticon = 0;
        int delEmoticon = 1;
        int sendEmoticon = 2;
        int addTemplate = 3; //add for adding template by KSJ 2016-01-31
        int addSpace = 4; //add for adding space by KSJ 2016-01-31
        int addEnter = 5; //add for adding enter by KSJ 2016-01-31

        /**
         * Do edit emoticon action.
         *
         * @param type        action type
         * @param emotionName the coding of emoticon
         */
        void doAction(int type, String emotionName);
    }

    /**
     * Sets the handler.
     *
     * @param handler the new handler
     */
    public void setHandler(Handler handler) {
        mHandler = handler;
    }

    public void setEditEmoticonListener(EditEmoticonListener l) {
        mListener = l;
    }

    private void startDelEmoticon() {
        if (mDelEmoticonThread != null) {
            synchronized (mDelEmoticonThread) {
                stopDelEmoticon();
            }
        }
        mNeedQuickDelete = true;
        mDelEmoticonThread = new DelEmoticonThread();
        synchronized (mDelEmoticonThread) {
            mDelEmoticonThread.start();
        }
    }

    private void stopDelEmoticon() {
        if (mDelEmoticonThread == null) {
            mNeedQuickDelete = false;
            return;
        }
        synchronized (mDelEmoticonThread) {
            mDelEmoticonThread.stopThread();
            mDelEmoticonThread = null;
        }
    }

    protected class IpMessageEmoticonAdapter extends BaseAdapter {

        protected int[] mIconArr;

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public IpMessageEmoticonAdapter(int[] iconArray) {
            mIconArr = iconArray;
        }

        @Override
        public int getCount() {
            return mIconArr.length;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.default_emoticon_grid_item, null);
                convertView.setTag(convertView);
            } else {
                convertView = (View) convertView.getTag();
            }

            ImageView ivPre = (ImageView) convertView.findViewById(R.id.iv_emoticon_icon);
            ivPre.setImageResource(mIconArr[position]);

            return convertView;
        }
    }

    protected class IpMessageEmoticonPlusAdapter extends BaseAdapter {

        protected int[] mIconArr;

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public IpMessageEmoticonPlusAdapter(int[] iconArray) {
            mIconArr = iconArray;
        }

        @Override
        public int getCount() {
            return mIconArr.length;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.defaultplus_emoticon_grid_item, null);
                convertView.setTag(convertView);
            } else {
                convertView = (View) convertView.getTag();
            }

            TextView ivPre = (TextView) convertView.findViewById(R.id.iv_emoticon_icon);
            ivPre.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 26);
            ivPre.setText(new String(new int[]{mIconArr[position]}, 0, 1));

            return convertView;
        }
    }

    private class DelEmoticonThread extends Thread {
        private boolean mStopThread = false;

        public void stopThread() {
            mStopThread = true;
        }

        @Override
        public void run() {
            synchronized (mObject) {
                try {
                    mObject.wait(1000);
                } catch (InterruptedException e) {
                }
            }
            Object object = new Object();
            if (mNeedQuickDelete) {
                while (!mStopThread) {
                    if (mHandler != null) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mListener.doAction(EditEmoticonListener.delEmoticon, "");
                            }
                        });
                    }
                    synchronized (object) {
                        try {
                            object.wait(100);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }
    }

    private RadioButton mTemplateTab;
    private RadioButton mDefaultTab;
    private RadioButton mDefaultplusTab;
    private String[] mDefaultName;
    private String[] mDefaultplusName;

    private class ViewPagerAdapter extends PagerAdapter {
        private LayoutInflater inflater;

        ViewPagerAdapter() {
            super();
            inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public Object instantiateItem(ViewGroup view, int position) {
            View emoticLayout = null;

            if (position == 0) {
                emoticLayout = inflater.inflate(R.layout.default_template_flipper, view, false);
                ListView listView = (ListView) emoticLayout.findViewById(R.id.gv_default_template_listview);
                listView.setDivider(null);

                List<String> quickTextsList = new ArrayList<String>();
                String[] defaultQuickTexts = getResources().getStringArray(R.array.default_quick_texts);
                for (int i = 0; i < defaultQuickTexts.length; i++) {
                    quickTextsList.add(defaultQuickTexts[i]);
                }
                List<Map<String, ?>> entries = new ArrayList<Map<String, ?>>();
                for (String text : quickTextsList) {
                    HashMap<String, Object> entry = new HashMap<String, Object>();
                    entry.put("text", text);
                    entries.add(entry);
                }

                final SimpleAdapter qtAdapter = new SimpleAdapter(mContext, entries, R.layout.default_template_list_item,
                        new String[]{"text"}, new int[]{R.id.quick_text});
                listView.setAdapter(qtAdapter);
                listView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        HashMap<String, Object> item = (HashMap<String, Object>) qtAdapter.getItem(position);
                        String templateStr = (String) item.get("text");
                        if (TextUtils.isEmpty(templateStr)) {
                            return;
                        }
                        mListener.doAction(EditEmoticonListener.addTemplate, templateStr);
                    }
                });
            } else if (position == 1) {
                emoticLayout = inflater.inflate(R.layout.default_emoticon_flipper, view, false);
                GridView gridView = (GridView) emoticLayout.findViewById(R.id.gv_default_emoticon_gridview);
                gridView.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);

                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    gridView.setNumColumns(mColumnArray[0]);
                } else {
                    gridView.setNumColumns(mColumnArray[1]);
                }
                IpMessageEmoticonAdapter adapter = new IpMessageEmoticonAdapter(getDefaultIconArray());
                gridView.setAdapter(adapter);
                gridView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String name = getDefaultName(position);
                        if (TextUtils.isEmpty(name)) {
                            return;
                        }
                        mListener.doAction(EditEmoticonListener.addEmoticon, name);
                    }
                });

                mDelEmoticon = (Button) emoticLayout.findViewById(R.id.smiley_panel_del_btn);
                mDelEmoticon.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                mListener.doAction(EditEmoticonListener.delEmoticon, "");
                                startDelEmoticon();
                                break;
                            case MotionEvent.ACTION_UP:
                                stopDelEmoticon();
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });

                mSpace = (Button) emoticLayout.findViewById(R.id.smiley_panel_space_btn);
                mSpace.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                mListener.doAction(EditEmoticonListener.addSpace, "");
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                mEnter = (Button) emoticLayout.findViewById(R.id.smiley_panel_enter_btn);
                mEnter.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                mListener.doAction(EditEmoticonListener.addEnter, "");
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
            } else if (position == 2) {
                emoticLayout = inflater.inflate(R.layout.defaultplus_emoticon_flipper, view, false);
                GridView gridView = (GridView) emoticLayout.findViewById(R.id.gv_defaultplus_emoticon_gridview);
                gridView.setScrollBarStyle(SCROLLBARS_OUTSIDE_OVERLAY);

                if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
                    gridView.setNumColumns(mColumnArray[0]);
                } else {
                    gridView.setNumColumns(mColumnArray[1]);
                }
                IpMessageEmoticonPlusAdapter plusadapter = new IpMessageEmoticonPlusAdapter(getDefaultplusIconArray());
                gridView.setAdapter(plusadapter);
                gridView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String name = getDefaultplusName(position);
                        if (TextUtils.isEmpty(name)) {
                            return;
                        }
                        mListener.doAction(EditEmoticonListener.addEmoticon, name);
                    }
                });

                mDelEmoticonPlus = (Button) emoticLayout.findViewById(R.id.smiley_panel_del_btn);
                mDelEmoticonPlus.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                mListener.doAction(EditEmoticonListener.delEmoticon, "");
                                startDelEmoticon();
                                break;
                            case MotionEvent.ACTION_UP:
                                stopDelEmoticon();
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });

                mSpacePlus = (Button) emoticLayout.findViewById(R.id.smiley_panel_space_btn);
                mSpacePlus.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                mListener.doAction(EditEmoticonListener.addSpace, "");
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                mEnterPlus = (Button) emoticLayout.findViewById(R.id.smiley_panel_enter_btn);
                mEnterPlus.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                mListener.doAction(EditEmoticonListener.addEnter, "");
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
            }

            view.addView(emoticLayout, 0);
            return emoticLayout;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object arg2) {
            collection.removeView((ViewGroup) arg2);
        }

    }

    private int[] getDefaultIconArray() {
        int[] sourceDefault = MessageConsts.defaultIconArr;
        int[] sourceGif = MessageConsts.giftIconArr;
        int total = sourceDefault.length + sourceGif.length;
        int[] arr = new int[total];
        for (int i = 0; i < sourceDefault.length; i++) {
            arr[i] = sourceDefault[i];
        }
        for (int j = 0; j < sourceGif.length; j++) {
            arr[sourceDefault.length + j] = sourceGif[j];
        }
        return arr;
    }

    private int[] getDefaultplusIconArray() {
        return mContext.getResources().getIntArray(R.array.defaultplus_emoticon_name);
    }

    private String getDefaultName(int position) {
        if (position < mDefaultName.length)
            return mDefaultName[position];
        return null;
    }

    private String getDefaultplusName(int position) {
        if (position >= mDefaultplusName.length) {
            return null;
        }
        return mDefaultplusName[position];
    }
}
