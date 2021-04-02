package com.thugdroid.memeking.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.utils.DialogUtils;

public class LoadingDialog {
    Context context;
    final Dialog dialog;
    String loadingText;
    public LoadingDialog(Context context) {
        this.context = context;
        this.dialog=new Dialog(context);
    }

    public void show() {
        if(dialog.isShowing()){
            return;
        }
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        dialog.setContentView(dialogView);
        if(loadingText!=null){
            ((TextView)(dialog.findViewById(R.id.loadingText))).setText(loadingText);
        }
        dialog.setCancelable(false);
        dialog.show();
        DialogUtils.setFullWidth(dialog);
    }
    public boolean isShowing(){
        return dialog.isShowing();
    }
    public void dismiss(){
        dialog.dismiss();
    }

    public void setLoadingText(String loadingText) {
        this.loadingText = loadingText;
    }
}
