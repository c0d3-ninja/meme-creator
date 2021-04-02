package com.thugdroid.memeking.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.thugdroid.memeking.MediaFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.ui.LoadingDialog;
import com.thugdroid.memeking.utils.AppUtils;

public class TempImageFragment extends MediaFragment {
    private Uri uri;
    private LoadingDialog loadingDialog;
    private ParentHandShakes parentHandShakes;
    private ViewHolder viewHolder;

    public TempImageFragment() {
    }

    public static TempImageFragment newInstance(Uri uri,ParentHandShakes parentHandShakes){
        TempImageFragment tempImageFragment=new TempImageFragment();
        tempImageFragment.setUri(uri);
        tempImageFragment.setParentHandShakes(parentHandShakes);
        return tempImageFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tempimage,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initViews(view);
        showLoadingDialog();
        Glide.with(getContext()).load(getUri()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Bitmap bitmap= AppUtils.getBitmapFromView(viewHolder.imageView);
                            new SaveImage(Constants.TEMP_IMAGES_FOLDER_NAME,bitmap,true,new TempImageSaveListener()).execute();
                        }catch (Exception e){
                            showCantLoadTempImage();
                            e.printStackTrace();
                        }
                    }
                },200);

                return false;
            }
        }).diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).into(viewHolder.imageView);
    }

    @Override
    public void initVariables() {

    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initObservers() {

    }
    @Override
    public void onClick(View view) {

    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public ParentHandShakes getParentHandShakes() {
        return parentHandShakes;
    }

    public void setParentHandShakes(ParentHandShakes parentHandShakes) {
        this.parentHandShakes = parentHandShakes;
    }

    private void showLoadingDialog(){
        getLoadingDialog().show();
    }
    private LoadingDialog getLoadingDialog(){
        if(loadingDialog==null){
            loadingDialog=new LoadingDialog(getContext());
            loadingDialog.setLoadingText(getString(R.string.preparing_dots));
        }
        return loadingDialog;
    }
    private void hideLoadingDialog(){
        getLoadingDialog().dismiss();
    }

    private void showCantLoadTempImage(){
        hideLoadingDialog();
        if(getParentHandShakes()!=null){
            getParentHandShakes().onImageSaveFailed();
        }
    }

    private class TempImageSaveListener implements SaveImageListener{
        @Override
        public void onSuccess(String uriString) {
            hideLoadingDialog();
            if(getParentHandShakes()!=null){
                getParentHandShakes().onImageSaved(uriString);
            }
        }

        @Override
        public void onFailure() {
            showCantLoadTempImage();
        }
    }


    private class ViewHolder{
        ImageView imageView;
        public ViewHolder() {
            imageView=findViewById(R.id.tempImageSaverImageView);
        }
    }

    public interface  ParentHandShakes{
        void onImageSaved(String uriString);
        void onImageSaveFailed();
    }
}
