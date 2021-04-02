package com.thugdroid.memeking.viewmodel.db;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.thugdroid.memeking.room.entity.LoggedInUserEntity;
import com.thugdroid.memeking.room.repository.LoggedInUserRepository;

public class LoggedInUserDbViewModel extends AndroidViewModel {
    LoggedInUserRepository loggedInUserRepository;
    LiveData<LoggedInUserEntity> loggedInUserEntityLiveData;
    LiveData<String> selectedRegionIdLiveData;
    public LoggedInUserDbViewModel(@NonNull Application application) {
        super(application);
        loggedInUserRepository =new LoggedInUserRepository(application);
        loggedInUserEntityLiveData=loggedInUserRepository.getLoggedInUserAsLiveData();
        selectedRegionIdLiveData=loggedInUserRepository.getSelectedRegionIdAsLiveData();
    }


    public LiveData<LoggedInUserEntity> getLoggedInUserAsLiveData(){
        return loggedInUserEntityLiveData;
    }

    public LiveData<String> getSelectedRegionIdAsLiveData(){
        return selectedRegionIdLiveData;
    }

    public void insert(LoggedInUserEntity loggedInUserEntity){
        loggedInUserRepository.insert(loggedInUserEntity);
    }
    public void updateRegion(String regionId){
        loggedInUserRepository.updateRegion(regionId);
    }
}
