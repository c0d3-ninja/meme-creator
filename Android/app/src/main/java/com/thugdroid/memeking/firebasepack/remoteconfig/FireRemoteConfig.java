package com.thugdroid.memeking.firebasepack.remoteconfig;

import android.app.Activity;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;
import com.thugdroid.memeking.constants.FireConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;

public class FireRemoteConfig {
    Activity activity;
    ConfigListener configListener;
    FirebaseRemoteConfig firebaseRemoteConfig;
    FirebaseRemoteConfigSettings configSettings;

    public FireRemoteConfig(Activity activity, ConfigListener configListener) {
        this.activity = activity;
        this.configListener = configListener;
    }

    public void fetchConfig(String key){
        getFirebaseRemoteConfig().setConfigSettingsAsync(getConfigSettings());
        getFirebaseRemoteConfig().fetchAndActivate().addOnCompleteListener(getActivity(), new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                try{
                    if(task.isSuccessful()){
                        FirebaseRemoteConfigValue  firebaseRemoteConfigValue
                                =firebaseRemoteConfig.getValue(key);
                        if(firebaseRemoteConfigValue!=null
                                && !"".equals(firebaseRemoteConfigValue.asString())){
                            JSONObject jsonObject= new JSONObject(firebaseRemoteConfigValue.asString());
                            if(getConfigListener()!=null){
                                getConfigListener().onData(key,jsonToHashMap(jsonObject));
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }

            }
        });
    }

    private FirebaseRemoteConfig getFirebaseRemoteConfig() {
        if(firebaseRemoteConfig==null){
            firebaseRemoteConfig=FirebaseRemoteConfig.getInstance();
        }
        return firebaseRemoteConfig;
    }

    private FirebaseRemoteConfigSettings getConfigSettings() {
        if(configSettings==null){
            configSettings = new FirebaseRemoteConfigSettings.Builder()
                    .setMinimumFetchIntervalInSeconds(FireConstants.REMOTE_CONFIG_INTERVAL)
                    .build();
        }
        return configSettings;
    }

    private Activity getActivity() {
        return activity;
    }


    private ConfigListener getConfigListener() {
        return configListener;
    }

    public interface ConfigListener{
        void onData(String key, HashMap configValuesMap);
    }

    private HashMap jsonToHashMap(JSONObject jsonObject){
        HashMap resultMap = new HashMap();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()){
            String key = keys.next();
            try {
                if(jsonObject.get(key)!=null){
                    resultMap.put(key,jsonObject.get(key));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return resultMap;
    }
}
