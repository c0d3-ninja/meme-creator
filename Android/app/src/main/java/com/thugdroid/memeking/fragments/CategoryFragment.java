package com.thugdroid.memeking.fragments;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;
import com.thugdroid.memeking.room.repository.AppPrefsRepository;
import com.thugdroid.memeking.room.repository.CategoryRepository;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.utils.TimeUtils;
import com.thugdroid.memeking.viewmodel.CategoryFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class CategoryFragment extends CustomFragment {
    private CategoryRepository categoryRepository;
    private ViewHolder viewHolder;
    private RequestManager glide;
    private ListItemClickListener listItemListener;
    private AppPrefsRepository appPrefsRepository;
    private WindowViewModel windowViewModel;
    private CategoryFragmentViewModel categoryFragmentViewModel;

    /*to fix Caused by java.lang.NoSuchMethodException*/
    public CategoryFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menuitems,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
        initObservers();
    }

    @Override
    public void initVariables() {
        categoryRepository =new ViewModelProvider(this).get(CategoryRepository.class);
        appPrefsRepository =new ViewModelProvider(this).get(AppPrefsRepository.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        categoryFragmentViewModel=new ViewModelProvider(this).get(CategoryFragmentViewModel.class);
        glide= Glide.with(getContext());
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
        categoryRepository.getAllCategoriesAsLiveData(windowViewModel.getRegionId()).observe(getViewLifecycleOwner(), new Observer<List<CategoryEntity>>() {
            @Override
            public void onChanged(List<CategoryEntity> categoryEntities) {
                if(categoryEntities!=null){
                    renderAllCategories(categoryEntities);
                }
            }
        });
        appPrefsRepository.getPref(AppPrefsEntity.CATEGORY_SILENTLY_CALLED_TIME).observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String time) {
                if(categoryFragmentViewModel.getSilentCallApiModel().getLoadingState()==ApiModel.LOADINGSTATE_IDLE){
                    if(time==null){
                        callCategoriesSilently();
                    }else{
                        Long longTime = Long.parseLong(time);
                        double diff = TimeUtils.getDifferenceInHrs(new Date().getTime(),longTime);
                        if(diff>=Constants.CATEGORY_SILENT_CALL_THRESHOLD_HOURS){
                            callCategoriesSilently();
                        }
                    }
                }

            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    public void setListItemListener(ListItemClickListener listItemListener) {
        this.listItemListener = listItemListener;
    }

    private void renderAllCategories(List<CategoryEntity> categoryEntities){
        viewHolder.parentContainer.removeAllViews();
        for(int i=0;i<categoryEntities.size();i++){
            CategoryEntity categoryEntity = categoryEntities.get(i);
            View convertView=getLayoutInflater().inflate(R.layout.item_menu,viewHolder.parentContainer,false);
            ImageView logo=convertView.findViewById(R.id.navDrawerItemLogo);
            TextView logoPlaceHolder=convertView.findViewById(R.id.navDrawerLogoPlaceholder);
            TextView title=convertView.findViewById(R.id.navDrawerItemTitle);
            if(categoryEntity.imageUrl==null){
                setCategoryText(logoPlaceHolder,logo,logoPlaceHolder,AppUtils.getShortenName(categoryEntity.name,1),i);
            }else{
                final int index=i;
                logoPlaceHolder.setVisibility(View.GONE);
                logo.setVisibility(View.VISIBLE);
                glide.asBitmap().load(categoryEntity.imageUrl)
                        .listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                setCategoryText(logoPlaceHolder,logo,logoPlaceHolder,AppUtils.getShortenName(categoryEntity.name,1),index);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                return false;
                            }
                        })
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                glide.load(resource).apply(new RequestOptions().transform(new CenterCrop(),new RoundedCorners(16))).into(logo);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            }
            title.setText(categoryEntity.name);
            if(listItemListener!=null){
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listItemListener.onListItemClick(categoryEntity);
                    }
                });
            }
            viewHolder.parentContainer.addView(convertView);
        }

        if(windowViewModel.getSelectedNavDrawerMenuIdAsLiveData().getValue()==null && windowViewModel.getSelectedCategoryEntityAsLiveData().getValue()==null  && categoryEntities.size()>0){
            appPrefsRepository.insertPref(AppPrefsEntity.SELECTED_CATEGORY,categoryEntities.get(0).getId());
        }
    }
    private void setCategoryText(View visibleView, View hideableView, TextView textView,String str,int position){
        visibleView.setVisibility(View.VISIBLE);
        hideableView.setVisibility(View.GONE);
        textView.setText(str);
        textView.setBackgroundResource(AppUtils.getDrawableFrom(position));
    }

    private void callCategoriesSilently(){
        HashMap apiData = new HashMap();
        LoggedInUserEntity loggedInUserEntity = windowViewModel.getLoggedInUserEntity();
        /*signout case*/
        if(loggedInUserEntity!=null){
            apiData.put(ApiConstants.KEY_REGION_ID,loggedInUserEntity.getRegionId());
            setSilentCallApiModel(ApiModel.LOADINGSTATE_REQUEST,HttpCodes.IDLE);
            getFireFunctions().callApi(ApiUrls.GET_CATEGORIES,apiData,new CategoryApiListener());
        }
    }

    private void setSilentCallApiModel(int loadingState,int statusCode){
        categoryFragmentViewModel.getSilentCallApiModel().setLoadingState(loadingState);
        categoryFragmentViewModel.getSilentCallApiModel().setStatusCode(statusCode);
    }

    private class ViewHolder{
        LinearLayout parentContainer;
        public ViewHolder() {
            parentContainer=findViewById(R.id.menuItemsFragmentContainer);
        }
    }

    class CategoryApiListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            setSilentCallApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode);
            if(statusCode== HttpCodes.SUCCESS){
                appPrefsRepository.insertPref(AppPrefsEntity.CATEGORY_SILENTLY_CALLED_TIME,String.valueOf(new Date().getTime()));
                List<CategoryEntity> categoryEntities= CategoryEntity.getEntityList(resultObject);
                categoryRepository.deleteUnwantedCategories(windowViewModel.getCategoriesCache(),categoryEntities,new CategoryDeleteListener(categoryEntities));
            }
        }

        @Override
        public void onFailure(Exception e) {
            setSilentCallApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.INTERNALSERVERERROR);
        }
    }

    public interface ListItemClickListener{
        void onListItemClick(CategoryEntity categoryEntity);
    }

    class CategoryDeleteListener implements AppDatabase.DbOperationCallbackListener{
        List<CategoryEntity> categoryEntities;

        public CategoryDeleteListener(List<CategoryEntity> categoryEntities) {
            this.categoryEntities = categoryEntities;
        }

        @Override
        public void onSuccess() {
            categoryRepository.insertAllCategories(categoryEntities);
        }
    }
}
