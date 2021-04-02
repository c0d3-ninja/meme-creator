package com.thugdroid.memeking.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FeedTemplateIdsEntity {
    @NonNull
    @PrimaryKey
    String id;
    Long createdTime;
    String categoryId;

    public FeedTemplateIdsEntity(@NonNull String id,Long createdTime,String categoryId) {
        this.id = id;
        this.categoryId = categoryId;
        this.createdTime=createdTime;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }
}
