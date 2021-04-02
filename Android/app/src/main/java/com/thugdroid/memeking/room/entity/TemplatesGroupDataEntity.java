package com.thugdroid.memeking.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
public class TemplatesGroupDataEntity {

    @Ignore
    public static final String APIKEY_ID="id";
    @Ignore
    public static final String APIKEY_NAME ="name";

    @Ignore
    public static final String APIKEY_SEARCH_STR="searchStr";

    @Ignore
    public static final String APIKEY_IMAGEURL="imageUrl";

    @Ignore
    public static final String APIKEY_REGION_ID="regionId";

    @Ignore
    public static final String APIKEY_CREATED_TIME="createdTime";


    @NonNull
    @PrimaryKey
    private String  id;
    private String name,imageUrl,searchStr,regionId;
    private Long createdTime;

    public TemplatesGroupDataEntity(@NonNull String id, String name, String imageUrl, String searchStr, String regionId, Long createdTime) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.searchStr = searchStr;
        this.regionId = regionId;
        this.createdTime = createdTime;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getSearchStr() {
        return searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public static TemplatesGroupDataEntity getEntity(Object resultObject){
        HashMap apiMap = (HashMap) resultObject;
        return (new TemplatesGroupDataEntity(
                (String) apiMap.get(APIKEY_ID),
                (String)apiMap.get(APIKEY_NAME),
                (String)apiMap.get(APIKEY_IMAGEURL),
                (String)apiMap.get(APIKEY_SEARCH_STR),
                (String)apiMap.get(APIKEY_REGION_ID),
                (Long)apiMap.get(APIKEY_CREATED_TIME)

        ));
    }

    public static List<TemplatesGroupDataEntity> getEntityList(Object resultObj){
        List apiList=(ArrayList)resultObj;
        List<TemplatesGroupDataEntity> templateGroupDataEntities=new ArrayList<>();
        for (Object object : apiList) {
            templateGroupDataEntities.add(getEntity(object));
        }
        return templateGroupDataEntities;
    }
}
