package com.multimediachat.ui.adapter.carouseltab;

import android.content.Context;
import android.content.res.Configuration;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.DebugConfig;

public class TabBar extends HorizontalScrollView implements ViewPager.Decor {
    private static final int TEXTCOLOR_ALPHA_MASK = 0xA675d1df;
    private TabsAdapter mAdapter;
    private Drawable mBackgroundDrawable;
    private Configuration mConfig;
    private OnLongClickListener mOnLongClickListener;
    public PageListener mPageListener;
    private int mSelectedIndicatorThickness;
    private final TabBarStrip mTabStrip;
    private int mTitleOffset;
    private int mUserBarHeight;
    private ViewPager mViewPager;
    private boolean isAutomotive = false;

    public TabBar(final Context context) {
        this(context, null);
    }

    public TabBar(final Context context, final AttributeSet set) {
        this(context, set, 0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        final ViewParent parent = getParent();
        linkWithParent(parent);
    }

    private LayoutInflater mInflater;

    public TabBar(final Context context, final AttributeSet set, final int n) {
        super(context, set, n);
        mOnLongClickListener = null;
        mUserBarHeight = -1;
        setHorizontalScrollBarEnabled(false);
        setFillViewport(true);
        mTitleOffset = (int) (24.0f * getResources().getDisplayMetrics().density);
        addView((View) (mTabStrip = new TabBarStrip(context)), -1, -1);
        setLayoutParams(new ViewGroup.LayoutParams(-1, getBarHeight(context)));
        mConfig = getContext().getResources().getConfiguration();

        mPageListener = new PageListener();
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public static int getBarHeight(final Context context) {
        return TabBarUtils.dimen.height(context, false);
    }

    private boolean isScreenPortriat() {
        if (mConfig == null)
            mConfig = getResources().getConfiguration();
        return mConfig.screenHeightDp > mConfig.screenWidthDp;
    }

    private void scrollToTab(int tabIndex, final int positionOffset) {
        final int tabStripChildCount = mTabStrip.getChildCount();
        if (tabStripChildCount != 0 && tabIndex >= 0 && tabIndex < tabStripChildCount) {
            final View selectedChild = mTabStrip.getChildAt(tabIndex);
            if (selectedChild != null) {
                final int targetScrollX = selectedChild.getLeft() + positionOffset;
                if (tabIndex > 0 || positionOffset > 0)
                    scrollTo(targetScrollX - mTitleOffset, 0);
                else
                    scrollTo(tabIndex, 0);
            }
        }
    }

    private void tintTabTextColor() {
        int curTextColor;
        if (isScreenPortriat() || mUserBarHeight > 0) {
            curTextColor = TabBarUtils.color.portriatTextColor(getContext());
        } else {
            curTextColor = TabBarUtils.color.landscapeTextColor(getContext());
        }
        if (mTabStrip != null) {
            for (int i = 0; i < mTabStrip.getChildCount(); ++i) {
                final TextView tv = (TextView) mTabStrip.getChildAt(i).findViewById(R.id.txt_tab);
                int otherTextColor;
                if (i == mViewPager.getCurrentItem()) {
                    otherTextColor = curTextColor;
                    Drawable drawable = getResources().getDrawable(mAdapter.getPageIconId(i));
                    tv.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                } else {
                    Drawable drawable = getResources().getDrawable(mAdapter.getPageIconId(i) + 1);
                    tv.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
                    otherTextColor = (curTextColor & TEXTCOLOR_ALPHA_MASK);
                }
                tv.setTextColor(otherTextColor);
            }
        }
    }

    private void tuningUIForOrientation() {
        final ViewGroup.LayoutParams params = getLayoutParams();
        if (params != null) {
            params.height = getBarHeight();
            setLayoutParams(params);
        }
        mSelectedIndicatorThickness = TabBarUtils.dimen.indicatorThickness(getContext(), isAutomotive);
        tintTabTextColor();
    }

    @ViewDebug.ExportedProperty(category = "CommonControl")
    public int getBarHeight() {
        if (mUserBarHeight != -1) {
            return mUserBarHeight;
        }
        return TabBarUtils.dimen.height(getContext(), isAutomotive);
    }

    boolean isAdapterSame(final TabsAdapter tabAdapter) {
        return mAdapter == tabAdapter;
    }

    public void linkWithParent(final ViewParent viewParent) {
        if (viewParent instanceof ViewPager) {
            final ViewPager pager = (ViewPager) viewParent;
            if ((mViewPager = pager) != null) {
                setAdapter((TabsAdapter) pager.getAdapter());
                if (mPageListener == null)
                    mPageListener = new PageListener();
                pager.setOnAdapterChangeListener(mPageListener);
                pager.setInternalPageChangeListener(mPageListener);
            }
            return;
        }
        throw new IllegalArgumentException("Only support two type as virtual parent: ViewPager and CarouselHost");
    }

    protected void onConfigurationChanged(final Configuration config) {
        super.onConfigurationChanged(config);
        mConfig = config;
        tuningUIForOrientation();
    }

    protected void onOverScrolled(final int scrollX, final int scrollY, final boolean clampedX, final boolean clampedY) {
        super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    }

    protected void onSizeChanged(final int w, final int h, final int oldw, final int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        scrollToTab(mViewPager.getCurrentItem(), 0);
    }


    @Override
    protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        // TODO Auto-generated method stub
        super.onLayout(arg0, arg1, arg2, arg3, arg4);
        scrollToTab(mViewPager.getCurrentItem(), 0);
    }

    public void populateTabStrip() {
        final TabClickListener onClickListener = new TabClickListener();
        for (int i = 0; i < mAdapter.getCount(); ++i) {
            View view = mInflater.inflate(R.layout.tab_normal, null);
            TextView tabTitleView = view.findViewById(R.id.txt_tab);
            setTabTextView(tabTitleView, getContext());
            tabTitleView.setText(mAdapter.getPageTitle(i));
            Drawable drawable = getResources().getDrawable(mAdapter.getPageIconId(i));
            tabTitleView.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
            tabTitleView.setSelected(true);
            view.setOnClickListener((OnClickListener) onClickListener);
            view.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(final View view) {
                    return TabBar.this.mOnLongClickListener != null && TabBar.this.mOnLongClickListener.onLongClick((View) TabBar.this);
                }
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            mTabStrip.addView(view, params);

        }
        mTabStrip.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(final View view) {
                return TabBar.this.mOnLongClickListener != null && TabBar.this.mOnLongClickListener.onLongClick((View) TabBar.this);
            }
        });
        tuningUIForOrientation();
    }

    public void setAdapter(final TabsAdapter adapter) {
        if (mAdapter == null || !isAdapterSame(adapter)) {
            if (mViewPager != null) {
                final TabsAdapter oldAdapter = mAdapter;
                if (oldAdapter != null && mPageListener != null) {
                    oldAdapter.unregisterDataSetObserver(mPageListener);
                }
                if ((mAdapter = adapter) != null) {
                    if (mPageListener == null)
                        mPageListener = new PageListener();
                    adapter.registerDataSetObserver(mPageListener);
                }
            }
            if (mTabStrip != null && mAdapter != null) {
                mTabStrip.removeAllViews();
                populateTabStrip();
            }
        }
    }

    @Deprecated
    public void setBackgroundDrawable(Drawable background) {
        mBackgroundDrawable = (Drawable) background;
        if (isScreenPortriat() || mUserBarHeight > 0) {
            if (background != null) {
                background = mBackgroundDrawable;
            } else {
                background = new ColorDrawable(TabBarUtils.color.backgroundColor(getContext(), isAutomotive));
            }
            super.setBackgroundDrawable((Drawable) background);
        } else
            super.setBackgroundDrawable((Drawable) new ColorDrawable(TabBarUtils.color.landscapeBackground(getContext())));
    }

    public void setBarHeight(final int height) {
        if (mUserBarHeight != height && height >= -1) {
            mUserBarHeight = height;
            final ViewGroup.LayoutParams params = getLayoutParams();
            if (params != null) {
                params.height = getBarHeight();
                setLayoutParams(params);
            }
        }
    }

    public void setOnLongClickListener(final OnLongClickListener longClickListener) {
        mOnLongClickListener = longClickListener;
    }

    public class PageListener extends DataSetObserver implements ViewPager.OnPageChangeListener, ViewPager.OnAdapterChangeListener {
        private int mScrollState;

        public void onAdapterChanged(final PagerAdapter oldAdapter, final PagerAdapter newAdapter) {
            TabBar.this.setAdapter((TabsAdapter) newAdapter);
        }

        public void onChanged() {
            if (TabBar.this.mTabStrip != null && TabBar.this.mAdapter != null) {
                TabBar.this.mTabStrip.removeAllViews();
                TabBar.this.populateTabStrip();
                TabBar.this.mTabStrip.onViewPagerPageChanged(TabBar.this.mViewPager.getCurrentItem(), 0.0f);
                TabBar.this.mTabStrip.getViewTreeObserver().addOnPreDrawListener((ViewTreeObserver.OnPreDrawListener) new ViewTreeObserver.OnPreDrawListener() {
                    public boolean onPreDraw() {
                        TabBar.this.scrollToTab(TabBar.this.mViewPager.getCurrentItem(), 0);
                        TabBar.this.mTabStrip.getViewTreeObserver().removeOnPreDrawListener((ViewTreeObserver.OnPreDrawListener) this);
                        return false;
                    }
                });
            }
        }

        public void onPageScrollStateChanged(final int state) {
            mScrollState = state;
        }

        public void onPageScrolled(final int position, final float positionOffset, int positionOffsetPixels) {
            int tabStripChildCount = TabBar.this.mTabStrip.getChildCount();
            if (tabStripChildCount == 0 || position < 0 || position >= tabStripChildCount) {
                return;
            }
            TabBar.this.mTabStrip.onViewPagerPageChanged(position, positionOffset);
            final View selectedTitle = TabBar.this.mTabStrip.getChildAt(position);
            int extraOffset;
            if (selectedTitle != null) {
                extraOffset = (int) (selectedTitle.getWidth() * positionOffset);
            } else {
                extraOffset = 0;
            }
            TabBar.this.scrollToTab(position, extraOffset);
        }

        public void onPageSelected(final int position) {
            if (mScrollState == 0) {
                TabBar.this.mTabStrip.onViewPagerPageChanged(position, 0.0f);
                TabBar.this.scrollToTab(position, 0);
            }
            TabBar.this.tintTabTextColor();
        }
    }

    private static class SimpleTabColorizer implements TabColorizer {
        private int[] mIndicatorColors;

        @Override
        public final int getIndicatorColor(final int n) {
            return mIndicatorColors[n % mIndicatorColors.length];
        }

        void setIndicatorColors(final int... indicatorColors) {
            mIndicatorColors = indicatorColors;
        }
    }

    public class TabBarStrip extends LinearLayout {
        private final SimpleTabColorizer mDefaultTabColorizer;
        private final Paint mSelectedIndicatorPaint;
        @ViewDebug.ExportedProperty(category = "CommonControl")
        private int mSelectedPosition;
        @ViewDebug.ExportedProperty(category = "CommonControl")
        private float mSelectionOffset;

        TabBarStrip(final Context context) {
            this(context, null);
        }

        TabBarStrip(final Context context, final AttributeSet set) {
            super(context, set);
            setWillNotDraw(false);
            context.getTheme().resolveAttribute(android.R.attr.colorForeground, new TypedValue(), true);
            mDefaultTabColorizer = new SimpleTabColorizer();
            mDefaultTabColorizer.setIndicatorColors(new int[]{TabBarUtils.color.categoryLight(context)});
            TabBar.this.mSelectedIndicatorThickness = TabBarUtils.dimen.indicatorThickness(context, TabBar.this.isAutomotive);
            mSelectedIndicatorPaint = new Paint();
        }

        protected void onDraw(final Canvas canvas) {
            final int height = getHeight();
            final int childCount = getChildCount();
            final SimpleTabColorizer tabColorizer = mDefaultTabColorizer;
            if (childCount > 0) {
                final View selectedTitle = getChildAt(mSelectedPosition);
                int left = selectedTitle.getLeft();
                int right = selectedTitle.getRight();
                final int color = ((TabColorizer) tabColorizer).getIndicatorColor(mSelectedPosition);
                if (mSelectionOffset > 0.0f) {
                    if (mSelectedPosition < getChildCount() - 1) {
                        final View nextTitle = getChildAt(mSelectedPosition + 1);
                        left = (int) (mSelectionOffset * nextTitle.getLeft() + (1.0f - mSelectionOffset) * left);
                        right = (int) (mSelectionOffset * nextTitle.getRight() + (1.0f - mSelectionOffset) * right);
                    }
                }
                mSelectedIndicatorPaint.setColor(color);
                canvas.drawRect((float) left, (float) (height - TabBar.this.mSelectedIndicatorThickness), (float) right, (float) height, mSelectedIndicatorPaint);
            }
        }

        void onViewPagerPageChanged(final int position, final float positionOffset) {
            mSelectedPosition = position;
            mSelectionOffset = positionOffset;
            invalidate();
        }
    }

    private class TabClickListener implements OnClickListener {
        public void onClick(final View view) {
            int i = 0;
            while (i < TabBar.this.mTabStrip.getChildCount()) {
                if (view == TabBar.this.mTabStrip.getChildAt(i)) {
                    if (TabBar.this.mViewPager != null) {
                        TabBar.this.mViewPager.setCurrentItem(i);
                        break;
                    }
                    TabBar.this.mTabStrip.onViewPagerPageChanged(i, 0.0f);
                } else {
                    ++i;
                }
            }
        }
    }

    public interface TabColorizer {
        int getIndicatorColor(int p0);
    }

    private void setTabTextView(TextView textView, final Context context) {
        textView.setGravity(Gravity.CENTER);
        textView.setAllCaps(true);
    }

    public int getTabCount() {
        return mTabStrip.getChildCount();
    }

    public View getTabItem(int index) {
        return mTabStrip.getChildAt(index);
    }
}
