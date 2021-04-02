package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.room.entity.CategoryEntity;

import java.util.ArrayList;
import java.util.List;

public class CategoryFragmentViewModel extends ViewModel {
    ApiModel silentCallApiModel;

    public ApiModel getSilentCallApiModel() {
        if(silentCallApiModel==null){
            silentCallApiModel=new ApiModel();
        }
        return silentCallApiModel;
    }

    public void setSilentCallApiModel(ApiModel silentCallApiModel) {
        this.silentCallApiModel = silentCallApiModel;
    }
}
