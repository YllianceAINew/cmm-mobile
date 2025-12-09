package com.multimediachat.ui;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.ImApp;

import java.util.List;


public class BaseActivity extends FragmentActivity implements View.OnClickListener{
	public static final int POS_LEFT = 1;
	public static final int POS_RIGHT = 2;

	public static final int REQ_PERMISSION_CAMERA = 2;
	public static final int REQ_PERMISSION_ALBUM = 3;

	private View.OnClickListener mAllowed = null;

	protected ImApp mApp;
	protected Resources	    r;
	protected ContentResolver cr;
	private	boolean					  wasBackground = false;

	public static boolean isActive = false;

	LinearLayout lyt_left, lyt_right;
	RelativeLayout lytActionBar, lytSelectionBar, lytSearchBar;

	Animation slideInRightToLeft, slideOutLeftToRight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mApp = (ImApp) getApplication();

		r = getResources();
		cr = getContentResolver();

		slideInRightToLeft = AnimationUtils.loadAnimation(this, R.anim.slide_in_right_to_left);
		slideOutLeftToRight = AnimationUtils.loadAnimation(this, R.anim.slide_out_left_to_right);
		slideInRightToLeft.setDuration(200);
		slideOutLeftToRight.setDuration(200);
	}

	@Override
	public void setContentView(int layoutResID) {
		super.setContentView(R.layout.base_background);
		RelativeLayout contentView = (RelativeLayout)findViewById(R.id.contentView);

		if ( layoutResID != -1 )
			getLayoutInflater().inflate(layoutResID, contentView);

		findViewById(R.id.btn_back).setOnClickListener(this);
		lyt_left = (LinearLayout) findViewById(R.id.lyt_left);
		lyt_right = (LinearLayout) findViewById(R.id.lyt_right);

		lytActionBar = findViewById(R.id.actionbar);
		lytSelectionBar = findViewById(R.id.selection_bar);
		lytSearchBar = findViewById(R.id.search_bar);

		DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

	}

	@Override
	protected void onPause() {
		super.onPause();
		isActive = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		isActive = true;
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (!hasFocus) {
			wasBackground = isApplicationBroughtToBackground();
		} else {
			if ( wasBackground ) {
				NotificationManager nMgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
				nMgr.cancelAll();
			}
		}
	}

	public static void setActive(boolean enable)
	{
		isActive = enable;
	}

	private boolean isApplicationBroughtToBackground(){
		try{
			ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
			List<RunningTaskInfo> tasks = am.getRunningTasks(1);
			if (!tasks.isEmpty()) {
				ComponentName topActivity = tasks.get(0).topActivity;
				if (!topActivity.getPackageName().equals(getPackageName())) {
					return true;
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId())
		{
			case R.id.btn_back:
				onBackPressed();
				break;
		}
	}

	public void hideBackButton() {
		findViewById(R.id.btn_back).setVisibility(View.GONE);
	}
	public void hideActionBar() {
		lytActionBar.setVisibility(View.GONE);
	}

	public void hideSelectionBar() {
		if ( (slideInRightToLeft.hasStarted() && !slideInRightToLeft.hasEnded()) || (slideOutLeftToRight.hasStarted() && !slideOutLeftToRight.hasEnded()) )
			return;

		lytActionBar.setVisibility(View.VISIBLE);
		lytSelectionBar.startAnimation(slideOutLeftToRight);
		slideOutLeftToRight.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				lytSelectionBar.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
	}

	public void showSelectionBar() {
		if ( (slideInRightToLeft.hasStarted() && !slideInRightToLeft.hasEnded()) || (slideOutLeftToRight.hasStarted() && !slideOutLeftToRight.hasEnded()) )
			return;

		findViewById(R.id.btn_select_all).setSelected(false);

		lytSelectionBar.setVisibility(View.VISIBLE);
		lytSelectionBar.startAnimation(slideInRightToLeft);
		slideInRightToLeft.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				lytActionBar.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
	}

	public void hideSearchBar() {
		if ( (slideInRightToLeft.hasStarted() && !slideInRightToLeft.hasEnded()) || (slideOutLeftToRight.hasStarted() && !slideOutLeftToRight.hasEnded()) )
			return;

		if (lytSearchBar.getVisibility() == View.GONE)
			return;

		lytActionBar.setVisibility(View.VISIBLE);
		lytSearchBar.startAnimation(slideOutLeftToRight);
		slideOutLeftToRight.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				lytSearchBar.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
	}

	public void showSearchBar() {
		if ( (slideInRightToLeft.hasStarted() && !slideInRightToLeft.hasEnded()) || (slideOutLeftToRight.hasStarted() && !slideOutLeftToRight.hasEnded()) )
			return;

		findViewById(R.id.btn_select_all).setSelected(false);

		lytSearchBar.setVisibility(View.VISIBLE);
		lytSearchBar.startAnimation(slideInRightToLeft);
		slideInRightToLeft.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				lytActionBar.setVisibility(View.INVISIBLE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}
		});
	}

	public void setActionBarTitle(String title) {
		((TextView)findViewById(R.id.titleView)).setText(title);
		((TextView)findViewById(R.id.titleView)).setSelected(true);
	}

	public void addImageButton(int resImageID, int resID, int pos) {
		ImageView imageView = new ImageView(this);

		imageView.setImageResource(resImageID);
		imageView.setId(resID);
		imageView.setAdjustViewBounds(true);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				AndroidUtility.dp(32),
				AndroidUtility.dp(32)
		);

		params.gravity = Gravity.CENTER_VERTICAL;

		if ( pos == POS_LEFT ) {
			lyt_left.addView(imageView);
		} else {
			lyt_right.addView(imageView);
			params.setMargins( 0, 0, AndroidUtility.dp(10), 0);
		}

		imageView.setLayoutParams(params);
//		imageView.setScaleType(ImageView.ScaleType.CENTER);
		imageView.setOnClickListener(this);
	}

	public void addImageButtonWithPadding(int resImageID, int resID, int pos, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
		ImageView imageView = new ImageView(this);

		imageView.setImageResource(resImageID);
		imageView.setId(resID);
		imageView.setAdjustViewBounds(true);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				AndroidUtility.dp(32),
				AndroidUtility.dp(32)
		);

		params.gravity = Gravity.CENTER_VERTICAL;

		if ( pos == POS_LEFT ) {
			lyt_left.addView(imageView);
		} else {
			lyt_right.addView(imageView);
			params.setMargins(0, 0, AndroidUtility.dp(10), 0);
		}

//		imageView.setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
		imageView.setLayoutParams(params);
		imageView.setOnClickListener(this);
	}

	public void removeImageButtons(int pos) {
		if ( pos == POS_LEFT ) {
			lyt_left.removeAllViews();
		} else {
			lyt_right.removeAllViews();
		}
	}

	public void addTextButton(String text, int resID, int pos)
	{
		TextView textView = new TextView(this);

		textView.setText(text);
		textView.setId(resID);
		textView.setBackgroundResource(R.drawable.btn_action_bar_text_bg);
		textView.setPadding(AndroidUtility.dp(10), 0, AndroidUtility.dp(10), 0);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
		);

		params.gravity = Gravity.CENTER_VERTICAL;

		if ( pos == POS_LEFT ) {
			lyt_left.addView(textView);
			params.setMargins(AndroidUtility.dp(10), 0, 0, 0);
		} else {
			lyt_right.addView(textView);
			params.setMargins(0, 0, AndroidUtility.dp(10), 0);
		}

		textView.setLayoutParams(params);
		textView.setGravity(Gravity.CENTER);
		textView.setMinWidth(AndroidUtility.dp(80));
		textView.setTextColor(getResources().getColor(R.color.white));
		textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_level_4));
		textView.setOnClickListener(this);
	}

	public boolean checkPermissions(String[] permissions, int req)
	{
		int i = 0;

		for ( i = 0; i < permissions.length; i ++ )
		{
			if ( ActivityCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED )
				break;
		}

		if ( i != permissions.length )
		{
			ActivityCompat.requestPermissions(this, permissions, req);
			return false;
		}

		mAllowed.onClick(null);
		return true;
	}

	public boolean checkPermission(String permission, int req)
	{
		if ( ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED )
		{
			ActivityCompat.requestPermissions(this, new String[]{permission}, req);
			return false;
		}

		mAllowed.onClick(null);
		return true;
	}

	public boolean checkAlbumPermission(View.OnClickListener allowed)
	{
		mAllowed = allowed;
		return checkPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERMISSION_ALBUM);
	}

	public boolean checkCameraPermission(View.OnClickListener allowed)
	{
		mAllowed = allowed;
		return checkPermissions(new String[]{android.Manifest.permission.CAMERA, android.Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQ_PERMISSION_CAMERA);
	}
}
