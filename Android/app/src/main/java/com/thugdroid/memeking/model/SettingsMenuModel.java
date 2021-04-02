package com.thugdroid.memeking.model;

import java.util.List;

public class SettingsMenuModel {
    private int titleResId;
    private List<SettingsMenuItemModel> settingsMenuItemModels;

    public SettingsMenuModel(int titleResId, List<SettingsMenuItemModel> settingsMenuItemModels) {
        this.titleResId = titleResId;
        this.settingsMenuItemModels = settingsMenuItemModels;
    }

    public int getTitleResId() {
        return titleResId;
    }

    public List<SettingsMenuItemModel> getSettingsMenuItemModels() {
        return settingsMenuItemModels;
    }
}
