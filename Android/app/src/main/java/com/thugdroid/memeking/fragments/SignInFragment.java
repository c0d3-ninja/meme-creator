package com.thugdroid.memeking.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.SignInButton;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.auth.FireSignIn;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;
import com.thugdroid.memeking.room.repository.AppPrefsRepository;
import com.thugdroid.memeking.room.repository.CategoryRepository;
import com.thugdroid.memeking.ui.LoadingDialog;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.viewmodel.SignInFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;
import com.thugdroid.memeking.viewmodel.db.LoggedInUserDbViewModel;

import java.util.HashMap;
import java.util.List;

public class SignInFragment extends CustomFragment {
    private ViewHolder viewHolder;

    private SignInFragmentViewModel signInFragmentViewModel;
    private LoadingDialog loadingDialog;

    private LoggedInUserDbViewModel loggedInUserDbViewModel;
    private FireSignIn fireSignIn;
    private WindowViewModel windowViewModel;
    private CategoryRepository categoryRepository;
    private AppPrefsRepository appPrefsRepository;
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public SignInFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_signin,container,false);
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
        fireSignIn=new FireSignIn(getMainActivity(),getString(R.string.default_web_client_id));
        signInFragmentViewModel = new ViewModelProvider(this).get(SignInFragmentViewModel.class);
        loggedInUserDbViewModel =new ViewModelProvider(this).get(LoggedInUserDbViewModel.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        categoryRepository =new ViewModelProvider(this).get(CategoryRepository.class);
        appPrefsRepository=new ViewModelProvider(this).get(AppPrefsRepository.class);
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
    }

    @Override
    public void initListeners() {
        viewHolder.signInButton.setOnClickListener(this::onClick);
        fireSignIn.setActivityListener(new FireSigninActivityListner());
        fireSignIn.setFirebaseUserAuthListener(new FirebaseUserAuthListener());
    }


    @Override
    public void initObservers() {
        signInFragmentViewModel.getSignInApiModel().observe(getViewLifecycleOwner(), new Observer<ApiModel>() {
            @Override
            public void onChanged(ApiModel apiModel) {
                if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST){
                    showLoading();

                }else {
                    hideLoading();
                    if(apiModel.getLoadingState()== ApiModel.LOADINGSTATE_REQUEST_FAILURE){
                        if(apiModel.getStatusCode()==HttpCodes.MESSAGE){
                            showMsg(apiModel.getErrorMessage());
                        }else{
                            showMsg(R.string.something_went_wrong);
                        }
                    }
                }
            }
        });
        loggedInUserDbViewModel.getLoggedInUserAsLiveData().observe(getViewLifecycleOwner(), loggedInUserEntity -> {
            if(loggedInUserEntity !=null){
                FirebaseCrashlytics.getInstance().setUserId(loggedInUserEntity.getId());
                windowViewModel.setLoggedInUserEntity(loggedInUserEntity);
                if(!windowViewModel.isCategoryObserverExecuted()){
                    observeCategory();
                }
                SignInFragmentDirections.ActionSignInFragmentToSelectRegionFragment action=SignInFragmentDirections.actionSignInFragmentToSelectRegionFragment(Constants.MODE_SELECT_REGION);
                navigate(action);
            }
        });
    }
    private void observeCategory(){
        windowViewModel.setCategoryObserverExecuted(true);
        categoryRepository.getAllCategoriesAsLiveData(windowViewModel.getRegionId()).observeForever(new Observer<List<CategoryEntity>>() {
            @Override
            public void onChanged(List<CategoryEntity> categoryEntities) {
                windowViewModel.setCategoriesCache(categoryEntities);
            }
        });
    }

    private class ViewHolder{
        SignInButton signInButton;
        public ViewHolder() {
            signInButton=findViewById(R.id.signInBtn);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.signInBtn:
                if(AppUtils.hasInternetConnection(getContext())){
                    fireSignIn.signOut();
                    fireSignIn.signIn();
                }else{
                    showMsg(R.string.no_internet_connection);
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode==FireSignIn.GOOGLE_SIGN_IN){
            if(resultCode== Activity.RESULT_OK && data!=null){
                fireSignIn.signInWithGoogle(data);
                setSignInApiModel(ApiModel.LOADINGSTATE_REQUEST,HttpCodes.IDLE);
            }else{
                showMsg(R.string.something_went_wrong);
            }
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        recordScreenView();
    }

    private void recordScreenView() {
        recordScreen(FireConstants.SCREEN_SIGNIN);
    }

    private void showLoading(){
        if(loadingDialog==null){
            loadingDialog=new LoadingDialog(getContext());
            loadingDialog.setLoadingText(getString(R.string.signing_in_dots));
        }
        loadingDialog.show();
    }
    private void hideLoading(){
        if(loadingDialog!=null){
            loadingDialog.dismiss();
        }
    }

    private void setSignInApiModel(int loadingState,int statusCode,String msg){
        ApiModel apiModel=signInFragmentViewModel.getSignInApiModel().getValue();
        apiModel.setLoadingState(loadingState);
        apiModel.setStatusCode(statusCode);
        apiModel.setErrorMessage(msg);
        signInFragmentViewModel.getSignInApiModel().setValue(apiModel);
    }
    private void setSignInApiModel(int loadingState,int statusCode){
        ApiModel apiModel=signInFragmentViewModel.getSignInApiModel().getValue();
        apiModel.setLoadingState(loadingState);
        apiModel.setStatusCode(statusCode);
        signInFragmentViewModel.getSignInApiModel().setValue(apiModel);
    }

    private class FireSigninActivityListner implements FireSignIn.ActivityListener{
        @Override
        public void onActivityResult(Intent intent, int requestCode) {
            startActivityForResult(intent,requestCode);
        }
    }

    private class FirebaseUserAuthListener implements FireSignIn.FirebaseUserAuthListener{
        @Override
        public void signedInListener(FirebaseUser firebaseUser) {
            getFireFunctions().callApi(ApiUrls.SIGNIN,null,new SignInApiListener());
        }

        @Override
        public void signedOutListener() {

        }

        @Override
        public void signInFailListener(Exception e) {
            e.printStackTrace();
            setSignInApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.INTERNALSERVERERROR);
        }
    }



    private class SignInApiListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            if(statusCode== HttpCodes.SUCCESS && resultObject!=null){
                loggedInUserDbViewModel.insert(LoggedInUserEntity.getEntity((HashMap)resultObject));
                String instaUsername=LoggedInUserEntity.getInstaUserNameFromResponse(resultObject);
                appPrefsRepository.insertPref(AppPrefsEntity.INSTA_USERNAME,instaUsername);
                setSignInApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode);
            }else{
                if(statusCode==HttpCodes.MESSAGE){
                    setSignInApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,statusCode,getErrorMsgFromApiData(resultObject));
                }else{
                    setSignInApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,statusCode);
                }

            }
        }

        @Override
        public void onFailure(Exception e) {
            e.printStackTrace();
            setSignInApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.INTERNALSERVERERROR);
        }
    }
}



