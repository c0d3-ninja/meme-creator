package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.model.FacetModel;

import java.util.ArrayList;
import java.util.List;

public class SearchSuggestionFragmentViewModel extends ViewModel {
    MutableLiveData<ApiModel> suggestionRequest;
    List<FacetModel> searchSuggestionList;
    public MutableLiveData<ApiModel> getSuggestionRequest() {
        if(suggestionRequest==null){
            suggestionRequest=new MutableLiveData<>(new ApiModel());
        }
        return suggestionRequest;
    }

    public List<FacetModel> getSearchSuggestionList() {
        if(searchSuggestionList==null){
            searchSuggestionList=new ArrayList<>();
        }
        return searchSuggestionList;
    }

    public void setSearchSuggestionList(List<FacetModel> searchSuggestionList) {
        clearSearchSuggestionList();
        this.searchSuggestionList.addAll(searchSuggestionList);
    }

    public void clearSearchSuggestionList(){
        this.searchSuggestionList.clear();
    }
}
