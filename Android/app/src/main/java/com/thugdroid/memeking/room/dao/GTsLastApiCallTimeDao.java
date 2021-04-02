package com.thugdroid.memeking.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.thugdroid.memeking.room.entity.GTsLastApiCallTimeEntity;

@Dao
public interface GTsLastApiCallTimeDao {

    @Query("select GTsLastApiCallTimeEntity.time from GTsLastApiCallTimeEntity where GTsLastApiCallTimeEntity.id=:groupTemplatesSearchStr")
    LiveData<Long> getApiCalledTimeAsLiveData(String groupTemplatesSearchStr);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GTsLastApiCallTimeEntity gTsLastApiCallTimeEntity);

    @Query("Delete From GTsLastApiCallTimeEntity")
    void deleteAll();
}
