package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.adapters.TemplatesGroupAdapter;
import com.thugdroid.memeking.model.ApiModel;

import java.util.ArrayList;
import java.util.List;

public class TemplatesGroupFragmentViewModel extends ViewModel {
    private static final int LOADING_ITEM_COUNT=2;
    private ApiModel templatesGroupLoading;
    private List templatesGroup;
    private List<TemplatesGroupAdapter.LoadingItem> loadingItems;
    private boolean isScrollDisabled;
    public List getTemplatesGroup() {
        if(templatesGroup==null){
            templatesGroup=new ArrayList();
        }
        return templatesGroup;
    }

    public ApiModel getTemplatesGroupApiModel(){
        if(templatesGroupLoading ==null){
            templatesGroupLoading =new ApiModel();
        }
        return templatesGroupLoading;
    }
    public void setTemplatesGroupLoading(int loadingStatus,int statusCode,boolean shouldCallApi){
        getTemplatesGroupApiModel().setLoadingState(loadingStatus);
        getTemplatesGroupApiModel().setStatusCode(statusCode);
        getTemplatesGroupApiModel().setShouldCallApi(shouldCallApi);
    }

    public List<TemplatesGroupAdapter.LoadingItem> getLoadingItems(){
        if(loadingItems==null){
            loadingItems=new ArrayList<>();
            for (int i = 0;i<LOADING_ITEM_COUNT;i++){{
                loadingItems.add(new TemplatesGroupAdapter.LoadingItem());
            }}
        }
        return loadingItems;
    }

    public boolean isScrollDisabled() {
        return isScrollDisabled;
    }

    public void setScrollDisabled(boolean scrollDisabled) {
        isScrollDisabled = scrollDisabled;
    }
}
