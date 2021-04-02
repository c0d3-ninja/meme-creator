package com.thugdroid.memeking.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
public class TemplateEntity {
    @Ignore
    public static final String APIKEY_ID="id";
    @Ignore
    public static final String APIKEY_SEARCHTAGS="searchTags";
    @Ignore
    public static final String APIKEY_IMGURL="imageUrl";
    @Ignore
    public static final String APIKEY_REGION_ID="regionId";
    @Ignore
    public static final String APIKEY_CATEGORY_ID="categoryId";
    @Ignore
    public static final String APIKEY_ISFAVORITE="isFavorite";
    @Ignore
    public static final String APIKEY_CREATEDBY="createdBy";
    @Ignore
    public static final String APIKEY_CREATEDTIME="createdTime";
    @Ignore
    public static final String APIKEY_CREATEDTIME_ID="createdTime_id";
    @Ignore
    public static final String APIKEY_FROM="from";

    @Ignore
    public static final String APIKEY_AUTHOR_ID="authorId";




    @NonNull
    @PrimaryKey
    public String id;
    public String searchTags,imageUrl;
    public String categoryId;
    boolean isFavorite;
    public String createdBy;
    public Long createdTime;
    public String regionId;
    public String authorId;

    public TemplateEntity(@NonNull String id, String searchTags, String imageUrl, String categoryId, boolean isFavorite, String createdBy, Long createdTime, String regionId, String authorId) {
        this.id = id;
        this.searchTags = searchTags;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.isFavorite = isFavorite;
        this.createdBy = createdBy;
        this.createdTime = createdTime;
        this.regionId = regionId;
        this.authorId = authorId;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getSearchTags() {
        return searchTags;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public void setSearchTags(String searchTags) {
        this.searchTags = searchTags;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public static TemplateEntity getEntity(Object resultObject){
        HashMap apiMap = (HashMap) resultObject;
        return (new TemplateEntity(
                (String) apiMap.get(APIKEY_ID),
                (String)apiMap.get(APIKEY_SEARCHTAGS),
                (String)apiMap.get(APIKEY_IMGURL),
                (String)apiMap.get(APIKEY_CATEGORY_ID),
                (boolean) apiMap.get(APIKEY_ISFAVORITE),
                (String)apiMap.get(APIKEY_CREATEDBY),
                (Long) apiMap.get(APIKEY_CREATEDTIME),
                (String)apiMap.get(APIKEY_REGION_ID),
                getAuthorId(apiMap)
        ));
    }

    public static String getAuthorId(HashMap apiMap){
        String authorId = null;
        if(apiMap.containsKey(APIKEY_AUTHOR_ID)){
            authorId = (String)apiMap.get(APIKEY_AUTHOR_ID);
        }
        return authorId;
    }

    public static List<TemplateEntity> getEntityList(Object resultObj){
        List apiList=(ArrayList)resultObj;
        List<TemplateEntity> templateEntities=new ArrayList<>();
        for (Object object : apiList) {
            templateEntities.add(getEntity(object));
        }
        return templateEntities;
    }
}
