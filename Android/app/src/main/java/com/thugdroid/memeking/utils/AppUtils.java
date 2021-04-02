package com.thugdroid.memeking.utils;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.Html;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.model.MyImage;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;

import static android.view.inputmethod.InputMethodManager.HIDE_IMPLICIT_ONLY;


public class AppUtils {

    public static final String ASSET_STICKERS="stickers";

    public static Bitmap takeScreenShot(Activity activity){
        View view=activity.getWindow().getDecorView();
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }
    public static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }



    public static Bitmap blurBitmap(Context context, Bitmap inBitmap,float scaleFactor){
        inBitmap=Bitmap.createScaledBitmap(inBitmap,Math.round(inBitmap.getWidth()*scaleFactor),Math.round(inBitmap.getHeight()*scaleFactor),false);
        Bitmap outBitmap=Bitmap.createBitmap(inBitmap.getWidth(),inBitmap.getHeight(),Bitmap.Config.ARGB_8888);
        RenderScript renderScript=RenderScript.create(context);
        ScriptIntrinsicBlur blurScript=ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        Allocation allocationIn=Allocation.createFromBitmap(renderScript,inBitmap);
        Allocation allocationOut=Allocation.createFromBitmap(renderScript,outBitmap);
        blurScript.setRadius(5f);
        blurScript.setInput(allocationIn);
        blurScript.forEach(allocationOut);
        allocationOut.copyTo(outBitmap);
        inBitmap.recycle();
        renderScript.destroy();
        return outBitmap;
    }

    public static MyImage getDetailedImage(Context context,Uri uri){
        MyImage myImage=new MyImage();
        myImage.setUri(uri);
        String mimeType = context.getContentResolver().getType(uri);
        if(mimeType!=null){
            myImage.setMimeType(mimeType);
            String[] mimeTypeArr=mimeType.split("/");
            if(mimeTypeArr.length>1){
                myImage.setExtension(mimeTypeArr[1]);
            }
        }
        Cursor cursor=context.getContentResolver().query(uri,null,null,null,null);
        int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
        cursor.moveToFirst();
        String filePath=cursor.getString(nameIndex);
        Long size=cursor.getLong(sizeIndex);
        myImage.setFilename(filePath);
        myImage.setSize(size);
        return  myImage;
    }

    public static String getAssetPath(String id){
        return "file:///android_asset/"+id;
    }

    public static boolean hasInternetConnection(Context context){
        try{
            ConnectivityManager cm =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork != null &&
                    activeNetwork.isConnectedOrConnecting();
            return isConnected;
        }catch (Exception e){
            return false;
        }
    }


    public static int getIndexOf(String[] stringArr,String value){
        for (int i = 0; i < stringArr.length; i++) {
            if(stringArr[i].equals(value)){
                return i;
            }
        }
        return -1;
    }

    public static String getShortenName(String name,int length){
        length=length>=2?2:length;
        String newName="";
        if(name.length()<length){
            return name;
        }
        switch (length){
            case 1:
                return  name.substring(0,1);
            case 2:
                String[] nameArr=name.split(" ");
                if(nameArr.length>1 && nameArr[1].length()>0){
                    return nameArr[0].substring(0,1)+nameArr[1].substring(0,1);
                }
                return nameArr[0].substring(0,2);
        }

        return newName;
    }

    public static void setHTML(TextView textView, String html){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            textView.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
        } else {
            textView.setText(Html.fromHtml(html));
        }
    }

    public static int getColor(Context context,int color){
        return ContextCompat.getColor(context,color);
    }

    public static int getColorForPosition(Context context,int position){
        int color;
        switch (position%4){
            case 1:
                color=R.color.roundedRectangle2;
                break;
            case 2:
                color=R.color.roundedRectangle3;
                break;
            case 3:
                color=R.color.roundedRectangle4;
                break;
            case 4:
                color=R.color.roundedRectangle5;
                break;
            default:
                color=R.color.roundedRectangle1;
                break;
        }
        return getColor(context,color);
    }

    public static String getColorString(Context context,int color){
        return context.getResources().getString(color);
    }

    private static void showKeyboard(Context context,View view){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
    }
    public static void hideKeyboard(Context context,View view){
//        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
//        //Find the currently focused view, so we can grab the correct window token from it.
//        View view = activity.getCurrentFocus();
//        //If no view currently has focus, create a new one, just so we can grab a window token from it
//        if (view == null) {
//            view = new View(activity);
//        }
//        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
    public static void focusEditText(Context context, EditText editText){
        if(editText.requestFocus()){
            showKeyboard(context,editText);
        }
    }

    public static void focusEditTextInDialog(Dialog dialog,EditText editText){
        //dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE| WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        if(editText.requestFocus()){
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    public static double bytesToMb(Long bytes){
        return (bytes/(1000*1000));
    }

    public static double bytesToKb(Long bytes){
        return (bytes/1000);
    }

    public static String getAppVersionName(Context context){
        try {
            PackageInfo packageInfo=context.getPackageManager().getPackageInfo(context.getPackageName(),0);
            return  packageInfo.versionName;
        } catch (Exception e) {
            e.printStackTrace();
            return "beta";
        }
    }


    public static long getAppVersionCode(Context context){
        try {
            PackageInfo packageInfo=context.getPackageManager().getPackageInfo(context.getPackageName(),0);
            if(Build.VERSION.SDK_INT<Build.VERSION_CODES.P){
                return packageInfo.versionCode;
            }else {
                return  packageInfo.getLongVersionCode();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    public static boolean isBlockedUser(LoggedInUserEntity loggedInUserEntity){
        if(loggedInUserEntity==null){
            return false;
        }
        return Constants.USER_STATUS_BLOCKED.equals(loggedInUserEntity.getStatus());
    }


    public static int getDrawableFrom(int position){
        switch (position%4){
            case 1:
                return R.drawable.rounded_rectangle_2;
            case 2:
                return R.drawable.rounded_rectangle_3;
            case 3:
                return R.drawable.rounded_rectangle_4;
            case 4:
                return R.drawable.rounded_rectangle_5;
            default:
                return R.drawable.rounded_rectangle_1;

        }
    }
    public static boolean isAllTypeCategory(String categoryId,String regionId){
        if(categoryId==null||regionId==null){
            return false;
        }
        return (Constants.PREFIX_ALL+regionId).equals(categoryId);
    }

    public static String getAppUrl(Activity activity){
        return "https://play.google.com/store/apps/details?id="+activity.getPackageName();
    }

    public static void shareApp(Activity activity,String shareAppContent) {
        if(shareAppContent==null){
            shareAppContent="";
        }
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.app_name));
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareAppContent+"\n\n"+getAppUrl(activity));
            activity.startActivity(Intent.createChooser(shareIntent, activity.getString(R.string.share)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean isMemesEnabled(String regionId){
//        for (int i = 0; i < Constants.MEMES_ENABLED_REGIONS.length; i++) {
//            String str = Constants.MEMES_ENABLED_REGIONS[i];
//            if(str.equals(regionId)){
//                return true;
//            }
//        }
        return true;
    }

    public static String getInstaDisplayUsername(String username){
        if(username==null){
            return "";
        }
        return "@"+username;
    }
    public static String getInstaProfileUrl(String username){
        return "https://instagram.com/"+username;
    }
}
