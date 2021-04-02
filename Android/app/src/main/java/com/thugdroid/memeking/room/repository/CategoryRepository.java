package com.thugdroid.memeking.room.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.dao.CategoryDao;
import com.thugdroid.memeking.room.entity.CategoryEntity;

import java.util.ArrayList;
import java.util.List;

public class CategoryRepository extends AndroidViewModel {
    CategoryDao categoryDao;
    public CategoryRepository(@NonNull Application application) {
        super(application);
        AppDatabase db= AppDatabase.getInstance(application);
        categoryDao=db.getCategoryDao();
    }
    public LiveData<List<CategoryEntity>> getAllCategoriesAsLiveData(String regionId){
        return categoryDao.getAllCategoriesAsLiveData(regionId);
    }

    public LiveData<List<CategoryEntity>> getTemplateUploadAbleCategoriesAsLiveData(String regionId){
        return categoryDao.getTemplateUploadAbleCategoriesAsLiveData(regionId);
    }

    public void insertAllCategories(List<CategoryEntity> categoryEntities){
        new InsertAll(categoryEntities).execute();
    }

    public void deleteUnwantedCategories(List<CategoryEntity> oldCategories, List<CategoryEntity> newCategories,AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new DeleteUnwantedCategories(oldCategories,newCategories,dbOperationCallbackListener).execute();
    }

    class InsertAll extends AsyncTask {
        List<CategoryEntity> categoryEntities;

        public InsertAll(List<CategoryEntity> categoryEntities ) {
            this.categoryEntities=categoryEntities;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            categoryDao.insertCategories(categoryEntities);
            return null;
        }
    }

    public class DeleteUnwantedCategories extends AsyncTask{
        List<CategoryEntity> oldCategories,newCategories;
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public DeleteUnwantedCategories(List<CategoryEntity> oldCategories, List<CategoryEntity> newCategories, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.oldCategories = oldCategories;
            this.newCategories = newCategories;
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            List<String> newCategoryIds = new ArrayList<>();
            List<CategoryEntity> deletableCategories = new ArrayList<>();
            if(oldCategories.size()!=0 && newCategories.size()!=0){
                for (CategoryEntity newCategory : newCategories) {
                    newCategoryIds.add(newCategory.getId());
                }
                for (CategoryEntity oldCategory : oldCategories) {
                    if(!newCategoryIds.contains(oldCategory.getId())){
                        deletableCategories.add(oldCategory);
                    }
                }
            }
            if(deletableCategories.size()>0){
                categoryDao.delete(deletableCategories);
            }
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
