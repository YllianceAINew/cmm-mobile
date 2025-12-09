package com.multimediachat.global;

import android.os.Environment;

import com.multimediachat.util.EncDecDes;


public class GlobalConstrants {

    public static final String MESSAGE_PARAM_CONTENT = "contents";

    private static String LOCAL_DIR_NAME = "MultimediaChat";
    public static String LOCAL_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + LOCAL_DIR_NAME
            + "/";

    public static String CHAT_DIR_NAME = "Chat";
    public static String OTHER_DIR_NAME = "Other";
    public static String POSTS_DIR_NAME = "Posts";
    public static String CAMERA_DIR_NAME = "Camera";
    public static String AVATAR_DIR_NAME = "Avatar";    // added by JHK(2019.11.07) directory "Avatar" save avatar files

    public static String ADMIN_USERNAME = "admin";
    public static String ADMIN_NICKNAME = "RSVCS Team";

    public static String AVATAR_DIR_PATH = LOCAL_PATH + OTHER_DIR_NAME + "/" + AVATAR_DIR_NAME + "/";
    public static String CAMERA_TEMP_PATH = LOCAL_PATH + OTHER_DIR_NAME + "/" + CAMERA_DIR_NAME + "/camera";
    public static String CAMERA_DIR_PATH = LOCAL_PATH + OTHER_DIR_NAME + "/" + CAMERA_DIR_NAME + "/";
    public static String SPLIT_DIR_PATH = LOCAL_PATH + OTHER_DIR_NAME + "/.temp/";

    public static String LOGIN_KEY_PATH = LOCAL_PATH + "loginkey.ims";

    public static String USER_PHOTO_PATH = GlobalConstrants.AVATAR_DIR_PATH + EncDecDes.getInstance().generateFileName("myprofile_photo.jpg");

    public static final String DEFAULT_TIMEZONE = "GMT+00:00";

    /**
     * Intent broadcast code
     **/
    public final static String UPLOAD_FAILED = "UPFAILED";
    public final static String UPLOAD_SUCCESS = "UPSUCCESS";
    public final static String UPLOAD_PROGRESS = "UPLOADPROGRESS";
    public final static String DOWNLOAD_SUCCESS = "DOWNLOADSUCCESS";
    public final static String DOWNLOAD_FAILED = "DOWNLOADFAILED";
    public final static String DOWNLOAD_PROGRESS = "DOWNLOADPROGRESS";

    public static String api_header = "https://";
    /*public static String server_domain = "172.16.61.211";
    public static String file_server_domain = "172.16.61.236";
    public static String voip_server_domain = "172.16.61.217";*/
    public static String server_domain = "192.168.1.231";
    public static String file_server_domain = "";
    public static String voip_server_domain = "192.168.1.237";
    public static String stun_server_domain = "";

    public static int JPEG_QUALITY = 60;
    public static int FILE_SIZE_LIMIT_BEFORE_COMPRESS_IN_BYTES = 20 * 1024 * 1024;
    public static int FILE_SIZE_LIMIT_IN_BYTES = 10 * 1024 * 1024;

    public static int FILE_SPLIT_SIZE = 368 * 4 * 1024; // (About 1MB)
    public static int FILE_SPLIT_SLEEP = 10;				// (ms)
    public static boolean FILE_SPLITTING = false;

    public static String CREATE_ACCOUNT = "createAccount.php";
    public static String LOGIN_ACCOUNT_URL = "loginAccount.php";
    public static String SEARCH_BY_ID_AND_PHONENUMBER_URL = "searchByIDAndPhoneNumber.php";
    public static String CHECK_PHONENUMBER = "checkPhoneNumber.php";
    public static String CONFIRM_PWD = "confirmPassword.php";
    public static String CHANGE_PWD = "changePassword.php";
    public static String CHECK_BIRTHNUM = "checkBirthnum.php";
    public static String GET_PROFILE = "getProfile.php";
    public static String UPLOAD_PHOTO = "uploadProfilePhoto.php";
    public static String REMOVE_PHOTO = "removeProfilePhoto.php";
    public static String GET_PROFILE_PHOTO_DOWNLOAD_URL = "getProfilePhoto.php";

    public static String UPLOAD_FILE = "uploadFile.php";
    public static String DOWNLOAD_FILE = "downloadFile.php";
    public static String CHECK_MIMETYPE = "checkMimetype.php";

    public static String CHECK_CALLABLE = "callable.php";

    //contacts
    public static String GET_MOBILE_CONTACTS = "getMobileContacts.php";

    public final static int STEP_NEED_LOGIN = 0;
    public final static int STEP_LOGGED_IN = 1;

    public final static String store_step = "STATE_STEP";
    public final static String store_picaAccountId = "PicaAccountID";
    public final static String store_picaProviderId = "PicaProviderID";

    public final static int IMAGE_SIZE = 400;
    public final static int IMAGE_SMALL_SIZE = 200;
    public final static int PHOTO_COVER_SIZE = 864;
    public final static int DEFAULT_STICKER_SIZE = 240;
    public final static int POST_IMAGE_SIZE = 400;

    public final static String LOCALE = "app_locale";
    public final static String FONT_SIZE_INDEX = "app_font_size_index";

    // this value stores time interval of current local time with server date.
    public final static String TIME_DIFF_WITH_SERVER = "time_diff_with_server";

    // loaded MainTabActivity
    public final static String LOADED_MAINTABACTIVITY = "loaded_maintabactivity";

    public final static String APP_ID = "497179630403921";

    public final static String API_HEADER = "api_header";
    public final static String API_SERVER = "api_server";
    public final static String VOIP_SERVER = "voip_server";
    public final static String TLS_SECURITY = "tls_security";

    public final static String API_URL = "api_url";
    public final static String FILE_SERVER = "file_server";
    public final static String FILE_SERVER_URL = "file_server_url";

    public final static String VIDEO_BITRATE = "video_bitrate";

    public final static String INTENT_CONNSTAT_CHANGED = "mobilecom_connect_status_changed";

    public final static String KEY_TOTAL_CONNECT_STATUS = "key_total_connect_status";
    public final static String KEY_TOTAL_CONNECT_ERRMSG = "key_total_connect_error_message";
    public final static String KEY_TOTAL_CONNECT_ERRCODE = "key_total_connect_error_code";

    public final static String KEY_XMPP_CONNECT_STATUS = "key_xmpp_connect_status";
    public final static String KEY_XMPP_CONNECT_ERRMSG = "key_xmpp_connect_error_message";
    public final static String KEY_XMPP_CONNECT_ERRCODE = "key_xmpp_connect_error_code";

    public final static String KEY_SIP_CONNECT_STATUS = "key_sip_connect_status";
    public final static String KEY_SIP_CONNECT_ERRMSG = "key_sip_connect_error_message";
    public final static String KEY_SIP_CONNECT_ERRCODE = "key_sip_connect_error_code";


    public static String PRESET = "Preset";

    public final static int IS_VIDEO_ATTACH = 0;
    public final static int IS_PHOTO_ATTACH = 1;

    // Linphone Error Messages
    public final static String CALL_REQUEST_TIMEOUT = "request timeout";
    public final static String CALL_BUSY_HERE = "busy here";
    public final static String CALL_BAD_NUMBER = "service unavailable (bad_number)";
    public final static String CALL_LEVEL_NOT_ALLOWED = "traff level is not allowed";

    public static int MAX_MESSAGE_LENGTH = 3000; // (bytes)
    public static int MAX_VIDEO_LENGTH = 2 * 60; //(seconds)

    public final static String VIDEO_LOG = "videolog";
    public final static String AUDIO_LOG = "audiolog";

    public final static String FILE_TYPE_IMAGE = "1";
    public final static String FILE_TYPE_AUDIO = "2";
    public final static String FILE_TYPE_VIDEO = "3";
    public final static String FILE_TYPE_VOICE = "4";
    public final static String FILE_TYPE_OTHER = "5";

    public final static String KEY_UPLOADING_STATUS = "key_uploading_status";
    public final static String KEY_DOWNLOADING_STATUS = "key_downloading_status";
    public final static int NO_UPLOADING = 0;
    public final static int UPLOADING = 1;
    public final static int NO_DOWNLOADING = 0;
    public final static int DOWNLOADING = 1;

    public static final String BROADCAST_FILE_UPLOAD_FINISHED = "broadcast_file_upload_finished";
    public static final String BROADCAST_FILE_DOWNLOAD_FINISHED = "broadcast_file_download_finished";

    public static final long XMPP_LOGIN_TIMEOUT = 30 * 1000;    // 30s

    public final static String PLAYBACK_GAIN = "playback_gain";
    public final static String NOISE_THRESHOLD = "noise_threshold";

    public static int playback_gain = 0;
    public static Float noise_threshold = 0.f;

    public final static String DELETED_CONTACT_ADDRESS = "deleted_contact_address";

    public final static String ORIGIN_FILE_PATH_BEFORE_SPLIT = "origin_file_path_before_split";

    public final static long MINIMUM_STORAGE_FREE_SPACE = 50 * 1024 * 1024; //(50MB)

}
