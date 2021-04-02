package com.thugdroid.memeking.room.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.dao.TemplateDao;
import com.thugdroid.memeking.room.entity.AllCategoryTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.FavTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.FeedTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.MyTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;

import java.util.List;

public class TemplateRepository {
    TemplateDao templateDao;
    public TemplateRepository(Application application) {
        AppDatabase db= AppDatabase.getInstance(application);
        this.templateDao=db.getTemplateDao();
    }

    public LiveData<List<TemplateEntity>> getFeedTemplatesAsLiveData(String categoryId){
        return templateDao.getFeedTemplatesAsLiveData(categoryId);
    }

    public LiveData<List<TemplateEntity>> getAllCategoryTemplatesAsLiveData(){
        return templateDao.getAllCategoryTemplatesAsLiveData();
    }

    public LiveData<List<TemplateEntity>> getMyTemplatesAsLiveData(){
        return templateDao.getMyTemplatesAsLiveData();
    }

    public LiveData<List<TemplateEntity>> getFavoriteTemplatesAsLiveData(){
        return templateDao.getFavoriteTemplatesAsLiveData();
    }

    public LiveData<List<TemplateEntity>> getFavoriteTemplatesAsLiveData(int limit){
        return templateDao.getFavoriteTemplatesAsLiveData(limit);
    }


    public void insertAllTemplateData(List<TemplateEntity> templateEntities){
        new InsertAllTemplateData(templateEntities).execute();
    }
    public void insertSingleTemplateData(TemplateEntity templateEntity){
        new InsertSingleTemplateData(templateEntity).execute();
    }
    public void insertSingleTemplateDataSync(TemplateEntity templateEntity){
        templateDao.insertSingleTemplateData(templateEntity);
    }
    public void updateIsFavorite(String id,boolean isFavorite){
        new UpdateIsFavorite(id,isFavorite).execute();
    }
    public void updateCatIdAndSearchTags(String id,String categoryId,String searchTags,AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new UpdateCatIdAndSearchTags(id,categoryId,searchTags,dbOperationCallbackListener).execute();
    }
    public void deleteSingleTemplateData(String id){
        new DeleteSingleTemplateData(id).execute();
    }
    public void deleteAllTemplateData(){
        new DeleteAllTemplateData().execute();
    }



    public void insertSingleFeedTemplateId(FeedTemplateIdsEntity feedTemplateIdsEntity){
        new InsertSingleTemplateId(feedTemplateIdsEntity).execute();
    }
    public void insertAllFeedTemplateIds(List<FeedTemplateIdsEntity> templateIdsEntities){
        new InsertAllFeedTemplateIds(templateIdsEntities).execute();
    }
    public void updateSingleFeedCategoryIdSync(String id,String categoryId){
        templateDao.updateSingleFeedCategoryId(id,categoryId);
    }
    public void deleteSingleFeedTemplateId(String id,AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteSingleFeedTemplateId(id,dbOperationCallbackListener).execute();
    }
    public void deleteAllFeedTemplatesByCategoryId(String categoryId,AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteAllFeedTemplatesByCategoryId(categoryId,dbOperationCallbackListener).execute();
    }
    public void deleteAllFeedTemplateIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteAllFeedTemplateIds(dbOperationCallbackListener).execute();
    }


    public void insertAllCategoryTemplateIds(List<AllCategoryTemplateIdsEntity> allTemplateIdsEntities){
        new InsertAllCategoryTemplateIds(allTemplateIdsEntities).execute();
    }
    public void insertSingleAllCategoryTemplateId(AllCategoryTemplateIdsEntity allCategoryTemplateIdsEntity){
        new InsertSingleAllCategoryTemplateId(allCategoryTemplateIdsEntity).execute();
    }
    public void deleteAllCategoryTemplateIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteAllCategoryTemplateIds(dbOperationCallbackListener).execute();

    }

    public void insertAllMyTemplateIds(List<MyTemplateIdsEntity> myTemplateIdsEntities){
        new InsertAllMyTemplateIds(myTemplateIdsEntities).execute();
    }
    public void insertSingleMyTemplateId(MyTemplateIdsEntity myTemplateIdsEntity){
        new InsertSingleMyTemplateId(myTemplateIdsEntity).execute();
    }
    public void deleteAllMyTemplateIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteAllMyTemplateIds(dbOperationCallbackListener).execute();

    }


    public void insertAllFavTemplateIds(List<FavTemplateIdsEntity> favTemplateIdsEntities){
        new InsertAllFavTemplateIds(favTemplateIdsEntities).execute();
    }
    public void insertSingleFavTemplateId(FavTemplateIdsEntity favTemplateIdsEntity){
        new InsertSingleFavTemplateId(favTemplateIdsEntity).execute();
    }
    public void deleteFavorite(String id){
        new DeleteFavorite(id).execute();
    }
    public void deleteAllFavTemplateIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteAllFavTemplateIds(dbOperationCallbackListener).execute();
    }


    public void deleteTemplate(String id,AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteTemplate(id,dbOperationCallbackListener).execute();
    }


    public void deleteTemplateForReports(String id, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteTemplateForReports(id,dbOperationCallbackListener).execute();
    }


    class InsertAllTemplateData extends AsyncTask {
        List<TemplateEntity> templateEntities;

        public InsertAllTemplateData(List<TemplateEntity> templateEntities) {
            this.templateEntities = templateEntities;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.insertAllTemplateData(templateEntities);
            return null;
        }
    }
    class InsertSingleTemplateData extends AsyncTask {
        TemplateEntity templateEntity;

        public InsertSingleTemplateData(TemplateEntity templateEntity) {
            this.templateEntity = templateEntity;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.insertSingleTemplateData(templateEntity);
            return null;
        }
    }
    class UpdateIsFavorite extends AsyncTask{
        String id;
        boolean isFavorite;

        public UpdateIsFavorite(String id, boolean isFavorite) {
            this.id = id;
            this.isFavorite = isFavorite;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.updateIsFavorite(id,isFavorite);
            return null;
        }
    }
    class UpdateCatIdAndSearchTags extends AsyncTask{
        String id;
        String categoryId;
        String searchTags;
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public UpdateCatIdAndSearchTags(String id, String categoryId, String searchTags,AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.id = id;
            this.categoryId = categoryId;
            this.searchTags = searchTags;
            this.dbOperationCallbackListener=dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.updateCatIdAndSearchTags(id,categoryId,searchTags);
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
    class DeleteSingleTemplateData extends AsyncTask{
        String id;

        public DeleteSingleTemplateData(String id) {
            this.id = id;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.deleteSingleTemplateData(id);
            return null;
        }
    }
    class DeleteAllTemplateData extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.deleteAllTemplateData();
            return null;
        }
    }


    class InsertSingleTemplateId extends AsyncTask{
        FeedTemplateIdsEntity feedTemplateIdsEntity;

        public InsertSingleTemplateId(FeedTemplateIdsEntity feedTemplateIdsEntity) {
            this.feedTemplateIdsEntity = feedTemplateIdsEntity;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.insertSingleFeedTemplateId(feedTemplateIdsEntity);
            return null;
        }
    }
    class InsertAllFeedTemplateIds extends AsyncTask{
        List<FeedTemplateIdsEntity> templateIdsEntities;

        public InsertAllFeedTemplateIds(List<FeedTemplateIdsEntity> templateIdsEntities) {
            this.templateIdsEntities = templateIdsEntities;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.insertAllFeedTemplateIds(templateIdsEntities);
            return null;
        }
    }
    class DeleteSingleFeedTemplateId extends AsyncTask{
        String id;
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteSingleFeedTemplateId(String id, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.id = id;
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.deleteSingleFeedTemplateId(id);
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
    class DeleteAllFeedTemplatesByCategoryId extends AsyncTask{
        String categoryId;
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteAllFeedTemplatesByCategoryId(String categoryId, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.categoryId = categoryId;
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.deleteAllFeedTemplatesByCategoryId(categoryId);
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
    class DeleteAllFeedTemplateIds extends AsyncTask{
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteAllFeedTemplateIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.deleteAllFeedTemplateIds();
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




    class InsertSingleAllCategoryTemplateId extends AsyncTask{
        AllCategoryTemplateIdsEntity allCategoryTemplateIdsEntity;

        public InsertSingleAllCategoryTemplateId(AllCategoryTemplateIdsEntity allCategoryTemplateIdsEntity) {
            this.allCategoryTemplateIdsEntity = allCategoryTemplateIdsEntity;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.insertSingleAllCategoryTemplateIds(allCategoryTemplateIdsEntity);
            return null;
        }
    }
    class InsertAllCategoryTemplateIds extends AsyncTask{
        List<AllCategoryTemplateIdsEntity> allTemplateIdsEntities;

        public InsertAllCategoryTemplateIds(List<AllCategoryTemplateIdsEntity> allTemplateIdsEntities) {
            this.allTemplateIdsEntities = allTemplateIdsEntities;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.insertAllCategoryTemplateIds(allTemplateIdsEntities);
            return null;
        }
    }
    class DeleteAllCategoryTemplateIds extends AsyncTask{
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteAllCategoryTemplateIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.deleteAllCategoryTemplateIds();
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



    class InsertSingleMyTemplateId extends AsyncTask{
        MyTemplateIdsEntity myTemplateIdsEntity;

        public InsertSingleMyTemplateId(MyTemplateIdsEntity myTemplateIdsEntity) {
            this.myTemplateIdsEntity = myTemplateIdsEntity;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.insertSingleMyTemplateIds(myTemplateIdsEntity);
            return null;
        }
    }
    class InsertAllMyTemplateIds extends AsyncTask{
        List<MyTemplateIdsEntity> myTemplateIdsEntities;

        public InsertAllMyTemplateIds(List<MyTemplateIdsEntity> myTemplateIdsEntities) {
            this.myTemplateIdsEntities = myTemplateIdsEntities;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.insertAllMyTemplateIds(myTemplateIdsEntities);
            return null;
        }
    }
    class DeleteAllMyTemplateIds extends AsyncTask{
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteAllMyTemplateIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.deleteAllMyTemplateIds();
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


    class InsertSingleFavTemplateId extends AsyncTask{
        FavTemplateIdsEntity favTemplateIdsEntity;

        public InsertSingleFavTemplateId(FavTemplateIdsEntity favTemplateIdsEntity) {
            this.favTemplateIdsEntity = favTemplateIdsEntity;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.insertSingleFavTemplateIds(favTemplateIdsEntity);
            return null;
        }
    }
    class InsertAllFavTemplateIds extends AsyncTask{
        List<FavTemplateIdsEntity> favTemplateIdsEntities;

        public InsertAllFavTemplateIds(List<FavTemplateIdsEntity> favTemplateIdsEntities) {
            this.favTemplateIdsEntities = favTemplateIdsEntities;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.insertAllFavTemplateIds(favTemplateIdsEntities);
            return null;
        }
    }
    class DeleteFavorite extends AsyncTask{
        String id;

        public DeleteFavorite(String id) {
            this.id = id;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.deleteSingleFavorite(id);
            return null;
        }
    }
    class DeleteAllFavTemplateIds extends AsyncTask{
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteAllFavTemplateIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.deleteAllFavTemplateIds();
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


    class DeleteTemplate extends AsyncTask{
        String templateId;
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteTemplate(String templateId, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.templateId = templateId;
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.deleteSingleMyTemplateId(templateId);
            templateDao.deleteSingleFeedTemplateId(templateId);
            templateDao.deleteSingleAllCategoryTemplateId(templateId);
            templateDao.deleteSingleTemplateData(templateId);
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

    class DeleteTemplateForReports  extends AsyncTask{
        String templateId;
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteTemplateForReports(String templateId, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.templateId = templateId;
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateDao.deleteSingleFeedTemplateId(templateId);
            templateDao.deleteSingleAllCategoryTemplateId(templateId);
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
