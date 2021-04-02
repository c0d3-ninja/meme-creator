package com.thugdroid.memeking.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.thugdroid.memeking.room.entity.AllCategoryTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.FavTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.FeedTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.MyTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;

import java.util.List;

@Dao
public interface TemplateDao {
    @Query("select TemplateEntity.* from AllCategoryTemplateIdsEntity inner join TemplateEntity on AllCategoryTemplateIdsEntity.id=TemplateEntity.id  order by AllCategoryTemplateIdsEntity.createdTime desc")
    LiveData<List<TemplateEntity>> getAllCategoryTemplatesAsLiveData();

    @Query("select TemplateEntity.* from FeedTemplateIdsEntity inner join TemplateEntity on FeedTemplateIdsEntity.id=TemplateEntity.id where FeedTemplateIdsEntity.categoryId=:categoryId order by FeedTemplateIdsEntity.createdTime desc")
    LiveData<List<TemplateEntity>> getFeedTemplatesAsLiveData(String categoryId);

    @Query("select TemplateEntity.* from MyTemplateIdsEntity inner join TemplateEntity on MyTemplateIdsEntity.id=TemplateEntity.id order by MyTemplateIdsEntity.createdTime desc")
    LiveData<List<TemplateEntity>> getMyTemplatesAsLiveData();

    @Query("select TemplateEntity.* from FavTemplateIdsEntity inner join TemplateEntity on FavTemplateIdsEntity.id=TemplateEntity.id order by FavTemplateIdsEntity.createdTime desc")
    LiveData<List<TemplateEntity>> getFavoriteTemplatesAsLiveData();

    @Query("select TemplateEntity.* from FavTemplateIdsEntity inner join TemplateEntity on FavTemplateIdsEntity.id=TemplateEntity.id order by FavTemplateIdsEntity.createdTime desc limit :limit")
    LiveData<List<TemplateEntity>> getFavoriteTemplatesAsLiveData(int limit);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleTemplateData(TemplateEntity templateEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllTemplateData(List<TemplateEntity> templateEntities);

    @Query("update TemplateEntity set isFavorite =:isFavorite where TemplateEntity.id=:id")
    void updateIsFavorite(String id,boolean isFavorite);

    @Query("update TemplateEntity set categoryId =:categoryId,searchTags=:searchTags where TemplateEntity.id=:templateId")
    void updateCatIdAndSearchTags(String templateId,String categoryId,String searchTags);

    @Query("delete from TemplateEntity where TemplateEntity.id=:id")
    void deleteSingleTemplateData(String id);

    @Query("Delete from TemplateEntity")
    void deleteAllTemplateData();




    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleFeedTemplateId(FeedTemplateIdsEntity feedTemplateIdsEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllFeedTemplateIds(List<FeedTemplateIdsEntity> templateIdsEntities);

    @Query("update FeedTemplateIdsEntity set categoryId=:categoryId where FeedTemplateIdsEntity.id=:id")
    void updateSingleFeedCategoryId(String id,String categoryId);

    @Query("delete from FeedTemplateIdsEntity where FeedTemplateIdsEntity.id=:id")
    void deleteSingleFeedTemplateId(String id);

    @Query("Delete from FeedTemplateIdsEntity where FeedTemplateIdsEntity.categoryId=:categoryId")
    void deleteAllFeedTemplatesByCategoryId(String categoryId);

    @Query("Delete from FeedTemplateIdsEntity")
    void deleteAllFeedTemplateIds();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleAllCategoryTemplateIds(AllCategoryTemplateIdsEntity allCategoryTemplateIdsEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllCategoryTemplateIds(List<AllCategoryTemplateIdsEntity> allTemplateIdsEntities);

    @Query("delete from AllCategoryTemplateIdsEntity where AllCategoryTemplateIdsEntity.id=:id")
    void deleteSingleAllCategoryTemplateId(String id);

    @Query("Delete from AllCategoryTemplateIdsEntity")
    void deleteAllCategoryTemplateIds();



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleMyTemplateIds(MyTemplateIdsEntity myTemplateIdsEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllMyTemplateIds(List<MyTemplateIdsEntity> myTemplateIdsEntities);

    @Query("delete from MyTemplateIdsEntity where MyTemplateIdsEntity.id=:id")
    void deleteSingleMyTemplateId(String id);

    @Query("Delete from MyTemplateIdsEntity")
    void deleteAllMyTemplateIds();



    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSingleFavTemplateIds(FavTemplateIdsEntity favTemplateIdsEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAllFavTemplateIds(List<FavTemplateIdsEntity> favTemplateIdsEntities);

    @Query("delete from FavTemplateIdsEntity where FavTemplateIdsEntity.id=:id")
    void deleteSingleFavorite(String id);

    @Query("Delete from FavTemplateIdsEntity")
    void deleteAllFavTemplateIds();

}
