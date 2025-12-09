package com.multimediachat.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.widget.TextView;

import com.multimediachat.R;
import com.multimediachat.util.connection.PicaApiUtility;

import java.util.ArrayList;

public class MainProgress extends Dialog implements View.OnClickListener{
	private TextView 	txt_msg;
	Animation 			animation;
	Context 			mContext;
	ArrayList<AsyncTask> mAsyncTasks = new ArrayList<>();

	boolean   cancelable = false;
	public MainProgress(Context context) {
		super(context, R.style.custom_dialog_theme);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.setContentView(R.layout.progress);
		mContext = context;
		setCancelable(false);

		txt_msg = (TextView)findViewById(R.id.txt_msg);
		findViewById(R.id.rootView).setOnClickListener(this);

		setOnKeyListener(new DialogInterface.OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
					PicaApiUtility.cancelRequests(mContext);
					for ( int i = 0; i < mAsyncTasks.size(); i ++  )
						mAsyncTasks.get(i).cancel(true);
					mAsyncTasks.clear();
					dismiss();
					((Activity)mContext).onBackPressed();
				}
				return false;
			}
		});
	}

	public void setMessage(final String msg)
	{
		txt_msg.setText(msg);
	}

	@Override
	public void dismiss() {
		try{
			super.dismiss();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()) {
		case R.id.rootView:
			if ( cancelable )
				dismiss();
			break;
		}
	}

	@Override
	public void show() {
		try{
			super.show();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void setCancelable(boolean flag) {
		super.setCancelable(flag);
		cancelable = flag;
	}

	public void addAsyncTask(AsyncTask task)
	{
		mAsyncTasks.add(task);
	}

}
