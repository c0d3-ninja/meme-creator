package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.model.ApiModel;

public class UploadMemePopupFragmentViewModel extends ViewModel {
    ApiModel imageUploadApiModel,addMemeApiModel;
    private boolean isUploadButtonClicked;
    private int notificationId;
    private String iUsername;
    public ApiModel getImageUploadApiModel() {
        if(imageUploadApiModel==null){
            imageUploadApiModel=new ApiModel();
        }
        return imageUploadApiModel;
    }

    public ApiModel getAddMemeApiModel() {
        if(addMemeApiModel==null){
            addMemeApiModel=new ApiModel();
        }
        return addMemeApiModel;
    }

    public boolean isUploadButtonClicked() {
        return isUploadButtonClicked;
    }

    public void setUploadButtonClicked(boolean uploadButtonClicked) {
        isUploadButtonClicked = uploadButtonClicked;
    }

    public int getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(int notificationId) {
        this.notificationId = notificationId;
    }

    public String getiUsername() {
        return iUsername;
    }

    public void setiUsername(String iUsername) {
        this.iUsername = iUsername;
    }
}
