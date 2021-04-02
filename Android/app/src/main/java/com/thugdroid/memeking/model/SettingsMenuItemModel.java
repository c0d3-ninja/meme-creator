package com.thugdroid.memeking.model;

public class SettingsMenuItemModel {
    private int id;
    private int titleResId;
    private int descResId;
    private int logoResId;

    public SettingsMenuItemModel(int id, int titleResId, int descResId, int logoResId) {
        this.id = id;
        this.titleResId = titleResId;
        this.descResId = descResId;
        this.logoResId = logoResId;
    }

    public int getId() {
        return id;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public int getDescResId() {
        return descResId;
    }

    public int getLogoResId() {
        return logoResId;
    }
}
