package com.thugdroid.memeking.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigValue;
import com.google.firebase.storage.UploadTask;
import com.thugdroid.memeking.MediaFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.adapters.CategorySpinnerAdapter;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.constants.TemplateValidation;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.firebasepack.storage.FireStorage;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.model.MyImage;
import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;
import com.thugdroid.memeking.room.entity.MyTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.room.repository.AppPrefsRepository;
import com.thugdroid.memeking.room.repository.CategoryRepository;
import com.thugdroid.memeking.ui.ConfirmationDialog;
import com.thugdroid.memeking.ui.InfoDialog;
import com.thugdroid.memeking.ui.LoadingDialog;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.utils.FileUtils;
import com.thugdroid.memeking.viewmodel.UploadTemplateFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;
import com.thugdroid.memeking.viewmodel.db.LoggedInUserDbViewModel;
import com.thugdroid.memeking.viewmodel.db.TemplateDbViewModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class UploadTemplateFragment extends MediaFragment {
    public static final int PICK_IMAGE_REQUEST_CODE=100;
    private String tempFolderUriStr;
    private ViewHolder viewHolder;
    private CategorySpinnerAdapter categorySpinnerAdapter;
    private CategoryRepository categoryRepository;
    private UploadTemplateFragmentViewModel uploadTemplateFragmentViewModel;
    private FireStorage fireStorage;
    private WindowViewModel windowViewModel;
    private LoadingDialog loadingDialog;
    private Handler searchTagsTimeOut;
    private AppPrefsRepository appPrefsRepository;
    private TemplateDbViewModel templateDbViewModel;
    private OnBackPressedCallback onBackPressedCallback;
    private LoggedInUserDbViewModel loggedInUserDbViewModel;
    private UploadTemplateFragmentArgs arguments;
    private RequestManager glide;
    /*override methods start*/
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public UploadTemplateFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_uploadtemplate,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
        initObservers();
        setTemplateData();
        fetchSearchExampleRemoteConfig();
    }




    @Override
    public void initVariables() {
        arguments=UploadTemplateFragmentArgs.fromBundle(getArguments());
        fireStorage=new FireStorage(getContext());
        glide=Glide.with(getContext());
        categoryRepository =new ViewModelProvider(this).get(CategoryRepository.class);
        uploadTemplateFragmentViewModel =new ViewModelProvider(this).get(UploadTemplateFragmentViewModel.class);
        appPrefsRepository =new ViewModelProvider(this).get(AppPrefsRepository.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        templateDbViewModel=new ViewModelProvider(this).get(TemplateDbViewModel.class);
        loggedInUserDbViewModel=new ViewModelProvider(this).get(LoggedInUserDbViewModel.class);
        categorySpinnerAdapter=new CategorySpinnerAdapter(getContext(), uploadTemplateFragmentViewModel.getCategoryEntities());
        tempFolderUriStr=getContext().getCacheDir()+ File.separator+Constants.TEMP_IMAGES_FOLDER_NAME;
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
        viewHolder.categorySpinner.setAdapter(categorySpinnerAdapter);
    }

    @Override
    public void initListeners() {
        viewHolder.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CategoryEntity categoryEntity=categorySpinnerAdapter.getCategory(position);
                uploadTemplateFragmentViewModel.setSelectedCategoryId(categoryEntity.id);
                changePositiveBtn();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        viewHolder.description.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(searchTagsTimeOut==null){
                    searchTagsTimeOut=new Handler();
                }
                searchTagsTimeOut.removeCallbacksAndMessages(null);
                searchTagsTimeOut.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        changePositiveBtn();
                    }
                },Constants.SEARCH_TIMEOUT);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        viewHolder.addTemplateButton.setOnClickListener(this::onClick);
        viewHolder.previewImage.setOnLongClickListener(new ImageLongPressListener());
        viewHolder.positiveBtn.setOnClickListener(this::onClick);
        viewHolder.positiveBtnDisabled.setOnClickListener(this::onClick);
        viewHolder.back.setOnClickListener(this::onClick);
        onBackPressedCallback=new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                if(viewHolder.cropFragmentContainer.getVisibility()==View.VISIBLE){
                    hideCropFragment();
                }
                onBackPressedCallback.setEnabled(false);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);
    }

    @Override
    public void initObservers() {
        categoryRepository.getTemplateUploadAbleCategoriesAsLiveData(arguments.getRegionId()).observe(getViewLifecycleOwner(), new Observer<List<CategoryEntity>>() {
            @Override
            public void onChanged(List<CategoryEntity> paramCategoryEntities) {
                if(paramCategoryEntities!=null && paramCategoryEntities.size()>0){
                    showMainContainer();
                    uploadTemplateFragmentViewModel.getCategoryEntities().clear();
                    uploadTemplateFragmentViewModel.getCategoryEntities().addAll(paramCategoryEntities);
                    uploadTemplateFragmentViewModel.getCategoryEntities().add(0,new CategoryEntity(CategoryEntity.DROPDOWN_NONE,
                            getString(R.string.select_a_category),
                            null,
                            arguments.getRegionId(),
                            true));
                    categorySpinnerAdapter.notifyDataSetChanged();
                    for (CategoryEntity paramCategoryEntity : paramCategoryEntities) {
                        uploadTemplateFragmentViewModel.getCategoryIdNameMap().put(paramCategoryEntity.getId(),paramCategoryEntity.getName());
                    }
                    setCategory(paramCategoryEntities);
                }else{
                    getCategories();
                }
            }
        });
        uploadTemplateFragmentViewModel.getCategoryApiModel().observe(getViewLifecycleOwner(), new Observer<ApiModel>() {
            @Override
            public void onChanged(ApiModel apiModel) {
                if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST){
                    showLoading();
                }else if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_FAILURE) {
                    if (apiModel.getStatusCode() == HttpCodes.NOINTERNETCONNECTION) {
                        customShowNoInternetConnection();
                    } else {
                        if(apiModel.getStatusCode()==HttpCodes.MESSAGE){
                            showMsg(apiModel.getErrorMessage());
                        }
                        customShowSomethingWentWrong();
                    }
                }
            }
        });
        uploadTemplateFragmentViewModel.getTemplateImage().observe(getViewLifecycleOwner(), new Observer<MyImage>() {
            @Override
            public void onChanged(MyImage myImage) {
                if(myImage.getUri()==null){
                    viewHolder.previewImage.setVisibility(View.GONE);
                    viewHolder.addTemplateButton.setVisibility(View.VISIBLE);
                }else{
                    viewHolder.previewImage.setVisibility(View.VISIBLE);
                    viewHolder.addTemplateButton.setVisibility(View.GONE);
                    glide.load(myImage.getUri()).into(viewHolder.previewImage);
                }
                changePositiveBtn();
            }
        });
        uploadTemplateFragmentViewModel.getImageUploadApiModel().observe(getViewLifecycleOwner(), new Observer<ApiModel>() {
            @Override
            public void onChanged(ApiModel apiModel) {
                if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_SUCCESS){
                    if(uploadTemplateFragmentViewModel.getIsUploadBtnClicked().getValue()){
                        addTemplate();
                    }
                }else if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_FAILURE){
                    if(uploadTemplateFragmentViewModel.getIsUploadBtnClicked().getValue()){
                        uploadTemplateFragmentViewModel.getIsUploadBtnClicked().setValue(false);
                        showMsg(R.string.cant_upload_template_try_again);
                    }
                }
            }
        });
        uploadTemplateFragmentViewModel.getAddTemplateApiModel().observe(getViewLifecycleOwner(), new Observer<ApiModel>() {
            @Override
            public void onChanged(ApiModel apiModel) {
                if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_FAILURE){
                    hideLoadingDialog();
                    uploadTemplateFragmentViewModel.getIsUploadBtnClicked().setValue(false);
                    if(apiModel.getStatusCode()==HttpCodes.BLOCKED){
                        LoggedInUserEntity loggedInUserEntity=windowViewModel.getLoggedInUserEntity();
                        loggedInUserEntity.setStatus(Constants.USER_STATUS_BLOCKED);
                        windowViewModel.setLoggedInUserEntity(loggedInUserEntity);
                        loggedInUserDbViewModel.insert(loggedInUserEntity);
                        showMsg(R.string.user_blocked_msg);
                    }else if(apiModel.getStatusCode()==HttpCodes.UNAUTHORIZED){
                        unAuthorizeSignOut(appPrefsRepository,windowViewModel,R.id.action_uploadTemplateFragment_to_signInFragment);
                    }else if(apiModel.getStatusCode()==HttpCodes.MESSAGE){
                        showMsg(apiModel.getErrorMessage());
                    }
                    else{
                        if(arguments.getId()==null){
                            showMsg(R.string.cant_upload_template_try_again);
                        }else{
                            showMsg(R.string.cant_update_template_try_again);
                        }
                    }

                }else if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_SUCCESS){
                    hideLoadingDialog();
                    if(arguments.getId()==null){
                        clearTemplateDetails();
                        showMsg(R.string.template_verification_under_progress);
                    }else{
                        showMsg(R.string.template_updated_successfully);
                        getMainActivity().onBackPressed();
                    }

                }
            }
        });
        uploadTemplateFragmentViewModel.getIsUploadBtnClicked().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isUploadBtnClicked) {
                if(isUploadBtnClicked){
                    showLoadingDialog(R.string.uploading_dots);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        recordScreenView();
    }
    private void recordScreenView() {
        recordScreen(FireConstants.SCREEN_UPLOAD_TEMPLATE);
    }
    private void showLoadingDialog(int resId){
        if(loadingDialog==null){
            loadingDialog=new LoadingDialog(getMainActivity());
            if(arguments.getId()==null){
                loadingDialog.setLoadingText(getString(resId));
            }else {
                loadingDialog.setLoadingText(getString(R.string.updating_dots));
            }

        }
        if(!loadingDialog.isShowing()){
            loadingDialog.show();
        }
    }
    private void hideLoadingDialog(){
        if(loadingDialog!=null){
            loadingDialog.dismiss();
        }
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.uploadTemplateAdd:
                if(AppUtils.isBlockedUser(windowViewModel.getLoggedInUserEntity())){
                    showMsg(R.string.user_blocked_msg);
                }else {
                    pickImage();
                }
                break;
            case R.id.uploadTemplatePositiveBtnInactive:
                isValidTemplate(true);
                break;
            case R.id.uploadTemplatePositiveBtn:
                if(isValidTemplate(true)){
                    if(AppUtils.hasInternetConnection(getContext())){
                        if(AppUtils.isBlockedUser(windowViewModel.getLoggedInUserEntity())){
                            showMsg(R.string.user_blocked_msg);
                        }else{
                            uploadTemplateFragmentViewModel.getIsUploadBtnClicked().setValue(true);
                            ApiModel apiModel= uploadTemplateFragmentViewModel.getImageUploadApiModel().getValue();
                            if(apiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST){
                                addTemplate();
                            }
                        }

                    }else{
                        showMsg(R.string.no_internet_connection);
                    }
                }
                break;
            case R.id.uploadTemplateBackParent:
                getMainActivity().onBackPressed();
                break;
        }
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
//                    viewHolder.description.setText(uploadTemplateFragmentViewModel.getCategoryIdNameMap().get(uploadTemplateFragmentViewModel.getSelectedCategoryId())+","+imageDetails.getFilename().replace(".jpg","")
//                            .replace(".jpeg",""));
                    String mimeType=imageDetails.getMimeType();
                    if(mimeType!=null && AppUtils.getIndexOf(Constants.IMAGE_ACCEPTABLE_MIME_TYPES,mimeType)!=-1){
                        if(AppUtils.bytesToMb(imageDetails.getSize())>Constants.UPLOAD_TEMPLATE_MAX_SIZE_IN_MB){
                            showMsg(R.string.image_size_exeeds_in_mb,Constants.UPLOAD_TEMPLATE_MAX_SIZE_IN_MB);
                        }else{
                            if(AppUtils.bytesToKb(imageDetails.getSize())>Constants.UPLOAD_TEMPLATE_MAX_SIZE_IN_KB){
                                showTempImageParent(imageDetails.getUri());
                            }else{
                                showCropFragment(imageDetails.getUri());
                            }
                        }

                    }else{
                        showMsg(R.string.jpg_png_images_allowed);
                    }
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        try{
            new ClearTempImageCache().execute();
        }catch (Exception e){
            e.printStackTrace();
        }
        onBackPressedCallback.setEnabled(false);
        super.onDestroyView();
    }

    /*override methods end*/

    /*other methods start*/
    private class TempImageParentHandShakes implements TempImageFragment.ParentHandShakes{
        @Override
        public void onImageSaved(String uriString) {
            hideTempImageParent();
            showCropFragment(Uri.parse(uriString));
        }

        @Override
        public void onImageSaveFailed() {
            showCantLoadTempImage();
        }
    }
    private void showTempImageParent(Uri uri){
        viewHolder.tempImageSaverParent.setVisibility(View.VISIBLE);
        TempImageFragment tempImageFragment=TempImageFragment.newInstance(uri,new TempImageParentHandShakes());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.uploadTemplateTempImageContainer,tempImageFragment).commit();
    }
    private void hideTempImageParent(){
        viewHolder.tempImageSaverParent.setVisibility(View.GONE);
    }

    private void showCantLoadTempImage(){
        hideTempImageParent();
        showMsg(R.string.can_t_load_image);
    }

    private void showLoading(){
        hideMainContainer();
        hideNoInternetConnection();
        hideSomethingWentWrong();
        viewHolder.loadingContainer.setVisibility(View.VISIBLE);
    }
    private void showMainContainer(){
        hideLoading();
        hideNoInternetConnection();
        hideSomethingWentWrong();
        viewHolder.mainContainer.setVisibility(View.VISIBLE);
    }
    private void hideMainContainer(){
        viewHolder.mainContainer.setVisibility(View.GONE);
    }
    private void hideLoading(){
        viewHolder.loadingContainer.setVisibility(View.GONE);
    }

    private void setTemplateData(){
        if(arguments.getSearchTags()!=null){
            viewHolder.description.setText(arguments.getSearchTags());
        }
        if(arguments.getImageUrl()!=null){
            MyImage myImage=new MyImage();
            Uri uri=Uri.parse(arguments.getImageUrl());
            myImage.setWebUri(uri);
            myImage.setUri(uri);
            uploadTemplateFragmentViewModel.getTemplateImage().setValue(myImage);
        }
        if(arguments.getId()!=null){
            viewHolder.positiveBtn.setText(getString(R.string.update_template));
            viewHolder.positiveBtnDisabled.setText(getString(R.string.update_template));
        }
    }
    private void setCategory(List<CategoryEntity> categoryEntities){
        if(arguments.getCategoryId()!=null){
            for (int i=0;i<categoryEntities.size();i++){
                CategoryEntity categoryEntity=categoryEntities.get(i);
                if(categoryEntity.getId().equals(arguments.getCategoryId())){
                    /*Because of default  placeholer(select a category) we need to add +1*/
                    viewHolder.categorySpinner.setSelection(i+1);
                    break;
                }
            }
        }
    }
    private void customShowNoInternetConnection(){
        hideMainContainer();
        hideLoading();
        hideSomethingWentWrong();
        showNoInternetConnection(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCategories();
            }
        });
    }
    private void customShowSomethingWentWrong(){
        hideMainContainer();
        hideLoading();
        hideNoInternetConnection();
        showSomethingWentWrong(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCategories();
            }
        },windowViewModel.getRegionId());
    }
    private void showCropFragment(Uri uri){
        onBackPressedCallback.setEnabled(true);
        viewHolder.cropFragmentContainer.setVisibility(View.VISIBLE);
        CropFragment cropFragment=new CropFragment();
        cropFragment.setUri(uri);
        cropFragment.setListener(new CropListener());
        getSupportFragmentManager().beginTransaction().replace(R.id.uploadTemplateCropFragmentContainer,cropFragment).commit();
    }
    private void hideCropFragment(){
        viewHolder.cropFragmentContainer.setVisibility(View.GONE);
        onBackPressedCallback.setEnabled(false);
    }
    private boolean isValidTemplate(boolean showAlert){
        if(uploadTemplateFragmentViewModel.getSelectedCategoryId().equals(CategoryEntity.DROPDOWN_NONE)){
            if(showAlert){
                showMsg(R.string.please_choose_category);
                viewHolder.categorySpinner.performClick();
            }
            return false;
        }
        String description= viewHolder.description.getText().toString();
        if(!TemplateValidation.isValidSearchTag(description)){
            if(showAlert){
                if(description.trim().length()==0){
                    showMsg(R.string.enter_atlease_x_letter,1);
                }else{
                    showMsg(R.string.please_enter_valid_searchtag);
                }
                AppUtils.focusEditText(getContext(),viewHolder.description);
            }
            return false;
        }
        Uri imageUri= uploadTemplateFragmentViewModel.getTemplateImage().getValue().getUri();
        if(imageUri==null){
            if(showAlert){
                showMsg(R.string.please_choose_image);
                pickImage();
            }
            return  false;
        }

        return true;
    }
    private void changePositiveBtn(){
        if(isValidTemplate(false)){
            enablePositiveBtn();
        }else{
            enableNegativeBtn();
        }
    }
    private void enablePositiveBtn(){
        viewHolder.positiveBtn.setVisibility(View.VISIBLE);
        viewHolder.positiveBtnDisabled.setVisibility(View.GONE);
    }
    private void enableNegativeBtn(){
        viewHolder.positiveBtn.setVisibility(View.GONE);
        viewHolder.positiveBtnDisabled.setVisibility(View.VISIBLE);
    }
    private void pickImage(){
        if(isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE,PICK_IMAGE_REQUEST_CODE)){
            openGallery(PICK_IMAGE_REQUEST_CODE);
        }
    }
    private void setImageUploadApiModel(int loadingState,int statusCode){
        ApiModel apiModel= uploadTemplateFragmentViewModel.getImageUploadApiModel().getValue();
        apiModel.setLoadingState(loadingState);
        apiModel.setStatusCode(statusCode);
        uploadTemplateFragmentViewModel.getImageUploadApiModel().setValue(apiModel);
    }
    private void setAddTemplateApiModel(int loadingState,int statusCode,String msg){
        ApiModel apiModel= uploadTemplateFragmentViewModel.getAddTemplateApiModel().getValue();
        apiModel.setLoadingState(loadingState);
        apiModel.setStatusCode(statusCode);
        apiModel.setErrorMessage(msg);
        uploadTemplateFragmentViewModel.getAddTemplateApiModel().setValue(apiModel);
    }
    private void setAddTemplateApiModel(int loadingState,int statusCode){
        ApiModel apiModel= uploadTemplateFragmentViewModel.getAddTemplateApiModel().getValue();
        apiModel.setLoadingState(loadingState);
        apiModel.setStatusCode(statusCode);
        uploadTemplateFragmentViewModel.getAddTemplateApiModel().setValue(apiModel);
    }
    private void showChangeImageAlertDialog(){
        ConfirmationDialog confirmationDialog =new ConfirmationDialog(getContext());
        confirmationDialog.setTitle(getString(R.string.do_you_want_to_change_template_image));
        confirmationDialog.setOnClickListener(new ChangeImageListener());
        confirmationDialog.show();
    }
    private void showInfoDialog(){
        InfoDialog  infoDialog=new InfoDialog(getContext());
        infoDialog.setTitle(getString(R.string.you_cant_change_template_image));
        infoDialog.show();
    }
    private void clearTemplateDetails(){
        viewHolder.description.setText("");
        uploadTemplateFragmentViewModel.getIsUploadBtnClicked().setValue(false);
        uploadTemplateFragmentViewModel.getAddTemplateApiModel().setValue(new ApiModel());
        uploadTemplateFragmentViewModel.getImageUploadApiModel().setValue(new ApiModel());
        uploadTemplateFragmentViewModel.getTemplateImage().setValue(new MyImage());
    }
    private void setCategoryApiModel(int loadingState,int statusCode,String msg){
        ApiModel categoryApiModel=uploadTemplateFragmentViewModel.getCategoryApiModel().getValue();
        categoryApiModel.setStatusCode(statusCode);
        categoryApiModel.setLoadingState(loadingState);
        categoryApiModel.setErrorMessage(msg);
        uploadTemplateFragmentViewModel.getCategoryApiModel().setValue(categoryApiModel);
    }
    private void setCategoryApiModel(int loadingState,int statusCode){
        ApiModel categoryApiModel=uploadTemplateFragmentViewModel.getCategoryApiModel().getValue();
        categoryApiModel.setStatusCode(statusCode);
        categoryApiModel.setLoadingState(loadingState);
        uploadTemplateFragmentViewModel.getCategoryApiModel().setValue(categoryApiModel);
    }
    private void fetchSearchExampleRemoteConfig(){
        FirebaseRemoteConfig firebaseRemoteConfig=FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(FireConstants.REMOTE_CONFIG_INTERVAL)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(getMainActivity(), new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<Boolean> task) {
                if(task.isSuccessful()){
                    try {
                        FirebaseRemoteConfigValue firebaseRemoteConfigValue
                                =firebaseRemoteConfig.getValue(FireConstants.REMOTE_CONFIG_SEARCH_EXAMPLE_KEY);
                        if(firebaseRemoteConfigValue!=null
                                && !"".equals(firebaseRemoteConfigValue.asString())){
                            JSONObject resultObj
                                    = new JSONObject(firebaseRemoteConfigValue.asString());
                            if(windowViewModel.getRegionId()!=null){
                                if(resultObj.has(windowViewModel.getRegionId())){
                                    String searchExample = resultObj.getString(windowViewModel.getRegionId());
                                    viewHolder.uploadSearchExampleTv.setText(searchExample);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    /*other methods end*/
    /*api callers start*/
    private void getCategories(){
        if(uploadTemplateFragmentViewModel.getCategoryApiModel().getValue().getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST){
            if(!AppUtils.hasInternetConnection(getContext())){
                setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.NOINTERNETCONNECTION);
            }else{
                setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST,HttpCodes.IDLE);
                HashMap apiData = new HashMap();
                apiData.put(ApiConstants.KEY_REGION_ID,arguments.getRegionId());
                getFireFunctions().callApi(ApiUrls.GET_CATEGORIES,apiData,new CategoryApiListener());
            }
        }
    }
    private int getTemplateType(){
        return arguments.getTemplateType();
    }
    private void addTemplate(){
        if(AppUtils.hasInternetConnection(getContext())){
            if(arguments.getId()==null){
                String categoryId= uploadTemplateFragmentViewModel.getSelectedCategoryId();
                String regionId=arguments.getRegionId();
                String imageUrl= uploadTemplateFragmentViewModel.getTemplateImage().getValue().getWebUri().toString();
                HashMap<String,String > data = new HashMap<>();
                data.put(TemplateEntity.APIKEY_CATEGORY_ID,categoryId);
                data.put(TemplateEntity.APIKEY_REGION_ID,regionId);
                data.put(TemplateEntity.APIKEY_IMGURL,imageUrl);
                data.put(TemplateEntity.APIKEY_SEARCHTAGS,viewHolder.description.getText().toString());
                getFireFunctions().callApi(ApiUrls.ADD_TEMPLATE,data,new AddTemplateListener());
            }else{
                String prevCategoryId=arguments.getCategoryId();
                String prevSearchTags =arguments.getSearchTags();
                String categoryId=uploadTemplateFragmentViewModel.getSelectedCategoryId();
                String searchTags = viewHolder.description.getText().toString();
                if(prevCategoryId!=null && prevSearchTags!=null
                        && prevCategoryId.equals(categoryId) && prevSearchTags.trim().equals(searchTags.trim())){
                    hideLoadingDialog();
                    setAddTemplateApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,HttpCodes.SUCCESS);
                    return;
                }
                HashMap<String,String > apiData = new HashMap<>();
                apiData.put(TemplateEntity.APIKEY_ID,arguments.getId());
                apiData.put(TemplateEntity.APIKEY_SEARCHTAGS,searchTags);
                if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
                    apiData.put(ApiConstants.KEY_TEMPLATETYPE,ApiConstants.FAV_TEMPLATETYPE);
                }
                apiData.put(TemplateEntity.APIKEY_CATEGORY_ID,uploadTemplateFragmentViewModel.getSelectedCategoryId());
                getFireFunctions().callApi(ApiUrls.UPDATE_TEMPLATE,apiData,new AddTemplateListener());
            }

        }else {
            showMsg(R.string.no_internet_connection);
        }
    }

    private void uploadImage(){
        if(AppUtils.hasInternetConnection(getContext())){
            setImageUploadApiModel(ApiModel.LOADINGSTATE_REQUEST,HttpCodes.IDLE);
            MyImage myImage= uploadTemplateFragmentViewModel.getTemplateImage().getValue();
            String fileName = uploadTemplateFragmentViewModel.getCategoryIdNameMap().get(uploadTemplateFragmentViewModel.getSelectedCategoryId());
            fireStorage.uploadImage(myImage.getUri(),Constants.TEMPLATES_FIRESTOREPATH+"/"+windowViewModel.getLoggedInUserEntity().getId(),myImage.getExtension(),new ImageUploadListener(myImage));
        }
    }
    /*api callers end*/

    /*custom listeners start*/
    private class ViewHolder{
        Spinner categorySpinner;
        ImageView previewImage;
        Button addTemplateButton;
        EditText description;
        Button positiveBtn,positiveBtnDisabled;
        View back;
        View cropFragmentContainer,tempImageSaverParent;
        ConstraintLayout loadingContainer,mainContainer;
        TextView uploadSearchExampleTv;
        ViewHolder(){
            previewImage=findViewById(R.id.templatePreviewImage);
            categorySpinner=findViewById(R.id.uploadTemplateCategorySpinner);
            addTemplateButton=findViewById(R.id.uploadTemplateAdd);
            description=findViewById(R.id.uploadTemplateSearchDescription);
            positiveBtn=findViewById(R.id.uploadTemplatePositiveBtn);
            positiveBtnDisabled=findViewById(R.id.uploadTemplatePositiveBtnInactive);
            back=findViewById(R.id.uploadTemplateBackParent);
            cropFragmentContainer=findViewById(R.id.uploadTemplateCropFragmentContainer);
            loadingContainer=findViewById(R.id.uploadTemplateLoadingParent);
            mainContainer=findViewById(R.id.uploadTemplateContentParent);
            uploadSearchExampleTv=findViewById(R.id.uploadSearchExampleTv);
            tempImageSaverParent=findViewById(R.id.uploadTemplateTempImageContainer);
        }
    }

    private class ClearTempImageCache extends AsyncTask{
        @Override
        protected Object doInBackground(Object[] objects) {
            FileUtils.deleteDir(new File(tempFolderUriStr));
            return null;
        }
    }

    private class ImageUploadListener implements FireStorage.FileUploadListener{
        MyImage tempImage;

        public ImageUploadListener(MyImage tempImage) {
            this.tempImage = tempImage;
        }

        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot, Uri uri) {
            MyImage myImage= uploadTemplateFragmentViewModel.getTemplateImage().getValue();
            if(tempImage.getUri().toString().equals(myImage.getUri().toString())){
                myImage.setWebUri(uri);
                uploadTemplateFragmentViewModel.getTemplateImage().setValue(myImage);
                setImageUploadApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,HttpCodes.SUCCESS);
            }
        }

        @Override
        public void onFail(Exception e) {
            MyImage myImage= uploadTemplateFragmentViewModel.getTemplateImage().getValue();
            if(tempImage.getUri().toString().equals(myImage.getUri().toString())){
                setImageUploadApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.INTERNALSERVERERROR);
            }
        }
    }

    private class AddTemplateListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            if(statusCode==HttpCodes.SUCCESS){
                if(arguments.getId()!=null){
                    TemplateEntity templateEntity = TemplateEntity.getEntity(resultObject);
                    templateDbViewModel.updateTemplate(templateEntity, new AppDatabase.DbOperationCallbackListener() {
                        @Override
                        public void onSuccess() {
                            setAddTemplateApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode);
                        }
                    });
                }else{
                    setAddTemplateApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode);
                }
            }else{
                if(statusCode==HttpCodes.MESSAGE){
                    setAddTemplateApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,statusCode,getErrorMsgFromApiData(resultObject));
                }else {
                    setAddTemplateApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,statusCode);
                }

            }
        }

        @Override
        public void onFailure(Exception e) {
            setAddTemplateApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.INTERNALSERVERERROR);
        }
    }

    private class ChangeImageListener implements  ConfirmationDialog.AlertDialogBtnClickListner{
        @Override
        public void onPositiveBtnClick(Dialog dialog) {
            dialog.dismiss();
            pickImage();
        }

        @Override
        public void onNegativeBtnClick(Dialog dialog) {
            dialog.dismiss();
        }
    }

    private class ImageLongPressListener implements View.OnLongClickListener{
        @Override
        public boolean onLongClick(View v) {
            if(arguments.getId()==null){
                showChangeImageAlertDialog();
            }else{
                showInfoDialog();
            }
            return true;
        }
    }
    private class CategoryApiListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            if(statusCode== HttpCodes.SUCCESS){
                setCategoryApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode);
                List<CategoryEntity> categoryEntities= CategoryEntity.getEntityList(resultObject);
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

    private void setMyImageAndUpload(Uri uri){
        if(arguments.getId()==null){
            MyImage myImage=AppUtils.getDetailedImage(getContext(),uri);
            uploadTemplateFragmentViewModel.getTemplateImage().setValue(myImage);
            hideCropFragment();
            uploadImage();
        }
    }
    private class CropListener implements CropFragment.Listener{
        @Override
        public void onBackClick() {
            getMainActivity().onBackPressed();
        }

        @Override
        public void onCrop(Uri uri) {
            setMyImageAndUpload(uri);
        }

        @Override
        public void onCropFailed(Uri uri) {
            setMyImageAndUpload(uri);
        }

        @Override
        public void onDontCropClick(Uri uri) {
            setMyImageAndUpload(uri);
        }
    }

    /*custom listeners end*/

}
