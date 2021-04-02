package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.adapters.TemplatesRecyclerAdapter;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.room.entity.TemplateEntity;

import java.util.ArrayList;
import java.util.List;

public class TemplateFragmentViewModel extends ViewModel {
    List templateList;
    List<TemplatesRecyclerAdapter.LoadingItem> loadingItems;
    MutableLiveData<ApiModel> templatesApiModel;
    /*while auto logging out, loggedin user entity  will be null, need  to cache this*/
    private String regionId;
    private String categoryId;

    private boolean isAllCategoryType;
    public static final int loadingItemCount=6;
    public List getTemplateList() {
        if(templateList==null){
            templateList=new ArrayList();
        }
        return templateList;
    }

    public List<TemplatesRecyclerAdapter.LoadingItem> getLoadingItems() {
        if(loadingItems==null){
            loadingItems=new ArrayList<>();
            for (int i = 0;i<loadingItemCount;i++){{
                loadingItems.add(new TemplatesRecyclerAdapter.LoadingItem());
            }}
        }
        return loadingItems;
    }

    public MutableLiveData<ApiModel> getTemplatesApiModelAsLiveData() {
        if(templatesApiModel ==null){
            templatesApiModel=new MutableLiveData<>(new ApiModel());
        }
        return templatesApiModel;
    }

    public ApiModel getTemplatesApiModel(){
        return getTemplatesApiModelAsLiveData().getValue();
    }


    public int getLoadingItemCount() {
        return loadingItemCount;
    }

    public String getRegionId() {
        if(regionId==null){
            return "";
        }
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getCategoryId() {
        if(categoryId==null){
            categoryId="";
        }
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public boolean isAllCategoryType() {
        return isAllCategoryType;
    }

    public void setAllCategoryType(boolean allCategoryType) {
        isAllCategoryType = allCategoryType;
    }

    public int getIndexById(String templateId){

        for (int i = 0; i < getTemplateList().size(); i++) {
            Object object = getTemplateList().get(i);
            if(object instanceof TemplateEntity){
                TemplateEntity templateEntity = (TemplateEntity) object;
                if(templateEntity.getId().equals(templateId)){
                    return i;
                }
            }
        }
        return -1;
    }


    public void setTemplatesApiModel(int loadingState,int statusCode,boolean isShouldCallApi){
        ApiModel apiModel=getTemplatesApiModel();
        apiModel.setLoadingState(loadingState);
        apiModel.setStatusCode(statusCode);
        apiModel.setShouldCallApi(isShouldCallApi);
        getTemplatesApiModelAsLiveData().setValue(apiModel);
    }
}
