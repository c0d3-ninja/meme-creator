package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.adapters.TemplatesRecyclerAdapter;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.room.entity.TemplateEntity;

import java.util.ArrayList;
import java.util.List;

public class SearchTemplatesFragmentViewModel extends ViewModel {
    List templateList;
    List<TemplatesRecyclerAdapter.LoadingItem> loadingItems;
    MutableLiveData<ApiModel> templatesApiModel;


    public final int loadingItemCount=6;
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

    public MutableLiveData<ApiModel> getTemplatesApiModel() {
        if(templatesApiModel ==null){
            templatesApiModel=new MutableLiveData<>(new ApiModel());
        }
        return templatesApiModel;
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

}
