package com.multimediachat.global;

import android.content.Context;
import android.widget.Toast;

import com.multimediachat.R;

/**
 * Created by jack on 12/3/2018.
 */

public class ErrorManager {
    public static Context s_context;

    public static void init(Context context)
    {
        s_context = context;
    }

    public static void showErrorForCode(int code)
    {
        String msg;

        switch (code)
        {
            case 400:
                msg = "Bad request";
                break;
            case 401:
                msg = "Verification code is invalid.";
                break;
            case 500:
                msg = "Database error";
                break;
            case 501:
                msg = "Already exist";
                break;
            case 502:
                msg = "No such user";
                break;
            case 600:
                msg = "You are locked.";
                break;
            case 601:
                msg = "You have tried too much. Try again after 24 hours.";
                break;
            case 602:
                msg = "Sending sms failed.";
                break;
            case 603:
                msg = "You can't use this verification code. Try another new verification code.";
                break;
            case 604:
                msg = "Invalid verification code.";
                break;
            case 605:
                msg = "No file to upload.";
                break;
            case 606:
                msg = "Deleting file failed.";
                break;
            case 607:
                msg = "Copying file failed.";
                break;
            default:
                showErrorForApiFail();
                return;
        }

        Toast.makeText(s_context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForApiFail()
    {
        Toast.makeText(s_context, s_context.getResources().getString(R.string.error_message_network_connect), Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForXmppFail()
    {
        Toast.makeText(s_context, "Connecting to server failed. Try again later.", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForPermission()
    {
        Toast.makeText(s_context, "You have no permission to continue", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForPermission(String msg)
    {
        Toast.makeText(s_context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForImageUpload()
    {
        Toast.makeText(s_context, "Uploading image failed. Try agin later.", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForUpload()
    {
        Toast.makeText(s_context, "Uploading failed. Try agin later.", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForImageDownload()
    {
        Toast.makeText(s_context, "Downloading image failed. Try agin later.", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForChangeProfileName()
    {
        Toast.makeText(s_context, "Changing name is failed. Try again later.", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForMaxSelect()
    {
        Toast.makeText(s_context, "You are not able to select more", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForFileOperation()
    {
        Toast.makeText(s_context, "Failed to process file operation", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForFileSize()
    {
        Toast.makeText(s_context, "The file size must be less than 10MB.", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForGroupMemberCount()
    {
        Toast.makeText(s_context, "You should select more than 2 members.", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForAddingMyPhone()
    {
        Toast.makeText(s_context, "Your phone number is no need to add.", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForNoFile()
    {
        Toast.makeText(s_context, "File not exists.", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForBlock()
    {
        Toast.makeText(s_context, "You should unblock this contact to call.", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForFileNotFound()
    {
        Toast.makeText(s_context, "File not found.", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForFileSizeBeforeCompress()
    {
        Toast.makeText(s_context, "The file size must be less than 20MB.", Toast.LENGTH_SHORT).show();
    }

    public static void showErrorForProcessFile()
    {
        Toast.makeText(s_context, "File operation failed.", Toast.LENGTH_SHORT).show();
    }
}
