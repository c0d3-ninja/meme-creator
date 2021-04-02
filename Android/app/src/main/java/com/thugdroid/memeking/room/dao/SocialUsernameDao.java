package com.thugdroid.memeking.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.thugdroid.memeking.room.entity.SocialUsernameEntity;

import java.util.List;

@Dao
public interface SocialUsernameDao {

    @Query("select SocialUsernameEntity.instaUsername from SocialUsernameEntity where SocialUsernameEntity.userId = :userId")
    LiveData<String> get(String userId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(List<SocialUsernameEntity> socialUsernameEntityList);

    @Query("Delete from SocialUsernameEntity")
    void deleteAll();
}
