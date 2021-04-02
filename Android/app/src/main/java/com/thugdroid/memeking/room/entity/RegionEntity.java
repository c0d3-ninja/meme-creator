package com.thugdroid.memeking.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.HashMap;

@Entity
public class RegionEntity {

    @Ignore
    public static final String APIKEY_ID="id";
    @Ignore
    public static final String APIKEY_NAME ="name";
    @Ignore
    public static final String APIKEY_LANGUAGE="language";

    @NonNull
    @PrimaryKey
    private String id;

    private String name;

    private String language;

    public RegionEntity(@NonNull String id, String name, String language) {
        this.id = id;
        this.name = name;
        this.language = language;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
