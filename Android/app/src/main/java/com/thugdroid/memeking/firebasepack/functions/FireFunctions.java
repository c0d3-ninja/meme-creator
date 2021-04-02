package com.thugdroid.memeking.firebasepack.functions;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.thugdroid.memeking.BuildConfig;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.utils.AppUtils;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class FireFunctions {
    private Context context;
    private FirebaseFunctions firebaseFunctions;

    public FireFunctions(Context context) {
        this.context = context;
        FirebaseApp app= FirebaseApp.initializeApp(context);
        firebaseFunctions= FirebaseFunctions.getInstance(app);
        if(BuildConfig.DEBUG){
          firebaseFunctions.useFunctionsEmulator("http://192.168.43.104:5000"); //mobile
        }
    }

    public void callApi(String apiPath, HashMap data, ApiListener apiListener){
        if(data==null){
            data=new HashMap();
        }
        data.put(ApiConstants.KEY_APP_VERSION, AppUtils.getAppVersionCode(context));
        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.M){
            firebaseFunctions
                    .getHttpsCallable(apiPath)
                    .withTimeout(60L,TimeUnit.SECONDS)
                    .call(data)
                    .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                        @Override
                        public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                            if(apiListener!=null){
                                if(!task.isSuccessful()){
                                    apiListener.onFailure(task.getException());
                                }else {
                                    HashMap resultMap =(HashMap)task.getResult().getData();
                                    int statusCode=(int)resultMap.get(ApiConstants.KEY_STATUSCODE);
                                    Object object=resultMap.get(ApiConstants.KEY_DATA);
                                    apiListener.onSuccess(statusCode,object);
                                }
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    if(apiListener!=null){
                        apiListener.onFailure(e);
                    }
                }
            });

        }else{
            firebaseFunctions
                    .getHttpsCallable(apiPath)
                    .call(data)
                    .addOnCompleteListener(new OnCompleteListener<HttpsCallableResult>() {
                        @Override
                        public void onComplete(@NonNull Task<HttpsCallableResult> task) {
                            if(apiListener!=null){
                                if(!task.isSuccessful()){
                                    apiListener.onFailure(task.getException());
                                }else {
                                    HashMap resultMap =(HashMap)task.getResult().getData();
                                    int statusCode=(int)resultMap.get(ApiConstants.KEY_STATUSCODE);
                                    Object object=resultMap.get(ApiConstants.KEY_DATA);
                                    apiListener.onSuccess(statusCode,object);
                                }
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    e.printStackTrace();
                    if(apiListener!=null){
                        apiListener.onFailure(e);
                    }
                }
            });
        }

    }

    public interface ApiListener{
        void onSuccess(int statusCode,Object resultObject);
        void onFailure(Exception e);
    }


}
