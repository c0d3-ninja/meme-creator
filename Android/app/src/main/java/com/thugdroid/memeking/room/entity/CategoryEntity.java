package com.thugdroid.memeking.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Entity
public class CategoryEntity {
    @Ignore
    public static final String APIKEY_ID="id";
    @Ignore
    public static final String APIKEY_NAME="name";
    @Ignore
    public static final String APIKEY_IMAGEURL ="imageUrl";
    @Ignore
    public static final String APIKEY_CAN_UPLOAD_TEMPLATE="canUploadTemplate";
    @Ignore
    public static final String APIKEY_REGIONID="regionId";

    @Ignore
    public static final String DROPDOWN_NONE="NONE";

    @NonNull
    @PrimaryKey
    public String id;
    public String name;
    public String imageUrl;
    public String regionId;
    public boolean canUploadTemplate;

    public CategoryEntity(@NonNull String id, String name, String imageUrl,String regionId,boolean canUploadTemplate) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.canUploadTemplate=canUploadTemplate;
        this.regionId=regionId;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public boolean isCanUploadTemplate() {
        return canUploadTemplate;
    }

    public String getRegionId() {
        return regionId;
    }

    public static List<CategoryEntity> getEntityList(Object resultObj){
        List<HashMap> resultList = (ArrayList<HashMap>) resultObj;
        List<CategoryEntity> categoryEntities=new ArrayList<>();
        for (HashMap categoryMap : resultList) {
            categoryEntities.add(CategoryEntity.getEntity(categoryMap));
        }
        return categoryEntities;
    }
    public static CategoryEntity getEntity(HashMap apiData){
        return  (new CategoryEntity((String) apiData.get(CategoryEntity.APIKEY_ID),
                (String) apiData.get(CategoryEntity.APIKEY_NAME),
                (String) apiData.get(CategoryEntity.APIKEY_IMAGEURL),
                (String) apiData.get(CategoryEntity.APIKEY_REGIONID),
                (boolean)apiData.get(CategoryEntity.APIKEY_CAN_UPLOAD_TEMPLATE)));
    }
}
