package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.model.ApiModel;

public class InitializingFragmentViewModel extends ViewModel {
    MutableLiveData<ApiModel> categoryApiModel;

    public MutableLiveData<ApiModel> getCategoryApiModel() {
        if(categoryApiModel ==null){
            categoryApiModel =new MutableLiveData<>(new ApiModel());
        }
        return categoryApiModel;
    }
}
