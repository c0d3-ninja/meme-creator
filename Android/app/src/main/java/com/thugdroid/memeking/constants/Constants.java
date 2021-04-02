package com.thugdroid.memeking.constants;

public class Constants {
    public static final String MIMETYPE_JPG="image/jpg";
    public static final String MIMETYPE_JPEG="image/jpeg";
    public static final String MIMETYPE_PNG = "image/png";
    public static final String TEMPLATES_FIRESTOREPATH="templates";
    public static final String MEMES_FIRESTOREPATH="memes";
    public static final int TEMPLATES_API_LIMIT=20;
    public static final int MEMES_API_LIMIT=10;
    public static final int TEMPLATES_GROUP_API_LIMIT=30;
    public static final int GROUP_TEMPLATES_API_LIMIT=30;
    public static final String MY_TEMPLATES_MENUID="MYTEMPLATES_MENU";
    public static final String MEMES_FEED_MENUID="MEMES_FEED_MENU";
    public static final String MY_MEMES_MENUID="MY_MEMES_MENU";
    public static final String FAVORITES_MENUID="FAVORITES_MENU";
    public static final String TEMPLATES_GROUP_MENU_ID="TEMPLATES_GROUP_MENU";
    public static final String DEFAULT_FONTNAME="Aainnfont.ttf";
    public static final String FONT_ASSET_PATH="fonts";
    public static final String STICKER_ASSET_PATH="stickers";
    public static final String ASSET_PATH="file:///android_asset";
    public static final String TEMPLATES_FOLDERNAME="Meme Templates";
    public static final String MEMES_FOLDERNAME="Memes";
    public static final String SHARE_MEME_FOLDER_NAME="Share";
    public static final String CROP_CACHE_FOLDER_NAME="Crop";
    public static final String TEMP_IMAGES_FOLDER_NAME="TempImages";
    public static final int IMAGE_QUALITY=80;
    public static final long UPLOAD_IMAGE_MAX_SIZE_IN_KB =300;
    public static final long UPLOAD_TEMPLATE_MAX_SIZE_IN_MB=5;
    public static final long UPLOAD_TEMPLATE_MAX_SIZE_IN_KB=300;

    public static final String REPORT_SPAM="SPAM";
    public static final String  REPORT_INAPPROPRIATE="INAPPROPRIATE";



    public static final int API_TYPE_MY_TEMPLATES =0;
    public static final int API_TYPE_FAV_TEMPLATES =1;
    public static final int API_TYPE_SEARCH_TEMPLATES =2;
    public static final int API_TYPE_TEMPLATES_FEED =3;
    public static final int API_TYPE_MEMES_FEED=4;
    public static final int API_TYPE_MY_MEMES=5;
    public static final int API_TYPE_TEMPLATES_GROUP=6;

    public static final long SEARCH_TIMEOUT=1000;
    public static final long SCROLL_TOOLS_TIMEOUT=1000;
    public static final int SCROLL_MAX_POSITION=5000;
    public static final int SEARCH_INPUT_MIN=1;
    public static final int CATEGORY_SILENT_CALL_THRESHOLD_HOURS=170;
    public static final int UPDATE_POPUP_THRESHOLD_IN_DAYS=3;
    public static final int GROUP_TEMPLATES_SILENT_CALL_THRESHOLD_IN_HOURS=170;
    public static final int TEMPLATES_GROUP_SCROLL_CALL_THRESHOLD_IN_DAYS=3;

    public static final String UPDATE_OPERATION="UPDATE_OPERATION";
    public static final String DELETE_OPERATION="DELETE_OPERATION";

    public static final String[] IMAGE_ACCEPTABLE_MIME_TYPES ={Constants.MIMETYPE_PNG,Constants.MIMETYPE_JPG,Constants.MIMETYPE_JPEG};

    public static final String MODE_SELECT_REGION="MODE_SELECT_REGION";
    public static final String MODE_CHANGE_REGION="MODE_CHANGE_REGION";

    public static final String USER_STATUS_BLOCKED="BLOCKED";

    public static final String REVIEW_TYPE_REVIEWED="REVIEWED";
    public static final String REVIEW_TYPE_LATER="LATER";
    public static final int REVIEW_THRESHOLD_IN_DAYS=7;

    public static final String REGION_TN_ID="TN";

    public static final String POST_NOTIFICATION_CHANNEL ="post_image";
    /*used for all templates*/
    public static final String PREFIX_ALL="ALL_";

    public static final String[] TEMPLATES_GROUP_ENABLED_REGIONS={"TN"};
    public static final String[] MEMES_ENABLED_REGIONS={"TN","EN"};

    public  enum ApiGetType {
        SMART_REFRESH, HARD_REFRESH, SCROLL_GET
    };


}
