package com.thugdroid.memeking.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.thugdroid.memeking.room.entity.MemesDataEntity;
import com.thugdroid.memeking.room.entity.MemesFeedIdsEntity;
import com.thugdroid.memeking.room.entity.MyMemesIdsEntity;

import java.util.List;

@Dao
public interface MemesDao {

    @Query("select MemesDataEntity.*,SocialUsernameEntity.instaUsername as instaUsername from MyMemesIdsEntity inner join MemesDataEntity on " +
            " MyMemesIdsEntity.id=MemesDataEntity.id left join SocialUsernameEntity on MemesDataEntity.createdBy=SocialUsernameEntity.userId" +
            " order by MyMemesIdsEntity.createdTime desc")
    LiveData<List<MemesDataEntity>> getMyMemesAsLiveData();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMemesDataEntity(List<MemesDataEntity> memesDataEntities);

    @Query("update MemesDataEntity set downloads =:count where MemesDataEntity.id=:id")
    void updateDownloadsCount(String id,int count);

    @Query("update MemesDataEntity set shares =:count where MemesDataEntity.id=:id")
    void updateSharesCount(String id,int count);

    @Query("Delete from MemesDataEntity where MemesDataEntity.id=:id")
    void deleteMemesDataById(String id);

    @Query("Delete from MemesDataEntity")
    void deleteAllMemesData();

    @Query("update MemesDataEntity set instaUsername =:instaUsername where MemesDataEntity.createdBy=:userId")
    void updateMyInstaUsername(String userId,String instaUsername);


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMemesFeedIdsEntity(List<MemesFeedIdsEntity> memesFeedIdsEntities);

    @Query("Delete from MemesFeedIdsEntity where MemesFeedIdsEntity.id=:id")
    void deleteMemesFeedId(String id);

    @Query("Delete from MemesFeedIdsEntity")
    void deleteAllMemesFeedIds();


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMyMemesIdsEntity(List<MyMemesIdsEntity> myMemesIdsEntity);


    @Query("Delete from MyMemesIdsEntity where MyMemesIdsEntity.id=:id")
    void deleteMyMemesId(String id);

    @Query("Delete from MyMemesIdsEntity")
    void deleteAllMyMemesIds();




}
