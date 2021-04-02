package com.thugdroid.memeking.ui.data;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.model.SettingsMenuModel;

import java.util.ArrayList;
import java.util.List;

public class SettingsMenuItemsData {
    public static List<SettingsMenuModel> getSettingsMenuModel(){
        List<SettingsMenuModel> settingsMenuModels=new ArrayList<>();
        int[] titles={R.string.meme,R.string.account,R.string.clear,R.string.feedback};
        for (int title : titles) {
                    settingsMenuModels.add(
                            new SettingsMenuModel(
                                    title,SettingsMenuItemData.getMenuItems(title)
                            ));

            }
        return settingsMenuModels;
    }
}
