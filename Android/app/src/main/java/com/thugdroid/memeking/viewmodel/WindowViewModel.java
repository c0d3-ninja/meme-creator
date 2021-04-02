package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;

import java.util.ArrayList;
import java.util.List;

public class WindowViewModel extends ViewModel {
     private LoggedInUserEntity loggedInUserEntity;
     private MutableLiveData<CategoryEntity> selectedCategoryEntityAsLiveData;
     private MutableLiveData<String> selectedNavDrawerMenuIdAsLiveData;
     private List<CategoryEntity> categoriesCache;
     private boolean isCategoryObserverExecuted;
     private List<Integer> pendingNotificationIds;
    public LoggedInUserEntity getLoggedInUserEntity() {
        return loggedInUserEntity;
    }

    public String getUserId(){
        if(getLoggedInUserEntity()!=null){
            return  getLoggedInUserEntity().getId();
        }
        return null;
    }

    public void setLoggedInUserEntity(LoggedInUserEntity loggedInUserEntity1) {
        loggedInUserEntity = loggedInUserEntity1;
    }

    public MutableLiveData<CategoryEntity> getSelectedCategoryEntityAsLiveData() {
        if(selectedCategoryEntityAsLiveData==null){
            selectedCategoryEntityAsLiveData=new MutableLiveData<>();
        }
        return selectedCategoryEntityAsLiveData;
    }

    public MutableLiveData<String> getSelectedNavDrawerMenuIdAsLiveData() {
        if(selectedNavDrawerMenuIdAsLiveData==null){
            selectedNavDrawerMenuIdAsLiveData=new MutableLiveData<>();
        }
        return selectedNavDrawerMenuIdAsLiveData;
    }

    public List<CategoryEntity> getCategoriesCache() {
        if(categoriesCache==null){
            categoriesCache=new ArrayList<>();
        }
        return categoriesCache;
    }

    public void setCategoriesCache(List<CategoryEntity> categoriesCache) {
        this.getCategoriesCache().clear();
        this.getCategoriesCache().addAll(categoriesCache);
    }

    public void resetAll(){
        selectedCategoryEntityAsLiveData=null;
        selectedNavDrawerMenuIdAsLiveData=null;
        categoriesCache=null;
        loggedInUserEntity=null;
    }

    public boolean isCategoryObserverExecuted() {
        return isCategoryObserverExecuted;
    }

    public void setCategoryObserverExecuted(boolean categoryObserverExecuted) {
        isCategoryObserverExecuted = categoryObserverExecuted;
    }

    public String getRegionId(){
        if(loggedInUserEntity==null){
            return null;
        }
        return loggedInUserEntity.getRegionId();
    }

    public List<Integer> getPendingNotificationIds() {
        if(pendingNotificationIds==null){
            pendingNotificationIds=new ArrayList<>();
        }
        return pendingNotificationIds;
    }
}
