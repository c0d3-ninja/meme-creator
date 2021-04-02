package com.thugdroid.memeking.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.thugdroid.memeking.room.entity.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    @Query("select * from categoryentity where CategoryEntity.regionId=:regionId")
    LiveData<List<CategoryEntity>> getAllCategoriesAsLiveData(String regionId);

    @Query("select * from categoryentity where CategoryEntity.canUploadTemplate=1 AND CategoryEntity.regionId=:regionId")
    LiveData<List<CategoryEntity>> getTemplateUploadAbleCategoriesAsLiveData(String regionId);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategories(List<CategoryEntity> categoryEntities);

    @Delete
    void delete(List<CategoryEntity> categoryEntities);

    @Query("Delete from CategoryEntity")
    void deleteAllCategories();

}
