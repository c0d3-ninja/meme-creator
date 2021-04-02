package com.thugdroid.memeking.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class SocialUsernameEntity {
    @Ignore
    public static final String KEY_INSTA_USERNAME="instaUsername";

    @PrimaryKey
    @NonNull
    public String userId;
    public String instaUsername;

    public SocialUsernameEntity(@NonNull String userId, String instaUsername) {
        this.userId = userId;
        this.instaUsername = LoggedInUserEntity.getSanitizedInstaUsername(instaUsername);
    }

    @NonNull
    public String getUserId() {
        return userId;
    }

    public void setUserId(@NonNull String userId) {
        this.userId = userId;
    }

    public String getInstaUsername() {
        return LoggedInUserEntity.getSanitizedInstaUsername(instaUsername);
    }

    public void setInstaUsername(String instaUsername) {
        this.instaUsername = instaUsername;
    }
    
}
