package com.multimediachat.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.multimediachat.R;


public class FileSaveDialog extends Dialog {
    Button dlg_btn_ok;
    Button dlg_btn_cancel;

    public FileSaveDialog(Context context) {
        super(context);
    }

    public FileSaveDialog(Context context, String strTitle, String strContent, String strBtnOK, String strBtnCancel) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.file_save_dialog);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        TextView title = (TextView) findViewById(R.id.msgtitle);
        title.setText(strTitle);
        TextView content = (TextView) findViewById(R.id.msgcontent);
        content.setText(strContent);
        dlg_btn_ok = (Button) findViewById(R.id.btn_ok);
        dlg_btn_cancel = (Button) findViewById(R.id.btn_cancel);

        dlg_btn_ok.setText(strBtnOK);

        if (strBtnCancel != null && !strBtnCancel.isEmpty()) {
            dlg_btn_cancel.setVisibility(View.VISIBLE);
            dlg_btn_cancel.setText(strBtnCancel);
        }
    }

    public void setOnOKClickListener(View.OnClickListener listener) {
        dlg_btn_ok.setOnClickListener(listener);
    }

    public void setOnCancelClickListener(View.OnClickListener listener) {
        dlg_btn_cancel.setOnClickListener(listener);
    }
}
