package com.thugdroid.memeking.ui;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.GridView;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.adapters.FontAdapter;
import com.thugdroid.memeking.utils.DialogUtils;

public class ReportTemplateDialog implements View.OnClickListener {
    Activity activity;
    Dialog dialog;
    DialogItemClickListener dialogItemClickListener;
    public ReportTemplateDialog(Activity activity) {
        this.activity = activity;
        this.dialog=new Dialog(activity);
    }

    public void setDialogItemClickListener(DialogItemClickListener dialogItemClickListener) {
        this.dialogItemClickListener = dialogItemClickListener;
    }

    public void show(){
        dialog.setContentView(R.layout.dialog_reporttemplate);
        dialog.setCancelable(true);
        if(dialogItemClickListener!=null){
            (dialog.findViewById(R.id.reportTemplateSpam)).setOnClickListener(this::onClick);
            (dialog.findViewById(R.id.reportTemplateInappropriate)).setOnClickListener(this::onClick);
            (dialog.findViewById(R.id.reportTemplateCancel)).setOnClickListener(this::onClick);
        }
        dialog.show();
        DialogUtils.setFullWidth(dialog);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.reportTemplateSpam:
                dialogItemClickListener.onSpamItemClick(dialog);
                break;
            case R.id.reportTemplateInappropriate:
                dialogItemClickListener.onInappropriateItemClick(dialog);
                break;
            case R.id.reportTemplateCancel:
                dialogItemClickListener.onCancelClick(dialog);
                break;
        }
    }

    public class DialogConfigurations{
        DialogItemClickListener dialogItemClickListener;

        public DialogConfigurations(DialogItemClickListener dialogItemClickListener) {
            this.dialogItemClickListener = dialogItemClickListener;
        }
    }

    public  interface DialogItemClickListener{
        void onSpamItemClick(Dialog dialog);
        void onInappropriateItemClick(Dialog dialog);
        void onCancelClick(Dialog dialog);
    }
}
