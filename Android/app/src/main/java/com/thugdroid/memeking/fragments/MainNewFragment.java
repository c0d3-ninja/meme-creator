package com.thugdroid.memeking.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.google.android.play.core.tasks.OnFailureListener;
import com.google.android.play.core.tasks.OnSuccessListener;
import com.google.android.play.core.tasks.Task;
import com.thugdroid.libs.collagegrid.constants.GridNameConstants;
import com.thugdroid.libs.spotlight.OnSpotlightStateChangedListener;
import com.thugdroid.libs.spotlight.WalkThrough;
import com.thugdroid.libs.spotlight.shape.Circle;
import com.thugdroid.libs.spotlight.target.SimpleTarget;
import com.thugdroid.memeking.BuildConfig;
import com.thugdroid.memeking.MediaFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.constants.FragmentTags;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.firebasepack.remoteconfig.FireRemoteConfig;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.model.MyImage;
import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.room.entity.TemplatesGroupDataEntity;
import com.thugdroid.memeking.room.repository.AppPrefsRepository;
import com.thugdroid.memeking.room.repository.CategoryRepository;
import com.thugdroid.memeking.ui.ConfirmationDialog;
import com.thugdroid.memeking.ui.data.MenuItemsData;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.utils.TimeUtils;
import com.thugdroid.memeking.viewmodel.MainNewFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class MainNewFragment extends MediaFragment {
    private static final int PICK_MEME=2;
    private ViewHolder viewHolder;
    private AppPrefsRepository appPrefsRepository;
    private MainNewFragmentViewModel mainNewFragmentViewModel;
    private WindowViewModel windowViewModel;
    private OnBackPressedCallback onBackPressedCallback;
    private int apiType;
    private CategoryRepository categoryRepository;
    private AppUpdateManager appUpdateManager;

    private boolean isTemplatesRefreshedFirstTime,isMemesRefreshedFirstTime;
    private FireRemoteConfig fireRemoteConfig;
    /*preserve scroll from templates fragment*/
    private int templateScrollX,templateScrollY;
    /*override methods start*/
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public MainNewFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_mainnew,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
        initObservers();
        CategoryFragment categoryFragment=new CategoryFragment();
        categoryFragment.setListItemListener(new CategoryClickListener());
        getSupportFragmentManager().beginTransaction().replace(R.id.mainNewCategoryContainer,categoryFragment).commit();
        initInAppUpdate();
        if(!mainNewFragmentViewModel.isFirstTimeComponentMounted()){
            getAdminUsersConfig();
            mainNewFragmentViewModel.setFirstTimeComponentMounted(true);
        }
    }

    private void initInAppUpdate(){
        appUpdateManager= AppUpdateManagerFactory.create(getContext());
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();
        appUpdateInfoTask.addOnSuccessListener(new OnSuccessListener<AppUpdateInfo>() {
            @Override
            public void onSuccess(AppUpdateInfo appUpdateInfo) {
                if(appUpdateInfo.updateAvailability()== UpdateAvailability.UPDATE_AVAILABLE){
                    viewHolder.appUpdateBtn.setVisibility(View.VISIBLE);
                    if(appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)){
                       viewHolder.appUpdateIv.setImageResource(R.drawable.ic_customupdate_red_24);
                    }else{
                        viewHolder.appUpdateIv.setImageResource(R.drawable.ic_customupdate_green_24);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }
    private void getAdminUsersConfig(){
        getFireRemoteConfig().fetchConfig(FireConstants.REMOTE_CONFIG_ADMIN_USERS);
    }



    @Override
    public void initVariables() {
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        appPrefsRepository =new ViewModelProvider(this).get(AppPrefsRepository.class);
        categoryRepository =new ViewModelProvider(this).get(CategoryRepository.class);
        mainNewFragmentViewModel=new ViewModelProvider(this).get(MainNewFragmentViewModel.class);
        mainNewFragmentViewModel.setHasTemplateScroll(true);
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
        NavDrawerMenuItemListener navDrawerMenuItemListener = new NavDrawerMenuItemListener();
        MenuItemsFragment navDrawerTemplateItemsFragment = MenuItemsFragment.newInstance(MenuItemsData.getTemplateItems(windowViewModel.getRegionId()),navDrawerMenuItemListener);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainNewNavDrawerTemplateItems,navDrawerTemplateItemsFragment).commit();
        MenuItemsFragment navDrawerBottomFragment = MenuItemsFragment.newInstance(MenuItemsData.getMainBottomItems(),navDrawerMenuItemListener);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainNewNavDrawerBottomItems,navDrawerBottomFragment).commit();

        if(AppUtils.isMemesEnabled(windowViewModel.getRegionId())){
            MenuItemsFragment navDrawerMemeFragment = MenuItemsFragment.newInstance(MenuItemsData.getMemeItems(),navDrawerMenuItemListener);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.mainNewNavDrawerMemeItems,navDrawerMemeFragment).commit();
        }else {
            (findViewById(R.id.mainNewNavDrawerMemeItems)).setVisibility(View.GONE);
            (findViewById(R.id.mainNewMemeItemsDivider)).setVisibility(View.GONE);
            (findViewById(R.id.mainNewMemeItemsTitle)).setVisibility(View.GONE);

        }


    }

    @Override
    public void initListeners() {
        viewHolder.categoryMenu.setOnClickListener(this::onClick);
        viewHolder.createMemeFab.setOnClickListener(this::onClick);
        viewHolder.mainNewSearchBtn.setOnClickListener(this::onClick);
        viewHolder.categoryRefreshIv.setOnClickListener(this::onClick);
        viewHolder.mainNewHardRefreshBtn.setOnClickListener(this::onClick);
        viewHolder.appUpdateBtn.setOnClickListener(this::onClick);
        onBackPressedCallback= new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(viewHolder.mainNewPostMemeFragment.getVisibility()==View.VISIBLE){
                    hideUploadMemeFragment();
                }
                else if(viewHolder.searchFragmentContainer.getVisibility()==View.VISIBLE){
                    hideSearchFragment();
                }else{
                    onBackPressedCallback.setEnabled(false);
                    getMainActivity().onBackPressed();
                }

            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);
    }

    @Override
    public void initObservers() {
        windowViewModel.getSelectedCategoryEntityAsLiveData().observe(getViewLifecycleOwner(), new Observer<CategoryEntity>() {
            @Override
            public void onChanged(CategoryEntity categoryEntity) {
                if(categoryEntity!=null && categoryEntity.getId()!=null){
                    viewHolder.selectedCategory.setText(categoryEntity.getName());
                    setApiType(Constants.API_TYPE_TEMPLATES_FEED);
                    renderTemplateFragment();
                }
            }
        });
        windowViewModel.getSelectedNavDrawerMenuIdAsLiveData().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String menuId) {
                if(menuId!=null){
                    switch (menuId){
                        case Constants.FAVORITES_MENUID:
                            viewHolder.selectedCategory.setText(getString(R.string.favourites));
                            setApiType(Constants.API_TYPE_FAV_TEMPLATES);
                            renderTemplateFragment();
                            break;
                        case Constants.MY_TEMPLATES_MENUID:
                            viewHolder.selectedCategory.setText(getString(R.string.my_templates));
                            setApiType(Constants.API_TYPE_MY_TEMPLATES);
                            renderTemplateFragment();
                            break;
                        case Constants.MEMES_FEED_MENUID:
                            viewHolder.selectedCategory.setText(getString(R.string.recent_10_memes));
                            setApiType(Constants.API_TYPE_MEMES_FEED);
                            renderMemesFragment();
                            break;
                        case Constants.MY_MEMES_MENUID:
                            viewHolder.selectedCategory.setText(getString(R.string.my_memes));
                            setApiType(Constants.API_TYPE_MY_MEMES);
                            renderMemesFragment();
                            break;
                        case Constants.TEMPLATES_GROUP_MENU_ID:
                            viewHolder.selectedCategory.setText(getString(R.string.movies));
                            setApiType(Constants.API_TYPE_TEMPLATES_GROUP);
                            renderTemplatesGroupFragment();
                            break;
                    }
                }
            }
        });
        LiveData<String> walkThroughLiveData = appPrefsRepository.getPref(AppPrefsEntity.WALKTHROUGH_CATEGORY_CLICK);
        walkThroughLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                walkThroughLiveData.removeObservers(getViewLifecycleOwner());
                if(s==null){
                    showMainSpotLight();
                }else{
                    loadBannerAd();
                }
            }
        });
        mainNewFragmentViewModel.getCategoryApiModel().observe(getViewLifecycleOwner(), new Observer<ApiModel>() {
            @Override
            public void onChanged(ApiModel apiModel) {
                if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST){
                    showCategoryLoading();
                }else{
                    hideCategoryLoading();
                }
            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.mainNewCategoryMenu:
                showNavDrawer();
                break;

            case R.id.mainNewCreateMemeFab:
                navigate(R.id.action_mainNewFragment_to_collageListFragment);
                break;

            case R.id.mainNewSearchBtn:
                showSearchFragment();
                break;
            case R.id.categoryRefresIv:
                getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_TEMPLATE_LIST_USAGE,FireConstants.EVENT_MANUAL_CATEGORY_REFRESH);
                getCategories();
                break;
            case R.id.mainNewHardRefreshBtn:
                showHardRefreshDialog();
                break;
            case R.id.mainNewUpdateBtn:
                showUpdateDialog();
                break;

        }
    }

    @Override
    public void onDestroyView() {
        /*hook to remove observers from templates fragment, commit allowing stateloss is used for main activity destory workaround*/
        onBackPressedCallback.setEnabled(false);
        try{
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(FragmentTags.HOME_TEMPLATES_FRAGMENT);
            if(fragment!=null){
                if(isMemesApiType()){
                    if(fragment instanceof MemesFragment){
                        MemesFragment memesFragment=(MemesFragment)fragment;
                        setTemplateScrollX(memesFragment.getScrollDx());
                        setTemplateScrollY(memesFragment.getScrollDy());
                        getSupportFragmentManager().beginTransaction().remove(memesFragment).commitAllowingStateLoss();
                        getSupportFragmentManager().executePendingTransactions();
                    }
                }else if (isTemplateGroupApiType()){
                    if(fragment instanceof TemplatesGroupFragment){
                        TemplatesGroupFragment templatesGroupFragment = (TemplatesGroupFragment)fragment;
                        setTemplateScrollX(templatesGroupFragment.getScrollDx());
                        setTemplateScrollY(templatesGroupFragment.getScrollDy());
                        getSupportFragmentManager().beginTransaction().remove(templatesGroupFragment).commitAllowingStateLoss();
                        getSupportFragmentManager().executePendingTransactions();
                    }
                } else{
                    if(fragment instanceof  TemplatesFragment){
                        TemplatesFragment templatesFragment =(TemplatesFragment) fragment;
                        setTemplateScrollX(templatesFragment.getScrollDx());
                        setTemplateScrollY(templatesFragment.getScrollDy());
                        getSupportFragmentManager().beginTransaction().remove(templatesFragment).commitAllowingStateLoss();
                        getSupportFragmentManager().executePendingTransactions();
                    }
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        recordScreenView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PICK_MEME){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickMeme();
            }else{
                showMsg(R.string.allow_permission_to_choose_image);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PICK_MEME:
                if(resultCode==RESULT_OK && data!=null){
                    Uri uri=data.getData();
                    MyImage imageDetails = AppUtils.getDetailedImage(getContext(),uri);
                    String mimeType=imageDetails.getMimeType();
                    if(mimeType!=null && AppUtils.getIndexOf(Constants.IMAGE_ACCEPTABLE_MIME_TYPES,mimeType)!=-1){
                        showUploadMemeFragment(imageDetails);
                    }else{
                        showMsg(R.string.jpg_png_images_allowed);
                    }
                }
                break;
        }
    }
    /*override methods end*/

    /*init ads start*/

    private FireRemoteConfig getFireRemoteConfig() {
        if(fireRemoteConfig==null){
            fireRemoteConfig=new FireRemoteConfig(getMainActivity(),new RemoteConfigListener());
        }
        return fireRemoteConfig;
    }

    private int getApiType() {
        return apiType;
    }

    private void setApiType(int apiType){
        this.apiType=apiType;
    }

    private void loadReviewContainer(boolean isReviewEnabled){
        if(isReviewEnabled){
            viewHolder.reviewContainer.setVisibility(View.VISIBLE);
            Button positiveBtn=viewHolder.reviewContainer.findViewById(R.id.rateNow);
            Button negativeBtn=viewHolder.reviewContainer.findViewById(R.id.rateLater);
            positiveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_TEMPLATE_LIST_USAGE,FireConstants.EVENT_RATE_NOW_VIA_VADIVELU);
                    getMainActivity().goToPlayStore(false);
                    loadReviewContainer(false);
                    appPrefsRepository.insertPref(AppPrefsEntity.REVIEWED_TYPE_AND_TIME,Constants.REVIEW_TYPE_REVIEWED+"&&"+new Date().getTime());
                }
            });
            negativeBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_TEMPLATE_LIST_USAGE,FireConstants.EVENT_RATE_LATER_VIA_VADIVELU);
                    loadReviewContainer(false);
                    appPrefsRepository.insertPref(AppPrefsEntity.REVIEWED_TYPE_AND_TIME,Constants.REVIEW_TYPE_LATER+"&&"+new Date().getTime());
                }
            });
        }else {
            viewHolder.reviewContainer.setVisibility(View.GONE);
        }
    }
    private void fetchReviewEnabledFromRemoteConfig(){
        getFireRemoteConfig().fetchConfig(FireConstants.REMOTE_CONFIG_REVIEW_KEY);
    }
    private void loadBannerAd(){
        //TODO: load ads in this page
//            viewHolder.bannerAdView.setVisibility(View.VISIBLE);
//            AdRequest adRequest = new AdRequest.Builder().build();
//            viewHolder.bannerAdView.loadAd(adRequest);

        LiveData<String> reviewLiveData = appPrefsRepository.getPref(AppPrefsEntity.REVIEWED_TYPE_AND_TIME);
        reviewLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String reviewTypeAndTime) {
                if(Constants.REGION_TN_ID.equals(windowViewModel.getRegionId())){
                    if(reviewTypeAndTime==null){
                        fetchReviewEnabledFromRemoteConfig();
                    }else{
                        try {
                            String[] reviewTypeAndTimeSplit = reviewTypeAndTime.split("&&");
                            String reviewType = reviewTypeAndTimeSplit[0];
                            if(!Constants.REVIEW_TYPE_REVIEWED.equals(reviewType)){
                                Long time = Long.parseLong(reviewTypeAndTimeSplit[1]);
                                if(TimeUtils.getDifferenceInDays(new Date().getTime(),time)>=Constants.REVIEW_THRESHOLD_IN_DAYS){
                                    fetchReviewEnabledFromRemoteConfig();
                                }
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
                reviewLiveData.removeObservers(getViewLifecycleOwner());
            }
        });

    }
    /*init ads end*/
    /*other methods start*/
    private void showUpdateDialog(){
        ConfirmationDialog confirmationDialog=new ConfirmationDialog(getContext());
        confirmationDialog.setTitle(getString(R.string.new_update_available));
        confirmationDialog.setPositiveBtnText(getString(R.string.update_now));
        confirmationDialog.setNegativeBtnText(getString(R.string.later));
        confirmationDialog.setOnClickListener(new UpdateConfirmationDialogListener());
        confirmationDialog.show();
    }
    private void pickMeme(){
        if(isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE,PICK_MEME)){
            openGallery(PICK_MEME);
        }
    }
    private void showUploadMemeFragment(MyImage myImage){
        UploadMemePopupFragment uploadMemePopupFragment = UploadMemePopupFragment.newInstance(myImage,new PostMemeHandShake());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.mainNewPostMemeFragment,uploadMemePopupFragment,FragmentTags.MAIN_NEW_POST_MEME_FRAGMENT).commit();
        viewHolder.mainNewPostMemeFragment.setVisibility(View.VISIBLE);
    }
    private void hideUploadMemeFragment(){
        viewHolder.mainNewPostMemeFragment.setVisibility(View.GONE);
        try{
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(FragmentTags.MAIN_NEW_POST_MEME_FRAGMENT);
            if(fragment!=null ){
                getSupportFragmentManager().beginTransaction().remove(fragment).commit();
            }

        }catch (Exception e){
        }
    }
    private void recordScreenView() {
        recordScreen(FireConstants.SCREEN_TEMPLATES_LIST);
    }
    private void showHardRefreshDialog(){
        ConfirmationDialog confirmationDialog=new ConfirmationDialog(getContext());
        if(isMemesApiType()){
            confirmationDialog.setTitle(getString(R.string.do_you_want_to_hard_reload_memes));
        }else{
            confirmationDialog.setTitle(getString(R.string.do_you_want_to_hard_reload_this_category));
        }
        confirmationDialog.setPositiveBtnColor(AppUtils.getColor(getContext(),R.color.colorPrimary));
        confirmationDialog.setOnClickListener(new HardRefreshDialogListener());
        confirmationDialog.show();
    }

    private boolean isMemesApiType(){
        return (getApiType() ==Constants.API_TYPE_MEMES_FEED || getApiType() ==Constants.API_TYPE_MY_MEMES);
    }

    private boolean isTemplateGroupApiType(){
        return (getApiType() ==Constants.API_TYPE_TEMPLATES_GROUP);
    }

    private void renderMemesFragment(){
        MemesFragment memesFragment;
        if(BuildConfig.DEBUG){
            if(mainNewFragmentViewModel.isHasTemplateScroll()){
                memesFragment=MemesFragment.newInstance(getApiType(),false,
                        getTemplateScrollX(),getTemplateScrollY(),new MemesListHandShake());
            }else{
                memesFragment=MemesFragment.newInstance(getApiType(),false,
                        0,0,new MemesListHandShake());
            }
        }else{
            if(mainNewFragmentViewModel.isHasTemplateScroll()){
                mainNewFragmentViewModel.setHasTemplateScroll(false);
                memesFragment=MemesFragment.newInstance(getApiType(),!isMemesRefreshedFirstTime,
                        getTemplateScrollX(),getTemplateScrollY(),new MemesListHandShake());
            }else{
                memesFragment=MemesFragment.newInstance(getApiType(),!isMemesRefreshedFirstTime,
                        0,0,new MemesListHandShake());
            }
        }
        isMemesRefreshedFirstTime =true;
        getSupportFragmentManager().beginTransaction().replace(R.id.mainNewFragmentsContainer,memesFragment, FragmentTags.HOME_TEMPLATES_FRAGMENT).commit();
    }
    private void renderTemplatesGroupFragment(){
        TemplatesGroupFragment templatesGroupFragment =
                new TemplatesGroupFragment();
        templatesGroupFragment.setParentHandShakes(new TemplatesGroupHandShake());
        templatesGroupFragment.setItemClickListener(new TemplatesGroupClickListener());
        templatesGroupFragment.setRefreshOnLoad(false);
        if(mainNewFragmentViewModel.isHasTemplateScroll()){
            mainNewFragmentViewModel.setHasTemplateScroll(false);
            /*maintaining previous scroll*/
            templatesGroupFragment.setScrollDx(getTemplateScrollX());
            templatesGroupFragment.setScrollDy(getTemplateScrollY());
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.mainNewFragmentsContainer,templatesGroupFragment, FragmentTags.HOME_TEMPLATES_FRAGMENT).commit();
    }
    private void renderTemplateFragment(){
        TemplatesFragment templatesFragment=TemplatesFragment.newInstance(getApiType(),
                windowViewModel.getSelectedCategoryEntityAsLiveData().getValue(),
                new ActionsListener(),
                new MainNewToTemplatesHandShake(),
                true,
                getApiType() !=Constants.API_TYPE_FAV_TEMPLATES);
        /*hard refresh to more templates when user enters first time*/
        if(BuildConfig.DEBUG){
            templatesFragment.setRefreshOnLoad(false);
        }else{
            templatesFragment.setRefreshOnLoad(!isTemplatesRefreshedFirstTime);
        }
        isTemplatesRefreshedFirstTime =true;
        if(mainNewFragmentViewModel.isHasTemplateScroll()){
            mainNewFragmentViewModel.setHasTemplateScroll(false);
            /*maintaining previous scroll*/
            templatesFragment.setScrollDx(getTemplateScrollX());
            templatesFragment.setScrollDy(getTemplateScrollY());
        }
        getSupportFragmentManager().beginTransaction().replace(R.id.mainNewFragmentsContainer,templatesFragment, FragmentTags.HOME_TEMPLATES_FRAGMENT).commit();

    }

    private void showNavDrawer(){
        viewHolder.drawerLayout.openDrawer(Gravity.LEFT);
    }
    private void hideNavDrawer(){
        viewHolder.drawerLayout.closeDrawer(Gravity.LEFT);
    }
    private void showSearchFragment(){
        viewHolder.searchFragmentContainer.setVisibility(View.VISIBLE);
        SearchSuggestionFragment searchSuggestionFragment =SearchSuggestionFragment.newInstance(
                getApiType(),
                windowViewModel.getSelectedCategoryEntityAsLiveData().getValue(),
                SearchSuggestionFragment.MODE_FULLSCREEN,
                Constants.SEARCH_INPUT_MIN,
                true,
                true,
                new SearchSuggestionListener()
        );
        getSupportFragmentManager().beginTransaction().replace(R.id.mainNewSearchFragmentContainer, searchSuggestionFragment).commit();
    }
    private void hideSearchFragment(){
        viewHolder.searchFragmentContainer.setVisibility(View.GONE);
    }

    private void showMainSpotLight(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(isAdded()){
                    int locations[] = getSpotLightLocations(viewHolder.selectedCategory);
                    int x = getContext().getResources().getInteger(R.integer.spotlightX);
                    int incrementY = getContext().getResources().getInteger(R.integer.spotlightIncrementY);
                    SimpleTarget categoryClickTarget = new SimpleTarget.Builder(getMainActivity())
                            .setPoint(locations[0],locations[1])
                            .setOverlayPoint(x,locations[1]+incrementY)
                            .setShape(new Circle(getResources().getInteger(R.integer.walkthroughCircleRadius)))
                            .setTitle(getString(R.string.walkthrough_category_title))
                            .setDescription(getString(R.string.walkthrough_category_desc))
                            .setImageResource(R.drawable.ic_click_gesture_white)
                            .setDuration(getResources().getInteger(R.integer.walkthroughNextFrameDuration))
                            .build();
                    locations = getSpotLightLocations(viewHolder.createMemeFab);
                    SimpleTarget createMemeClick = new SimpleTarget.Builder(getMainActivity())
                            .setPoint(locations[0],locations[1])
                            .setOverlayPoint(x,locations[1]-incrementY)
                            .setShape(new Circle(getResources().getInteger(R.integer.walkthroughCircleRadius)))
                            .setTitle(getString(R.string.walkthrough_creatememe_title))
                            .setDescription(getString(R.string.walkthrough_creatememe_desc))
                            .setImageResource(R.drawable.ic_click_gesture_white)
                            .setDuration(getResources().getInteger(R.integer.walkthroughNextFrameDuration))
                            .build();

                    WalkThrough.with(getMainActivity())
                            .setOverlayColor(R.color.background)
                            .setDuration(getResources().getInteger(R.integer.walkthroughSpotAnimationDuration))
                            .setTargets(categoryClickTarget,createMemeClick)
                            .setClosedOnTouchedOutside(true)
                            .setAnimation(new DecelerateInterpolator(1f))
                            .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                                @Override
                                public void onStarted() {

                                }

                                @Override
                                public void onEnded() {
                                    appPrefsRepository.insertPref(AppPrefsEntity.WALKTHROUGH_CATEGORY_CLICK,String.valueOf(new Date().getTime()));
                                    /*need to handle this here while showing spotlight because of obeserver calling more than one time to trigger this spotlight*/
                                    loadBannerAd();
                                    showNavDrawer();
                                }
                            })
                            .start();
                }
            }
        },getResources().getInteger(R.integer.mainWalkthroughDelay));

    }
    private void showCategoryLoading(){
        viewHolder.categoryRefreshIv.setVisibility(View.GONE);
        viewHolder.categoryRefreshProgressBar.setVisibility(View.VISIBLE);
    }
    private void hideCategoryLoading(){
        viewHolder.categoryRefreshIv.setVisibility(View.VISIBLE);
        viewHolder.categoryRefreshProgressBar.setVisibility(View.GONE);
    }

    private void setCategoryApiModel(int loadingState,int statusCode){
        ApiModel apiModel = mainNewFragmentViewModel.getCategoryApiModel().getValue();
        apiModel.setLoadingState(loadingState);
        apiModel.setStatusCode(statusCode);
        mainNewFragmentViewModel.getCategoryApiModel().setValue(apiModel);
    }

    public int getTemplateScrollX() {
        return templateScrollX;
    }

    public void setTemplateScrollX(int templateScrollX) {
        this.templateScrollX = templateScrollX;
    }

    public int getTemplateScrollY() {
        return templateScrollY;
    }

    public void setTemplateScrollY(int templateScrollY) {
        this.templateScrollY = templateScrollY;
    }

    /*other methods end*/

    /*api methods start*/
    private void getCategories(){
        if(AppUtils.hasInternetConnection(getContext())){
            HashMap apiData = new HashMap();
            String regionId = windowViewModel.getRegionId();
            /*signout case*/
            if(regionId!=null){
                apiData.put(ApiConstants.KEY_REGION_ID,regionId);
                setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST,HttpCodes.IDLE);
                getFireFunctions().callApi(ApiUrls.GET_CATEGORIES,apiData,new CategoryApiListener());
            }
        }else{
            showMsg(R.string.no_internet_connection);
        }

    }

    /*api methods end*/

    /*classes start*/
    private class ViewHolder{
        ConstraintLayout categoryMenu;
        DrawerLayout drawerLayout;
        TextView selectedCategory;
        FloatingActionButton createMemeFab;
        ConstraintLayout searchFragmentContainer,mainNewFragmentsContainer;
        ConstraintLayout mainNewSearchBtn,mainNewHardRefreshBtn,appUpdateBtn;
        ImageView appUpdateIv;
        View categoryRefreshIv,categoryRefreshProgressBar,reviewContainer,mainNewPostMemeFragment;
        public ViewHolder() {
            drawerLayout=findViewById(R.id.drawer_layout);
            categoryMenu=findViewById(R.id.mainNewCategoryMenu);
            selectedCategory=findViewById(R.id.mainNewCategoryName);
            createMemeFab=findViewById(R.id.mainNewCreateMemeFab);
            searchFragmentContainer=findViewById(R.id.mainNewSearchFragmentContainer);
            mainNewFragmentsContainer=findViewById(R.id.mainNewFragmentsContainer);
            mainNewSearchBtn=findViewById(R.id.mainNewSearchBtn);
            categoryRefreshIv=findViewById(R.id.categoryRefresIv);
            categoryRefreshProgressBar=findViewById(R.id.categoryRefreshProgressBar);
            mainNewHardRefreshBtn=findViewById(R.id.mainNewHardRefreshBtn);
            reviewContainer=findViewById(R.id.mainNewReviewContainer);
            reviewContainer.setVisibility(View.GONE);

            mainNewPostMemeFragment=findViewById(R.id.mainNewPostMemeFragment);
            mainNewPostMemeFragment.setVisibility(View.GONE);
            appUpdateBtn=findViewById(R.id.mainNewUpdateBtn);
            appUpdateIv=findViewById(R.id.mainNewUpdateImageView);
            appUpdateBtn.setVisibility(View.GONE);
        }
    }
    private class CategoryClickListener implements CategoryFragment.ListItemClickListener{
        @Override
        public void onListItemClick(CategoryEntity categoryEntity) {
            hideNavDrawer();
            appPrefsRepository.updateSelectedCategory(categoryEntity.getId());
            getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_CATEGORY_USAGE,categoryEntity.getName().toLowerCase());
        }
    }
    private class ActionsListener implements TemplatesFragment.ActionsListener {
        @Override
        public void onTemplateItemClick(TemplateEntity templateEntity, int templateType) {
            MainNewFragmentDirections.ActionMainNewFragmentToTemplatePreviewFragment action=
                    MainNewFragmentDirections.actionMainNewFragmentToTemplatePreviewFragment(templateEntity.getId(),templateEntity.getSearchTags()
                            ,templateEntity.getImageUrl(),templateEntity.getCategoryId(),templateEntity.isFavorite(),
                            templateEntity.getCreatedBy(),templateEntity.getCreatedTime(),templateEntity.getRegionId(),templateEntity.getAuthorId(),
                            templateType,false);
            navigate(action);
        }

    }
    private class CategoryApiListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            if(statusCode== HttpCodes.SUCCESS){
                setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode);
                appPrefsRepository.insertPref(AppPrefsEntity.CATEGORY_SILENTLY_CALLED_TIME,String.valueOf(new Date().getTime()));
                List<CategoryEntity> categoryEntities= CategoryEntity.getEntityList(resultObject);
                categoryRepository.deleteUnwantedCategories(windowViewModel.getCategoriesCache(),categoryEntities,new CategoryDeleteListener(categoryEntities));
                showMsg(R.string.categories_updated_successfully);
            }else{
                setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,statusCode);
                showMsg(R.string.can_t_fetch_categories);
            }
        }

        @Override
        public void onFailure(Exception e) {
            setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.INTERNALSERVERERROR);
            showMsg(R.string.can_t_fetch_categories);
        }
    }
    private class CategoryDeleteListener implements AppDatabase.DbOperationCallbackListener{
        List<CategoryEntity> categoryEntities;

        public CategoryDeleteListener(List<CategoryEntity> categoryEntities) {
            this.categoryEntities = categoryEntities;
        }

        @Override
        public void onSuccess() {
            categoryRepository.insertAllCategories(categoryEntities);
        }
    }

    private class SearchSuggestionListener implements SearchSuggestionFragment.Listeners{
        @Override
        public void onSearchSuggestionClick(String str) {
            MainNewFragmentDirections.ActionMainNewFragmentToCustomTemplatesFragment actions =
                    MainNewFragmentDirections.actionMainNewFragmentToCustomTemplatesFragment(Constants.API_TYPE_SEARCH_TEMPLATES, getApiType(),str,false);
            navigate(actions);
        }


        @Override
        public void onSearchEnterClick(String str) {
            MainNewFragmentDirections.ActionMainNewFragmentToCustomTemplatesFragment actions =
                    MainNewFragmentDirections.actionMainNewFragmentToCustomTemplatesFragment(Constants.API_TYPE_SEARCH_TEMPLATES, getApiType(),str,false);
            navigate(actions);
        }

        @Override
        public void onBackClick() {
            getMainActivity().onBackPressed();
        }

    }
    private class HardRefreshDialogListener implements ConfirmationDialog.AlertDialogBtnClickListner{
        @Override
        public void onPositiveBtnClick(Dialog dialog) {
            getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_TEMPLATE_LIST_USAGE,FireConstants.EVENT_HARD_REFRESH);
            if(isMemesApiType()){
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(FragmentTags.HOME_TEMPLATES_FRAGMENT);
                if(fragment!=null && fragment.isAdded() && fragment instanceof MemesFragment){
                    MemesFragment memesFragment=(MemesFragment) fragment;
                    memesFragment.triggerHardRefresh();
                }
            }else{
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(FragmentTags.HOME_TEMPLATES_FRAGMENT);
                if(fragment!=null && fragment.isAdded() && fragment instanceof TemplatesFragment){
                    TemplatesFragment templatesFragment =(TemplatesFragment) fragment;
                    templatesFragment.triggerHardRefresh();
                }
            }

            dialog.dismiss();
        }

        @Override
        public void onNegativeBtnClick(Dialog dialog) {
            dialog.dismiss();
        }
    }
    private class MainNewToTemplatesHandShake implements TemplatesFragment.ParentHandShakes{
        @Override
        public void onDataEmpty() {
            viewHolder.mainNewSearchBtn.setVisibility(View.GONE);
            viewHolder.mainNewHardRefreshBtn.setVisibility(View.GONE);
        }

        @Override
        public void onData(int size) {
            viewHolder.mainNewSearchBtn.setVisibility(View.VISIBLE);
            viewHolder.mainNewHardRefreshBtn.setVisibility(View.VISIBLE);
        }

        @Override
        public void onRegionIdNull() {
            unAuthorizeSignOut(appPrefsRepository,windowViewModel,R.id.action_mainNewFragment_to_signInFragment);
        }

        @Override
        public void onCreateMemeClick(TemplateEntity templateEntity) {
            MainNewFragmentDirections.ActionMainNewFragmentToCreateMemeFragment action=
                    MainNewFragmentDirections.actionMainNewFragmentToCreateMemeFragment(GridNameConstants.L1,templateEntity.getImageUrl(),templateEntity.getAuthorId());
            navigate(action);
        }
    }

    private class NavDrawerMenuItemListener implements MenuItemsFragment.ListItemClickListener{
        @Override
        public void onListItemClick(int id) {
            switch (id){
                case MenuItemsData.ID_UPLOAD_TEMPLATES:
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_UPLOAD_TEMPLATE_USAGE,FireConstants.EVENT_UPLOAD_TEMPLATE_NEW);
                    MainNewFragmentDirections.ActionMainNewFragmentToUploadTemplateFragment action=
                            MainNewFragmentDirections.actionMainNewFragmentToUploadTemplateFragment(null,null,null,
                                    null,
                                    windowViewModel.getRegionId(),
                                    false,
                                    Constants.API_TYPE_TEMPLATES_FEED);
                    navigate(action);
                    hideNavDrawer();
                    break;
                case MenuItemsData.ID_MY_TEMPLATES:
                    hideNavDrawer();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_CATEGORY_USAGE,FireConstants.EVENT_MY_TEMPLATES_TAB);
                    appPrefsRepository.updateSelectedNavDrawerMenu(Constants.MY_TEMPLATES_MENUID);
                    break;
                case MenuItemsData.ID_FAV_TEMPLATES:
                    hideNavDrawer();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_CATEGORY_USAGE,FireConstants.EVENT_FAVOURITE_TEMPLATES_TAB);
                    appPrefsRepository.updateSelectedNavDrawerMenu(Constants.FAVORITES_MENUID);
                    break;
                case MenuItemsData.ID_TEMPLATE_GROUPS:
                    hideNavDrawer();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_CATEGORY_USAGE,FireConstants.EVENT_TEMPLATES_GROUP_TAB);
                    appPrefsRepository.updateSelectedNavDrawerMenu(Constants.TEMPLATES_GROUP_MENU_ID);
                    break;
                case MenuItemsData.ID_RATE_APP:
                    hideNavDrawer();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_TEMPLATE_LIST_USAGE,FireConstants.EVENT_RATE_APP);
                    getMainActivity().goToPlayStore(false);
                    break;
                case MenuItemsData.ID_SHARE_APP:
                    hideNavDrawer();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_TEMPLATE_LIST_USAGE,FireConstants.EVENT_SHARE_APP);
                    AppUtils.shareApp(getMainActivity(),getString(R.string.i_am_using_meme_king_to_create_memes));
                    break;
                case MenuItemsData.ID_SETTINGS:
                    hideNavDrawer();
                    navigate(R.id.action_mainNewFragment_to_settingsFragment);
                    break;
                case MenuItemsData.ID_MEMES:
                    hideNavDrawer();
                    appPrefsRepository.updateSelectedNavDrawerMenu(Constants.MEMES_FEED_MENUID);
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_CATEGORY_USAGE,FireConstants.EVENT_MEMES_TAB);
                    break;
                case MenuItemsData.ID_MY_MEMES:
                    hideNavDrawer();
                    appPrefsRepository.updateSelectedNavDrawerMenu(Constants.MY_MEMES_MENUID);
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_CATEGORY_USAGE,FireConstants.EVENT_MY_MEMES_TAB);
                    break;
                case MenuItemsData.ID_POST_MEME:
                    hideNavDrawer();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_CATEGORY_USAGE,FireConstants.EVENT_POST_MEMES_TAB);
                    pickMeme();
                    break;

            }
        }
    }

    private class PostMemeHandShake implements UploadMemePopupFragment.ParentHandShakes{
        @Override
        public void onHide() {
            hideUploadMemeFragment();
        }
    }
    private class MemesListHandShake implements MemesFragment.ParentHandShakes{
        @Override
        public void onDataEmpty() {
            viewHolder.mainNewSearchBtn.setVisibility(View.GONE);
            viewHolder.mainNewHardRefreshBtn.setVisibility(View.GONE);
        }

        @Override
        public void onData(int size) {
            viewHolder.mainNewSearchBtn.setVisibility(View.GONE);
            if(getApiType()==Constants.API_TYPE_MY_MEMES){
                viewHolder.mainNewHardRefreshBtn.setVisibility(View.VISIBLE);
            }else{
                viewHolder.mainNewHardRefreshBtn.setVisibility(View.GONE);
            }

        }

        @Override
        public void onRegionIdNull() {
            unAuthorizeSignOut(appPrefsRepository,windowViewModel,R.id.action_mainNewFragment_to_signInFragment);
        }
    }

    private class TemplatesGroupHandShake implements TemplatesGroupFragment.ParentHandShakes{
        @Override
        public void onDataEmpty() {
            viewHolder.mainNewSearchBtn.setVisibility(View.GONE);
            viewHolder.mainNewHardRefreshBtn.setVisibility(View.GONE);
        }

        @Override
        public void onData(int size) {
            viewHolder.mainNewSearchBtn.setVisibility(View.VISIBLE);
            viewHolder.mainNewHardRefreshBtn.setVisibility(View.GONE);
        }

        @Override
        public void onRegionIdNull() {
            unAuthorizeSignOut(appPrefsRepository,windowViewModel,R.id.action_mainNewFragment_to_signInFragment);
        }
    }

    private class TemplatesGroupClickListener implements TemplatesGroupFragment.ItemClickListener{
        @Override
        public void onClick(TemplatesGroupDataEntity templatesGroupDataEntity) {
            MainNewFragmentDirections.ActionMainNewFragmentToCustomTemplatesFragment actions =
                    MainNewFragmentDirections.actionMainNewFragmentToCustomTemplatesFragment(Constants.API_TYPE_SEARCH_TEMPLATES,Constants.API_TYPE_TEMPLATES_GROUP,templatesGroupDataEntity.getSearchStr(),true);
            navigate(actions);
        }
    }
    private class UpdateConfirmationDialogListener implements ConfirmationDialog.AlertDialogBtnClickListner{
        @Override
        public void onPositiveBtnClick(Dialog dialog) {
            dialog.dismiss();
            getMainActivity().goToPlayStore(true);
        }

        @Override
        public void onNegativeBtnClick(Dialog dialog) {
            dialog.dismiss();
        }
    }
    private class RemoteConfigListener implements FireRemoteConfig.ConfigListener{
        @Override
        public void onData(String key, HashMap configValuesMap) {
            switch (key){
                case FireConstants.REMOTE_CONFIG_REVIEW_KEY:
                    if(windowViewModel.getRegionId()!=null && configValuesMap.containsKey(windowViewModel.getRegionId()) ){
                        boolean isReviewEnabled = (boolean) configValuesMap.get(windowViewModel.getRegionId());
                        loadReviewContainer(isReviewEnabled);
                    }
                    break;
                case FireConstants.REMOTE_CONFIG_ADMIN_USERS:
                    if(configValuesMap.containsKey(FireConstants.REMOTE_CONFIG_IDS)){
                        try {
                            JSONArray jsonArray = (JSONArray)configValuesMap.get(FireConstants.REMOTE_CONFIG_IDS);
                            boolean isAdminUser=false;
                            for (int i=0;i<jsonArray.length();i++){
                                String currUserId = (String)jsonArray.get(i);
                                if(currUserId!=null && currUserId.equals(windowViewModel.getUserId())){
                                    isAdminUser=true;
                                    break;
                                }
                            }
                            if(isAdminUser){
                                appPrefsRepository.insertPref(AppPrefsEntity.IS_ADMIN_USER,"true");
                            }else{
                                appPrefsRepository.insertPref(AppPrefsEntity.IS_ADMIN_USER,null);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                    break;
            }

        }
    }

    /*classes end*/



}
