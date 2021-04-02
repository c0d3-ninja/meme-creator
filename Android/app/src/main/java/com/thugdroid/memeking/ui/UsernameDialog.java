package com.thugdroid.memeking.ui;

import android.app.Dialog;
import android.content.Context;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;
import com.thugdroid.memeking.utils.AppUtils;

import java.util.HashMap;

import static com.thugdroid.memeking.R.string.enter_atlease_x_letter;

public class UsernameDialog {
    private Context context;
    private String username;
    private FireFunctions fireFunctions;
    private AddTextDialog addTextDialog;
    private LoadingDialog loadingDialog;
    private ParentHandShakes parentHandShakes;


    public UsernameDialog(Context context, String username,ParentHandShakes parentHandShakes) {
        this.context = context;
        this.username = username;
        this.parentHandShakes=parentHandShakes;
        addTextDialog = new AddTextDialog(context);
        addTextDialog.setPlaceHolder(context.getString(R.string.enter_insta_username));
    }

    public void show(){
        addTextDialog.setDialogBtnClickListener(new IUserDialogListener());
        addTextDialog.setHasDeleteBtn(username!=null && username.trim().length()>0);
        addTextDialog.setDeleteBtnTxt(context.getString(R.string.remove));
        addTextDialog.show(username);

    }

    private Context getContext(){
        return this.context;
    }

    private void showMsg(int resId){
        Toast.makeText(getContext(),context.getString(resId),Toast.LENGTH_SHORT).show();
    }
    private void showMsg(int resId,@Nullable Object... formatArgs){
        Toast.makeText(getContext(),context.getString(resId,formatArgs),Toast.LENGTH_SHORT).show();
    }

    private FireFunctions getFireFunctions(){
        if(fireFunctions==null){
            fireFunctions=new FireFunctions(getContext());
        }
        return fireFunctions;
    }

    private ParentHandShakes getParentHandShakes(){
        return parentHandShakes;
    }

    private LoadingDialog getLoadingDialog(){
        if(loadingDialog==null){
            loadingDialog=new LoadingDialog(getContext());
        }
        return loadingDialog;
    }
    private void showLoadingDialog(){
        if(!getLoadingDialog().isShowing()){
            getLoadingDialog().show();
        }
    }
    private void hideLoadingDialog(){
        if(getLoadingDialog().isShowing()){
            getLoadingDialog().dismiss();
        }
    }
    private void updateUsername(){
        showLoadingDialog();
        HashMap apiMap=new HashMap();
        apiMap.put(LoggedInUserEntity.KEY_INSTA_USERNAME,username);
        getFireFunctions().callApi(ApiUrls.ADD_INSTA_USERNAME,apiMap,new UpdateIUserNameApiListener());
    }

    private class UpdateIUserNameApiListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            hideLoadingDialog();
            switch (statusCode){
                case HttpCodes.SUCCESS:
                    if(getParentHandShakes()!=null){
                        getParentHandShakes().onUpdateUsername(LoggedInUserEntity.getInstaUserNameFromResponse(resultObject));
                    }
                    showMsg(R.string.username_updated_successfully);
                    break;
                default:
                    showMsg(R.string.something_went_wrong_while_updating_username);
            }
        }

        @Override
        public void onFailure(Exception e) {
            hideLoadingDialog();
            showMsg(R.string.something_went_wrong_while_updating_username);
        }
    }

    private class IUserDialogListener implements AddTextDialog.DialogBtnClickListener{
        @Override
        public void onPositiveBtnClick(Dialog dialog, String text) {
            if(!AppUtils.hasInternetConnection(getContext())){
                showMsg(R.string.no_internet_connection);
                return;
            }
            if(text.length()>0){
                username=text;
                dialog.dismiss();
                updateUsername();
            }else{
                showMsg(enter_atlease_x_letter,1);
            }
        }

        @Override
        public void onNegativeBtnClick(Dialog dialog) {
            dialog.dismiss();
        }

        @Override
        public void onDeleteBtnClick(Dialog dialog) {
            if(!AppUtils.hasInternetConnection(getContext())){
                showMsg(R.string.no_internet_connection);
                return;
            }
            dialog.dismiss();
            username=null;
            updateUsername();
        }
    }


    public interface ParentHandShakes{
        void onUnAuthorize();
        void onUpdateUsername(String username);
    }
}
