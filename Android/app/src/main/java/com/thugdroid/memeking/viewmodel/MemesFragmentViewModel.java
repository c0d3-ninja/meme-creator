package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.memeking.adapters.MemesRecyclerAdapter;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.room.entity.MemesDataEntity;

import java.util.ArrayList;
import java.util.List;

public class MemesFragmentViewModel extends ViewModel {
    public List memesFeedDataEntities;
    public List<MemesRecyclerAdapter.LoadingItem> loadingItems;
    private MutableLiveData<ApiModel> memesApiModel;
    public static final int loadingItemCount=1;

    private int currentItemPosition;
    private MemesDataEntity currentItemMemesDataEntity;
    public List getMemes() {
        if(memesFeedDataEntities==null){
            memesFeedDataEntities=new ArrayList<>();
        }
        return memesFeedDataEntities;
    }

    public List<MemesRecyclerAdapter.LoadingItem> getLoadingItems(){
        if (loadingItems==null){
            loadingItems=new ArrayList<>();
            for (int i = 0;i<loadingItemCount;i++){
                loadingItems.add(new MemesRecyclerAdapter.LoadingItem());
            }
        }
        return loadingItems;
    }

    public MutableLiveData<ApiModel> getMemesApiModel() {
        if(memesApiModel==null){
            memesApiModel=new MutableLiveData<>(new ApiModel());
        }
        return memesApiModel;
    }

    public int getCurrentItemPosition() {
        return currentItemPosition;
    }

    public void setCurrentItemPosition(int currentItemPosition) {
        this.currentItemPosition = currentItemPosition;
    }

    public MemesDataEntity getCurrentItemMemesDataEntity() {
        return currentItemMemesDataEntity;
    }

    public void setCurrentItemMemesDataEntity(MemesDataEntity currentItemMemesDataEntity) {
        this.currentItemMemesDataEntity = currentItemMemesDataEntity;
    }
    public int deleteAMeme(String id,int position){
        Object object = getMemes().get(position);
        if(object!=null && object instanceof MemesDataEntity){
            MemesDataEntity memesDataEntity = (MemesDataEntity)object;
            if(memesDataEntity.getId().equals(id)){
                getMemes().remove(position);
                return position;
            }
        }

        for (int i = 0; i < getMemes().size(); i++) {
             object = getMemes().get(i);
             if(object!=null && object instanceof MemesDataEntity) {
                 MemesDataEntity memesDataEntity = (MemesDataEntity) object;
                 if (memesDataEntity.getId().equals(id)) {
                     getMemes().remove(i);
                     return i ;
                 }
             }
        }
        return -1;
    }

    public int deleteAMeme(String id){
        Object object;
        for (int i = 0; i < getMemes().size(); i++) {
            object = getMemes().get(i);
            if(object!=null && object instanceof MemesDataEntity) {
                MemesDataEntity memesDataEntity = (MemesDataEntity) object;
                if (memesDataEntity.getId().equals(id)) {
                    getMemes().remove(i);
                    return i ;
                }
            }
        }
        return -1;
    }
}
