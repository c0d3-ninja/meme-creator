package com.thugdroid.memeking.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.thugdroid.memeking.room.entity.LoggedInUserEntity;

@Dao
public interface LoggedInUserDao {

    @Query("select * from LoggedInUserEntity limit 1")
    LiveData<LoggedInUserEntity> getLoggedInUserAsLiveData();

    @Query("select LoggedInUserEntity.regionId from LoggedInUserEntity limit 1")
    LiveData<String> getSelectedRegionIdAsLiveData();

    @Query("update LoggedInUserEntity set regionid=:regionId")
    void updateRegion(String regionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LoggedInUserEntity loggedInUserEntity);

    @Delete
    void delete(LoggedInUserEntity loggedInUserEntity);
    @Query("Delete from LoggedInUserEntity")
    void deleteAll();
}
