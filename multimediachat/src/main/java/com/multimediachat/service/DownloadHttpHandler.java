package com.multimediachat.service;

import android.content.Intent;
import android.os.Environment;
import android.os.StatFs;

import com.multimediachat.R;
import com.multimediachat.app.DatabaseUtils;
import com.multimediachat.app.ImApp;
import com.multimediachat.app.im.provider.Imps;
import com.multimediachat.global.GlobalConstrants;
import com.multimediachat.global.GlobalFunc;
import com.multimediachat.ui.ChatRoomActivity;
import com.multimediachat.ui.MainTabNavigationActivity;
import com.multimediachat.util.ChatRoomMediaUtil;
import com.multimediachat.util.EncDecDes;
import com.loopj.android.http.BinaryHttpResponseHandler;

import org.apache.http.Header;

import java.io.File;
import java.io.RandomAccessFile;

import static com.multimediachat.global.GlobalConstrants.FILE_TYPE_AUDIO;
import static com.multimediachat.global.GlobalConstrants.FILE_TYPE_IMAGE;
import static com.multimediachat.global.GlobalConstrants.FILE_TYPE_VIDEO;


public class DownloadHttpHandler extends BinaryHttpResponseHandler {
    static String[] allowedContentTypes = new String[]{"application/octet-stream",
            "image/png", "image/jpeg", "image/jpg",
            "image/bmp", "image/png;charset=UTF-8", "audio/mp4", "audio/mpeg", "video/mpeg", "video/mp4", "video/3gp", "text/html; charset=ISO-8859-1"};

    private FtpService mService = null;
    private String packetId;
    private String chatId;
    private String filePath = "";
    private String type;
    private String nickName; //group chatting(sent friend name ex: group_name@conference.talk.pica.mn/friendname)
    private String thumbnailPath;
    private int recvCount;
    private int totalCount;
    public int progress;

    DownloadHttpHandler(FtpService svc, String _chatId, String _packetId, String _filePath, String _type, String _nickName, String _thumbnailPath, int _recvCount, int _totalCount) {
        super(allowedContentTypes);
        setUsePoolThread(true);
        mService = svc;
        chatId = _chatId;
        packetId = _packetId;
        filePath = _filePath;
        type = _type;
        progress = 0;
        nickName = _nickName;
        thumbnailPath = _thumbnailPath;
        recvCount = _recvCount;
        totalCount = _totalCount;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onFinish() {
        super.onFinish();
        if (!GlobalFunc.isStorageWrittable()) {
            onFailed();
        }
    }

    @Override
    public void onProgress(long bytesWritten, long totalSize) {
        super.onProgress(bytesWritten, totalSize);
        int curProgress = (recvCount * 100 + (int) (bytesWritten * 100 / totalSize)) / totalCount;

        if (progress + 2 < curProgress) {
            String msgHeader = GlobalConstrants.DOWNLOAD_PROGRESS;
            String[] params = new String[3];
            params[0] = msgHeader;
            params[1] = packetId;
            params[2] = String.valueOf(curProgress);

            Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FILE_UP_DOWN_LOAD);
            intent.putExtra("msg", msgHeader);
            intent.putExtra("content", params);
            mService.sendBroadcast(intent);
            progress = curProgress;
        }
    }

    @Override
    public void onFailure(int statusCode, Header[] headers,
                          byte[] binaryData, Throwable error) {
        onFailed();
    }

    @Override
    public void onSuccess(int i, Header[] headers, byte[] binaryData) {
        if (!mService.containDownloadThread(packetId)) {
            onFailed();
            return;
        }

        if (Imps.getErrorByPacketId(packetId) == -1)
            return;

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        if (bytesAvailable < binaryData.length) {
            onFailed();
            if (ChatRoomActivity.instance() != null) {
                if (!ChatRoomActivity.instance().isbackground) {
                    if (nickName.equals(ChatRoomActivity.instance().contactName.substring(0, ChatRoomActivity.instance().contactName.indexOf('@')))) {
                        GlobalFunc.showToast(ChatRoomActivity.instance(), R.string.error_message_low_storage, true);
                    }
                }
            }
            return;
        }

        File file = null;
        try {
            String strFilename = null;
            String[] splits;
            splits = filePath.split("&");
            if (splits.length > 0) {
                strFilename = splits[splits.length - 1];
                splits = strFilename.split("_");
                String result = "";
                if (splits.length > 4) {
                    result = splits[3] + "_";
                    for (i = 4; i < splits.length; i++)
                        result += splits[i];
                    strFilename = result;
                }
            }

            GlobalFunc.makeChatDir();
            String chatPath = GlobalConstrants.LOCAL_PATH + GlobalConstrants.CHAT_DIR_NAME + "/" + DatabaseUtils.mAccountID + "/" + chatId + "/";
            File fileChat = new File(chatPath);
            if (!fileChat.exists())
                fileChat.mkdirs();

            file = new File(chatPath + strFilename);
            RandomAccessFile bos = new RandomAccessFile(file, "rw");
            bos.seek(recvCount * GlobalConstrants.FILE_SPLIT_SIZE);
            bos.write(binaryData);
            bos.close();
            if (file == null || !file.exists()) {
                onFailed();
                return;
            }

            if (totalCount == (recvCount + 1)) {
                String savedFilePath = file.getAbsolutePath();
                saveFileUriAndBroadCast(chatId, savedFilePath);
            } else {
                ImApp.getInstance().deleteDownloadHandler(packetId);
                Imps.updateMessageSendCount(mService.getContentResolver(), packetId, recvCount + 1);
                Intent intent = new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED);
                intent.putExtra("packetid", packetId);
                mService.sendBroadcast(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            onFailed();
        }
    }

    private void saveFileUriAndBroadCast(String chatId, String filePath) {
        String msgHeader = GlobalConstrants.DOWNLOAD_SUCCESS;
        String[] params = new String[4];
        params[0] = filePath;
        params[1] = null;

        int[] ret = null;
        String samplePath = null;

        GlobalFunc.makeChatDir();
        String chatPath = GlobalConstrants.LOCAL_PATH + GlobalConstrants.CHAT_DIR_NAME + "/" + DatabaseUtils.mAccountID + "/" + chatId + "/";
        File fileChat = new File(chatPath);
        if (!fileChat.exists())
            fileChat.mkdirs();

        if (type.equals(FILE_TYPE_IMAGE)) {
            params[1] = "image/1";
            if (thumbnailPath == null) {
                samplePath = chatPath + EncDecDes.getInstance().generateFileName("thumbnail_" + System.currentTimeMillis()) + ".jpg";
                ret = ChatRoomMediaUtil.sampleImage(mService.getResources(), filePath, samplePath);
                if (ret == null) {
                    samplePath = null;
                }
            }
        } else if (type.equals(FILE_TYPE_AUDIO)) {
            params[1] = "audio/2";
        } else if (type.equals(FILE_TYPE_VIDEO)) {
            params[1] = "video/3";
            if (thumbnailPath == null) {
                samplePath = chatPath + EncDecDes.getInstance().generateFileName("thumbnail_" + System.currentTimeMillis()) + ".jpg";
                ret = ChatRoomMediaUtil.sampleVideo(mService.getResources(), filePath, samplePath);
                if (ret == null) {
                    samplePath = null;
                }
            }
        } else
            params[1] = "";

        params[2] = packetId;
        params[3] = nickName;
        Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FILE_UP_DOWN_LOAD);
        intent.putExtra("msg", msgHeader);
        intent.putExtra("content", params);
        Imps.updateMessageInDb(mService.getContentResolver(), packetId, params[1], params[0]);
        Imps.updateOperMessageError(mService.getContentResolver(), packetId, Imps.FileErrorCode.DOWNLOADSUCCESS);
        GlobalFunc.setDownloadingStatus(GlobalConstrants.NO_DOWNLOADING);

        if (ret != null) {
            try {
                Imps.updateMessageInDb(mService.getContentResolver(), packetId, samplePath, ret[0], ret[1]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mService.sendBroadcast(intent);
        mService.sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED));
    }

    private void onFailed() {
        String msgHeader = GlobalConstrants.DOWNLOAD_FAILED;
        String[] params = new String[2];
        params[0] = packetId;
        Imps.updateOperMessageError(mService.getContentResolver(), packetId, Imps.FileErrorCode.DOWNLOADFAILED);
        GlobalFunc.setDownloadingStatus(GlobalConstrants.NO_DOWNLOADING);
        Intent intent = new Intent(MainTabNavigationActivity.BROADCAST_FILE_UP_DOWN_LOAD);
        intent.putExtra("msg", msgHeader);
        intent.putExtra("content", params);
        mService.sendBroadcast(intent);

        mService.sendBroadcast(new Intent(GlobalConstrants.BROADCAST_FILE_DOWNLOAD_FINISHED));
    }
}
