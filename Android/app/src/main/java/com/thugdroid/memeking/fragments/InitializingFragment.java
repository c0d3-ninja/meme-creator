package com.thugdroid.memeking.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.repository.AppPrefsRepository;
import com.thugdroid.memeking.room.repository.CategoryRepository;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.viewmodel.InitializingFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class InitializingFragment extends CustomFragment {
    private CategoryRepository categoryRepository;
    private ViewHolder viewHolder;
    private InitializingFragmentViewModel initializingFragmentViewModel;
    private WindowViewModel windowViewModel;
    private AppPrefsRepository appPrefsRepository;
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public InitializingFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_initializing,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
        initObservers();
        recordScreenView();
        initFCM();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.swwRetryBtn:
            case R.id.noConnectionRetryBtn:
                getCategories();
                break;
        }
    }

    @Override
    public void initVariables() {
        categoryRepository =new ViewModelProvider(this).get(CategoryRepository.class);
        initializingFragmentViewModel =new ViewModelProvider(this).get(InitializingFragmentViewModel.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        appPrefsRepository =new ViewModelProvider(this).get(AppPrefsRepository.class);
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
        initializingFragmentViewModel.getCategoryApiModel().observe(getViewLifecycleOwner(), new Observer<ApiModel>() {
            @Override
            public void onChanged(ApiModel apiModel) {
                if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST || apiModel.getLoadingState()==ApiModel.LOADINGSTATE_IDLE){
                    showLoading();
                }else if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_FAILURE){
                    if(apiModel.getStatusCode()==HttpCodes.NOINTERNETCONNECTION){
                        showCustomNoConnection();
                    }else{
                        if(apiModel.getStatusCode()==HttpCodes.MESSAGE){
                            showMsg(apiModel.getErrorMessage());
                        }
                        showCustomSomethingWentWrong();
                    }
                }
            }
        });

        categoryRepository.getAllCategoriesAsLiveData(windowViewModel.getRegionId()).observe(getViewLifecycleOwner(), new Observer<List<CategoryEntity>>() {
            @Override
            public void onChanged(List<CategoryEntity> categoryEntities) {
                if(categoryEntities!=null && categoryEntities.size()>0){
                    if(!AppUtils.isMemesEnabled(windowViewModel.getRegionId())){
                        String catId = categoryEntities.get(0).getId();
                        appPrefsRepository.updateSelectedCategory(catId, new AppDatabase.DbOperationCallbackListener() {
                            @Override
                            public void onSuccess() {
                                navigate(R.id.action_initializingFragment_to_mainNewFragment);
                            }
                        });
                        return;
                    }
                    String menuId;
                    if(isTemplatesGroupEnabled()){
                        menuId=Constants.TEMPLATES_GROUP_MENU_ID;
                    }else{
                        menuId=Constants.MEMES_FEED_MENUID;
                    }
                    appPrefsRepository.updateSelectedNavDrawerMenu(menuId, new AppDatabase.DbOperationCallbackListener() {
                        @Override
                        public void onSuccess() {
                            navigate(R.id.action_initializingFragment_to_mainNewFragment);
                        }
                    });

                }else{
                    ApiModel apiModel= initializingFragmentViewModel.getCategoryApiModel().getValue();
                    if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_IDLE && apiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST){
                        getCategories();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        recordScreenView();
    }

    private boolean isTemplatesGroupEnabled(){
        for (int i = 0; i < Constants.TEMPLATES_GROUP_ENABLED_REGIONS.length; i++) {
            String str = Constants.TEMPLATES_GROUP_ENABLED_REGIONS[i];
            if(str.equals(windowViewModel.getRegionId())){
                return true;
            }
        }
        return false;
    }



    private void initFCM(){

        new InitFCM().execute();
    }

    private class ViewHolder{
        ConstraintLayout loadingParent;
        public ViewHolder() {
            loadingParent=findViewById(R.id.initializingParent);
        }
    }

    private void recordScreenView(){
        recordScreen(FireConstants.SCREEN_INITIALIZING);
    }
    private void showLoading(){
        hideNoInternetConnection();
        hideSomethingWentWrong();
        viewHolder.loadingParent.setVisibility(View.VISIBLE);
    }

    private void hideLoading(){
        viewHolder.loadingParent.setVisibility(View.GONE);
    }

    private void showCustomNoConnection(){
        hideSomethingWentWrong();
        hideLoading();
        showNoInternetConnection(this::onClick);
    }

    private void showCustomSomethingWentWrong(){
        hideNoInternetConnection();
        hideLoading();
        showSomethingWentWrong(this::onClick,null);
    }

    private void setCategoryApiModel(int loadingState,int statusCode,String msg){
        ApiModel apiModel=initializingFragmentViewModel.getCategoryApiModel().getValue();
        apiModel.setLoadingState(loadingState);
        apiModel.setStatusCode(statusCode);
        apiModel.setErrorMessage(msg);
        initializingFragmentViewModel.getCategoryApiModel().setValue(apiModel);
    }

    private void setCategoryApiModel(int loadingState,int statusCode){
        ApiModel apiModel=initializingFragmentViewModel.getCategoryApiModel().getValue();
        apiModel.setLoadingState(loadingState);
        apiModel.setStatusCode(statusCode);
        initializingFragmentViewModel.getCategoryApiModel().setValue(apiModel);
    }

    private void getCategories(){
        if(AppUtils.hasInternetConnection(getContext())){
            setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST,HttpCodes.IDLE);
            HashMap apiData=new HashMap();
            apiData.put(ApiConstants.KEY_REGION_ID,windowViewModel.getRegionId());
            getFireFunctions().callApi(ApiUrls.GET_CATEGORIES,apiData,new CategoryApiListener());
        }else {
            setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.NOINTERNETCONNECTION);
        }
    }

    private class CategoryApiListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            if(statusCode==HttpCodes.UNAUTHORIZED){
                unAuthorizeSignOut(appPrefsRepository,windowViewModel,R.id.action_initializingFragment_to_signInFragment);
            }
            else if(statusCode==HttpCodes.SUCCESS){
                setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode);
                List<CategoryEntity> categoryEntities= CategoryEntity.getEntityList(resultObject);
                appPrefsRepository.insertPref(AppPrefsEntity.CATEGORY_SILENTLY_CALLED_TIME,String.valueOf(new Date().getTime()));
                categoryRepository.insertAllCategories(categoryEntities);
            }else{
                if(statusCode==HttpCodes.MESSAGE){
                    setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,statusCode,getErrorMsgFromApiData(resultObject));
                }else{
                    setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,statusCode);
                }
            }
        }

        @Override
        public void onFailure(Exception e) {
            setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.INTERNALSERVERERROR);
        }
    }

    private class InitFCM extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            try {
                FirebaseInstanceId.getInstance().deleteInstanceId();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            try{

                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        subscribeFCMTopic(windowViewModel.getRegionId());
                    }
                });

            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
