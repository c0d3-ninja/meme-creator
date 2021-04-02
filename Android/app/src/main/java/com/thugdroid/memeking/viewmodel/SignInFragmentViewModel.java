package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.model.ApiModel;

public class SignInFragmentViewModel extends ViewModel {

    MutableLiveData<ApiModel> signInApiModel;
    public MutableLiveData<ApiModel> getSignInApiModel() {
        if(signInApiModel==null){
            signInApiModel=new MutableLiveData<>(new ApiModel());
        }
        return signInApiModel;
    }
}
