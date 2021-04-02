package com.thugdroid.memeking.model;

import android.view.View;

public class NoTemplatesFoundConfigurations {
    private String title;
    private boolean hasActionBtn;
    private View.OnClickListener uploadBtnClickListener;
    private String regionId;

    public NoTemplatesFoundConfigurations(String title, boolean hasActionBtn, View.OnClickListener uploadBtnClickListener) {
        this.title = title;
        this.hasActionBtn = hasActionBtn;
        this.uploadBtnClickListener = uploadBtnClickListener;
    }

    public String getTitle() {
        return title;
    }

    public boolean isHasActionBtn() {
        return hasActionBtn;
    }

    public View.OnClickListener getUploadBtnClickListener() {
        return uploadBtnClickListener;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }
}
