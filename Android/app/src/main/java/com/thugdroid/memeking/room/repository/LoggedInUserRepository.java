package com.thugdroid.memeking.room.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.dao.LoggedInUserDao;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;

public class LoggedInUserRepository {
    LoggedInUserDao userDao;
    public LoggedInUserRepository(Application application) {
        AppDatabase db=AppDatabase.getInstance(application);
        this.userDao = db.getUserDao();
    }

    public LiveData<LoggedInUserEntity> getLoggedInUserAsLiveData(){return userDao.getLoggedInUserAsLiveData();}

    public LiveData<String> getSelectedRegionIdAsLiveData(){
        return userDao.getSelectedRegionIdAsLiveData();
    }


    public void insert(LoggedInUserEntity loggedInUserEntity){
        new Insert(loggedInUserEntity).execute();
    }

    public void updateRegion(String regionId){
        new UpdateRegion(regionId).execute();
    }

    class UpdateRegion extends AsyncTask{
        String regionId;

        public UpdateRegion(String regionId) {
            this.regionId = regionId;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            userDao.updateRegion(regionId);
            return null;
        }
    }

    class Insert extends AsyncTask{
        LoggedInUserEntity loggedInUserEntity;

        public Insert(LoggedInUserEntity loggedInUserEntity) {
            this.loggedInUserEntity = loggedInUserEntity;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            userDao.insert(loggedInUserEntity);
            return null;
        }
    }
}
