package com.thugdroid.memeking.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.thugdroid.libs.collagegrid.adapters.CollageGridAdapter;
import com.thugdroid.libs.collagegrid.constants.GridNameConstants;
import com.thugdroid.libs.collagegrid.model.MyImage;
import com.thugdroid.libs.collagegrid.utils.AppUtils;
import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.MediaFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.FireConstants;

import static android.app.Activity.RESULT_OK;

public class CollageListFragment extends MediaFragment {
    private static final int PICK_SINGLE_IMAGE=1;
    private ViewHolder viewHolder;
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public CollageListFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragement_collagelist,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
    }

    @Override
    public void initVariables() {

    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
        CollageGridAdapter collageAdapter=new CollageGridAdapter(getContext());
        collageAdapter.setCollageGridClickListener(new CollageGridClicker());
        viewHolder.collageListGrid.setAdapter(collageAdapter);
    }

    @Override
    public void initListeners() {
        viewHolder.back.setOnClickListener(this::onClick);
    }

    @Override
    public void initObservers() {

    }

    private class ViewHolder{
        GridView collageListGrid;
        ConstraintLayout back;
        public ViewHolder(){
            collageListGrid=findViewById(R.id.collageListGridView);
            back=findViewById(R.id.collageListBack);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.collageListBack:
                getMainActivity().onBackPressed();
                break;
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        recordScreenView();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PICK_SINGLE_IMAGE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickImage();
            }else{
                showMsg(com.thugdroid.libs.collagegrid.R.string.allow_permission_to_choose_image);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PICK_SINGLE_IMAGE:
                if(resultCode==RESULT_OK && data!=null){
                    Uri uri=data.getData();
                    MyImage imageDetails = AppUtils.getDetailedImage(getContext(),uri);
                    String mimeType=imageDetails.getMimeType();
                    if(mimeType!=null && mimeType.startsWith("image")){
                        CollageListFragmentDirections.ActionCollageListFragmentToCreateMemeFragment action =
                                CollageListFragmentDirections.actionCollageListFragmentToCreateMemeFragment(GridNameConstants.L1,uri.toString(),null);
                        navigate(action);
                    }else{
                        showMsg(com.thugdroid.libs.collagegrid.R.string.unsupported_file_format);
                    }
                }
                break;
        }
    }

    /*other methods start*/
    public void pickImage(){
        if(isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE,PICK_SINGLE_IMAGE)){
            openGallery(PICK_SINGLE_IMAGE);
        }
    }
    /*other methods end*/
    private void recordScreenView() {
        recordScreen(FireConstants.SCREEN_COLLAGE_LIST);
    }

    class CollageGridClicker implements CollageGridAdapter.CollageGridClickListener{
        @Override
        public void onClick(String collageGridName, int position) {
            getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_CREATE_MEME_USAGE,FireConstants.EVENT_CREATE_MEME_FROM_COLLAGE);
            getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_COLLAGE_USAGE,collageGridName);
            if(GridNameConstants.L1.equals(collageGridName)){
                pickImage();
            }else{
                CollageListFragmentDirections.ActionCollageListFragmentToCreateCollageFragment action =
                        CollageListFragmentDirections.actionCollageListFragmentToCreateCollageFragment(collageGridName);
                navigate(action);
            }


        }
    }
}
