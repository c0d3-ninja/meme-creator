package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.model.ApiModel;

public class MainNewFragmentViewModel extends ViewModel {
    /*preserve scroll state on back press from other fragments*/
    boolean hasTemplateScroll, firstTimeComponentMounted;
    MutableLiveData<ApiModel> categoryApiModel;
    int updatePopupShownDiffInDays;

    public boolean isHasTemplateScroll() {
        return hasTemplateScroll;
    }

    public void setHasTemplateScroll(boolean hasTemplateScroll) {
        this.hasTemplateScroll = hasTemplateScroll;
    }

    public MutableLiveData<ApiModel> getCategoryApiModel() {
        if(categoryApiModel==null){
            categoryApiModel=new MutableLiveData<>(new ApiModel());
        }
        return categoryApiModel;
    }

    public int getUpdatePopupShownDiffInDays() {
        return updatePopupShownDiffInDays;
    }

    public void setLastUpdatePopupShownTime(int lastUpdatePopupShownTime) {
        this.updatePopupShownDiffInDays = lastUpdatePopupShownTime;
    }

    public boolean isFirstTimeComponentMounted() {
        return firstTimeComponentMounted;
    }

    public void setFirstTimeComponentMounted(boolean firstTimeComponentMounted) {
        this.firstTimeComponentMounted = firstTimeComponentMounted;
    }
}
