package com.thugdroid.memeking.firebasepack.analytics;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class FireAnalytics {
    private FirebaseAnalytics firebaseAnalytics;
    private String customEventName;
    public FireAnalytics(Context context,String customEventName){
        firebaseAnalytics=FirebaseAnalytics.getInstance(context);
        this.customEventName=customEventName;
    }
    public void logSingleEvent(String eventId,String eventValue){
        Bundle bundle=new Bundle();
        bundle.putString(eventId,eventValue);
        firebaseAnalytics.logEvent(customEventName,bundle);
    }
    public void setCurrentScreen(Activity activity,String screenName){
        firebaseAnalytics.setCurrentScreen(activity,screenName,null);
    }
}
