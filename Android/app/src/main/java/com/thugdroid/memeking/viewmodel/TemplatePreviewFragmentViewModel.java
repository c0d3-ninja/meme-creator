package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.room.entity.TemplateEntity;

public class TemplatePreviewFragmentViewModel extends ViewModel {
    MutableLiveData<TemplateEntity> templateEntity;
    boolean isWaterMarkHideAble;
    public MutableLiveData<TemplateEntity> getTemplateEntity() {
        if(templateEntity==null){
            templateEntity=new MutableLiveData<>();
        }
        return templateEntity;
    }

    public boolean isWaterMarkHideAble() {
        return isWaterMarkHideAble;
    }

    public void setWaterMarkHideAble(boolean waterMarkHideAble) {
        isWaterMarkHideAble = waterMarkHideAble;
    }
}
