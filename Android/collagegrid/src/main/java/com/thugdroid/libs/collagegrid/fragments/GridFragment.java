package com.thugdroid.libs.collagegrid.fragments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.thugdroid.libs.collagegrid.CustomFragment;
import com.thugdroid.libs.collagegrid.R;
import com.thugdroid.libs.collagegrid.constants.Constants;
import com.thugdroid.libs.collagegrid.constants.GridNameConstants;
import com.thugdroid.libs.collagegrid.model.GridConfig;
import com.thugdroid.libs.collagegrid.model.MyImage;
import com.thugdroid.libs.collagegrid.utils.AppUtils;
import com.thugdroid.libs.collagegrid.utils.GridUtils;
import com.thugdroid.libs.collagegrid.utils.ImageUtils;
import com.thugdroid.libs.zoomableimageview.ZoomableImageView;

import static android.app.Activity.RESULT_OK;

public class GridFragment extends CustomFragment {

    private static final int PICK_IMAGE_REQUEST_CODE=444;
    public static final String GRID_NAME="gridName";
    public static final String MAX_WIDTH="maxWidth";
    public static final String MAX_HEIGHT="maxHeight";
    private int displayWidth;
    private GridConfig gridConfig;
    private View gridView;
    private int clickedGridNo;
    private String bundleImageUrl;
    private LayoutMeasurementChangeListener layoutMeasurementChangeListener;
    private GridTouchListener gridTouchListener;
    private GestureDetector gestureDetector;
    private ImageInterceptionListener imageInterceptionListener;
    private ImagePickInterceptionListener imagePickInterceptionListener;

    /*to fix Caused by java.lang.NoSuchMethodException*/

    public static GridFragment newInstance(String gridName,int maxWidth,int maxHeight){
        GridFragment gridFragment=new GridFragment();
        Bundle args = new Bundle();
        args.putString(GRID_NAME,gridName);
        args.putInt(MAX_WIDTH,maxWidth);
        args.putInt(MAX_HEIGHT,maxHeight);
        gridFragment.setArguments(args);
        return gridFragment;
    }


    /*override methods start*/
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_grid,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        if(bundleImageUrl!=null){
            setImage(bundleImageUrl,1,false);
        }
    }

    @Override
    public void initVariables() {
        setActivity(getActivity());
        DisplayMetrics displayMetrics=new DisplayMetrics();
        getMainActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayWidth = displayMetrics.widthPixels;
        gridConfig = GridUtils.getGridConfig(getContext(),getArguments().getString(GRID_NAME),displayWidth);
        gridView = getLayoutInflater().inflate(gridConfig.getLayout(),null,false);
        gestureDetector=new GestureDetector(getContext(),new SingleGridGestureListener());
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        setViewLayouts(gridConfig.getLayoutCount());
        ConstraintLayout constraintLayout=view.findViewById(R.id.gridFragmentContainer);
        constraintLayout.addView(gridView);
    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initObservers() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PICK_IMAGE_REQUEST_CODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickImage();
            }else{
                showMsg(R.string.allow_permission_to_choose_image);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PICK_IMAGE_REQUEST_CODE:
                if(resultCode==RESULT_OK && data!=null){
                    Uri uri=data.getData();
                    MyImage imageDetails = AppUtils.getDetailedImage(getContext(),uri);
                    String mimeType=imageDetails.getMimeType();
                    if(mimeType!=null && mimeType.startsWith("image")){
                        if(imageInterceptionListener!=null){
                            imageInterceptionListener.onImagePicked(uri,clickedGridNo);
                        }else{
                            setImage(uri,clickedGridNo,false);
                        }
                    }else{
                        showMsg(R.string.unsupported_file_format);
                    }
                }
                break;
        }
    }

    /*override methods end*/

    /*getter setter start*/

    public void setImageInterceptionListener(ImageInterceptionListener imageInterceptionListener) {
        this.imageInterceptionListener = imageInterceptionListener;
    }

    public void setImagePickInterceptionListener(ImagePickInterceptionListener imagePickInterceptionListener) {
        this.imagePickInterceptionListener = imagePickInterceptionListener;
    }

    public void setBundleImageUrl(String bundleImageUrl) {
        this.bundleImageUrl = bundleImageUrl;
    }

    public void setLayoutMeasurementChangeListener(LayoutMeasurementChangeListener layoutMeasurementChangeListener) {
        this.layoutMeasurementChangeListener = layoutMeasurementChangeListener;
    }

    public void setGridTouchListener(GridTouchListener gridTouchListener) {
        this.gridTouchListener = gridTouchListener;
    }
    /*getter setter end*/

    /*other methods start*/

    private void setViewLayouts(int count){
        switch (count){
            case 1:
                ConstraintLayout grid1 =  gridView.findViewById(GridUtils.getGridId(1));
                grid1.getLayoutParams().width=gridConfig.getGrid1Width();
                grid1.getLayoutParams().height=gridConfig.getGrid1Height();
                grid1.setOnClickListener(new GridClickListener(1));
                break;
            case 2:
                setViewLayouts(1);
                ConstraintLayout grid2 =  gridView.findViewById(GridUtils.getGridId(2));
                grid2.getLayoutParams().width=gridConfig.getGrid2Width();
                grid2.getLayoutParams().height=gridConfig.getGrid2Height();
                grid2.setOnClickListener(new GridClickListener(2));
                break;
            case 3:
                setViewLayouts(2);
                ConstraintLayout grid3 =  gridView.findViewById(GridUtils.getGridId(3));
                grid3.getLayoutParams().width=gridConfig.getGrid3Width();
                grid3.getLayoutParams().height=gridConfig.getGrid3Height();
                grid3.setOnClickListener(new GridClickListener(3));
                break;
            case 4:
                setViewLayouts(3);
                ConstraintLayout grid4 =  gridView.findViewById(GridUtils.getGridId(4));
                grid4.getLayoutParams().width=gridConfig.getGrid4Width();
                grid4.getLayoutParams().height=gridConfig.getGrid4Height();
                grid4.setOnClickListener(new GridClickListener(4));
                break;
            case 5:
                setViewLayouts(4);
                ConstraintLayout grid5 =  gridView.findViewById(GridUtils.getGridId(5));
                grid5.getLayoutParams().width=gridConfig.getGrid5Width();
                grid5.getLayoutParams().height=gridConfig.getGrid5Height();
                grid5.setOnClickListener(new GridClickListener(5));
                break;
            case 6:
                setViewLayouts(5);
                ConstraintLayout grid6 =  gridView.findViewById(GridUtils.getGridId(6));
                grid6.getLayoutParams().width=gridConfig.getGrid6Width();
                grid6.getLayoutParams().height=gridConfig.getGrid6Height();
                grid6.setOnClickListener(new GridClickListener(6));
                break;
        }

    }


    public void pickImage(){
        if(isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE,PICK_IMAGE_REQUEST_CODE)){
            openGallery(PICK_IMAGE_REQUEST_CODE);
        }
    }

    private void openGallery(int requestCode){
        Intent _pickIntent=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(_pickIntent,requestCode);
    }



    /*call listener is a hook for setting image from external like bordered image */
    public void setImage(Object uri,final int gridNo,boolean useCache){
        final ConstraintLayout currentGrid = findViewById(GridUtils.getGridId(gridNo));
        hidePlaceHolder(gridNo);
        showProgressBar(gridNo);
        Glide.with(getContext()).asBitmap().listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                showMsg(R.string.can_t_load_this_image);
                boolean isShowPlaceHolder=true;
                for (int i=currentGrid.getChildCount();i>=0;i--){
                    View view = currentGrid.getChildAt(i);
                    if(view instanceof ZoomableImageView){
                        isShowPlaceHolder=false;
                    }
                }
                if(isShowPlaceHolder){
                    showPlaceHolder(gridNo);
                }
                hideProgressBar(gridNo);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).skipMemoryCache(useCache?false:true)
                .diskCacheStrategy(useCache?DiskCacheStrategy.AUTOMATIC:DiskCacheStrategy.NONE)
                .load(uri).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                hideProgressBar(gridNo);
                if(GridNameConstants.L1.equals(getArguments().getString(GRID_NAME))){
                    currentGrid.setOnClickListener(null);
                    try{
                        ImageUtils.Size size=ImageUtils.getImageMaxSize(resource,getArguments().getInt(MAX_WIDTH),getArguments().getInt(MAX_HEIGHT));
                        int width= size.getWidth();
                        int height=size.getHeight();
                        currentGrid.getLayoutParams().width = width;
                        currentGrid.getLayoutParams().height=height;
                        if(layoutMeasurementChangeListener!=null){
                            layoutMeasurementChangeListener.onLayoutMeasurementChanged(width,height);
                        }
                        ImageView imageView=currentGrid.findViewById(R.id.singleGridImage);
                        imageView.getLayoutParams().width=width;
                        imageView.getLayoutParams().height=height;
                        imageView.setOnTouchListener(new SingleGridTouchListener());
                        Glide.with(getContext()).load(resource).override(size.getWidth(),size.getHeight()).into(imageView);
                        imageView.setVisibility(View.VISIBLE);
                        /*for changing glide skipMemoryCache*/
                        bundleImageUrl=null;
                        currentGrid.setBackgroundColor(AppUtils.getColor(getContext(),R.color.gridBgDark));
                    }catch (OutOfMemoryError outOfMemoryError){
                        outOfMemoryError.printStackTrace();
                        showMsg(R.string.not_enough_memory_to_load_this_image);
                    }catch (Exception e){
                        e.printStackTrace();
                        showMsg(R.string.can_t_load_this_image);
                    }

                }else{
                    for (int i=currentGrid.getChildCount();i>=0;i--){
                        View view = currentGrid.getChildAt(i);
                        if(view instanceof ZoomableImageView){
                            currentGrid.removeView(view);
                        }
                    }
                    ZoomableImageView zoomableImageView=new ZoomableImageView(getContext());
                    if(currentGrid!=null){
                        try{
                            Bitmap bitmap=ImageUtils.getScaleBitmapForGrid(resource,currentGrid.getWidth(),currentGrid.getHeight());
                            zoomableImageView.setImageBitmap(bitmap);
                            zoomableImageView.setOnImageLongPressListener(new ImageLongPressListener(gridNo));
                            zoomableImageView.setOnImageTouchedListener(new ImageOnTouchListener());
                            currentGrid.addView(zoomableImageView);
                            currentGrid.setBackgroundColor(AppUtils.getColor(getContext(),R.color.gridBgDark));
                        }catch (OutOfMemoryError outOfMemoryError){
                            outOfMemoryError.printStackTrace();
                            showMsg(R.string.not_enough_memory_to_load_this_image);
                        }catch (Exception e){
                            e.printStackTrace();
                            showMsg(R.string.can_t_load_this_image);
                        }
                    }
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {
            }
        });
    }


    public int getClickedGridNo(){
        return clickedGridNo;
    }

    private void showPlaceHolder(int gridNo){
        ConstraintLayout currentGrid = findViewById(GridUtils.getGridId(gridNo));
        (currentGrid.findViewById(R.id.placeHolder)).setVisibility(View.VISIBLE);
    }
    private void hidePlaceHolder(int gridNo){
        ConstraintLayout currentGrid = findViewById(GridUtils.getGridId(gridNo));
        (currentGrid.findViewById(R.id.placeHolder)).setVisibility(View.GONE);
    }

    private void showProgressBar(int gridNo){
        ConstraintLayout currentGrid = findViewById(GridUtils.getGridId(gridNo));
        (currentGrid.findViewById(R.id.progressBar)).setVisibility(View.VISIBLE);
    }

    private void hideProgressBar(int gridNo){
        ConstraintLayout currentGrid = findViewById(GridUtils.getGridId(gridNo));
        (currentGrid.findViewById(R.id.progressBar)).setVisibility(View.GONE);
    }


    public int getTotalGridCount(){
        return gridConfig.getLayoutCount();
    }
    public int getImageAddedGridCount(){
        if(gridConfig.getLayoutCount()==1){
            ConstraintLayout currentGrid = findViewById(GridUtils.getGridId(1));
            ImageView imageView=currentGrid.findViewById(R.id.singleGridImage);
            if(imageView.getVisibility()==View.VISIBLE){
                return 1;
            }else {
                return 0;
            }
        }
        int count =0;
        for (int i =getTotalGridCount()-1;i>=0;i--){
            if(hasZoomableImageView((ConstraintLayout)findViewById(Constants.allGridIds[i]))){
                count++;
            }
        }
        return count;
    }

    private boolean hasZoomableImageView(ConstraintLayout gridView){
        for (int i=gridView.getChildCount();i>=0;i--){
            View view = gridView.getChildAt(i);
            if(view!=null && view instanceof ZoomableImageView){
                return  true;
            }
        }
        return false;
    }

    /*other methods end*/


    /*classes start */
    private class GridClickListener implements View.OnClickListener{
        int gridNo;

        public GridClickListener(int gridNo) {
            this.gridNo = gridNo;
        }

        @Override
        public void onClick(View v) {
            clickedGridNo= gridNo;
            if(imagePickInterceptionListener!=null){
                imagePickInterceptionListener.onImagePick();
            }else{
                pickImage();
            }
        }
    }
    private class ImageLongPressListener implements ZoomableImageView.OnImageLongPressListener{
        int gridNo;

        public ImageLongPressListener(int gridNo) {
            this.gridNo = gridNo;
        }

        @Override
        public void onImageLongPressed(ZoomableImageView zoomableImageView) {
            clickedGridNo=gridNo;
            if(imagePickInterceptionListener!=null){
                imagePickInterceptionListener.onImagePick();
            }else{
                pickImage();
            }
        }
    }
    private class ImageOnTouchListener implements ZoomableImageView.OnImageTouchedListener{
        @Override
        public void onImageTouched() {
            if(gridTouchListener!=null){
                gridTouchListener.onTouch();
            }
        }
    }
    private class SingleGridTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {
                gestureDetector.onTouchEvent(event);
            return true;
        }
    }
    private class SingleGridGestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            if(gridTouchListener!=null){
                gridTouchListener.onTouch();
                return false;
            }
            return super.onDown(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            super.onLongPress(e);
            if(imagePickInterceptionListener!=null){
                imagePickInterceptionListener.onImagePick();
            }else{
                pickImage();
            }
        }
    }
    /*classes end*/



    /*interfaces start*/
    public interface LayoutMeasurementChangeListener{
        void onLayoutMeasurementChanged(int width,int height);
    }
    public interface GridTouchListener{
        void onTouch();
    }

    public interface ImagePickInterceptionListener{
        void onImagePick();
    }

    public interface ImageInterceptionListener{
        void onImagePicked(Uri uri, int gridNo);
    }
    /*interfaces end*/


}
