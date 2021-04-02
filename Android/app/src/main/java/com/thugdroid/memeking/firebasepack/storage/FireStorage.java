package com.thugdroid.memeking.firebasepack.storage;

import android.content.Context;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Random;


public class FireStorage {
    private Context context;
    private FirebaseStorage firebaseStorage;

    public FireStorage(Context context){
        this.context = context;
        firebaseStorage = FirebaseStorage.getInstance();
    }

    public void uploadImage(@NonNull Uri uri, @NonNull String storagePath,String mimeType, FileUploadListener fileUploadListener){
        StorageReference storageReference;
        String randomName= getRandomName(11)+"_"+getMilliSeconds();
        if(mimeType==null || "".equals(mimeType)){
            storageReference = firebaseStorage.getReference().child(storagePath+"/" +randomName);
        }else{
            storageReference = firebaseStorage.getReference().child(storagePath+ "/"+randomName+"."+mimeType);
        }

// Register observers to listen for when the download is done or if it fails
        storageReference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        if(fileUploadListener!=null){
                            fileUploadListener.onSuccess(taskSnapshot,uri);
                        }

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(fileUploadListener!=null){
                            fileUploadListener.onFail(e);
                        }
                    }
                });
            }
        });

    }

    private  Long getMilliSeconds(){
        return System.currentTimeMillis();
    }

    public interface FileUploadListener{
        void onSuccess(UploadTask.TaskSnapshot taskSnapshot, Uri uri);
        void onFail(Exception e);
    }
    public static String getRandomName(int count){
        String randomName="";
        for (int i = 0; i < count; i++) {
            Random random=new Random();
            char c = (char)(random.nextInt(20) + 'a');
            randomName+=c;
        }
        return  randomName;
    }
}
