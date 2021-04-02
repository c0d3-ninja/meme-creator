package com.thugdroid.memeking.model;

public class MenuModel {
    private int id;
    private int titleResId;
    private int logoResId;

    public MenuModel(int id, int titleResId, int logoResId) {
        this.id = id;
        this.titleResId = titleResId;
        this.logoResId = logoResId;
    }

    public int getId() {
        return id;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public int getLogoResId() {
        return logoResId;
    }
}
