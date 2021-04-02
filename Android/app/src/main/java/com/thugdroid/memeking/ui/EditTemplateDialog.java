package com.thugdroid.memeking.ui;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.EditText;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.utils.DialogUtils;

public class EditTemplateDialog {
    Activity activity;
    Dialog dialog;
    EditText editTemplateEditText;
    DialogBtnClickListener dialogBtnClickListener;
    public EditTemplateDialog(Activity activity) {
        this.activity = activity;
        dialog=new Dialog(activity);
    }

    public void setDialogBtnClickListener(DialogBtnClickListener dialogBtnClickListener) {
        this.dialogBtnClickListener = dialogBtnClickListener;
    }

    public void show(String str){
        dialog.setContentView(R.layout.dialog_edittemplate);
        dialog.setCancelable(true);
        editTemplateEditText =dialog.findViewById(R.id.editTemplateEditText);
        editTemplateEditText.setText(str);
        if(dialogBtnClickListener!=null){
            (dialog.findViewById(R.id.editTemplatePositiveBtn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogBtnClickListener.onPositiveBtnClick(dialog, editTemplateEditText.getText().toString());
                }
            });
            (dialog.findViewById(R.id.editTemplateNegativeBtn)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogBtnClickListener.onNegativeBtnClick(dialog);
                }
            });
        }
        dialog.show();
        DialogUtils.setFullWidth(dialog);
        AppUtils.focusEditTextInDialog(dialog,editTemplateEditText);
    }



    public interface DialogBtnClickListener{
        void onPositiveBtnClick(Dialog dialog, String text);
        void onNegativeBtnClick(Dialog dialog);
    }

}
