package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.fragments.UploadTemplateFragment;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.model.MyImage;
import com.thugdroid.memeking.room.entity.CategoryEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UploadTemplateFragmentViewModel extends ViewModel {
    MutableLiveData<ApiModel> imageUploadApiModel;
    MutableLiveData<ApiModel> addTemplateApiModel;
    MutableLiveData<MyImage> templateImage;
    MutableLiveData<Boolean> isUploadBtnClicked;
    MutableLiveData<ApiModel> categoryApiModel;
    String selectedCategoryId;
    public  List<CategoryEntity> categoryEntities;

    //used for naming template image
    public HashMap<String,String > categoryIdNameMap;
    public MutableLiveData<ApiModel> getImageUploadApiModel() {
        if(imageUploadApiModel ==null){
            imageUploadApiModel =new MutableLiveData<>(new ApiModel());
        }
        return imageUploadApiModel;
    }

    public MutableLiveData<ApiModel> getAddTemplateApiModel() {
        if(addTemplateApiModel ==null){
            addTemplateApiModel =new MutableLiveData<>(new ApiModel());
        }
        return addTemplateApiModel;
    }

    public MutableLiveData<MyImage> getTemplateImage() {
        if(templateImage==null){
            templateImage=new MutableLiveData<>(new MyImage());
        }
        return templateImage;
    }

    public MutableLiveData<Boolean> getIsUploadBtnClicked() {
        if(isUploadBtnClicked==null){
            isUploadBtnClicked=new MutableLiveData<>(false);
        }
        return isUploadBtnClicked;
    }

    public String getSelectedCategoryId() {
        if(selectedCategoryId==null){
            return CategoryEntity.DROPDOWN_NONE;
        }
        return selectedCategoryId;
    }

    public void setSelectedCategoryId(String selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
    }

    public List<CategoryEntity> getCategoryEntities() {
        if(categoryEntities==null){
            categoryEntities=new ArrayList<>();
        }
        return categoryEntities;
    }

    public HashMap<String, String> getCategoryIdNameMap() {
        if(categoryIdNameMap==null){
            categoryIdNameMap=new HashMap<>();
        }
        return categoryIdNameMap;
    }

    public MutableLiveData<ApiModel> getCategoryApiModel() {
        if(categoryApiModel==null){
            categoryApiModel=new MutableLiveData<>(new ApiModel());
        }
        return categoryApiModel;
    }
}
