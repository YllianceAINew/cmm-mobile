package com.multimediachat.ui;

import com.multimediachat.R;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class TutorialActivity extends BaseActivity implements OnClickListener{

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tutorial_activity);
		hideActionBar();

		findViewById(R.id.btnLogin).setOnClickListener(this);
		findViewById(R.id.btnCreatNewAccount).setOnClickListener(this);

		findViewById(R.id.btnSetServerIP).setOnClickListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.btnLogin:
			showLoginActivity();
			break;
		case R.id.btnCreatNewAccount:
			showCreateNewAccountActivity();
			break;
		case R.id.btnSetServerIP:
			setServerIP();
			break;
		}
	}
	
	private void showLoginActivity(){
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);
	}
	
	private void showCreateNewAccountActivity() {
		Intent intent = new Intent(this, CreateProfileActivity.class);
		startActivity(intent);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if ( resultCode == RESULT_OK )
			recreate();
	}

	private void setServerIP(){
		startActivity(new Intent(this, SettingIPTestActivity.class));
	}
}
