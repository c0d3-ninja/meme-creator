package com.thugdroid.memeking.ui;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.utils.DialogUtils;

public class AddTextDialog {
    private Context context;
    private Dialog dialog;
    private EditText addTextEditText;
    private DialogBtnClickListener dialogBtnClickListener;
    private String placeHolder, positiveBtnText,negativeBtnText,deleteBtnTxt;
    private boolean hasDeleteBtn;
    public AddTextDialog(Context context) {
        this.context = context;
        dialog=new Dialog(context);
    }

    public void setDialogBtnClickListener(DialogBtnClickListener dialogBtnClickListener) {
        this.dialogBtnClickListener = dialogBtnClickListener;
    }

    public void setPlaceHolder(String placeHolder) {
        this.placeHolder = placeHolder;
    }

    public void setPositiveBtnText(String positiveBtnText) {
        this.positiveBtnText = positiveBtnText;
    }

    public void setNegativeBtnText(String negativeBtnText) {
        this.negativeBtnText = negativeBtnText;
    }

    public String getDeleteBtnTxt() {
        return deleteBtnTxt;
    }

    public void setDeleteBtnTxt(String deleteBtnTxt) {
        this.deleteBtnTxt = deleteBtnTxt;
    }

    public boolean hasDeleteBtn() {
        return hasDeleteBtn;
    }

    public void setHasDeleteBtn(boolean hasDeleteBtn) {
        this.hasDeleteBtn = hasDeleteBtn;
    }

    public void show(String str){
        dialog.setContentView(R.layout.dialog_addtext);
        dialog.setCancelable(true);
        addTextEditText=dialog.findViewById(R.id.addTextEditText);
        addTextEditText.setText(str);
        Button positiveBtn = dialog.findViewById(R.id.addTextPositiveBtn);
        Button negativeBtn=dialog.findViewById(R.id.addTextNegativeBtn);
        Button deleteBtn = dialog.findViewById(R.id.addTextDeleteBtn);
        if(placeHolder!=null){
            addTextEditText.setHint(placeHolder);
        }
        if(positiveBtnText!=null){
            positiveBtn.setText(positiveBtnText);
        }
        if(negativeBtnText!=null){
            negativeBtn.setText(negativeBtnText);
        }

        if(hasDeleteBtn){
            deleteBtn.setVisibility(View.VISIBLE);
            if(getDeleteBtnTxt()!=null){
                deleteBtn.setText(getDeleteBtnTxt());
            }
        }else {
            deleteBtn.setVisibility(View.GONE);
        }

        if(dialogBtnClickListener!=null){
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogBtnClickListener.onPositiveBtnClick(dialog,addTextEditText.getText().toString());
                }
            });
            negativeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialogBtnClickListener.onNegativeBtnClick(dialog);
                }
            });
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogBtnClickListener.onDeleteBtnClick(dialog);
                }
            });
        }
        dialog.show();
        DialogUtils.setFullWidth(dialog);
        AppUtils.focusEditTextInDialog(dialog,addTextEditText);
    }



    public interface DialogBtnClickListener{
        void onPositiveBtnClick(Dialog dialog,String text);
        void onNegativeBtnClick(Dialog dialog);
        void onDeleteBtnClick(Dialog dialog);
    }

}
