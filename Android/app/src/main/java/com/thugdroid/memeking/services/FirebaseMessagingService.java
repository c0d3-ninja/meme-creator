package com.thugdroid.memeking.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.RemoteMessage;
import com.thugdroid.memeking.BuildConfig;
import com.thugdroid.memeking.MainActivity;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if(remoteMessage.getData().size()>0){
            Map<String,String> remoteData = remoteMessage.getData();
            if(remoteData!=null){
                String title = remoteData.get(FireConstants.FCM_API_KEY_TITLE);
                String description = remoteData.get(FireConstants.FCM_API_KEY_DESC);
                String imageUrl = remoteData.get(FireConstants.FCM_API_KEY_IMAGE_URL);
                String channelId = remoteData.get(FireConstants.FCM_API_KEY_CHANNEL_ID);
                String linkUrl = remoteData.get(FireConstants.FCM_LINK);
                String categoryId = remoteData.get(FireConstants.FCM_CATEGORY_ID);
                String versionStr=remoteData.get(FireConstants.FCM_APP_VERSION_CODE);
                if(versionStr!=null){
                    int apiVersionCode = Integer.parseInt(versionStr);
                    String operator = remoteData.get(FireConstants.FCM_VERSION_OPERATOR);
                    if(operator!=null){
                        int currentAppVersionCode= BuildConfig.VERSION_CODE;
                        if(FireConstants.FCM_VERSION_OPERATOR_EQUAL.equals(operator)
                         && !(currentAppVersionCode==apiVersionCode)){
                            return;
                        }
                        else if(FireConstants.FCM_VERSION_OPERATOR_GREATOR_THAN_OR_EQUAL.equals(operator)
                                && !(currentAppVersionCode>=apiVersionCode)){
                            return;
                        }
                        else if(FireConstants.FCM_VERSION_OPERATOR_LESS_THAN_OR_EQUAL.equals(operator)
                                && !(currentAppVersionCode<=apiVersionCode)){
                            return;
                        }
                    }
                }

                NotificationCompat.Builder notificationBuilder =
                        new NotificationCompat.Builder(this, channelId)
                                .setSmallIcon(R.drawable.ic_notification)
                                .setContentTitle(title!=null?title:"")
                                .setStyle(new NotificationCompat.BigTextStyle().bigText(description!=null?description:""))
                                .setContentText(description!=null?description:"")
                                .setAutoCancel(true)
                                .setPriority(NotificationCompat.PRIORITY_MAX);

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                // Since android Oreo notification channel is needed.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    String notificationChannelName = getString(R.string.fcm_channel_templates_title);
                    String notificationDesc = getString(R.string.fcm_channel_templates_desc);
                    if(channelId!=null){
                        switch (channelId){
                            case FireConstants.FCM_MEMES_CHANNEL_ID:
                                notificationChannelName=getString(R.string.fcm_channel_memes_title);
                                notificationDesc = getString(R.string.fcm_channel_memes_desc);
                                break;
                            case FireConstants.FCM_APP_UPDATES_CHANNEL_ID:
                                notificationChannelName=getString(R.string.fcm_channel_appupdates_title);
                                notificationDesc=getString(R.string.fcm_channel_appupdates_desc);
                                break;
                            case FireConstants.FCM_VERIFICATION_CHANNEL_ID:
                                notificationChannelName=getString(R.string.fcm_channel_verification_title);
                                notificationDesc=getString(R.string.fcm_channel_verification_desc);
                                break;

                        }
                    }
                    NotificationChannel channel = new NotificationChannel(channelId,
                            notificationChannelName,
                            NotificationManager.IMPORTANCE_HIGH);
                    channel.enableLights(true);
                    channel.setDescription(notificationDesc);
                    notificationManager.createNotificationChannel(channel);
                }

                Intent intent = new Intent(this, MainActivity.class);
                if(linkUrl!=null){
                    intent.putExtra(FireConstants.FCM_LINK,linkUrl);
                }
                if(categoryId!=null){
                    intent.putExtra(FireConstants.FCM_CATEGORY_ID,categoryId);
                }
                intent.putExtra(FireConstants.FCM_API_KEY_CHANNEL_ID,channelId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                        PendingIntent.FLAG_ONE_SHOT);
                if(FireConstants.FCM_VERIFICATION_CHANNEL_ID.equals(channelId)){
                    if(imageUrl!=null){
                        Glide.with(this).asBitmap().load(imageUrl).override(100,100).into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                notificationBuilder.setLargeIcon(resource);
                                notificationBuilder.setContentIntent(pendingIntent);
                                notificationManager.notify(((int)new Date().getTime())/* ID of notification */, notificationBuilder.build());
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
                    }
                }else if(imageUrl!=null && (FireConstants.FCM_TEMPLATE_CHANNEL_ID.equals(channelId) ||
                        FireConstants.FCM_MEMES_CHANNEL_ID.equals(channelId))){
                    intent.putExtra(FireConstants.FCM_API_KEY_IMAGE_URL,imageUrl);
                    Glide.with(this).asBitmap().load(imageUrl).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            notificationBuilder.setStyle(new NotificationCompat.BigPictureStyle().bigPicture(resource));
                            notificationBuilder.setLargeIcon(BitmapFactory.decodeResource(getResources(),R.drawable.ic_launcher));
                            notificationBuilder.setContentIntent(pendingIntent);
                            notificationManager.notify(((int)new Date().getTime())/* ID of notification */, notificationBuilder.build());

                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
                }else if(FireConstants.FCM_APP_UPDATES_CHANNEL_ID.equals(channelId)){
                    imageUrl=remoteData.get(FireConstants.FCM_LARGE_ICON);
                    if(imageUrl!=null){
                        Glide.with(this).asBitmap().load(imageUrl).into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                notificationBuilder.setLargeIcon(resource);
                                notificationBuilder.setContentIntent(pendingIntent);
                                notificationManager.notify(((int)new Date().getTime())/* ID of notification */, notificationBuilder.build());
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
                    }

                }

            }


        }
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FireFunctions fireFunctions=new FireFunctions(this);
        HashMap hashMap=new HashMap();
        hashMap.put(ApiConstants.KEY_NOTIFICATION_TOKEN,s);
        fireFunctions.callApi(ApiUrls.UPDATE_NOTIFICATION_TOKEN,hashMap,null);
    }
}
