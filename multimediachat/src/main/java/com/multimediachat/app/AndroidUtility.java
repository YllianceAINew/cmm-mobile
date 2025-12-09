package com.multimediachat.app;

import com.multimediachat.R;
import com.multimediachat.app.im.engine.Contact;
import com.multimediachat.global.GlobalFunc;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AndroidUtility {
    public static final int ERROR_CONNECTION = 1;
    public static final int ERROR_XMPP_LOGIN = 2;
    public static final int ERROR_USER_DELETED = 5;
    public static final int ERROR_USER_REPORTED = 6;
    public static final int ERROR_WRONG_PHONE_FORMAT = 7;
    public static final int ERROR_WRONG_PHONENUMBER = 8;
    public static final int ERROR_EMPTY_COUNTRYCODE = 9;
    public static final int ERROR_CODE_PASSWORD_EMPTY = 10;
    public static final int ERROR_CODE_PASSWORD_NOT_MATCH = 11;
    public static final int ERROR_CODE_PASSWORD_LENGTH_NOT_ENOUGH = 12;
    public static final int ERROR_FILE_SIZE_TOO_LARGE = 13;
    public static final int ERROR_FILE_TYPE_WRONG = 14;
    public static final int ERROR_SELECT_CONTACT = 15;
    public static final int ERROR_CODE_NICKNAME = 16;
    public static final int ERROR_CODE_UBCHATID = 17;
    public static final int ERROR_CODE_FEEDBACK = 18;
    public static final int ERROR_CODE_EMAIL_NOT_CORRECT = 19;
    public static final int ERROR_CODE_TOO_MANY_REQUESTS = 20;
    public static final int ERROR_CODE_PASSWORD = 21;
    public static final int ERROR_CODE_UNREGISTERED_USERID = 22;
    public static final int ERROR_CODE_UNREGISTERED_PHONENUMBER = 23;
    public static final int ERROR_CODE_INVALID_ID = 24;
    public static final int ERROR_CODE_NO_LOGIN_KEY = 25;
    public static final int ERROR_CODE_NOT_ALLOWED = 26;
    public static final int ERROR_CODE_API_KEY = 27;
    public static final int ERROR_CODE_NO_SIMCARD = 28;
    public static final int ERROR_CODE_USERID_FORMAT = 29;
    public static final int ERROR_CODE_EMPTY_USERNAME = 30;
    public static final int ERROR_CODE_EMPTY_BIRTHNUM = 31;
    public static final int ERROR_CODE_DUPLICATE_USERID = 32;
    public static final int ERROR_CODE_FAILED_DELETE_ROOM = 33;
    public static final int ERROR_CODE_FAILED_DELETE_CONTACT = 34;
    public static final int ERROR_CODE_FAILED_CHANGE_PWD = 35;
    public static final int ERROR_CODE_FAILED_CHECK_USER_INFO = 36;

	public static float density = 1;
	public static Point displaySize = new Point();
	
	private static Boolean isTablet = null;
	
	private String phoneIMEI;
	
	private static volatile AndroidUtility Instance = null;
    public static AndroidUtility getInstance() {
    	AndroidUtility localInstance = Instance;
        if (localInstance == null) {
            synchronized (AndroidUtility.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new AndroidUtility();
                }
            }
        }
        return localInstance;
    }
    
    static {
        density = ImApp.applicationContext.getResources().getDisplayMetrics().density;
        checkDisplaySize();
    }
    
    public AndroidUtility(){
    }

    public static int dp(int value) {
        return (int)(Math.max(1, density * value));
    }
    
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public static void checkDisplaySize() {
        try {
            WindowManager manager = (WindowManager)ImApp.applicationContext.getSystemService(Context.WINDOW_SERVICE);
            if (manager != null) {
                Display display = manager.getDefaultDisplay();
                if (display != null) {
                    if(android.os.Build.VERSION.SDK_INT < 13) {
                        displaySize.set(display.getWidth(), display.getHeight());
                    } else {
                        display.getSize(displaySize);
                    }
                }
            }
        } catch (Exception e) {
        }
    }
    
    public static boolean isTablet() {
        if (isTablet == null) {
            isTablet = ImApp.applicationContext.getResources().getBoolean(R.bool.isTablet);
        }
        return isTablet;
    }

    public static void hideSystemUI(Activity activity) {
        // Set the IMMERSIVE flag.
        // Set the content to appear under the system bars so that the content
        // doesn't resize when the system bars hide and show.

        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    public static void showKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager inputManager = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);

        ((InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(view, 0);
    }

    public static void hideKeyboard(View view) {
        if (view == null) {
            return;
        }
        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (!imm.isActive()) {
            return;
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    
    public static void RunOnUIThread(Runnable runnable) {
        RunOnUIThread(runnable, 0);
    }

    public static void RunOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            ImApp.applicationHandler.post(runnable);
        } else {
            ImApp.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void showErrorMessage(Context context, int res) {
        // custom dialog

        final Dialog dlg = GlobalFunc.createDialog(context, R.layout.msgdialog, true);

        TextView title = (TextView) dlg.findViewById(R.id.msgtitle);
        TextView content = (TextView) dlg.findViewById(R.id.msgcontent);

        if (res == ERROR_CONNECTION) {
            title.setText(R.string.error);
            content.setText(R.string.content_connectError);
        } else if (res == ERROR_XMPP_LOGIN) {
            title.setText(R.string.error);
            content.setText(R.string.content_loginError);
        } else if (res == ERROR_CODE_PASSWORD_NOT_MATCH) {
            title.setText(R.string.error);
            content.setText(R.string.passwords_not_match);
        } else if (res == ERROR_CODE_PASSWORD_LENGTH_NOT_ENOUGH) {
            title.setText(R.string.error);
            content.setText(R.string.passwords_length_not_enough);
        } else if (res == ERROR_CODE_PASSWORD_EMPTY) {
            title.setText(R.string.warning);
            content.setText(R.string.input_password);
        } else if (res == ERROR_WRONG_PHONENUMBER) {
            title.setText(R.string.error_message_invalid_phone_number_title);
            content.setText(R.string.error_message_invalid_phone_number);
        } else if (res == ERROR_CODE_NO_LOGIN_KEY) {
            title.setText(R.string.warning);
            content.setText(R.string.error_message_not_allowed);
        } else if (res == ERROR_CODE_NOT_ALLOWED) {
            title.setText(R.string.error);
            content.setText(R.string.error_message_not_allowed);
        } else if (res == ERROR_CODE_PASSWORD) {
            title.setText(R.string.error);
            content.setText(R.string.error_message_wrong_password);
        } else if (res == ERROR_CODE_API_KEY) {
            title.setText(R.string.alert);
            content.setText(R.string.error_message_wrong_api_key);
        } else if (res == ERROR_CODE_NO_SIMCARD) {
            title.setText(R.string.error);
            content.setText(R.string.error_message_no_simcard);
        } else if (res == ERROR_CODE_USERID_FORMAT) {
            title.setText(R.string.error);
            content.setText(R.string.error_message_userid_format);
        } else if (res == ERROR_CODE_EMPTY_USERNAME) {
            title.setText(R.string.error);
            content.setText(R.string.error_message_empty_username);
        } else if (res == ERROR_CODE_EMPTY_BIRTHNUM) {
            title.setText(R.string.error);
            content.setText(R.string.error_message_empty_birthnum);
        } else if (res == ERROR_CODE_DUPLICATE_USERID) {
            title.setText(R.string.error);
            content.setText(R.string.error_message_duplicate_userid);
        } else if (res == ERROR_CODE_FAILED_DELETE_ROOM) {
            title.setText(R.string.error);
            content.setText(R.string.error_message_failed_delete_room);
        } else if (res == ERROR_CODE_FAILED_DELETE_CONTACT) {
            title.setText(R.string.error);
            content.setText(R.string.error_message_failed_delete_contact);
        } else if (res == ERROR_CODE_FAILED_CHANGE_PWD) {
            title.setText(R.string.error);
            content.setText(R.string.error_message_failed_change_pwd);
        } else if (res == ERROR_CODE_UNREGISTERED_USERID) {
            title.setText(R.string.error);
            content.setText(R.string.error_message_unregistered_userid);
        } else if (res == ERROR_CODE_UNREGISTERED_PHONENUMBER) {
            title.setText(R.string.error);
            content.setText(R.string.error_message_unregistered_phonenumber);
        } else if (res == ERROR_CODE_FAILED_CHECK_USER_INFO) {
            title.setText(R.string.error_message_failed_check_user_info_title);
            content.setText(R.string.error_message_failed_check_user_info);
        }

        Button dlg_btn_ok = (Button) dlg.findViewById(R.id.btn_ok);
        dlg_btn_ok.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    dlg.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        dlg.setCanceledOnTouchOutside(false);
        try {
            dlg.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
