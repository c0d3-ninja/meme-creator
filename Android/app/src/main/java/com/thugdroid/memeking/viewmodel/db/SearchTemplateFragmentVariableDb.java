package com.thugdroid.memeking.viewmodel.db;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.room.entity.TemplateEntity;

import java.util.HashMap;
import java.util.List;

public class SearchTemplateFragmentVariableDb extends ViewModel {
    MutableLiveData<HashMap<String,  List<TemplateEntity>>> searchData;
    String searchStr;
    public MutableLiveData<HashMap<String, List<TemplateEntity>>> getSearchData() {
        if(searchData==null){
            searchData=new MutableLiveData<>(null);
        }
        return searchData;
    }
    public void clearAll(){
            getSearchData().setValue(null);
    }

    public String getSearchStr() {
        if(searchStr==null){
            searchStr="";
        }
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }
}
