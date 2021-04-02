package com.thugdroid.memeking.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.utils.DialogUtils;

public class InfoDialog {
    Context context;
    Dialog dialog;
    String title;
    int positiveBtnColor;
    String positiveBtnText;
    int negativeBtnColor;
    String negativeBtnText;
    public InfoDialog(Context context) {
        this.context = context;
        dialog=new Dialog(context);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setPositiveBtnColor(int positiveBtnColor) {
        this.positiveBtnColor = positiveBtnColor;
    }

    public void setPositiveBtnText(String positiveBtnText) {
        this.positiveBtnText = positiveBtnText;
    }


    public void show(){
        if(dialog.isShowing()){
            return;
        }
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_info, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        if(title!=null){
            ((TextView)dialog.findViewById(R.id.infoDialogTitle)).setText(title);
        }
        Button positiveBtn = dialog.findViewById(R.id.infoDialogPositive);
        if(positiveBtnText !=null){
            positiveBtn.setText(positiveBtnText);
        }
        if(positiveBtnColor !=0){
            positiveBtn.setTextColor(positiveBtnColor);
        }
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

        dialog.show();
        DialogUtils.setFullWidth(dialog);
    }
    public boolean isShowing(){
        return dialog.isShowing();
    }
    public void dismiss(){
        dialog.dismiss();
    }

}
