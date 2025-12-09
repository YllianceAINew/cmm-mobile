package com.multimediachat.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.multimediachat.R;

/**
 * Created by jack on 1/15/2018.
 */

public class CustomDialog extends Dialog {
    Button dlg_btn_ok;
    Button dlg_btn_cancel;

    public CustomDialog(Context context) {
        super(context);
    }
    public CustomDialog(Context context, String strTitle, String strContent, String strBtnOK, String strBtnCancel){
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.confirm_dialog);

        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        wlp.gravity = Gravity.BOTTOM;
        window.setAttributes(wlp);

        TextView title = (TextView)findViewById(R.id.msgtitle);
        title.setText(strTitle);
        TextView content = (TextView)findViewById(R.id.msgcontent);
        content.setText(strContent);
        dlg_btn_ok = (Button)findViewById(R.id.btn_ok);
        dlg_btn_cancel = (Button)findViewById(R.id.btn_cancel);

        dlg_btn_ok.setText(strBtnOK);

        if ( strBtnCancel != null && !strBtnCancel.isEmpty() ) {
            dlg_btn_cancel.setVisibility(View.VISIBLE);
            dlg_btn_cancel.setText(strBtnCancel);
        }
    }

    public CustomDialog(Context context, String strTitle, String strContent, String strBtnOK, String strBtnCancel, Boolean isBottom){
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.confirm_dialog);

        Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setAttributes(wlp);

        TextView title = (TextView)findViewById(R.id.msgtitle);
        title.setText(strTitle);
        TextView content = (TextView)findViewById(R.id.msgcontent);
        content.setText(strContent);
        dlg_btn_ok = (Button)findViewById(R.id.btn_ok);
        dlg_btn_cancel = (Button)findViewById(R.id.btn_cancel);

        dlg_btn_ok.setText(strBtnOK);

        if ( strBtnCancel != null && !strBtnCancel.isEmpty() ) {
            dlg_btn_cancel.setVisibility(View.VISIBLE);
            dlg_btn_cancel.setText(strBtnCancel);
        }
    }

    public void setOnOKClickListener(View.OnClickListener listener)
    {
        dlg_btn_ok.setOnClickListener(listener);
    }
    public void setOnCancelClickListener(View.OnClickListener listener)
    {
        dlg_btn_cancel.setOnClickListener(listener);
    }

    public static void showAlertDialog(Context context, String strTitle)
    {
        final CustomDialog dlg = new CustomDialog(context, strTitle, "", context.getString(R.string.ok), null);

        dlg.setOnCancelClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        dlg.setOnOKClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        dlg.show();
    }

    public static void showAlertDialog(Context context, String strTitle, String strContent)
    {
        final CustomDialog dlg = new CustomDialog(context, strTitle, strContent, context.getString(R.string.ok), null);

        dlg.setOnCancelClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        dlg.setOnOKClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        dlg.show();
    }
}
