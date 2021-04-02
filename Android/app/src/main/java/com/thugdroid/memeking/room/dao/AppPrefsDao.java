package com.thugdroid.memeking.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.CategoryEntity;

import java.util.List;

@Dao
public interface AppPrefsDao {
    @Query("select CategoryEntity.id as id,CategoryEntity.name as name," +
            "CategoryEntity.imageUrl as imageUrl,CategoryEntity.canUploadTemplate as canUploadTemplate," +
            "CategoryEntity.regionId as regionId from AppPrefsEntity inner join CategoryEntity on CategoryEntity.id=AppPrefsEntity.value" +
            " where AppPrefsEntity.id='"+AppPrefsEntity.SELECTED_CATEGORY+"'")
    LiveData<CategoryEntity> getSelectedCategoryAsLiveData();


    @Query("select AppPrefsEntity.value from AppPrefsEntity where AppPrefsEntity.id=:id")
    LiveData<String> getPref(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPref(AppPrefsEntity appPrefsEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPrefs(List<AppPrefsEntity> appPrefsEntities);


    @Query("Delete from AppPrefsEntity where AppPrefsEntity.id=:id")
    void delete(String id);


    @Query("Delete from AppPrefsEntity")
    void deleteAll();
}
