package com.thugdroid.memeking;

import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.thugdroid.memeking.constants.Constants;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Date;

public abstract class MediaFragment extends CustomFragment {
    protected   boolean isPermissionGranted(@NonNull String permission, @NonNull int requestCode){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermission(permission,requestCode);
                return  false;
            }
            else{
                return  true;
            }
        }
        else{
            return true;
        }

    }
    protected void requestPermission(@NonNull String permission,@NonNull int requestCode){
        requestPermissions(new String[]{permission},requestCode);
    }

    protected void openGallery(int requestCode){
        try{
            Intent _pickIntent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(_pickIntent,requestCode);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
            showMsg(R.string.no_apps_found_to_choose_image);
        }

    }

    public class SaveImage extends AsyncTask {
        String folderName;
        Bitmap bitmap;
        SaveImageListener saveImageListener;
        boolean isCache;
        boolean saved=false;
        String uriString;
        public SaveImage(String folderName, Bitmap bitmap,boolean isCache, SaveImageListener saveImageListener) {
            this.folderName = folderName;
            this.bitmap = bitmap;
            this.saveImageListener = saveImageListener;
            this.isCache=isCache;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                OutputStream fos;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && !isCache) {
                    ContentResolver resolver = getContext().getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, new Date().getTime()+"");
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_PICTURES+ File.separator+folderName );
                    Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    fos = resolver.openOutputStream(imageUri);
                    uriString=imageUri.getPath();
                } else {
                    String imagesDir;
                    if(isCache){
                        imagesDir= getContext().getCacheDir() + File.separator + folderName;
                    }else{
                    imagesDir=Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_PICTURES).toString() + File.separator + folderName;
                    }
                    File file = new File(imagesDir);
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File image = new File(imagesDir, new Date().getTime()+ ".jpg");

                    fos = new FileOutputStream(image);
                    if(isCache){
                        uriString= FileProvider.getUriForFile(getContext(),getString(R.string.fileprovider_authority),new File(image.getAbsolutePath())).toString();
                    }else{
                        uriString=image.getAbsolutePath();
                    }

                }
                saved = bitmap.compress(Bitmap.CompressFormat.JPEG, Constants.IMAGE_QUALITY, fos);
                fos.flush();
                fos.close();
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            if(saved){
                saveImageListener.onSuccess(uriString);
            }else {
                saveImageListener.onFailure();
            }
        }
    }

    public interface SaveImageListener{
        void onSuccess(String uriString);
        void onFailure();
    }
}
