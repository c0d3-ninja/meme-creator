package com.thugdroid.memeking.room.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.dao.AppPrefsDao;
import com.thugdroid.memeking.room.dao.CategoryDao;
import com.thugdroid.memeking.room.dao.GTsLastApiCallTimeDao;
import com.thugdroid.memeking.room.dao.GroupTemplatesDao;
import com.thugdroid.memeking.room.dao.LoggedInUserDao;
import com.thugdroid.memeking.room.dao.MemesDao;
import com.thugdroid.memeking.room.dao.RegionDao;
import com.thugdroid.memeking.room.dao.SocialUsernameDao;
import com.thugdroid.memeking.room.dao.TemplateDao;
import com.thugdroid.memeking.room.dao.TemplatesGroupDataDao;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.CategoryEntity;

import java.util.ArrayList;
import java.util.List;

public class AppPrefsRepository extends AndroidViewModel {
    AppPrefsDao appPrefsDao;
    AppDatabase db;
    public AppPrefsRepository(@NonNull Application application) {
        super(application);
         db= AppDatabase.getInstance(application);
        this.appPrefsDao = db.getAppPrefsDao();
    }

    public AppDatabase getDb() {
        return db;
    }

    public LiveData<CategoryEntity> getSelectedCategoryDetailsAsLiveData(){
        return appPrefsDao.getSelectedCategoryAsLiveData();
    }

    public LiveData<String> getPref(String id){
        return appPrefsDao.getPref(id);
    }

    public void insertPref(String id, String value){
        new InsertPref(id,value).execute();
   }

   public void insertPrefs(List<AppPrefsEntity> appPrefsEntities){
        new InsertPrefs(appPrefsEntities).execute();
   }
    public void insertPrefs(List<AppPrefsEntity> appPrefsEntities, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new InsertPrefs(appPrefsEntities,dbOperationCallbackListener).execute();
    }

    public void delete(String id){
        new Delete(id).execute();
    }

    public void updateSelectedCategory(String categoryId){
        updateSelectedCategory(categoryId,null);
    }

    public void updateSelectedCategory(String categoryId,AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        List<AppPrefsEntity> appPrefsEntities = new ArrayList<>();
        AppPrefsEntity appPrefsEntity = new AppPrefsEntity(AppPrefsEntity.SELECTED_CATEGORY,categoryId);
        AppPrefsEntity appPrefsEntity1 = new AppPrefsEntity(AppPrefsEntity.SELECTED_NAVDRAWER_MENU,null);
        appPrefsEntities.add(appPrefsEntity);
        appPrefsEntities.add(appPrefsEntity1);
        insertPrefs(appPrefsEntities,dbOperationCallbackListener);
    }

    public void updateSelectedNavDrawerMenu(String menuId){
        updateSelectedNavDrawerMenu(menuId,null);
    }

    public void updateSelectedNavDrawerMenu(String menuId, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        List<AppPrefsEntity> appPrefsEntities = new ArrayList<>();
        AppPrefsEntity appPrefsEntity = new AppPrefsEntity(AppPrefsEntity.SELECTED_CATEGORY,null);
        AppPrefsEntity appPrefsEntity1 = new AppPrefsEntity(AppPrefsEntity.SELECTED_NAVDRAWER_MENU,menuId);
        appPrefsEntities.add(appPrefsEntity);
        appPrefsEntities.add(appPrefsEntity1);
        insertPrefs(appPrefsEntities,dbOperationCallbackListener);
    }
    public void updateLogoUrl(String url){
        insertPref(AppPrefsEntity.LOGO_PATH,url);
    }

    public void clearAllTablesSync(){
        LoggedInUserDao loggedInUserDao=db.getUserDao();
        loggedInUserDao.deleteAll();
        String[] deletableAppPrefKeys = {AppPrefsEntity.LOGO_PATH,
                AppPrefsEntity.SELECTED_NAVDRAWER_MENU,
                AppPrefsEntity.SELECTED_CATEGORY,AppPrefsEntity.CATEGORY_SILENTLY_CALLED_TIME,
                AppPrefsEntity.SAVE_MEME_REWARDED_AD_LAST_SHOWTIME,
                AppPrefsEntity.TEMPLATES_GROUP_SCROLL_EMPTY_TIME,
        AppPrefsEntity.INSTA_USERNAME,AppPrefsEntity.IS_ADMIN_USER};
        for (String deletableAppPrefKey : deletableAppPrefKeys) {
            appPrefsDao.delete(deletableAppPrefKey);
        }
        CategoryDao categoryDao=db.getCategoryDao();
        categoryDao.deleteAllCategories();
        RegionDao regionDao=db.getRegionDao();
        regionDao.deleteAll();
        TemplateDao templateDao=db.getTemplateDao();
        templateDao.deleteAllMyTemplateIds();
        templateDao.deleteAllCategoryTemplateIds();
        templateDao.deleteAllFavTemplateIds();
        templateDao.deleteAllFeedTemplateIds();
        templateDao.deleteAllTemplateData();
        MemesDao memesDao=db.getMemesDao();
        memesDao.deleteAllMyMemesIds();
        memesDao.deleteAllMemesFeedIds();
        memesDao.deleteAllMemesData();
        TemplatesGroupDataDao templatesGroupDataDao=db.getTemplatesGroupDataDao();
        templatesGroupDataDao.deleteAll();
        GroupTemplatesDao groupTemplatesDao=db.getGroupTemplatesDao();
        groupTemplatesDao.deleteAll();
        GTsLastApiCallTimeDao gTsLastApiCallTimeDao=db.getGTsLastApiCallTimeDao();
        gTsLastApiCallTimeDao.deleteAll();
        SocialUsernameDao socialUsernameDao=db.getSocialUsernameDao();
        socialUsernameDao.deleteAll();
    }

    class InsertPref extends AsyncTask{
        String id,value;

        public InsertPref(String id, String value) {
            this.id = id;
            this.value = value;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            AppPrefsEntity appPrefsEntity=new AppPrefsEntity(id,value);
            appPrefsDao.insertPref(appPrefsEntity);
            return null;
        }
    }

    class Delete extends AsyncTask{
        String id;
        public Delete(String id) {
            this.id = id;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            appPrefsDao.delete(id);
            return null;
        }
    }


    class InsertPrefs extends AsyncTask{
        List<AppPrefsEntity> appPrefsEntities;
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public InsertPrefs(List<AppPrefsEntity> appPrefsEntities, AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.appPrefsEntities = appPrefsEntities;
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        public InsertPrefs(List<AppPrefsEntity> appPrefsEntities) {
            this.appPrefsEntities = appPrefsEntities;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            appPrefsDao.insertPrefs(appPrefsEntities);
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
