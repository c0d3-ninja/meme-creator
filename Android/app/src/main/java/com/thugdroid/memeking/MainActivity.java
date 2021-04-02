package com.thugdroid.memeking;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.ViewModelProvider;

import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.viewmodel.WindowViewModel;

public class MainActivity extends AppCompatActivity {
    WindowViewModel windowViewModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        windowViewModel=new ViewModelProvider(this).get(WindowViewModel.class);
    }

    public void goToPlayStore(boolean isFinish){
        goToUrl(AppUtils.getAppUrl(this),isFinish);
    }

    public void goToUrl(String url,boolean isFinish){
        try {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }catch (ActivityNotFoundException e){
            e.printStackTrace();
            Toast.makeText(this, R.string.no_apps_found_to_open_this_url,Toast.LENGTH_SHORT).show();
        }
        if(isFinish){
            finish();
        }

    }

    @Override
    protected void onDestroy() {
        try{
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            for (Integer pendingNotificationId : windowViewModel.getPendingNotificationIds()) {
                notificationManager.cancel(pendingNotificationId);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            super.onDestroy();
        }


    }
}
