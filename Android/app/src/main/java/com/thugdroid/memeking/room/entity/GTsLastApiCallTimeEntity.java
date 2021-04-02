package com.thugdroid.memeking.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class GTsLastApiCallTimeEntity {
    @NonNull
    @PrimaryKey
    private String id;
    private Long time;

    public GTsLastApiCallTimeEntity(@NonNull String id, Long time) {
        this.id = id;
        this.time = time;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }
}
