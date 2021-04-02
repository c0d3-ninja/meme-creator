package com.thugdroid.memeking.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.thugdroid.libs.simplecrop.CropImageView;
import com.thugdroid.memeking.MediaFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.ui.LoadingDialog;
import com.thugdroid.memeking.utils.AppUtils;

import java.io.File;

public class CropFragment extends MediaFragment {
    private ViewHolder viewHolder;
    private Uri uri;
    private Listener listener;
    private LoadingDialog loadingDialog;
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public CropFragment(){

    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_crop,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
        initObservers();
        initCropOptions();
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
        viewHolder.positiveBtn.setOnClickListener(this::onClick);
        (findViewById(R.id.cropFragmentBack)).setOnClickListener(this::onClick);
        viewHolder.dontCropBtn.setOnClickListener(this::onClick);
    }

    @Override
    public void initObservers() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.cropFragmentPositiveBtn:
                cropImage();
                break;
            case R.id.cropFragmentBack:
                if(listener!=null){
                    listener.onBackClick();
                }
                break;
            case R.id.cropFragmentDontCropBtn:
                if(listener!=null){
                    listener.onDontCropClick(uri);
                }
                break;
        }
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    private void showLoadingDialog(){
        if(loadingDialog==null){
            loadingDialog=new LoadingDialog(getContext());
            loadingDialog.setLoadingText(getString(R.string.cropping_dots));
        }
        loadingDialog.show();
    }
    private void hideLoadingDialog(){
        if(loadingDialog!=null){
            loadingDialog.dismiss();
        }
    }
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    private void cropImage(){
        Bitmap bitmap = viewHolder.cropImageView.getCroppedBitmap();
        showLoadingDialog();
        new SaveImage(Constants.CROP_CACHE_FOLDER_NAME,bitmap,true,new SaveImageListener()).execute();
    }

    private void initCropOptions(){
        showProgressbar();
        Glide.with(getContext()).asBitmap().load(uri).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                showMsg(R.string.can_t_load_image);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                showCropImageView();
                Glide.with(getContext()).load(resource).into(viewHolder.cropImageView);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });

    }
    private void showProgressbar(){
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        viewHolder.cropImageView.setVisibility(View.GONE);
        viewHolder.positiveBtn.setEnabled(false);
        viewHolder.dontCropBtn.setEnabled(false);
    }

    private void showCropImageView(){
        viewHolder.progressBar.setVisibility(View.GONE);
        viewHolder.cropImageView.setVisibility(View.VISIBLE);
        viewHolder.positiveBtn.setEnabled(true);
        viewHolder.dontCropBtn.setEnabled(true);
    }


    private class ViewHolder{
        CropImageView cropImageView;
        Button positiveBtn,dontCropBtn;
        ProgressBar progressBar;
        public ViewHolder() {
            positiveBtn=findViewById(R.id.cropFragmentPositiveBtn);
            cropImageView=findViewById(R.id.cropFragmentMainImage);
            dontCropBtn=findViewById(R.id.cropFragmentDontCropBtn);
            progressBar=findViewById(R.id.cropImageProgressbar);
            cropImageView.setCropMode(CropImageView.CropMode.FREE);
            cropImageView.setInitialFrameScale(getResources().getInteger(R.integer.cropInitialScale));
            cropImageView.setHandleColor(AppUtils.getColor(getContext(),R.color.colorPrimary));
            cropImageView.setHandleSizeInDp(getResources().getInteger(R.integer.cropHandleSizeInDp));
            cropImageView.setTouchPaddingInDp(getResources().getInteger(R.integer.cropTouchPaddingInDp));
        }
    }

    private class SaveImageListener implements MediaFragment.SaveImageListener{
        @Override
        public void onSuccess(String uriStr) {
            hideLoadingDialog();
            if(listener!=null){
                Uri uri= Uri.parse(uriStr);
                listener.onCrop(uri);
            }
        }

        @Override
        public void onFailure() {
            hideLoadingDialog();
            if(listener!=null){
                listener.onCropFailed(uri);
            }
        }
    }

    public interface Listener{
        void onBackClick();
        void onCrop(Uri uri);
        void onCropFailed(Uri uri);
        void onDontCropClick(Uri uri);
    }
}
