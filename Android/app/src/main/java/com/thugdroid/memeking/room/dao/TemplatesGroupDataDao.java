package com.thugdroid.memeking.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.thugdroid.memeking.room.entity.TemplatesGroupDataEntity;

import java.util.List;

@Dao
public interface TemplatesGroupDataDao {
    @Query("select * from TemplatesGroupDataEntity where TemplatesGroupDataEntity.regionId=:regionId order by TemplatesGroupDataEntity.name asc")
    LiveData<List<TemplatesGroupDataEntity>> getAllTemplatesGroupsAsLiveData(String regionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<TemplatesGroupDataEntity> templatesGroupDataEntities);

    @Query("Delete from TemplatesGroupDataEntity")
    void deleteAll();
}
