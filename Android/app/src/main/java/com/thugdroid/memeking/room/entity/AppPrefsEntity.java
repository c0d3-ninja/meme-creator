package com.thugdroid.memeking.room.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class AppPrefsEntity {

    @Ignore
    public static final String SELECTED_CATEGORY="SELECTED_CATEGORY";

    @Ignore
    public static final String SELECTED_NAVDRAWER_MENU="SELECTED_NAVDRAWER_MENU";

    @Ignore
    public static final String LOGO_PATH="LOGO_PATH";

    @Ignore
    public static final String WALKTHROUGH_CATEGORY_CLICK="WALKTHROUGH_CATEGORY_CLICK_V3";

    @Ignore
    public static final String WALKTHROUGH_SINGLE_GRID="WALKTHROUGH_SINGLE_GRID_V2";

    @Ignore
    public static final String WALKTHROUGH_MULTIPLE_GRID="WALKTHROUGH_MULTIPLE_GRID";

    @Ignore
    public static final String CATEGORY_SILENTLY_CALLED_TIME="CATEGORY_SILENTLY_CALLED_TIME";

    @Ignore
    public static final String SAVE_MEME_REWARDED_AD_LAST_SHOWTIME="SAVE_MEME_REWARDED_AD_LAST_SHOWTIME";

    @Ignore
    public static final String LAST_UPDATE_POPUP_SHOWN_TIME="LAST_UPDATE_POPUP_SHOWN_TIME";

    @Ignore
    public static final String REVIEWED_TYPE_AND_TIME="REVIEWED_TYPE_AND_TIME";

    @Ignore
    public static final String TEMPLATES_GROUP_SCROLL_EMPTY_TIME="TEMPLATES_GROUP_SCROLL_EMPTY_TIME";

    @Ignore
    public static  final String INSTA_USERNAME="INSTA_USERNAME";

    @Ignore
    public static final String IS_ADMIN_USER="IS_ADMIN_USER";

    @NonNull
    @PrimaryKey
    public String id;
    public String value;

    public AppPrefsEntity(@NonNull String id, String value) {
        this.id = id;
        this.value = value;
    }
}
