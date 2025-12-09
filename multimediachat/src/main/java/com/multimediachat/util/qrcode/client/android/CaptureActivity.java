/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.multimediachat.util.qrcode.client.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.multimediachat.util.qrcode.BarcodeFormat;
import com.multimediachat.util.qrcode.BinaryBitmap;
import com.multimediachat.util.qrcode.DecodeHintType;
import com.multimediachat.util.qrcode.LuminanceSource;
import com.multimediachat.util.qrcode.MultiFormatReader;
import com.multimediachat.util.qrcode.NotFoundException;
import com.multimediachat.util.qrcode.RGBLuminanceSource;
import com.multimediachat.util.qrcode.ReaderException;
import com.multimediachat.util.qrcode.Result;
import com.multimediachat.util.qrcode.client.android.camera.CameraManager;
import com.multimediachat.util.qrcode.common.HybridBinarizer;
import com.multimediachat.util.PrefUtil.mPref;
import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.app.im.plugin.xmpp.XmppAddress;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.BaseActivity;
import com.multimediachat.ui.FriendProfileChattingActivity;
import com.multimediachat.ui.MainActivity;
import com.multimediachat.ui.MainTabNavigationActivity;
import com.multimediachat.ui.MyProfileActivity;
import com.multimediachat.ui.dialog.CustomDialog;
import com.multimediachat.util.BitmapUtil;
import com.multimediachat.util.connection.MyXMLResponseHandler;
import com.multimediachat.util.connection.PicaApiUtility;

import org.jivesoftware.smack.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * This activity opens the camera and does the actual scanning on a background
 * thread. It draws a viewfinder to help the user place the barcode correctly,
 * shows feedback as the image processing is happening, and then overlays the
 * results when a scan is successful.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 * @author Sean Owen
 */
public final class CaptureActivity extends BaseActivity implements SurfaceHolder.Callback, OnClickListener, NotificationCenter.NotificationCenterDelegate {

	private static final String TAG = CaptureActivity.class.getSimpleName();

	private static final String[] ZXING_URLS = { "http://zxing.appspot.com/scan", "zxing://scan/" };

	private static final int GET_GALLERY = 1010;

	private CameraManager cameraManager;
	private CaptureActivityHandler handler;
	private Result savedResultToShow;
	private ViewfinderView viewfinderView;
	private Result lastResult;
	private boolean hasSurface;
	private IntentSource source;
	private Collection<BarcodeFormat> decodeFormats;
	private Map<DecodeHintType, ?> decodeHints;
	private String characterSet;
	private InactivityTimer inactivityTimer = null;
	private BeepManager beepManager;
	private AmbientLightManager ambientLightManager;

	MainProgress mProgressDlg;

	ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	View btnLight;
	View bgFindView;
	TextView flashTextView;

	private MultiFormatReader multiFormatReader;

	public Handler getHandler() {
		return handler;
	}

	CameraManager getCameraManager() {
		return cameraManager;
	}

	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);

		if ( !MainTabNavigationActivity.isInstanciated() )
		{
			Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra("activity", "qrcode");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
			finish();
			return;
		}

		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.capture);

		setActionBarTitle(getString(R.string.qrcode_scan));

		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		beepManager = new BeepManager(this);
		ambientLightManager = new AmbientLightManager(this);

		mProgressDlg = new MainProgress(this);
		mProgressDlg.setMessage("");

		bgFindView = findViewById(R.id.bg_findview);
		flashTextView = (TextView) findViewById(R.id.flashlight_text);
		btnLight = findViewById(R.id.btnLight);
		btnLight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				btnLight.setSelected(!btnLight.isSelected());

				if (cameraManager != null) {
					cameraManager.setTorch(btnLight.isSelected());
				}

				flashTextView.setText(btnLight.isSelected()?getResources().getString(R.string.tap_to_turn_light_off):getResources().getString(R.string.tap_to_turn_light_on));
			}
		});

		multiFormatReader = new MultiFormatReader();
		NotificationCenter.getInstance().addObserver(this, NotificationCenter.qrcode_photos_compressed);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// CameraManager must be initialized here, not in onCreate(). This is
		// necessary because we don't
		// want to open the camera driver and measure the screen size if we're
		// going to show the help on
		// first launch. That led to bugs where the scanning rectangle was the
		// wrong size and partially
		// off screen.
		cameraManager = new CameraManager(getApplication());

		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		viewfinderView.setCameraManager(cameraManager);

		handler = null;
		lastResult = null;

		// setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

		resetStatusView();

		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			// The activity was paused but not stopped, so the surface still
			// exists. Therefore
			// surfaceCreated() won't be called, so init the camera here.
			initCamera(surfaceHolder);
		} else {
			// Install the callback and wait for surfaceCreated() to init the
			// camera.
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		beepManager.updatePrefs();
		ambientLightManager.start(cameraManager);

		inactivityTimer.onResume();

		source = IntentSource.NONE;
		decodeFormats = null;
		characterSet = null;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == GET_GALLERY) {
			if (resultCode == RESULT_OK) {
				Uri selectedPhoto = data.getData();
				if (selectedPhoto == null)
					return;

				String photoPath = null;
				String[] filePath = { MediaStore.Images.Media.DATA };
				Cursor c = null;
				try {
					c = getContentResolver().query(selectedPhoto, filePath, null, null, null);
					if (c.moveToFirst()) {
						try {
							int columnIndex = c.getColumnIndex(filePath[0]);
							photoPath = c.getString(columnIndex);
						} catch (Exception e) {
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (c != null) {
						c.close();
					}
				}

				if (photoPath == null && selectedPhoto.getHost().startsWith("com.")) { // google.android.gallery3d.provider
					photoPath = data.getData().toString();
				}

				if (photoPath == null)
					return;

				// decode barcode image
				Bitmap bm = BitmapUtil.getBitmapFromFile(photoPath);
				int[] intArray = new int[bm.getWidth() * bm.getHeight()];
				bm.getPixels(intArray, 0, bm.getWidth(), 0, 0, bm.getWidth(), bm.getHeight());
				LuminanceSource source = new RGBLuminanceSource(bm.getWidth(), bm.getHeight(), intArray);
				BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
				MultiFormatReader reader = new MultiFormatReader();
				try {
					Result result = reader.decode(bitmap);
					if (result != null) {
						handleDecode(result, null, 0);
					}
				} catch (ReaderException e) {
					e.printStackTrace();
					return;
				}
			}
		}
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		ambientLightManager.stop();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		if ( inactivityTimer != null )
			inactivityTimer.shutdown();
		NotificationCenter.getInstance().removeObserver(this, NotificationCenter.qrcode_photos_compressed);
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if (source == IntentSource.NATIVE_APP_INTENT) {
					setResult(RESULT_CANCELED);
					finish();
					return true;
				}
				if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK) && lastResult != null) {
					restartPreviewAfterDelay(0L);
					return true;
				}
				break;
			case KeyEvent.KEYCODE_FOCUS:
			case KeyEvent.KEYCODE_CAMERA:
				// Handle these events so they don't launch the Camera app
				return true;
			// Use volume up/down to turn on light
			case KeyEvent.KEYCODE_VOLUME_DOWN:
				cameraManager.setTorch(false);
				return true;
			case KeyEvent.KEYCODE_VOLUME_UP:
				cameraManager.setTorch(true);
				return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void decodeOrStoreSavedBitmap(Bitmap bitmap, Result result) {
		// Bitmap isn't used yet -- will be used soon
		if (handler == null) {
			savedResultToShow = result;
		} else {
			if (result != null) {
				savedResultToShow = result;
			}
			if (savedResultToShow != null) {
				Message message = Message.obtain(handler, R.id.decode_succeeded, savedResultToShow);
				handler.sendMessage(message);
			}
			savedResultToShow = null;
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 *
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param scaleFactor
	 *            amount by which thumbnail was scaled
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode, float scaleFactor) {
		inactivityTimer.onActivity();
		beepManager.playBeepSoundAndVibrate();
		final String resultString = rawResult.getText();
		cameraManager.stopPreview();
		//Toast.makeText(this, resultString, Toast.LENGTH_SHORT).show();
		// FIXME
		if (resultString.equals("")) {
			showWarningDialog(getString(R.string.qr_code_not_found));
		} else {
			if (resultString.endsWith("@" + GlobalConstrants.server_domain)) {
				mProgressDlg.show();
				Log.e("ICICI", resultString);

				//if (DebugConfig.USE_OLD_API) {
					startFindFriendsByNameOrId(StringUtils.parseName(resultString));
				//}
				/*else {
					PicaApiUtility.getSettingsFriendQrcode(this, StringUtils.parseName(resultString), new MyJSONResponseHandler() {
						@Override
						public void onMySuccess(JSONObject response) {
							try {
								if ( response.getString(Imps.Params.STATUS).equals("true") )
								{
									startFindFriendsByNameOrId(StringUtils.parseName(resultString));
								} else {
									mProgressDlg.dismiss();
									showWarningDialog(getString(R.string.qr_code_not_found));
								}
							} catch (JSONException e) {
								e.printStackTrace();
								mProgressDlg.dismiss();
								showWarningDialog(getString(R.string.qr_code_not_found));
							}
						}

						@Override
						public void onMyFailure(int errcode) {
							mProgressDlg.dismiss();
							GlobalFunc.showErrorMessageToast(CaptureActivity.this, errcode, false);
						}
					});
				}*/
			}
			else
			{
				showWarningDialog(getString(R.string.qr_code_not_found));
			}
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			Log.w(TAG, "initCamera() while already open -- late SurfaceView callback?");
			return;
		}

		try {
			cameraManager.openDriver(surfaceHolder);
			// Creating the handler starts the preview, which can also throw a
			// RuntimeException.
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats, decodeHints, characterSet, cameraManager);
			}
			decodeOrStoreSavedBitmap(null, null);

			RelativeLayout.LayoutParams layoutParams=new RelativeLayout.LayoutParams(cameraManager.getFramingRect().width(), cameraManager.getFramingRect().height());
			layoutParams.setMargins(0, cameraManager.getFramingRect().top, 0, 0);
			layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			bgFindView.setLayoutParams(layoutParams);
			bgFindView.setVisibility(View.VISIBLE);
		} catch (IOException ioe) {
			Log.w(TAG, ioe);
			displayFrameworkBugMessageAndExit();
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			Log.w(TAG, "Unexpected error initializing camera", e);
			displayFrameworkBugMessageAndExit();
		}
	}

	private void displayFrameworkBugMessageAndExit() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.app_name));
		builder.setMessage(getString(R.string.msg_camera_framework_bug));
		builder.setPositiveButton(R.string.ok, new FinishListener(this));
		builder.setOnCancelListener(new FinishListener(this));
		builder.show();
	}

	public void restartPreviewAfterDelay(long delayMS) {
		cameraManager.startPreview();
		if (handler != null) {
			handler.sendEmptyMessageDelayed(R.id.restart_preview, delayMS);
		}
		resetStatusView();
	}

	private void resetStatusView() {
		viewfinderView.setVisibility(View.VISIBLE);
		lastResult = null;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	public void startFindFriendsByNameOrId(final String name) {
		if (this == null || this.isFinishing()) {
			mProgressDlg.dismiss();
			return;
		}

		String username = mPref.getString("username", "");

		if ( username.equals(name) )
		{
			mProgressDlg.dismiss();
			Intent intent = new Intent(this, MyProfileActivity.class);
			startActivity(intent);
			finish();
			return;
		}

		Contact contact = DatabaseUtils.getContactInfo(cr, name+"@"+GlobalConstrants.server_domain);

		if ( contact != null )
		{
			mProgressDlg.dismiss();
			showFriendProfile(contact);
			return;
		}

		PicaApiUtility.getProfile(this, name, new MyXMLResponseHandler() {
			@Override
			public void onMySuccess(JSONObject response) {
				mProgressDlg.dismiss();
				try {
					String nickName = response.getString(Imps.Contacts.NICKNAME);
					Contact contact = new Contact(new XmppAddress(name+"@"+GlobalConstrants.server_domain), nickName);
					String gender = response.getString(Imps.Contacts.GENDER);
					String region = response.getString(Imps.Contacts.REGION);
					String status = response.getString(Imps.Contacts.STATUS);
					String hash = response.getString(Imps.Contacts.HASH);
					/*if (region == null)
						contact.region = GlobalConstrants.DEFAULT_REGION;*/
					contact.setProfile(status, gender, region);
					contact.setHash(hash);
					contact.sub_type = Imps.Contacts.SUBSCRIPTION_TYPE_NONE;
					contact.userid = response.getString(Imps.Contacts.USERID);
					contact.mFullName = nickName;
					showFriendProfile(contact);
				} catch (JSONException e) {
					e.printStackTrace();
					onMyFailure(-1);
				}
			}

			@Override
			public void onMyFailure(int errcode) {
				mProgressDlg.dismiss();
				GlobalFunc.showErrorMessageToast(CaptureActivity.this, errcode, false);
			}
		});
	}

	private Result qrcodeFromBitmap(Bitmap bMap)
	{
		int[] intArray = new int[bMap.getWidth()*bMap.getHeight()];
//copy pixel data from the Bitmap into the 'intArray' array
		bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

		LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(),intArray);

		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Result result = null;
		try {
			result = multiFormatReader.decodeWithState(bitmap);
		} catch (NotFoundException e) {
			e.printStackTrace();
		}
		multiFormatReader.reset();

		return result;
	}

	public void startFindingQRCodeForBitmapPath(String path)
	{
		mProgressDlg.show();
		new AsyncTask<String, Void, Result>()
		{
			@Override
			protected Result doInBackground(String... params) {
				Bitmap bitmap = BitmapUtil.getBitmapFromFile(params[0]);
				Result result = qrcodeFromBitmap(bitmap);
				return result;
			}

			@Override
			protected void onPostExecute(Result result) {
				super.onPostExecute(result);
				mProgressDlg.dismiss();
				if ( result != null )
				{
					handleDecode(result, null, 0);
				} else {
					showWarningDialog(getString(R.string.qr_code_not_found));
				}
			}
		}.execute(path);
	}

	@Override
	public void didReceivedNotification(int id, final Object... args) {
		if (id == NotificationCenter.qrcode_photos_compressed) {
			ArrayList<String> pathList = (ArrayList<String>) args[0];

			if (pathList != null && pathList.size() > 0) {
				startFindingQRCodeForBitmapPath(pathList.get(0));
			}
		}
	}

	private void showWarningDialog(String string)
	{
		final CustomDialog dlg = new CustomDialog(this, getString(R.string.warning), string, getString(R.string.ok), null);
		dlg.setOnOKClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				try {
					dlg.dismiss();
					restartPreviewAfterDelay(0);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		dlg.show();
	}

	private void showFriendProfile(Contact contact)
	{
		Intent intent = new Intent(CaptureActivity.this, FriendProfileChattingActivity.class);
		FriendProfileChattingActivity.contact = contact;
		startActivity(intent);
		finish();
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
			default:
				super.onClick(v);
				break;
		}
	}
}
