package com.thugdroid.memeking.ui.data;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.model.SettingsMenuItemModel;

import java.util.ArrayList;
import java.util.List;

public class SettingsMenuItemData {
    public static final int ID_SET_LOGO=1;
    public static final int ID_CHANGE_REGION=2;
    public static final int ID_SIGNOUT=3;
    public static final int ID_SEND_FEEDBACK=4;
    public static final int ID_CLEAR_CACHE=5;
    public static final int ID_INSTA_USERNAME=6;

    public static List<SettingsMenuItemModel> getMemeMenuItems(){
        int[] idsArr ={ID_SET_LOGO};
        int[] namesArr = {0};
        int[] descArr = {R.string.use_this_logo_as_a_brand_in_your_memes};
        int[] logosArr = {0};
        List<SettingsMenuItemModel> settingsMenuItemModels=new ArrayList<>();
        for (int i = 0; i < idsArr.length; i++) {
            settingsMenuItemModels.add(
                    new SettingsMenuItemModel(
                            idsArr[i],
                            namesArr[i],
                            descArr[i],
                            logosArr[i]
                    )
            );
        }
        return settingsMenuItemModels;
    }

    public static List<SettingsMenuItemModel> getAccountMenuItems(){
        int[] idsArr ={ID_INSTA_USERNAME,ID_CHANGE_REGION,ID_SIGNOUT};
        int[] namesArr = {0,R.string.change_region,R.string.sign_out};
        int[] descArr = {R.string.get_your_credits_on_templates_and_memes,R.string.select_region_subtitle,R.string.sign_out_from_this_device};
        int[] logosArr = {R.drawable.ic_insta,R.drawable.ic_compare_arrows_grey_24dp,R.drawable.ic_exit_to_app_grey_24dp};
        List<SettingsMenuItemModel> settingsMenuItemModels=new ArrayList<>();
        for (int i = 0; i < idsArr.length; i++) {
            settingsMenuItemModels.add(
                    new SettingsMenuItemModel(
                            idsArr[i],
                            namesArr[i],
                            descArr[i],
                            logosArr[i]
                    )
            );
        }
        return settingsMenuItemModels;
    }

    public static List<SettingsMenuItemModel> getClearMenuItems(){
        int[] idsArr ={ID_CLEAR_CACHE};
        int[] namesArr = {R.string.clear_cache};
        int[] descArr = {R.string.clear_cache_description};
        int[] logosArr = {R.drawable.ic_delete_grey2_24dp};
        List<SettingsMenuItemModel> settingsMenuItemModels=new ArrayList<>();
        for (int i = 0; i < idsArr.length; i++) {
            settingsMenuItemModels.add(
                    new SettingsMenuItemModel(
                            idsArr[i],
                            namesArr[i],
                            descArr[i],
                            logosArr[i]
                    )
            );
        }
        return settingsMenuItemModels;
    }

    public static List<SettingsMenuItemModel> getFeedbackMenuItems(){
        int[] idsArr ={ID_SEND_FEEDBACK};
        int[] namesArr = {R.string.send_feedback};
        int[] descArr = {R.string.help_us_to_improve_our_app};
        int[] logosArr = {R.drawable.ic_feedback_grey_24dp};
        List<SettingsMenuItemModel> settingsMenuItemModels=new ArrayList<>();
        for (int i = 0; i < idsArr.length; i++) {
            settingsMenuItemModels.add(
                    new SettingsMenuItemModel(
                            idsArr[i],
                            namesArr[i],
                            descArr[i],
                            logosArr[i]
                    )
            );
        }
        return settingsMenuItemModels;
    }

    public static List<SettingsMenuItemModel> getMenuItems(int titleResId){
        switch (titleResId){
            case R.string.meme:
                return getMemeMenuItems();
            case R.string.account:
                return getAccountMenuItems();
            case R.string.feedback:
                return getFeedbackMenuItems();
            case R.string.clear:
                return getClearMenuItems();
        }
        return new ArrayList<>();
    }
}
