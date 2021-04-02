package com.thugdroid.memeking.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class MyMemesIdsEntity {
    @NonNull
    @PrimaryKey
    String id;
    Long createdTime;

    public MyMemesIdsEntity(@NonNull String id, Long createdTime) {
        this.id = id;
        this.createdTime=createdTime;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public Long getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Long createdTime) {
        this.createdTime = createdTime;
    }
}
