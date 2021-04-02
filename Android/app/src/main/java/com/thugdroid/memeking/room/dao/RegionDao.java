package com.thugdroid.memeking.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.thugdroid.memeking.room.entity.RegionEntity;

import java.util.List;

@Dao
public interface RegionDao {

    @Query("select * from RegionEntity")
    LiveData<List<RegionEntity>> getList();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(RegionEntity regionEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<RegionEntity> regionEntities);

    @Delete
    void delete(RegionEntity regionEntity);

    @Query("Delete from RegionEntity")
    void deleteAll();
}
