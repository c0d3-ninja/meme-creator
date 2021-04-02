package com.thugdroid.memeking.firebasepack.auth;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;



public  class FireSignIn {
    public static int GOOGLE_SIGN_IN = 601;
    public FirebaseAuth firebaseAuth;
    public GoogleSignInClient googleSignInClient;

    private ActivityListener activityListener;
    private GoogleUserAuthListener googleUserAuthListnerListener;
    private FirebaseUserAuthListener firebaseUserAuthListener;
    private Activity activity;
    public FireSignIn(Activity activity,String requestIdToken){
        this.activity = activity;
        firebaseAuth= FirebaseAuth.getInstance();
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(requestIdToken).requestEmail().build();
        googleSignInClient = GoogleSignIn.getClient(activity,googleSignInOptions);

    }

    public  void setGoogleUserAuthListnerListener(GoogleUserAuthListener googleUserAuthListnerListener) {
        this.googleUserAuthListnerListener = googleUserAuthListnerListener;
    }

    public  void setFirebaseUserAuthListener(FirebaseUserAuthListener firebaseUserAuthListener) {
        this.firebaseUserAuthListener = firebaseUserAuthListener;
    }

    public void setActivityListener(ActivityListener activityListener) {
        this.activityListener = activityListener;
    }

    public  void signIn(){
        Intent intent = googleSignInClient.getSignInIntent();
        if(activityListener==null){
            activity.startActivityForResult(intent, GOOGLE_SIGN_IN);
        }else {
            activityListener.onActivityResult(intent,GOOGLE_SIGN_IN);
        }

    }

    public void signInWithGoogle(Intent data){
        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
        try{
            GoogleSignInAccount googleSignInAccount = task.getResult(ApiException.class);
            if(googleSignInAccount!=null){
                if(googleUserAuthListnerListener!=null){
                    googleUserAuthListnerListener.signedInListener(googleSignInAccount);
                }
                firebaseAuth(googleSignInAccount);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void firebaseAuth(GoogleSignInAccount googleSignInAccount){
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(),null);

        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(activity, task->{
            firebaseSignInSuccess(task);
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                firebaseSignInFail(e);
            }
        });
    }

    public FirebaseUser getCurrentUser(){
        return FirebaseAuth.getInstance().getCurrentUser();
    }
    public void signOut(){
        googleSignInClient.signOut();
        firebaseAuth.signOut();
        if(googleUserAuthListnerListener!=null){
            googleUserAuthListnerListener.signedOutListener();
        }
        if(firebaseUserAuthListener!=null){
            firebaseUserAuthListener.signedOutListener();
        }
    }

    private void firebaseSignInSuccess(Task<AuthResult> task){
        if(task.isSuccessful()){
            FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
            if(firebaseUserAuthListener!=null){
                firebaseUserAuthListener.signedInListener(firebaseUser);
            }
        }
    }

    private void firebaseSignInFail(Exception e){
        if(firebaseUserAuthListener!=null){
            firebaseUserAuthListener.signInFailListener(e);
        }
    }
    public void anonymousSignIn(){
        firebaseAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                firebaseSignInSuccess(task);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                firebaseSignInFail(e);
            }
        });
    }

    public interface FirebaseUserAuthListener {
        void signedInListener(FirebaseUser firebaseUser);
        void signedOutListener();
        void signInFailListener(Exception e);
    }

    public interface GoogleUserAuthListener {
        void signedInListener(GoogleSignInAccount googleSignInAccount);
        void signedOutListener();
    }

    public interface ActivityListener{
        void onActivityResult(Intent intent,int requestCode);
    }
}


