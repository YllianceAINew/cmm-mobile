package com.multimediachat.ui;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.multimediachat.util.qrcode.client.android.camera.CameraConfigurationUtils;
import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.ui.dialog.MainProgress;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.dialog.CustomDialog;
import com.multimediachat.util.BitmapUtil;
import com.multimediachat.util.CameraHelper;
import com.multimediachat.util.ImageLoaderUtil;
import com.yovenny.videocompress.MediaController;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import static android.media.MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED;

/**
 * Created by jack on 12/1/2017.
 */

public class CameraActivity extends Activity implements View.OnClickListener {
    private final static int REQ_PREVIEW = 100;

    private final int MAX_DURATION_MS = 10000;

    private Camera mCamera;
    private TextureView mPreview;
    private MediaRecorder mMediaRecorder = null;
    private File mOutputFile;
    boolean isRecording = false, isPictureTaking = false;
    private static final String TAG = "Recorder";
    private View mBtnOK, mBtnRefresh, mBtnCamera;
    private boolean isVideo = false;
    private String mFilePath = "", mImageFilePath = "";
    private boolean isCameraInitialized = false;
    private ImageView mImageView;
    private VideoView mVideoView;
    private ProgressBar mProgress;
    private View mSwitchCameara;
    private boolean isBackCameraSelected = true;
    private boolean isPhotoOnly = false;
    private boolean showPreview = false;
    private boolean needCompressImage = false;

    OrientationEventListener orientationListener;
    int mOrientation = 0;
    int mTakenOrientation = 0;
    int THRESHOLD = 45;

    MainProgress mProgressDlg;

    private int RECORD_DELAY_TIME_MS = 1000;    //add this to prevent cutting last record in mediarecorder

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        //Remove notification bar
        AndroidUtility.hideSystemUI(this);
        isPhotoOnly = getIntent().getBooleanExtra("isphotoonly", false);
        showPreview = getIntent().getBooleanExtra("showPreview", false);
        needCompressImage = getIntent().getBooleanExtra("needCompressImage", false);
        initUI();

        mProgressDlg = new MainProgress(this);
        mProgressDlg.setMessage("");

        orientationListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI) {
            @Override
            public void onOrientationChanged(int orientation) {
                int orient = 0;

                if ( ( orientation >= 360 - THRESHOLD && orientation <= 360 ) || ( orientation >= 0 && orientation <= THRESHOLD ) )
                    orient = 0;
                else if ( orientation <= 90 + THRESHOLD )
                    orient = 90;
                else if ( orientation <= 180 + THRESHOLD )
                    orient = 180;
                else
                    orient = 270;

                if ( mOrientation != orient ) {
                    DebugConfig.error("*****", "Orientation Changed:" + orient);
                    mOrientation = orient;
                }
            }
        };
    }

    private void initUI()
    {
        setContentView(R.layout.camera_activity);
        mPreview = (TextureView) findViewById(R.id.surface_view);

        mBtnOK = findViewById(R.id.btn_ok);
        mBtnRefresh = findViewById(R.id.btn_refresh);
        mBtnCamera = findViewById(R.id.btn_camera);
        mImageView = (ImageView) findViewById(R.id.imageView);
        mVideoView = (VideoView) findViewById(R.id.videoView);
        mProgress = (ProgressBar) findViewById(R.id.progressRecord);
        mSwitchCameara = findViewById(R.id.switchCamera);

        mSwitchCameara.setOnClickListener(this);

        mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });

        mBtnOK.setOnClickListener(this);
        mBtnRefresh.setOnClickListener(this);

        findViewById(R.id.btnBack).setOnClickListener(this);

        if ( !isPhotoOnly ) {
            mBtnCamera.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //start record
                    // BEGIN_INCLUDE(prepare_start_media_recorder)

                    if (isCameraInitialized && !isRecording) {
                        isRecording = true;
                        new MediaPrepareTask().execute(null, null, null);
                    }
                    // END_INCLUDE(prepare_start_media_recorder)
                    return true;
                }
            });
        }
        mBtnCamera.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ( event.getAction() == MotionEvent.ACTION_UP )
                {
                    if ( isRecording ) {
                        try {
                            Thread.sleep(RECORD_DELAY_TIME_MS);
                        } catch (Exception e) {
                        }

                        stopRecording(false);
                    } else {
                        startTakingPicture();
                    }
                }
                return isPhotoOnly;
            }
        });
    }

    private void startTakingPicture()
    {
        if ( !isPictureTaking ) {
            if ( mCamera != null ) {
                isPictureTaking = true;
                setCameraRotation();
                mCamera.takePicture(null, null, mPicture);
            }
        }
    }

    private void stopRecording(boolean isPause)
    {
        if ( isRecording )
        {
            //stop record
            // BEGIN_INCLUDE(stop_release_media_recorder)

            // stop recording and release camera
            try {
                mMediaRecorder.stop();  // stop the recording
            } catch (RuntimeException e) {
                // RuntimeException is thrown when stop() is called immediately after start().
                // In this case the output file is not properly constructed ans should be deleted.
                Log.d(TAG, "RuntimeException: stop() is called immediately after start()");
                //noinspection ResultOfMethodCallIgnored
//                mOutputFile.delete();
//                GlobalFunc.showToast(CameraActivity.this, "Video is too short", true);

                customHandler.removeCallbacks(updateTimerThread);

                mProgress.setProgress(0);
                mSwitchCameara.setVisibility(View.VISIBLE);

                releaseMediaRecorder();
//                releaseCamera();
                mCamera.lock();         // take camera access back from MediaRecorder
                isRecording = false;
                mProgress.setVisibility(View.INVISIBLE);
//                if ( !isPause )
//                    initCameraPreview();

                startTakingPicture();
                return;
            }

//            timeSwapBuff += timeInMilliseconds;
            customHandler.removeCallbacks(updateTimerThread);

            releaseMediaRecorder(); // release the MediaRecorder object
            mCamera.lock();         // take camera access back from MediaRecorder

            // inform the user that recording has stopped
            isRecording = false;
//            releaseCamera();
            // END_INCLUDE(stop_release_media_recorder)

            mBtnCamera.setVisibility(View.INVISIBLE);
            mBtnRefresh.setVisibility(View.VISIBLE);
            mBtnOK.setVisibility(View.VISIBLE);
            isVideo = true;
            mImageView.setVisibility(View.GONE);
            mVideoView.setVisibility(View.VISIBLE);
            mPreview.setVisibility(View.GONE);
            mProgress.setVisibility(View.INVISIBLE);
            mSwitchCameara.setVisibility(View.GONE);

            mVideoView.setVideoPath(mFilePath);
            mVideoView.start();
        }
    }

    private long startHTime = 0L;
    private Handler customHandler = new Handler();
    long timeInMilliseconds = 0L;
    //    long timeSwapBuff = 0L;
    long updatedTime = 0L;

    private Runnable updateTimerThread = new Runnable() {

        public void run() {

            timeInMilliseconds = SystemClock.uptimeMillis() - startHTime;

//            updatedTime = timeSwapBuff + timeInMilliseconds;
            updatedTime = timeInMilliseconds;

            if ( updatedTime >= MAX_DURATION_MS )
            {
                mProgress.setProgress(100);
            }
            else
            {
                mProgress.setProgress((int)updatedTime*100/MAX_DURATION_MS);
            }

//            Log.e("CameraRecording","Current Progress:"+mProgress.getProgress());

            customHandler.postDelayed(this, 100);
        }

    };

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        File file = new File(mFilePath);
        if ( file.exists() )
            file.delete();
        file = new File(mImageFilePath);
        if ( file.exists() )
            file.delete();
    }

    private void setCameraRotation()
    {
        Camera.CameraInfo info = new Camera.CameraInfo();
        if ( isBackCameraSelected )
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        else
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, info);
        int rotate = 0;

        if ( isBackCameraSelected )
            rotate = (info.orientation + mOrientation + 360) % 360;
        else
            rotate = (info.orientation - mOrientation + 360) % 360;

        Camera.Parameters params = mCamera.getParameters();
        mTakenOrientation = rotate;
        params.setRotation(rotate);
        DebugConfig.error("*****", "setParam:"+rotate);
        mCamera.setParameters(params);
    }

    private void sendImageVideos()
    {
        final Intent i = new Intent();
        i.putExtra("isVideo", isVideo);

        if ( isVideo ) {
            mVideoView.stopPlayback();    //this cause Android 4.1.2 crash

            mProgressDlg.show();

            AsyncTask task = new AsyncTask<Void, Void, String>(){
                @Override
                protected String doInBackground(Void... voids) {
                    String filePath = null;

                    filePath = GlobalConstrants.CAMERA_TEMP_PATH + System.currentTimeMillis();
                    try {
                        boolean isCompressSuccess = MediaController.getInstance().convertVideo(mFilePath, filePath);

                        if ( isCompressSuccess )
                        {
                            DebugConfig.error("*****", String.format("Compress Success:%d->%d", new File(mFilePath).length(), new File(filePath).length()));
                            new File(mFilePath).delete();
                        }

                        return isCompressSuccess ? filePath : null;
                    } catch ( Exception e ) {
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(String filePath) {
                    mProgressDlg.dismiss();

                    super.onPostExecute(filePath);
                    if ( filePath != null )
                        i.putExtra("path", filePath);
                    else
                        i.putExtra("path", mFilePath);

                    setResult(RESULT_OK, i);
                    finish();
                }
            }.execute();

            mProgressDlg.addAsyncTask(task);
        }
        else {
            String filePath = GlobalConstrants.CAMERA_TEMP_PATH + System.currentTimeMillis();
            boolean isCompressSuccess = true;

            if ( needCompressImage ) {
                isCompressSuccess = ImageLoaderUtil.compressImage(mImageFilePath, filePath);

                if ( isCompressSuccess )
                    DebugConfig.error("*****", String.format("Compress Image:%d->%d", new File(mImageFilePath).length(), new File(filePath).length()));
            }
            else {
                try {
                    FileUtils.moveFile(new File(mImageFilePath), new File(filePath));
                } catch (IOException e) {
                    e.printStackTrace();
                    isCompressSuccess = false;
                }
            }

            if (isCompressSuccess) {
                new File(mImageFilePath).delete();
                i.putExtra("path", filePath);
            } else {
                i.putExtra("path", mImageFilePath);
            }

            setResult(RESULT_OK, i);
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btnBack:
                onBackPressed();
                break;

            case R.id.btn_ok:
                if ( showPreview )
                {
                    Intent intent = new Intent(CameraActivity.this, WallPaperPreviewActivity.class);
                    intent.putExtra("path", mImageFilePath);
                    intent.putExtra("type", WallPaperPreviewActivity.TYPE_CAMERA);
                    startActivityForResult(intent, REQ_PREVIEW);
                } else {
                    sendImageVideos();
                }
                break;

            case R.id.btn_refresh:
                mBtnCamera.setVisibility(View.VISIBLE);
                mBtnRefresh.setVisibility(View.INVISIBLE);
                mBtnOK.setVisibility(View.INVISIBLE);
                mImageView.setVisibility(View.INVISIBLE);
                mVideoView.setVisibility(View.INVISIBLE);
                mPreview.setVisibility(View.VISIBLE);
                mProgress.setVisibility(View.INVISIBLE);
                mSwitchCameara.setVisibility(View.VISIBLE);

                mVideoView.stopPlayback();
                mVideoView.clearAnimation();
                mVideoView.suspend();
                mVideoView.setVideoURI(null);

                File file = new File(mFilePath);
                if ( file.exists() )
                    file.delete();

                file = new File(mImageFilePath);
                if ( file.exists() )
                    file.delete();

                if ( mCamera == null ) {
                    initCameraPreview();
                }
                else
                {
                    mCamera.lock();
                    mCamera.startPreview();
                }

                break;

            case R.id.switchCamera:
                switchCamera();
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        orientationListener.enable();

        BaseActivity.setActive(true);

        if ( mBtnCamera.getVisibility() == View.VISIBLE ) {
            mProgress.setVisibility(View.INVISIBLE);
            customHandler.removeCallbacks(updateTimerThread);

            if ( mPreview.getSurfaceTexture() != null )
                initCameraPreview();
            else
            {
                mPreview.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                        initCameraPreview();
                    }

                    @Override
                    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

                    }

                    @Override
                    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                        return false;
                    }

                    @Override
                    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

                    }
                });
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        orientationListener.disable();

        BaseActivity.setActive(false);

        if ( isRecording ) {
            stopRecording(true);
        }
        // if we are using MediaRecorder, release it first
        releaseMediaRecorder();
        // release the camera immediately on pause event
        releaseCamera();
    }

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            // clear recorder configuration
            mMediaRecorder.reset();
            // release the recorder object
            mMediaRecorder.release();
            mMediaRecorder = null;
            // Lock camera for later use i.e taking it back from MediaRecorder.
            // MediaRecorder doesn't need it anymore and we will release it if the activity pauses.
            mCamera.lock();
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            // release the camera for other applications
            mCamera.release();
            mCamera = null;
        }
    }

    private boolean switchCamera(){
        if ( Camera.getNumberOfCameras() < 2 )
            return false;

        releaseMediaRecorder();
        releaseCamera();

        isBackCameraSelected = !isBackCameraSelected;

        return initCameraPreview();
    }

    private boolean initCameraPreview() {
        // BEGIN_INCLUDE (configure_preview)
        try {
            if (isBackCameraSelected)
                mCamera = CameraHelper.getDefaultCameraInstance();
            else
                mCamera = CameraHelper.getDefaultFrontFacingCameraInstance();
        }catch (Exception e){
//            GlobalFunc.showToast(CameraActivity.this, "Failed to connect camera device", false);
            CameraActivity.this.finish();
            return false;
        }

        Camera.Parameters parameters = mCamera.getParameters();

        WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();

        Point screenResolution = new Point();
        display.getSize(screenResolution);
//        Point cameraResolution = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, screenResolution);
        Point bestPreviewSize = CameraConfigurationUtils.findBestPreviewSizeValue(parameters, screenResolution);

//        boolean isScreenPortrait = screenResolution.x < screenResolution.y;
//        boolean isPreviewSizePortrait = bestPreviewSize.x < bestPreviewSize.y;
//
//        Point previewSizeOnScreen;
//
//        if (isScreenPortrait == isPreviewSizePortrait) {
//            previewSizeOnScreen = bestPreviewSize;
//        } else {
//            previewSizeOnScreen = new Point(bestPreviewSize.y, bestPreviewSize.x);
//        }

        String parametersFlattened = parameters == null ? null : parameters.flatten(); // Save these, temporarily

        try {
            CameraConfigurationUtils.setFocus(parameters, true, false, false);
        } catch (RuntimeException re) {
            if (parametersFlattened != null) {
                parameters = mCamera.getParameters();
                parameters.unflatten(parametersFlattened);
                try {
                    mCamera.setParameters(parameters);
                    CameraConfigurationUtils.setFocus(parameters, true, false, true);
                } catch (RuntimeException re2) {
                    // Well, darn. Give up
                    Log.w(TAG, "Camera rejected even safe-mode parameters! No configuration");
                }
            }
        }

        parameters.setPreviewSize(bestPreviewSize.x, bestPreviewSize.y);
        parameters.setPictureSize(bestPreviewSize.x, bestPreviewSize.y);

        // We need to make sure that our preview and recording video size are supported by the
        // camera. Query camera to find all the sizes and choose the optimal size given the
        // dimensions of our preview surface.

        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        if ( isBackCameraSelected )
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
        else
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);

        parameters.setRotation(cameraInfo.orientation);
/*
        List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
        Camera.Size optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                mSupportedPreviewSizes, mPreview.getWidth(), mPreview.getHeight());

        // Use the same size for recording profile.
        CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        profile.videoFrameWidth = optimalSize.width;
        profile.videoFrameHeight = optimalSize.height;

        // likewise for the camera object itself.
        parameters.setPreviewSize(profile.videoFrameWidth, profile.videoFrameHeight);
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        if ( supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO) )
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
*/
        DebugConfig.error("*****", "setParamInit:"+cameraInfo.orientation);
        mCamera.setParameters(parameters);
        try {
            // Requires API level 11+, For backward compatibility use {@link setPreviewDisplay}
            // with {@link SurfaceView}
            mCamera.setPreviewTexture(mPreview.getSurfaceTexture());
        } catch (IOException e) {
            Log.e(TAG, "Surface texture is unavailable or unsuitable" + e.getMessage());
            return false;
        }
        // END_INCLUDE (configure_preview)


        // Step 1: Unlock and set camera to MediaRecorder
        mCamera.setDisplayOrientation(90); // use for set the orientation of the preview
        mCamera.startPreview();

        isCameraInitialized = true;

        return true;
    }

    Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            String tmpPath = GlobalConstrants.LOCAL_PATH + "pica_temp";
            File pictureFile = new File(tmpPath);
            if (pictureFile == null) {
                isPictureTaking = false;
                return;
            }
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();
            } catch (FileNotFoundException e) {

            } catch (IOException e) {
            }

//            releaseMediaRecorder(); // release the MediaRecorder object
//            mCamera.cii_lock();         // take camera access back from MediaRecorder
//
//            releaseCamera();

            mBtnCamera.setVisibility(View.INVISIBLE);
            mBtnRefresh.setVisibility(View.VISIBLE);
            mBtnOK.setVisibility(View.VISIBLE);
            isVideo = false;
            isPictureTaking = false;
            mImageView.setVisibility(View.VISIBLE);
            mVideoView.setVisibility(View.GONE);
            mPreview.setVisibility(View.GONE);
            mSwitchCameara.setVisibility(View.GONE);

            mImageFilePath = GlobalConstrants.CAMERA_TEMP_PATH + System.currentTimeMillis() + ".jpg";

//            if ( BitmapUtil.makePostBitmap(tmpPath, mImageFilePath, CameraActivity.this) == false )
//            {
            File file = new File(tmpPath);
            File file1 = new File(mImageFilePath);

            if ( file != null && file.exists() )
                file.renameTo(file1);
//            }
//            else
//            {
//                File file = new File(tmpPath);
//                if ( file != null && file.exists() )
//                    file.delete();
//            }

            ImageLoaderUtil.loadImageFile(CameraActivity.this, mImageFilePath, mImageView);
        }
    };
    private boolean prepareVideoRecorder(){
        if ( mMediaRecorder != null )
            releaseMediaRecorder();
        // BEGIN_INCLUDE (configure_media_recorder)
        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mr, int what, int extra) {
                if ( what == MEDIA_RECORDER_INFO_MAX_DURATION_REACHED )
                    stopRecording(false);
            }
        });

        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        if ( isBackCameraSelected )
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, cameraInfo);
        else
            Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_FRONT, cameraInfo);

        int rotate = 0;

        if ( isBackCameraSelected )
            rotate = (cameraInfo.orientation + mOrientation + 360) % 360;
        else
            rotate = (cameraInfo.orientation - mOrientation + 360) % 360;

        mTakenOrientation = rotate;
        mMediaRecorder.setOrientationHint(rotate); // use for set the orientation of output video

        // Step 2: Set sources
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT );
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        try {
            // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
            if (MediaController.getInstance().canCompress())  //If compress available, use high quality camera and compress it
            {
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
                mMediaRecorder.setProfile(profile);
            } else {    //If compress unavailable, set video size to smaller size when recording, and not compress it
                CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
                mMediaRecorder.setProfile(profile);
            }
        } catch (Exception e) {
            return false;
        }

        // Step 4: Set output file
        File file = new File(mFilePath);
        if ( file.exists() )
            file.delete();

        mFilePath = GlobalConstrants.CAMERA_TEMP_PATH + System.currentTimeMillis() + ".mp4";
        mOutputFile = new File(mFilePath);
        if (mOutputFile == null) {
            return false;
        }
        mMediaRecorder.setOutputFile(mOutputFile.getPath());
        // END_INCLUDE (configure_media_recorder)

        mMediaRecorder.setMaxDuration(MAX_DURATION_MS);

        // Step 5: Prepare configured MediaRecorder
        try {
            mMediaRecorder.prepare();
        } catch (IllegalStateException e) {
            Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
            releaseMediaRecorder();
            return false;
        }
        return true;
    }
    /**
     * Asynchronous task for preparing the {@link android.media.MediaRecorder} since it's a long blocking
     * operation.
     */
    class MediaPrepareTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            // initialize video camera
            if (prepareVideoRecorder()) {
                // Camera is available and unlocked, MediaRecorder is prepared,
                // now you can start recording

                try {
                    mMediaRecorder.start();
                }catch (Exception e)
                {
                    mCamera.lock();
                    return false;
                }

                startHTime = SystemClock.uptimeMillis();
                customHandler.postDelayed(updateTimerThread, 100);
            } else {
                // prepare didn't work, release the camera
                releaseMediaRecorder();
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) {
//                GlobalFunc.showToast(CameraActivity.this, "Failed to start recording", false);
//                CameraActivity.this.finish();
                isRecording = false;
                return;
            }

            // inform the user that recording has started
            if ( isRecording ) {
                mProgress.setProgress(0);
                mProgress.setVisibility(View.VISIBLE);
                mSwitchCameara.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if ( resultCode == RESULT_OK )
        {
            if ( requestCode == REQ_PREVIEW )
            {
                sendImageVideos();
            }
        }
    }
}
