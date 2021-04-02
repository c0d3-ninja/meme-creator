package com.thugdroid.memeking.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.HashMap;

@Entity
public class LoggedInUserEntity {

    @Ignore
    public static final String KEY_ID="id";

    @Ignore
    public static final String KEY_EMAIL="email";

    @Ignore
    public static final String KEY_REGION_ID="regionId";

    @Ignore
    public static final String KEY_STATUS="status";

    //used in app prefs entity
    @Ignore
    public static final String KEY_INSTA_USERNAME="instaUsername";

    @Ignore
    public static final String INSTA_USER_NAME_NULL="@null";

    @NonNull
    @PrimaryKey
    public String id;
    public String email;
    public String status;
    public String regionId;



    public LoggedInUserEntity(@NonNull String id, String email, String status, String regionId) {
        this.id = id;
        this.email = email;
        this.status = status;
        this.regionId = regionId;
    }

    public String getRegionId() {
        return regionId;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public static LoggedInUserEntity getEntity(HashMap apiMap){
        return (new LoggedInUserEntity(
                (String) apiMap.get(LoggedInUserEntity.KEY_ID),
                (String) apiMap.get(LoggedInUserEntity.KEY_EMAIL),
                (String) apiMap.get(LoggedInUserEntity.KEY_STATUS),
                (String) apiMap.get(LoggedInUserEntity.KEY_REGION_ID)));
    }

    public static String getInstaUserNameFromResponse(Object resultObject){
        HashMap resultMap = (HashMap) resultObject;
        return ((String)resultMap.get(KEY_INSTA_USERNAME));
    }

    public static String getSanitizedInstaUsername(String username){
        if(isInstaUsernameNull(username)){
            return INSTA_USER_NAME_NULL;
        }
        return username;
    }

    public static boolean isInstaUsernameNull(String username){
        return (username==null || INSTA_USER_NAME_NULL.equals(username));
    }
}
