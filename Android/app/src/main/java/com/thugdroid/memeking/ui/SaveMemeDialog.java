package com.thugdroid.memeking.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.utils.DialogUtils;

public class SaveMemeDialog implements View.OnClickListener {
    Context context;
    Dialog dialog;
    DialogItemClickListener dialogItemClickListener;
    String firstOptionTxt;
    String secondOptionTxt;
    public SaveMemeDialog(Context context) {
        this.context = context;
        this.dialog=new Dialog(context);
    }

    public void setDialogItemClickListener(DialogItemClickListener dialogItemClickListener) {
        this.dialogItemClickListener = dialogItemClickListener;
    }

    public void setFirstOptionTxt(String firstOptionTxt) {
        this.firstOptionTxt = firstOptionTxt;
    }

    public void setSecondOptionTxt(String secondOptionTxt) {
        this.secondOptionTxt = secondOptionTxt;
    }

    public void show(){
        dialog.setContentView(R.layout.dialog_save);
        if(firstOptionTxt!=null){
            ((TextView)dialog.findViewById(R.id.dialogSaveFirstOption)).setText(firstOptionTxt);
        }
        if(secondOptionTxt!=null){
            ((TextView)dialog.findViewById(R.id.dialogSaveWOWMTv)).setText(secondOptionTxt);
        }
        dialog.setCancelable(true);

        if(dialogItemClickListener!=null){
            (dialog.findViewById(R.id.dialogSaveMeme)).setOnClickListener(this::onClick);
            (dialog.findViewById(R.id.dialogSaveMemeWOWM)).setOnClickListener(this::onClick);
        }
        dialog.show();
        DialogUtils.setFullWidth(dialog);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dialogSaveMeme:
                dialogItemClickListener.onSaveClick(dialog);
                break;
            case R.id.dialogSaveMemeWOWM:
                dialogItemClickListener.onSaveWOWaterMarkClick(dialog);
                break;
        }
    }


    public  interface DialogItemClickListener{
        void onSaveClick(Dialog dialog);
        void onSaveWOWaterMarkClick(Dialog dialog);
    }
}
