package com.multimediachat.ui;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.multimediachat.R;
import com.multimediachat.app.AndroidUtility;
import com.multimediachat.app.DebugConfig;
import com.multimediachat.app.GlideApp;
import com.multimediachat.app.NotificationCenter;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.util.BitmapUtil;
import com.multimediachat.util.ImageLoaderUtil;
import com.multimediachat.ui.views.gestures.GestureController;
import com.multimediachat.ui.views.gestures.State;
import com.multimediachat.ui.views.gestures.commons.CropAreaView;
import com.multimediachat.ui.views.gestures.views.GestureImageView;

import java.io.File;
import java.util.ArrayList;

public class PhotoCropActivity extends BaseActivity implements OnClickListener, NotificationCenter.NotificationCenterDelegate{

    public final static int GET_CAMERA = 1009;
    public final static int GET_GALLERY = 1010;
    public final static int REQUEST_CODE_PHOTO_CAMERA = 1011;
//	boolean isImageDeleted = false;

    private Uri mCropImageUri;

    private GestureImageView imageView;
    private CropAreaView cropView;
    private int gridRulesCount = 2;

    private int CROP_SIZE = 200;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photocropactivity);
        setActionBarTitle(getString(R.string.myprofile_str_change_picture));
        addImageButton(R.drawable.btn_view_more, R.id.btn_more, POS_RIGHT);

        findViewById(R.id.btn_ok).setOnClickListener(this);
        findViewById(R.id.btn_refresh).setOnClickListener(this);

        NotificationCenter.getInstance().addObserver(this, NotificationCenter.photocrop_photos_compressed);

        imageView = findViewById(R.id.image_crop_viewer);

        cropView = findViewById(R.id.image_crop_area);
        cropView.setImageView(imageView);
        cropView.setRulesCount(gridRulesCount, gridRulesCount);

        displayImage(getIntent().getStringExtra("photoPath"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationCenter.getInstance().removeObserver(this, NotificationCenter.photocrop_photos_compressed);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void displayImage(String photo)
    {
        // bitmap resize for large image : soch
        if ( photo == null || photo.isEmpty() ) {
//			isImageDeleted = true;
            imageView.setImageResource(R.drawable.profilephoto);
            return;
        }

        if (!photo.startsWith("file://"))
            photo = "file://"+photo;

        if ( !(new File(Uri.parse(photo).getPath()).exists()) )
        {
//			isImageDeleted = true;
            imageView.setImageResource(R.drawable.profilephoto);
            return;
        }

//		isImageDeleted = false;
        mCropImageUri = Uri.parse(photo);
        ImageLoaderUtil.loadImageUri(this, mCropImageUri, imageView);
    }

    @Override
    public void didReceivedNotification(int id, final Object... args) {
        if (id == NotificationCenter.photocrop_photos_compressed) {
            AndroidUtility.RunOnUIThread(new Runnable() {
                @Override
                public void run() {
                    ArrayList<String> pathList = (ArrayList<String>) args[0];
                    if (pathList != null && pathList.size() > 0) {
                        for (String photo : pathList) {
                            displayImage(photo);
                            break;
                        }
                    }
                }
            });
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        if ( requestCode == GET_CAMERA )
        {
            if ( resultCode == RESULT_OK )
            {
                File f = new File(GlobalConstrants.LOCAL_PATH);
                if (f != null) {
                    for (File temp : f.listFiles()) {
                        if (temp != null && temp.getName() != null && temp.getName().equals("pica_temp")) {
                            f = temp;
                            break;
                        }
                    }

                    if (f.isFile()) {
                        String photoPath = f.getAbsolutePath();

                        final ArrayList<String> photos = new ArrayList<String>();
                        photos.add(0, photoPath);

                        if ( photos.size() > 0 )  {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.photocrop_photos_compressed, photos);
                                }
                            }).start();
                        }
                    }
                }
            }
        } else if ( requestCode == REQUEST_CODE_PHOTO_CAMERA )
        {
            if ( resultCode == RESULT_OK )
            {
                String photoPath = data.getStringExtra("path");
                final ArrayList<String> photos = new ArrayList<String>();
                photos.add(0, photoPath);

                if ( photos.size() > 0 )  {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.photocrop_photos_compressed, photos);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void selectImage() throws Exception {
        final Dialog dlg = new Dialog(this);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dlg.setContentView(R.layout.photo_picker_dlg);
        dlg.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        RelativeLayout lyt_album = (RelativeLayout) dlg.findViewById(R.id.lyt_album);
        RelativeLayout lyt_camera = (RelativeLayout) dlg.findViewById(R.id.lyt_camera);
        RelativeLayout lyt_remove = (RelativeLayout) dlg.findViewById(R.id.lyt_remove);

        lyt_album.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dlg.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                checkAlbumPermission(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(PhotoCropActivity.this, PhotoPickerActivity.class);
                        i.putExtra("setbackground", true);
                        i.putExtra("type",PhotoPickerActivity.TYPE_PHOTOCROP);
                        startActivity(i);
                    }
                });
            }
        });

        lyt_camera.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dlg.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                checkCameraPermission(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                    Intent intent = new Intent(PhotoCropActivity.this, CameraActivity.class);
                    intent.putExtra("isphotoonly", true);
                    startActivityForResult(intent, REQUEST_CODE_PHOTO_CAMERA);
                    }
                });
            }
        });

        lyt_remove.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    dlg.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                imageView.setImageResource(R.drawable.profilephoto);

                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        dlg.setCanceledOnTouchOutside(true);
        dlg.show();
    }

    private void onMorePressed()
    {
        try {
            selectImage();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_MENU:
                onMorePressed();
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_more:
                onMorePressed();
                break;
            case R.id.btn_ok:
                cropImage();
                break;
            case R.id.btn_refresh:
                rotateImage(true);
                break;
            default:
                super.onClick(v);
                break;
        }
    }

    private void rotateImage(boolean animate) {
        final GestureController controller = imageView.getController();

        if (controller.isAnimating()) {
            return; // Waiting for animation end
        }

        final State state = controller.getState().copy();
        final PointF pivot = getPivot();

        // Rotating to closest next 90 degree ccw
        float rotation = Math.round(state.getRotation()) % 90f == 0f
                ? state.getRotation() + 90f : (float) Math.ceil(state.getRotation() / 90f) * 90f;
        state.rotateTo(rotation, pivot.x, pivot.y);

        if (animate) {
            // Animating state changes. Do not forget to make a state's copy prior to any changes.
            controller.setPivot(pivot.x, pivot.y);
            controller.animateStateTo(state);
        } else {
            // Immediately applying state changes
            controller.getState().set(state);
            controller.updateState();
        }
    }

    private PointF getPivot() {
        // Default pivot point is a view center
        PointF pivot = new PointF();
        pivot.x = 0.5f * imageView.getController().getSettings().getViewportW();
        pivot.y = 0.5f * imageView.getController().getSettings().getViewportH();
        return pivot;
    }

    protected void cropImage() {
        Bitmap cropped = imageView.crop();
        if (cropped != null) {
            String cropPath = GlobalConstrants.AVATAR_DIR_PATH + "croppedPhotoTmp";
            Bitmap resized = null;

            if ( cropped.getWidth() != CROP_SIZE || cropped.getHeight() != CROP_SIZE )
            {
                resized = BitmapUtil.resizeBitmap(cropped, CROP_SIZE, CROP_SIZE);
            } else {
                resized = cropped;
            }

            DebugConfig.error("*****", String.format("PhotoCrop:(%d,%d)->(%d,%d)", cropped.getWidth(), cropped.getHeight(), resized.getWidth(), resized.getHeight()));

            if ( BitmapUtil.writeBmpToFile(resized, cropPath, GlobalConstrants.JPEG_QUALITY) )
            {
                DebugConfig.error("*****",String.format("Cropped Image Size:%d", new File(cropPath).length()));
                Intent intent = new Intent();
                intent.putExtra("croppedPhotoPath", cropPath);
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }
}