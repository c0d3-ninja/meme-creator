package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CreateMemePickTemplateViewModel extends ViewModel {
    MutableLiveData<String> searchStr;

    public MutableLiveData<String> getSearchStr() {
        if (searchStr==null){
            searchStr=new MutableLiveData<>("");
        }
        return searchStr;
    }
}
