package com.thugdroid.libs.collagegrid;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

public abstract class CustomFragment extends Fragment implements CommonFragmentInterface {
    Activity activity;
    View rootView;


    protected Activity getMainActivity() {
        return activity;
    }

    protected void setActivity(Activity activity) {
        this.activity = activity;
    }

    protected View getRootView() {
        return rootView;
    }

    protected void setRootView(View rootView) {
        this.rootView = rootView;
    }

    protected  <T extends View> T findViewById(int id){
        return getRootView().findViewById(id);
    }


    protected void showMsg(int resourceId){
        if(getMainActivity()!=null && isAdded()){
            Toast.makeText(activity,getResources().getString(resourceId),Toast.LENGTH_SHORT).show();
        }
    }

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
}
