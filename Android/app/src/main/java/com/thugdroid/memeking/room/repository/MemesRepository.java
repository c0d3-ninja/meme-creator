package com.thugdroid.memeking.room.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.dao.MemesDao;
import com.thugdroid.memeking.room.entity.MemesDataEntity;
import com.thugdroid.memeking.room.entity.MemesFeedIdsEntity;
import com.thugdroid.memeking.room.entity.MyMemesIdsEntity;

import java.util.ArrayList;
import java.util.List;

public class MemesRepository extends AndroidViewModel{
    MemesDao memesDao;
    public MemesRepository(@NonNull Application application) {
        super(application);
        AppDatabase db= AppDatabase.getInstance(application);
        this.memesDao=db.getMemesDao();
    }


    public LiveData<List<MemesDataEntity>> getMyMemesAsLiveData(){
        return memesDao.getMyMemesAsLiveData();
    }

    public void insertMemesDataEntity(List<MemesDataEntity> memesDataEntities){
        new InsertMemesDataEntity(memesDataEntities).execute();
    }

    public void updateDownloadsCount(String id,int count){
        new UpdateDownloadsCount(id,count).execute();
    }

    public void updateSharesCount(String id,int count){
        new UpdateSharesCount(id,count).execute();
    }

    public void deleteAllMemesFeedIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteAllMemesFeedIds(dbOperationCallbackListener).execute();
    }

    public void insertMyMemesIdsEntity(List<MyMemesIdsEntity> myMemesIdsEntity){
        new InsertMyMemesIdsEntity(myMemesIdsEntity).execute();
    }


    public void deleteAllMyMemesIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteAllMyMemesIds(dbOperationCallbackListener).execute();
    }

    public void deleteAMeme(String id, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteAMeme(id,dbOperationCallbackListener).execute();
    }

    public void  updateMyInstaUsername(String userId,String instaUsername){
        new UpdateMyInstaUsername(userId,instaUsername).execute();
    }


    private class UpdateMyInstaUsername extends AsyncTask{
        private String userId,instaUsername;

        public UpdateMyInstaUsername(String userId, String instaUsername) {
            this.userId = userId;
            this.instaUsername = instaUsername;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            memesDao.updateMyInstaUsername(userId,instaUsername);
            return null;
        }
    }


    private class InsertMemesDataEntity extends AsyncTask {
        List<MemesDataEntity> memesDataEntities;

        public InsertMemesDataEntity(MemesDataEntity memesDataEntity) {
            memesDataEntities=new ArrayList<>();
            memesDataEntities.add(memesDataEntity);
        }

        public InsertMemesDataEntity(List<MemesDataEntity> memesDataEntities) {
            this.memesDataEntities = memesDataEntities;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            memesDao.insertMemesDataEntity(memesDataEntities);
            return null;
        }
    }

    private class InsertMemesFeedIdsEntity extends AsyncTask{
        List<MemesFeedIdsEntity> memesFeedIdsEntities;

        public InsertMemesFeedIdsEntity(List<MemesFeedIdsEntity> memesFeedIdsEntities) {
            this.memesFeedIdsEntities = memesFeedIdsEntities;
        }
        public InsertMemesFeedIdsEntity(MemesFeedIdsEntity memesFeedIdsEntity) {
            memesFeedIdsEntities=new ArrayList<>();
            memesFeedIdsEntities.add(memesFeedIdsEntity);
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            memesDao.insertMemesFeedIdsEntity(memesFeedIdsEntities);
            return null;
        }
    }

    private class DeleteMemesFeedId extends AsyncTask{
        String id;

        public DeleteMemesFeedId(String id) {
            this.id = id;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            memesDao.deleteMemesFeedId(id);
            return null;
        }
    }

    private class DeleteAllMemesFeedIds extends AsyncTask{
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;
        public DeleteAllMemesFeedIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.dbOperationCallbackListener=dbOperationCallbackListener;
        }
        @Override
        protected Object doInBackground(Object[] objects) {
            memesDao.deleteAllMemesFeedIds();
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

    private class InsertMyMemesIdsEntity extends AsyncTask{
        List<MyMemesIdsEntity> myMemesIdsEntity;

        public InsertMyMemesIdsEntity(List<MyMemesIdsEntity> myMemesIdsEntity) {
            this.myMemesIdsEntity = myMemesIdsEntity;
        }

        public InsertMyMemesIdsEntity(MyMemesIdsEntity myMemesIdsEntity) {
            this.myMemesIdsEntity=new ArrayList<>();
            this.myMemesIdsEntity.add(myMemesIdsEntity);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            memesDao.insertMyMemesIdsEntity(myMemesIdsEntity);
            return null;
        }
    }

    private class DeleteMyMemesId extends AsyncTask{
        String id;

        public DeleteMyMemesId(String id) {
            this.id = id;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            memesDao.deleteMyMemesId(id);
            return null;
        }
    }

    private class DeleteAllMyMemesIds extends AsyncTask{
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteAllMyMemesIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            memesDao.deleteAllMyMemesIds();
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

    private class UpdateDownloadsCount extends AsyncTask{
        String id;
        int count;

        public UpdateDownloadsCount(String id, int count) {
            this.id = id;
            this.count = count;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            memesDao.updateDownloadsCount(id,count);
            return null;
        }
    }
    private class UpdateSharesCount extends AsyncTask{
        String id;
        int count;

        public UpdateSharesCount(String id, int count) {
            this.id = id;
            this.count = count;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            memesDao.updateSharesCount(id,count);
            return null;
        }
    }

    private class DeleteAMeme extends AsyncTask{
        String id;
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteAMeme(String id, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.id = id;
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            memesDao.deleteMemesFeedId(id);
            memesDao.deleteMyMemesId(id);
            memesDao.deleteMemesDataById(id);
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
