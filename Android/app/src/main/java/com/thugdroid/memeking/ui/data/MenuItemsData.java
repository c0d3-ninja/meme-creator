package com.thugdroid.memeking.ui.data;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.model.MenuModel;

import java.util.ArrayList;
import java.util.List;

public class MenuItemsData {
    public static final int ID_UPLOAD_TEMPLATES=1;
    public static final int ID_MY_TEMPLATES=2;
    public static final int ID_FAV_TEMPLATES=3;
    public static final int ID_SHARE_APP=4;
    public static final int ID_RATE_APP=5;
    public static final int ID_SETTINGS=6;
    public static final int ID_GALLERY=7;
    public static final int ID_MEMES=8;
    public static final int ID_MY_MEMES=9;
    public static final int ID_POST_MEME=10;
    public static final int ID_TEMPLATE_GROUPS=11;

    public static  List<MenuModel> getTemplateItems(String regionId){
        List<MenuModel> menuModels = new ArrayList<>();
        int[] idsArr ={ID_UPLOAD_TEMPLATES,ID_MY_TEMPLATES,ID_FAV_TEMPLATES};
        int[] namesArr = {R.string.upload_template,R.string.my_templates,R.string.favourites};
        int[] logosArr = {R.drawable.ic_file_upload_grey_24dp,R.drawable.ic_mytemplates_grey_24dp,R.drawable.ic_star_grey_24dp};

        for (int i = 0; i < idsArr.length; i++) {
            menuModels.add(new MenuModel(idsArr[i],
                    namesArr[i],logosArr[i]));
        }
        if(regionId!=null && "TN".equals(regionId)){
            menuModels.add(new MenuModel(ID_TEMPLATE_GROUPS,R.string.movies,R.drawable.ic_local_movies_grey_24dp));
        }
        return menuModels;
    }

    public static  List<MenuModel> getMemeItems(){
        List<MenuModel> menuModels = new ArrayList<>();
        int[] idsArr ={ID_POST_MEME,ID_MEMES,ID_MY_MEMES};
        int[] namesArr = {R.string.post_meme,R.string.recent_10_memes,R.string.my_memes};
        int[] logosArr = {R.drawable.ic_send_grey_24dp,R.drawable.ic_feed_grey_24dp,R.drawable.ic_mytemplates_grey_24dp};
        for (int i = 0; i < idsArr.length; i++) {
            menuModels.add(new MenuModel(idsArr[i],
                    namesArr[i],logosArr[i]));
        }
        return menuModels;
    }

    public static  List<MenuModel> getMainBottomItems(){
        List<MenuModel> menuModels = new ArrayList<>();
        int[] idsArr ={ID_SHARE_APP,ID_RATE_APP,ID_SETTINGS};
        int[] namesArr = {R.string.share_app,R.string.rate_app,R.string.settings};
        int[] logosArr = {R.drawable.ic_share_grey_24dp,R.drawable.ic_star_yellow_24dp,R.drawable.ic_settings_grey_24dp};
        for (int i = 0; i < idsArr.length; i++) {
            menuModels.add(new MenuModel(idsArr[i],
                    namesArr[i],logosArr[i]));
        }
        return menuModels;
    }

    public static  List<MenuModel> getCreateMemeImagePickItems(){
        List<MenuModel> menuModels = new ArrayList<>();
        int[] idsArr ={ID_GALLERY,ID_MY_TEMPLATES,ID_FAV_TEMPLATES};
        int[] namesArr = {R.string.gallery,R.string.my_templates,R.string.favourites};
        int[] logosArr = {R.drawable.ic_image_grey_24dp,R.drawable.ic_mytemplates_grey_24dp,R.drawable.ic_star_grey_24dp};
        for (int i = 0; i < idsArr.length; i++) {
            menuModels.add(new MenuModel(idsArr[i],
                    namesArr[i],logosArr[i]));
        }
        return menuModels;
    }
}
