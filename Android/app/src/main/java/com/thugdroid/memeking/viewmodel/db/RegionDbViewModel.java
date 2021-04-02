package com.thugdroid.memeking.viewmodel.db;

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
import com.thugdroid.memeking.room.dao.MemesDao;
import com.thugdroid.memeking.room.dao.SocialUsernameDao;
import com.thugdroid.memeking.room.dao.TemplateDao;
import com.thugdroid.memeking.room.dao.TemplatesGroupDataDao;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.RegionEntity;
import com.thugdroid.memeking.room.repository.RegionRepository;

import java.util.List;

public class RegionDbViewModel extends AndroidViewModel {
    RegionRepository regionRepository;
    LiveData<List<RegionEntity>> regions;
    public RegionDbViewModel(@NonNull Application application) {
        super(application);
        regionRepository=new RegionRepository(application);
        regions=regionRepository.getList();
    }

    public LiveData<List<RegionEntity>> getRegionsAsLiveData(){
        return regions;
    }

    public void insertRegion(RegionEntity regionEntity){
        regionRepository.insert(regionEntity);
    }

    public void readyDataForChangeRegion(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener){
        new ReadyDataForChangeRegion(dbOperationCallbackListener).execute();
    }

    public void insertRegions(List<RegionEntity> regionEntities){
        regionRepository.insertAll(regionEntities);
    }

    class ReadyDataForChangeRegion extends AsyncTask{
        AppDatabase.DbOperationCallbackListener dbOperationCallbackListener;

        public ReadyDataForChangeRegion(AppDatabase.DbOperationCallbackListener dbOperationCallbackListener) {
            this.dbOperationCallbackListener = dbOperationCallbackListener;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            AppDatabase db= AppDatabase.getInstance(getApplication());
            CategoryDao categoryDao=db.getCategoryDao();
            categoryDao.deleteAllCategories();

            TemplateDao templateDao=db.getTemplateDao();
            templateDao.deleteAllTemplateData();
            templateDao.deleteAllFavTemplateIds();
            templateDao.deleteAllMyTemplateIds();
            templateDao.deleteAllCategoryTemplateIds();
            templateDao.deleteAllFeedTemplateIds();

            AppPrefsDao appPrefsDao=db.getAppPrefsDao();
            String[] deletableAppPrefKeys = {AppPrefsEntity.SELECTED_CATEGORY,AppPrefsEntity.SELECTED_NAVDRAWER_MENU,
                    AppPrefsEntity.TEMPLATES_GROUP_SCROLL_EMPTY_TIME};
            for (String deletableAppPrefKey : deletableAppPrefKeys) {
                appPrefsDao.delete(deletableAppPrefKey);
            }

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
