package com.thugdroid.memeking.utils;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.ViewGroup;

public class DialogUtils {
    public static void setFullWidth(Dialog dialog){
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    public static void setBlurredBg(Dialog dialog, Activity activity){
        Bitmap windowBitmap= AppUtils.takeScreenShot(activity);
        Bitmap blurBitmap=AppUtils.blurBitmap(activity,windowBitmap,0.1f);
        dialog.getWindow().setBackgroundDrawable(new BitmapDrawable(activity.getResources(),blurBitmap));
    }
}
