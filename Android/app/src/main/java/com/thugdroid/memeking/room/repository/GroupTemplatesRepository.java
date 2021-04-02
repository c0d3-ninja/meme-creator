package com.thugdroid.memeking.room.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.dao.GTsLastApiCallTimeDao;
import com.thugdroid.memeking.room.dao.GroupTemplatesDao;
import com.thugdroid.memeking.room.entity.GTsLastApiCallTimeEntity;
import com.thugdroid.memeking.room.entity.GroupTemplatesIdsEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;

import java.util.List;

public class GroupTemplatesRepository extends AndroidViewModel {
    AppDatabase db;
    GroupTemplatesDao groupTemplatesDao;
    GTsLastApiCallTimeDao gTsLastApiCallTimeDao;
    public GroupTemplatesRepository(@NonNull Application application) {
        super(application);
        db= AppDatabase.getInstance(application);
        groupTemplatesDao=db.getGroupTemplatesDao();
        gTsLastApiCallTimeDao=db.getGTsLastApiCallTimeDao();
    }

    public LiveData<List<TemplateEntity>> getAllGroupTemplatesAsLiveData(String searchStr,String regionId){
        return groupTemplatesDao.getAllGroupTemplatesAsLiveData(searchStr,regionId);
    }

    public LiveData<Long> getApiCalledTimeAsLiveData(String groupTemplatesSearchStr){
        return gTsLastApiCallTimeDao.getApiCalledTimeAsLiveData(groupTemplatesSearchStr);
    }

    public void insertGTsLastCalledTime(GTsLastApiCallTimeEntity gTsLastApiCallTimeEntity){
        new InsertGTsLastCalledTime(gTsLastApiCallTimeEntity).execute();
    }

    public void insert(List<GroupTemplatesIdsEntity> groupTemplateIdsEntities){
        new Insert(groupTemplateIdsEntities).execute();
    }

    public void deleteAllBySearchStr(String searchStr, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteAllBySearchStr(searchStr,dbOperationCallbackListener).execute();
    }

    public void deleteAll(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteAll(dbOperationCallbackListener).execute();
    }


    private class InsertGTsLastCalledTime extends AsyncTask{
        GTsLastApiCallTimeEntity gTsLastApiCallTimeEntity;

        public InsertGTsLastCalledTime(GTsLastApiCallTimeEntity gTsLastApiCallTimeEntity) {
            this.gTsLastApiCallTimeEntity = gTsLastApiCallTimeEntity;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            gTsLastApiCallTimeDao.insert(gTsLastApiCallTimeEntity);
            return null;
        }
    }

    private class Insert extends AsyncTask{
        List<GroupTemplatesIdsEntity> groupTemplateIdsEntities;
        public Insert(List<GroupTemplatesIdsEntity> groupTemplateIdsEntities) {
            this.groupTemplateIdsEntities = groupTemplateIdsEntities;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            for (GroupTemplatesIdsEntity groupTemplatesIdsEntity : groupTemplateIdsEntities) {
                groupTemplatesDao.insert(groupTemplatesIdsEntity.getSearchStr(), groupTemplatesIdsEntity.getTemplateId());
            }
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
            groupTemplatesDao.deleteAll();
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

    private class DeleteAllBySearchStr extends AsyncTask{
        String searchStr;
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteAllBySearchStr(String searchStr, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.searchStr = searchStr;
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            groupTemplatesDao.deleteAllBySearchStr(searchStr);
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
