package com.thugdroid.memeking.room.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.dao.SocialUsernameDao;
import com.thugdroid.memeking.room.entity.SocialUsernameEntity;

import java.util.ArrayList;
import java.util.List;

public class SocialUsernameRepository extends AndroidViewModel {
    private SocialUsernameDao socialUsernameDao;

    public SocialUsernameRepository(@NonNull Application application) {
        super(application);
        AppDatabase db= AppDatabase.getInstance(application);
        socialUsernameDao=db.getSocialUsernameDao();
    }

    public LiveData<String> get(String userId){
        return socialUsernameDao.get(userId);
    }

    public void insert(SocialUsernameEntity socialUsernameEntity){
        List<SocialUsernameEntity> socialUsernameEntities=new ArrayList<>();
        socialUsernameEntities.add(socialUsernameEntity);
        insert(socialUsernameEntities);
    }

    public void insert(List<SocialUsernameEntity> socialUsernameEntityList){
        new Insert(socialUsernameEntityList).execute();
    }

    private class Insert extends AsyncTask{
        List<SocialUsernameEntity> socialUsernameEntityList;

        public Insert(List<SocialUsernameEntity> socialUsernameEntityList) {
            this.socialUsernameEntityList = socialUsernameEntityList;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            socialUsernameDao.insert(socialUsernameEntityList);
            return null;
        }
    }

}
