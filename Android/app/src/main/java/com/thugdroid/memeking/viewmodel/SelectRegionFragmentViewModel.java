package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.model.ApiModel;

import java.util.ArrayList;

public class SelectRegionFragmentViewModel extends ViewModel {
    MutableLiveData<Boolean> showNextBtn;
    MutableLiveData<ApiModel> apiModel;
    MutableLiveData<String> selectedRegionId;
    boolean isUserHasRegion;
    public ArrayList regions;
    public MutableLiveData<Boolean> getShowNextBtn() {
        if(showNextBtn==null){
            showNextBtn=new MutableLiveData<>(false);
        }
        return showNextBtn;
    }

    public MutableLiveData<ApiModel> getApiModel() {
        if(apiModel==null){
            apiModel=new MutableLiveData<>(new ApiModel());
        }
        return apiModel;
    }

    public ArrayList getRegions() {
        if(regions==null){
            regions=new ArrayList();
        }
        return regions;
    }

    public MutableLiveData<String> getSelectedRegionId() {
        if(selectedRegionId==null){
            selectedRegionId=new MutableLiveData<>(null);
        }
        return selectedRegionId;
    }

    public boolean isUserHasRegion() {
        return isUserHasRegion;
    }

    public void setUserHasRegion(boolean userHasRegion) {
        this.isUserHasRegion = userHasRegion;
    }
}
