package com.thugdroid.memeking.viewmodel.db;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.entity.AllCategoryTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.FavTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.FeedTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.MyTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.room.repository.TemplateRepository;

import java.util.List;

public class TemplateDbViewModel extends AndroidViewModel {
    TemplateRepository templateRepository;
    LiveData<List<TemplateEntity>> templateEntityLiveData;
    public TemplateDbViewModel(@NonNull Application application) {
        super(application);
        this.templateRepository=new TemplateRepository(application);
    }
    public LiveData<List<TemplateEntity>> getFeedTemplatesAsLiveData(String categoryId){
        if(templateEntityLiveData==null){
            templateEntityLiveData=templateRepository.getFeedTemplatesAsLiveData(categoryId);
        }
        return templateEntityLiveData;
    }

    public LiveData<List<TemplateEntity>> getAllCategoryTemplatesAsLiveData(){
        if(templateEntityLiveData==null){
            templateEntityLiveData=templateRepository.getAllCategoryTemplatesAsLiveData();
        }
        return templateEntityLiveData;
    }
    public LiveData<List<TemplateEntity>> getMyTemplatesAsLiveData(){
        if(templateEntityLiveData==null){
            templateEntityLiveData=templateRepository.getMyTemplatesAsLiveData();
        }
        return templateEntityLiveData;
    }

    public LiveData<List<TemplateEntity>> getFavoriteTemplatesAsLiveData(){
        if(templateEntityLiveData==null){
            templateEntityLiveData=templateRepository.getFavoriteTemplatesAsLiveData();
        }
        return templateEntityLiveData;
    }

    public LiveData<List<TemplateEntity>> getFavoriteTemplatesAsLiveData(int limit){
        return templateRepository.getFavoriteTemplatesAsLiveData(limit);
    }

    public void insertSingleTemplateData(TemplateEntity templateEntity){
        templateRepository.insertSingleTemplateData(templateEntity);
    }
    public void insertAllTemplateData(List<TemplateEntity> templateEntities){
        templateRepository.insertAllTemplateData(templateEntities);
    }
    public void updateIsFavorite(String id,boolean isFavorite){
        templateRepository.updateIsFavorite(id,isFavorite);
    }
    public void updateCatIdAndSearchTags(String id,String categoryId,String searchTags,AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        templateRepository.updateCatIdAndSearchTags(id,categoryId,searchTags,dbOperationCallbackListener);
    }
    public void deleteSingleTemplateData(String id){
        templateRepository.deleteSingleTemplateData(id);
    }
    public void deleteAllTemplateData(){
        templateRepository.deleteAllTemplateData();
    }


    public void insertAllFeedTemplateIds(List<FeedTemplateIdsEntity> templateIdsEntities){
        templateRepository.insertAllFeedTemplateIds(templateIdsEntities);
    }

    public void insertSingleFeedTemplateId(FeedTemplateIdsEntity feedTemplateIdsEntity){
        templateRepository.insertSingleFeedTemplateId(feedTemplateIdsEntity);
    }
    public void deleteSingleFeedTemplateId(String templateId,AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        templateRepository.deleteSingleFeedTemplateId(templateId,dbOperationCallbackListener);
    }
    public void deleteAllFeedTemplateIdsByCategoryId(String categoryId,AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        templateRepository.deleteAllFeedTemplatesByCategoryId(categoryId,dbOperationCallbackListener);
    }
    public void deleteAllFeedTemplateIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        templateRepository.deleteAllFeedTemplateIds(dbOperationCallbackListener);
    }


    public void insertSingleAllCategoryTemplateId(AllCategoryTemplateIdsEntity allCategoryTemplateIdsEntity){
        templateRepository.insertSingleAllCategoryTemplateId(allCategoryTemplateIdsEntity);
    }
    public void insertAllCategoryTemplateIds(List<AllCategoryTemplateIdsEntity> allTemplateIdsEntities){
        templateRepository.insertAllCategoryTemplateIds(allTemplateIdsEntities);
    }
    public void deleteAllCategoryTemplateIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        templateRepository.deleteAllCategoryTemplateIds(dbOperationCallbackListener);

    }



    public void insertSingleMyTemplateId(MyTemplateIdsEntity myTemplateIdsEntity){
        templateRepository.insertSingleMyTemplateId(myTemplateIdsEntity);
    }
    public void insertAllMyTemplateIds(List<MyTemplateIdsEntity> myTemplateIdsEntities){
        templateRepository.insertAllMyTemplateIds(myTemplateIdsEntities);
    }
    public void deleteAllMyTemplateIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        templateRepository.deleteAllMyTemplateIds(dbOperationCallbackListener);

    }


    public void insertSingleFavTemplateId(FavTemplateIdsEntity favTemplateIdsEntity){
        templateRepository.insertSingleFavTemplateId(favTemplateIdsEntity);
    }
    public void insertAllFavTemplateIds(List<FavTemplateIdsEntity> favTemplateIdsEntities){
        templateRepository.insertAllFavTemplateIds(favTemplateIdsEntities);
    }
    public void deleteFavorite(String id){
        templateRepository.deleteFavorite(id);
    }
    public void deleteAllFavTemplateIds(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        templateRepository.deleteAllFavTemplateIds(dbOperationCallbackListener);
    }


    public void deleteTemplate(String id, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        templateRepository.deleteTemplate(id,dbOperationCallbackListener);
    }

    public void updateTemplate(TemplateEntity templateEntity,AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new UpdateTemplate(templateEntity,dbOperationCallbackListener).execute();
    }


    public void deleteTemplateForReports(String id, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        templateRepository.deleteTemplateForReports(id,dbOperationCallbackListener);
    }


    public class UpdateTemplate extends AsyncTask{
        TemplateEntity templateEntity;
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public UpdateTemplate(TemplateEntity templateEntity,AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
           this.templateEntity=templateEntity;
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            templateRepository.updateSingleFeedCategoryIdSync(templateEntity.getId(),templateEntity.getCategoryId());
            templateRepository.insertSingleTemplateDataSync(templateEntity);
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
