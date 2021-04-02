package com.thugdroid.memeking.room.repository;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;

import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.dao.RegionDao;
import com.thugdroid.memeking.room.entity.RegionEntity;

import java.util.List;

public class RegionRepository {
    RegionDao regionDao;
    public RegionRepository(Application application) {
        AppDatabase db= AppDatabase.getInstance(application);
        this.regionDao = db.getRegionDao();
    }

    public LiveData<List<RegionEntity>> getList(){
        return regionDao.getList();
    }

    public void insertAll(List<RegionEntity> regionEntities){
        new Insert(regionEntities).execute();
    }


    public void insert(RegionEntity regionEntity){
        new Insert(regionEntity).execute();
    }

    class Insert extends AsyncTask{
        RegionEntity regionEntity;
        List<RegionEntity> regionEntities;
        public Insert(RegionEntity regionEntity) {
            this.regionEntity = regionEntity;
        }

        public Insert(List<RegionEntity> regionEntities) {
            this.regionEntities = regionEntities;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            if(regionEntities!=null){
                regionDao.insertAll(regionEntities);
            }else{
                regionDao.insert(regionEntity);
            }

            return null;
        }
    }

}
