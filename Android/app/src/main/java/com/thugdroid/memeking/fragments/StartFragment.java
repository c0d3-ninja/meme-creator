package com.thugdroid.memeking.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;


import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.thugdroid.libs.collagegrid.constants.GridNameConstants;
import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;
import com.thugdroid.memeking.room.repository.AppPrefsRepository;
import com.thugdroid.memeking.room.repository.CategoryRepository;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.viewmodel.StartFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;
import com.thugdroid.memeking.viewmodel.db.LoggedInUserDbViewModel;

import java.util.List;

public class StartFragment extends CustomFragment {
    private LoggedInUserDbViewModel loggedInUserDbViewModel;
    private AppPrefsRepository appPrefsRepository;
    private WindowViewModel windowViewModel;
    private StartFragmentViewModel startFragmentViewModel;
    private CategoryRepository categoryRepository;
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public StartFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_start,container,false);
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
        loggedInUserDbViewModel =new ViewModelProvider(this).get(LoggedInUserDbViewModel.class);
        appPrefsRepository =new ViewModelProvider(this).get(AppPrefsRepository.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        startFragmentViewModel =new ViewModelProvider(this).get(StartFragmentViewModel.class);
        categoryRepository =new ViewModelProvider(this).get(CategoryRepository.class);
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
    }

    @Override
    public void initListeners() {

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
    @Override
    public void initObservers() {
        appPrefsRepository.getSelectedCategoryDetailsAsLiveData().observe(getViewLifecycleOwner(), new Observer<CategoryEntity>() {
            @Override
            public void onChanged(CategoryEntity categoryEntity) {
                windowViewModel.getSelectedCategoryEntityAsLiveData().setValue(categoryEntity);
                startFragmentViewModel.setSelectedCategoryObserverExecuted(true);
                validateAndNavigate();
            }
        });

        appPrefsRepository.getPref(AppPrefsEntity.SELECTED_NAVDRAWER_MENU).observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String menuId) {
                windowViewModel.getSelectedNavDrawerMenuIdAsLiveData().setValue(menuId);
                startFragmentViewModel.setSelectedNavDrawerMenuExecuted(true);
                validateAndNavigate();
            }
        });
        loggedInUserDbViewModel.getLoggedInUserAsLiveData().observe(getViewLifecycleOwner(), (loggedInUser) -> {
            windowViewModel.setLoggedInUserEntity(loggedInUser);
            if(loggedInUser!=null){
                FirebaseCrashlytics.getInstance().setUserId(loggedInUser.getId());
                observeCategory();
            }
            startFragmentViewModel.setLoggedInUserObserverExecuted(true);
            validateAndNavigate();
        });
        appPrefsRepository.getSelectedCategoryDetailsAsLiveData().observeForever(new Observer<CategoryEntity>() {
            @Override
            public void onChanged(CategoryEntity categoryEntity) {
                CategoryEntity prevCategoryEntity = windowViewModel.getSelectedCategoryEntityAsLiveData().getValue();
                if(((prevCategoryEntity==null && categoryEntity!=null) || (prevCategoryEntity!=null && categoryEntity==null)) ||
                        (prevCategoryEntity!=null && categoryEntity!=null &&!prevCategoryEntity.getId().equals(categoryEntity.getId()))){
                    windowViewModel.getSelectedCategoryEntityAsLiveData().setValue(categoryEntity);
                }
            }
        });
        appPrefsRepository.getPref(AppPrefsEntity.SELECTED_NAVDRAWER_MENU).observeForever(new Observer<String>() {
            @Override
            public void onChanged(String menuId) {
                String prevMenuId = windowViewModel.getSelectedNavDrawerMenuIdAsLiveData().getValue();
                if(((prevMenuId==null && menuId!=null) || (prevMenuId!=null && menuId==null)) || (prevMenuId!=null && menuId!=null && !prevMenuId.equals(menuId))){
                    windowViewModel.getSelectedNavDrawerMenuIdAsLiveData().setValue(menuId);
                }
            }
        });
    }

    private boolean hasCategoryId(String id,List<CategoryEntity> categoryEntities){
        if(id==null || "".equals(id)){
            return false;
        }
        for (int i = 0; i < categoryEntities.size(); i++) {
         CategoryEntity categoryEntity=categoryEntities.get(i);
         if(categoryEntity.getId().equals(id)){
             return true;
         }
        }
        return false;
    }
    private Uri getUriFromIntent(Intent intent,String action){
        return Intent.ACTION_SEND.equals(action)?(Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM):intent.getData();
    }
    private void navigateToMainNewFragment(){
        navigate(R.id.action_startFragment_to_mainNewFragment);
    }
    private void validateAndNavigate(){
        if(startFragmentViewModel.isLoggedInUserObserverExecuted()
                &&
                startFragmentViewModel.isSelectedCategoryObserverExecuted()
                && startFragmentViewModel.isSelectedNavDrawerMenuExecuted()
        ){
            LoggedInUserEntity loggedInUser=windowViewModel.getLoggedInUserEntity();
            if(loggedInUser==null){
                navigate(R.id.action_startFragment_to_signInFragment);
            }else if(loggedInUser.regionId==null){
                StartFragmentDirections.ActionStartFragmentToSelectRegionFragment action=StartFragmentDirections.actionStartFragmentToSelectRegionFragment(Constants.MODE_SELECT_REGION);
                navigate(action);
            }else{

                Intent intent=getActivity().getIntent();
                String notificationChannel="";
                String categoryId="";
                if(intent!=null){
                    String type = intent.getType();
                    String action = intent.getAction();
                    notificationChannel = intent.getStringExtra(FireConstants.FCM_API_KEY_CHANNEL_ID);
                    categoryId=intent.getStringExtra(FireConstants.FCM_CATEGORY_ID);
                    if((Intent.ACTION_SEND.equals(action) || Intent.ACTION_EDIT.equals(action))&&(type!=null && type.startsWith("image/"))){
                        Uri imageUri = getUriFromIntent(intent,action);
                        if(imageUri!=null){
                            getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_CREATE_MEME_USAGE,
                                    Intent.ACTION_SEND.equals(action)?FireConstants.EVENT_CREATE_MEME_FROM_SHARE:
                                            FireConstants.EVENT_CREATE_MEME_FROM_EDIT);
                            StartFragmentDirections.ActionStartFragmentToCreateMemeFragment navAction= StartFragmentDirections.actionStartFragmentToCreateMemeFragment(GridNameConstants.L1,imageUri.toString(),null);
                            navigate(navAction);
                            return;
                        }else{
                            showMsg(R.string.cant_read_this_image);
                        }
                    }
                }
                if(windowViewModel.getSelectedCategoryEntityAsLiveData().getValue()!=null ||
                   windowViewModel.getSelectedNavDrawerMenuIdAsLiveData().getValue()!=null){
                        if(FireConstants.FCM_APP_UPDATES_CHANNEL_ID.equals(notificationChannel)){
                            /*link notification click*/
                            String link = intent.getStringExtra(FireConstants.FCM_LINK);
                            if(link!=null){
                                getMainActivity().goToUrl(link,true);
                                return;
                            }else{
                                navigateToMainNewFragment();
                            }
                        }else if(FireConstants.FCM_TEMPLATE_CHANNEL_ID.equals(notificationChannel)){
                            final String catId = categoryId;
                            categoryRepository.getAllCategoriesAsLiveData(windowViewModel.getRegionId()).observe(getViewLifecycleOwner(), new Observer<List<CategoryEntity>>() {
                                @Override
                                public void onChanged(List<CategoryEntity> categoryEntities) {
                                    if(categoryEntities.size()>0){
                                        String navigationCatId=catId;
                                        if(!hasCategoryId(catId,categoryEntities)){
                                            navigationCatId= categoryEntities.get(0).getId();
                                        }
                                        appPrefsRepository.updateSelectedCategory(navigationCatId, new AppDatabase.DbOperationCallbackListener() {
                                            @Override
                                            public void onSuccess() {
                                                navigateToMainNewFragment();
                                            }
                                        });
                                    }else{
                                        navigate(R.id.action_startFragment_to_initializingFragment);
                                    }
                                }
                            });

                        }else if(FireConstants.FCM_MEMES_CHANNEL_ID.equals(notificationChannel)  && AppUtils.isMemesEnabled(windowViewModel.getRegionId())){
                            appPrefsRepository.updateSelectedNavDrawerMenu(Constants.MEMES_FEED_MENUID, new AppDatabase.DbOperationCallbackListener() {
                                @Override
                                public void onSuccess() {
                                    navigateToMainNewFragment();
                                }
                            });
                        }else{
                            /*memes will be available for some regions.If any user selected memes menu then need to change it to category*/
                            String selectedMenu=windowViewModel.getSelectedNavDrawerMenuIdAsLiveData().getValue();
                            if(Constants.MEMES_FEED_MENUID.equals(selectedMenu)||Constants.MY_MEMES_MENUID.equals(selectedMenu)){
                                if(!AppUtils.isMemesEnabled(windowViewModel.getRegionId())){
                                    navigate(R.id.action_startFragment_to_initializingFragment);
                                    return;
                                }
                            }
                            navigateToMainNewFragment();
                        }

                }else{
                    navigate(R.id.action_startFragment_to_initializingFragment);
                }

            }
        }
    }

    @Override
    public void onClick(View v) {

    }

}
