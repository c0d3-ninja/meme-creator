package com.thugdroid.memeking.room.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.dao.TemplatesGroupDataDao;
import com.thugdroid.memeking.room.entity.TemplatesGroupDataEntity;

import java.util.List;

public class TemplatesGroupRepository extends AndroidViewModel {

    TemplatesGroupDataDao templatesGroupDataDao;
    public TemplatesGroupRepository(@NonNull Application application) {
        super(application);
        AppDatabase db= AppDatabase.getInstance(application);
        this.templatesGroupDataDao=db.getTemplatesGroupDataDao();
    }

    public LiveData<List<TemplatesGroupDataEntity>> getAllTemplatesGroupsAsLiveData(String regionId){
        return templatesGroupDataDao.getAllTemplatesGroupsAsLiveData(regionId);
    }
    public void insert(List<TemplatesGroupDataEntity> templatesGroupDataEntities){
        new Insert(templatesGroupDataEntities).execute();
    }

    public void deleteAll(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteAll(dbOperationCallbackListener).execute();
    }


    private class Insert extends AsyncTask{
        List<TemplatesGroupDataEntity> templatesGroupDataEntities;

        public Insert(List<TemplatesGroupDataEntity> templatesGroupDataEntities) {
            this.templatesGroupDataEntities = templatesGroupDataEntities;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templatesGroupDataDao.insert(templatesGroupDataEntities);
            return null;
        }
    }

    private class DeleteAll extends AsyncTask{
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteAll(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templatesGroupDataDao.deleteAll();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(dbOperationCallbackListener!=null){
                dbOperationCallbackListener.onSuccess();
            }
        }
    }

}
