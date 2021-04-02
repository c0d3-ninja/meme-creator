package com.thugdroid.memeking.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
public class MemesDataEntity {
    @Ignore
    public static final String APIKEY_ID="id";

    @Ignore
    public static final String APIKEY_IMGURL="imageUrl";
    @Ignore
    public static final String APIKEY_REGION_ID="regionId";

    @Ignore
    public static final String APIKEY_CREATEDBY="createdBy";
    @Ignore
    public static final String APIKEY_CREATEDTIME="createdTime";
    @Ignore
    public static final String APIKEY_CREATEDTIME_ID="createdTime_id";
    @Ignore
    public static final String APIKEY_FROM="from";
    @Ignore
    public static final String APIKEY_DOWNLOADS="downloads";

    @Ignore
    public static final String APIKEY_SHARES="shares";

    @NonNull
    @PrimaryKey
    public String id;
    public String imageUrl;
    public String regionId;
    public int downloads;
    public int shares;
    public String createdBy;
    public Long createdTime;
    public String instaUsername;

    public MemesDataEntity(@NonNull String id, String imageUrl, String regionId, int downloads, int shares, String createdBy, Long createdTime, String instaUsername) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.regionId = regionId;
        this.downloads = downloads;
        this.shares = shares;
        this.createdBy = createdBy;
        this.createdTime = createdTime;
        this.instaUsername = instaUsername;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getRegionId() {
        return regionId;
    }

    public int getDownloads() {
        return downloads;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public int getShares() {
        return shares;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public String getInstaUsername() {
        return instaUsername;
    }

    public void setInstaUsername(String instaUsername) {
        this.instaUsername = instaUsername;
    }

    public static MemesDataEntity getEntity(Object resultObj){
        HashMap resultMap = (HashMap) resultObj;
        return (new MemesDataEntity(
                (String)resultMap.get(MemesDataEntity.APIKEY_ID),
                (String)resultMap.get(MemesDataEntity.APIKEY_IMGURL),
                (String)resultMap.get(MemesDataEntity.APIKEY_REGION_ID),
                (int)resultMap.get(MemesDataEntity.APIKEY_DOWNLOADS),
                (int)resultMap.get(MemesDataEntity.APIKEY_SHARES),
                (String)resultMap.get(MemesDataEntity.APIKEY_CREATEDBY),
                (Long)resultMap.get(MemesDataEntity.APIKEY_CREATEDTIME),(String)resultMap.get(LoggedInUserEntity.KEY_INSTA_USERNAME)
        ));
    }
    public static List<MemesDataEntity> getEntityList(Object resultObj){
        List resultList = new ArrayList();
        List tempList = (ArrayList)resultObj;
        for (Object object : tempList) {
            resultList.add(getEntity(object));
        }
        return resultList;
    }

    public static MemesDataEntity getEntityForDb(Object resultObj){
        HashMap resultMap = (HashMap) resultObj;
        return (new MemesDataEntity(
                (String)resultMap.get(MemesDataEntity.APIKEY_ID),
                (String)resultMap.get(MemesDataEntity.APIKEY_IMGURL),
                (String)resultMap.get(MemesDataEntity.APIKEY_REGION_ID),
                (int)resultMap.get(MemesDataEntity.APIKEY_DOWNLOADS),
                (int)resultMap.get(MemesDataEntity.APIKEY_SHARES),
                (String)resultMap.get(MemesDataEntity.APIKEY_CREATEDBY),
                (Long)resultMap.get(MemesDataEntity.APIKEY_CREATEDTIME),null
        ));
    }
    public static List<MemesDataEntity> getEntityListForDb(Object resultObj){
        List resultList = new ArrayList();
        List tempList = (ArrayList)resultObj;
        for (Object object : tempList) {
            resultList.add(getEntityForDb(object));
        }
        return resultList;
    }
}
