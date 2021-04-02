package com.thugdroid.memeking.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.utils.DialogUtils;

public class ConfirmationDialog {
    Context context;
    Dialog dialog;
    String title;
    AlertDialogBtnClickListner onClickListener;
    int positiveBtnColor;
    String positiveBtnText;
    int negativeBtnColor;
    String negativeBtnText;
    public ConfirmationDialog(Context context) {
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

    public void setNegativeBtnColor(int negativeBtnColor) {
        this.negativeBtnColor = negativeBtnColor;
    }

    public void setNegativeBtnText(String negativeBtnText) {
        this.negativeBtnText = negativeBtnText;
    }

    public void show(){
        if(dialog.isShowing()){
            return;
        }
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_confirmation, null);
        dialog.setContentView(dialogView);
        dialog.setCancelable(true);
        if(title!=null){
            ((TextView)dialog.findViewById(R.id.alertDialogTitle)).setText(title);
        }
        if(positiveBtnText !=null){
            ((Button)dialog.findViewById(R.id.alertDialogPositive)).setText(positiveBtnText);
        }
        if(positiveBtnColor !=0){
            ((Button)dialog.findViewById(R.id.alertDialogPositive)).setTextColor(positiveBtnColor);
        }
        if(negativeBtnText !=null){
            ((Button)dialog.findViewById(R.id.alertDialogNegative)).setText(negativeBtnText);
        }
        if(negativeBtnColor !=0){
            ((Button)dialog.findViewById(R.id.alertDialogNegative)).setTextColor(negativeBtnColor);
        }
        if(onClickListener!=null){
            (dialog.findViewById(R.id.alertDialogPositive)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onPositiveBtnClick(dialog);
                }
            });
            (dialog.findViewById(R.id.alertDialogNegative)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickListener.onNegativeBtnClick(dialog);
                }
            });
        }
        dialog.show();
        DialogUtils.setFullWidth(dialog);
    }
    public boolean isShowing(){
        return dialog.isShowing();
    }
    public void dismiss(){
        dialog.dismiss();
    }

    public interface AlertDialogBtnClickListner{
        void onPositiveBtnClick(Dialog dialog);
        void onNegativeBtnClick(Dialog dialog);
    }

    public void setOnClickListener(AlertDialogBtnClickListner onClickListener) {
        this.onClickListener = onClickListener;
    }
}
