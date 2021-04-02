package com.thugdroid.libs.collagegrid.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;

import com.thugdroid.libs.collagegrid.model.MyImage;

public class AppUtils {
    public static MyImage getDetailedImage(Context context, Uri uri){
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
    public static int getColor(Context context,int color){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getResources().getColor(color,null);
        }
        else{
            return context.getResources().getColor(color);
        }
    }
}
