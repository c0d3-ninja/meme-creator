package com.thugdroid.memeking.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.thugdroid.libs.collagegrid.constants.GridNameConstants;
import com.thugdroid.memeking.MediaFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.FavTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.room.repository.AppPrefsRepository;
import com.thugdroid.memeking.ui.ConfirmationDialog;
import com.thugdroid.memeking.ui.LoadingDialog;
import com.thugdroid.memeking.ui.ReportTemplateDialog;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.viewmodel.TemplatePreviewFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;
import com.thugdroid.memeking.viewmodel.db.LoggedInUserDbViewModel;
import com.thugdroid.memeking.viewmodel.db.SearchTemplateFragmentVariableDb;
import com.thugdroid.memeking.viewmodel.db.TemplateDbViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TemplatePreviewFragment extends MediaFragment {
    private TemplateEntity templateEntity;
    private ViewHolder viewHolder;
    private TemplatePreviewFragmentViewModel templatePreviewFragmentViewModel;
    private TemplateDbViewModel templateDbViewModel;
    private WindowViewModel windowViewModel;
    private int templateType;
    private LoadingDialog loadingDialog;
    private OnBackPressedCallback onBackPressedCallback;
    private SearchTemplateFragmentVariableDb searchTemplateFragmentVariableDb;
    private AppPrefsRepository appPrefsRepository;
    private LoggedInUserDbViewModel loggedInUserDbViewModel;

    private boolean isFromSearch;
    private static final int PERMISSION_SAVE_TEMPLATE=4;

    private RequestManager glide;
    private boolean isImageLoaded;
    private Context context;
    private boolean hasFavTemplatesInLocal;
    private TemplatePreviewFragmentArgs args;
    /*override methods start*/
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public TemplatePreviewFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_previewtemplate,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRootView(view);
        initVariables();
    }

    @Override
    public void initVariables() {
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        appPrefsRepository =new ViewModelProvider(this).get(AppPrefsRepository.class);
        if(windowViewModel.getUserId()==null){
            unAuthorizeSignOut(appPrefsRepository,windowViewModel,R.id.action_templatePreviewFragment_to_signInFragment);
        }else{
            glide =Glide.with(getContext());
            context=getContext();
             args=TemplatePreviewFragmentArgs.fromBundle(getArguments());
            templateEntity=new TemplateEntity(args.getId(),args.getSearchTags(),
                    args.getImageUrl(),args.getCategoryId(),
                    args.getIsFavorite(),args.getCreatedBy(),
                    args.getCreatedTime(),args.getRegionId(),args.getAuthorId());
            templateType=args.getTemplateType();
            isFromSearch=args.getIsFromSearch();
            templatePreviewFragmentViewModel=new ViewModelProvider(this).get(TemplatePreviewFragmentViewModel.class);
            templatePreviewFragmentViewModel.getTemplateEntity().setValue(templateEntity);
            templateDbViewModel=new ViewModelProvider(this).get(TemplateDbViewModel.class);
            loggedInUserDbViewModel=new ViewModelProvider(this).get(LoggedInUserDbViewModel.class);
            searchTemplateFragmentVariableDb=new ViewModelProvider(requireActivity()).get(SearchTemplateFragmentVariableDb.class);
            initViews(getRootView());
            initListeners();
            initObservers();
            loadPreviewImage();
            insertTemplateCreditsFragment();
        }
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
        switch (getTemplateType()){
            case Constants.API_TYPE_FAV_TEMPLATES:
                viewHolder.deleteBtn.setVisibility(View.GONE);
                viewHolder.reportBtn.setVisibility(View.GONE);
                break;
            case Constants.API_TYPE_MY_TEMPLATES:
                viewHolder.reportBtn.setVisibility(View.GONE);
                break;
            case Constants.API_TYPE_SEARCH_TEMPLATES:
            case Constants.API_TYPE_TEMPLATES_FEED:
                if(templateEntity.getCreatedBy()!=null && templateEntity.getCreatedBy().equals(windowViewModel.getUserId())){
                    viewHolder.reportBtn.setVisibility(View.GONE);
                }else{
                    viewHolder.deleteBtn.setVisibility(View.GONE);
                    viewHolder.editBtn.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void initListeners() {
        (findViewById(R.id.bsCreateMeme)).setOnClickListener(this::onClick);
        viewHolder.favoriteBtn.setOnClickListener(this::onClick);
        (findViewById(R.id.bsTemplateDownload)).setOnClickListener(this::onClick);
        (findViewById(R.id.previewTemplateBack)).setOnClickListener(this::onClick);
        viewHolder.editBtn.setOnClickListener(this::onClick);
        viewHolder.deleteBtn.setOnClickListener(this::onClick);
        viewHolder.reportBtn.setOnClickListener(this::onClick);
        (findViewById(R.id.bsTemplateSearchTags)).setOnClickListener(this::onClick);
        viewHolder.freezeLayer.setOnClickListener(this::onClick);
        onBackPressedCallback=new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(viewHolder.moreInfoContainer.getVisibility()==View.VISIBLE){
                    hideMoreInfo();
                }else{
                    onBackPressedCallback.setEnabled(false);
                    getMainActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);


    }

    @Override
    public void initObservers() {
        templatePreviewFragmentViewModel.getTemplateEntity().observe(getViewLifecycleOwner(), new Observer<TemplateEntity>() {
            @Override
            public void onChanged(TemplateEntity templateEntity) {
                if(templateEntity!=null){
                    if(templateEntity.isFavorite()){
                        viewHolder.favIcon.setImageResource(R.drawable.ic_star_grey_24dp);
                        viewHolder.favText.setText(getString(R.string.unfavourite));
                    }else{
                        viewHolder.favIcon.setImageResource(R.drawable.ic_star_border_grey_24dp);
                        viewHolder.favText.setText(getString(R.string.favourite));
                    }
                }
            }
        });
        LiveData<List<TemplateEntity>> favTemplateLiveData = templateDbViewModel.getFavoriteTemplatesAsLiveData(1);
        favTemplateLiveData.observe(getViewLifecycleOwner(), new Observer<List<TemplateEntity>>() {
            @Override
            public void onChanged(List<TemplateEntity> templateEntities) {
                favTemplateLiveData.removeObservers(getViewLifecycleOwner());
                hasFavTemplatesInLocal=templateEntities.size()>0;
            }
        });
        LiveData<String> adminUserLiveData = appPrefsRepository.getPref(AppPrefsEntity.IS_ADMIN_USER);
        adminUserLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String value) {
                adminUserLiveData.removeObservers(getViewLifecycleOwner());
                if(value!=null){
                    viewHolder.watermark.setImageAlpha(0);
                    viewHolder.appNameText.setText("");
                }
            }
        });

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bsCreateMeme:
                gotoCreateMemeFragment(templatePreviewFragmentViewModel.getTemplateEntity().getValue().getImageUrl());
                break;
            case R.id.bsAddToFavorites:
                /*allowing user to unfavorite*/
                if(!AppUtils.isBlockedUser(windowViewModel.getLoggedInUserEntity()) || templateEntity.isFavorite()){
                    callFavApi();
                }else {
                    showMsg(R.string.user_blocked_msg);
                }
                break;
            case R.id.bsTemplateDownload:
                saveImage();
                break;
            case R.id.previewTemplateFreezeLayer:
            case R.id.previewTemplateBack:
                getMainActivity().onBackPressed();
                break;
            case R.id.bsTemplateEdit:
                if(AppUtils.isBlockedUser(windowViewModel.getLoggedInUserEntity())){
                    showMsg(R.string.user_blocked_msg);
                }else {
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_UPLOAD_TEMPLATE_USAGE,FireConstants.EVENT_UPLOAD_TEMPLATE_EDIT);
                    TemplatePreviewFragmentDirections.ActionTemplatePreviewFragmentToUploadTemplateFragment action=
                            TemplatePreviewFragmentDirections.actionTemplatePreviewFragmentToUploadTemplateFragment(
                                    templateEntity.getId(),
                                    templateEntity.getSearchTags(),
                                    templateEntity.getImageUrl(),
                                    templateEntity.getCategoryId(),
                                    templateEntity.getRegionId(),
                                    templateEntity.isFavorite(),
                                    getTemplateType()
                            );
                    navigate(action);
                }
                break;
            case R.id.bsTemplateDelete:
                showDeleteTemplateDialog();
                break;
            case R.id.bsTemplateReport:
                showReportTemplateDialog();
                break;
            case R.id.bsTemplateSearchTags:
                showMoreInfo();
                break;



        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PERMISSION_SAVE_TEMPLATE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveImage();
            }else{
                showMsg(R.string.allow_permission_to_save_template);
            }
        }
    }

    @Override
    public void onDestroyView() {
        //unauthorized signout
        if(onBackPressedCallback!=null){
            onBackPressedCallback.setEnabled(false);
        }
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        recordScreenView();
    }

    /*override methods end*/


    /*other methods start*/
    private void insertTemplateCreditsFragment(){
        TemplateCreditsFragment templateCreditsFragment=TemplateCreditsFragment.newInstance(templateEntity.getAuthorId(),new TemplateCreditsHandshakes());
        getSupportFragmentManager().beginTransaction().replace(R.id.previewTemplateCreditsContainer,templateCreditsFragment)
                .commit();

    }
    private void showCreditsContainer(){
        viewHolder.templateCreditsContainer.setVisibility(View.VISIBLE);
    }
    private void hideCreditsContainer(){
        viewHolder.templateCreditsContainer.setVisibility(View.GONE);
    }



    private void loadPreviewImage(){
        if(templateEntity!=null && templateEntity.imageUrl!=null){
            showProgress();
            glide.asBitmap().load(templateEntity.imageUrl).priority(Priority.IMMEDIATE).diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .skipMemoryCache(false).listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    viewHolder.progressBar.setVisibility(View.GONE);
                    if(AppUtils.hasInternetConnection(context)){
                        showMsg(R.string.can_t_load_image);
                    }else{
                        showMsg(R.string.no_internet_connection);
                    }
                    if(isFragmentVisible()){
                        getMainActivity().onBackPressed();
                    }
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    glide.load(resource).priority(Priority.IMMEDIATE).into(viewHolder.previewImage);
                    isImageLoaded=true;
                    showTemplateImage();
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });

        }else if(templateEntity!=null && templateEntity.imageUrl==null){
            showMsg(R.string.can_t_load_image);
            getMainActivity().onBackPressed();
        }
    }
    private void recordScreenView() {
        recordScreen(FireConstants.SCREEN_TEMPLATE_PREVIEW);
    }


    private boolean getHasFavTemplatesInLocal(){
        return hasFavTemplatesInLocal;
    }
    private int getTemplateType(){
        return  templateType;
    }
    private void setFavorite(boolean isFavorite){
        TemplateEntity currentTemplateEntity=templatePreviewFragmentViewModel.getTemplateEntity().getValue();
        currentTemplateEntity.setFavorite(isFavorite);
        templatePreviewFragmentViewModel.getTemplateEntity().setValue(templateEntity);
    }
    private void saveImage(){
        if(isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE,PERMISSION_SAVE_TEMPLATE)){
            if(isImageLoaded){
                Bitmap bitmap=AppUtils.getBitmapFromView(findViewById(R.id.templatePreviewImageParent));
                new SaveImage(Constants.TEMPLATES_FOLDERNAME,bitmap,false,new TemplateSaveListener()).execute();
            }
        }
    }
    private void showDeleteTemplateDialog(){
        ConfirmationDialog confirmationDialog =new ConfirmationDialog(getContext());
        confirmationDialog.setOnClickListener(new DeleteTemplateDialogClickListener());
        confirmationDialog.setPositiveBtnText(getString(R.string.delete));
        confirmationDialog.setPositiveBtnColor(AppUtils.getColor(getContext(),R.color.buttonColorNegative));
        confirmationDialog.show();
    }
    private void showLoadingDialog(String  loadingText){
        if(loadingDialog==null){
            loadingDialog=new LoadingDialog(getMainActivity());
        }
        loadingDialog.setLoadingText(loadingText);
        loadingDialog.show();
    }
    private void hideLoadingDialog(){
        if(loadingDialog!=null && loadingDialog.isShowing()){
            loadingDialog.dismiss();
        }
    }
    private void showReportTemplateDialog(){
        ReportTemplateDialog reportTemplateDialog=new ReportTemplateDialog(getMainActivity());
        reportTemplateDialog.setDialogItemClickListener(new ReportTemplateClickListeners());
        reportTemplateDialog.show();

    }
    private void showTemplateReportedMsg(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showMsg(R.string.template_reported_successfully);
                templateDbViewModel.deleteTemplateForReports(templatePreviewFragmentViewModel.getTemplateEntity().getValue().getId(),new FeedTemplateDeleteListener());
            }
        },300);
    }
    private void showMoreInfo(){
        View view;
        boolean hasChild = viewHolder.moreInfoContainer.getChildCount()>0;
        if(!hasChild){
            view =getLayoutInflater().inflate(R.layout.dialog_searchtags, viewHolder.moreInfoContainer,false);
        }
        else {
            view = viewHolder.moreInfoContainer.getChildAt(0);
        }
        ((TextView)view.findViewById(R.id.moreInfoDescription)).setText(getNumbersAddedSearchKeywords(templateEntity.getSearchTags()));
        if(!hasChild){
            viewHolder.moreInfoContainer.addView(view);
        }
        showFreezeLayer();
        viewHolder.moreInfoContainer.setVisibility(View.VISIBLE);
    }
    private void hideMoreInfo(){
        viewHolder.moreInfoContainer.setVisibility(View.GONE);
        hideFreezeLayer();
    }

    private void showFreezeLayer(){
        viewHolder.freezeLayer.setVisibility(View.VISIBLE);
    }
    private void hideFreezeLayer(){
        viewHolder.freezeLayer.setVisibility(View.GONE);
    }

    private String getNumbersAddedSearchKeywords(String searchableKeywords){
        String string="";
        if(searchableKeywords!=null){
            String[] searchArr = searchableKeywords.split(",");
            for (int i = 0; i < searchArr.length; i++) {
                String currStr = searchArr[i];
                string+=(i+1)+". "+currStr+"\n\n";
            }
            return string;
        }

        return "";
    }

    private void gotoCreateMemeFragment(String uriStr){
        getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_CREATE_MEME_USAGE,FireConstants.EVENT_CREATE_MEME_FROM_TEMPLATES);
        TemplatePreviewFragmentDirections.ActionTemplatePreviewFragmentToCreateMemeFragment action =
                TemplatePreviewFragmentDirections.actionTemplatePreviewFragmentToCreateMemeFragment(GridNameConstants.L1,uriStr,args.getAuthorId());
        navigate(action);
    }
    private void setSearchDbValues(TemplateEntity paramTemplateEntity,String operation){
        if(isFromSearch){
            HashMap<String,List<TemplateEntity>> templateMap = searchTemplateFragmentVariableDb.getSearchData().getValue();
            if(templateMap!=null){
                List<TemplateEntity> templateEntities = templateMap.get(searchTemplateFragmentVariableDb.getSearchStr());
                if(templateEntities!=null){
                    for (int i = 0; i < templateEntities.size(); i++) {
                        TemplateEntity currTemplateEntity = templateEntities.get(i);
                        if(currTemplateEntity!=null && currTemplateEntity.getId().equals(paramTemplateEntity.getId())){
                            templateEntities.remove(i);
                            if(operation.equals(Constants.UPDATE_OPERATION)){
                                templateEntities.add(i,paramTemplateEntity);
                            }
                            break;
                        }
                    }
                }
                templateMap.clear();
                templateMap.put(searchTemplateFragmentVariableDb.getSearchStr(),templateEntities);
                searchTemplateFragmentVariableDb.getSearchData().setValue(templateMap);
            }
        }

    }

    private void showProgress(){
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        viewHolder.templatePreviewImageParent.setVisibility(View.GONE);
    }
    private void showTemplateImage(){
        viewHolder.progressBar.setVisibility(View.GONE);
        viewHolder.templatePreviewImageParent.setVisibility(View.VISIBLE);
        showWaterMark();
    }

    private void showWaterMark(){
        if(Constants.API_TYPE_MY_TEMPLATES != getTemplateType()){
            viewHolder.watermark.setVisibility(View.VISIBLE);
            viewHolder.appNameText.setVisibility(View.VISIBLE);
        }
    }
    private void hideWaterMark(){
        viewHolder.watermark.setVisibility(View.GONE);
    }
    /*other methods end*/


    /*api methods start*/
    private void callFavApi(){
        if(!AppUtils.hasInternetConnection(getContext())){
            showMsg(R.string.no_internet_connection);
            return;
        }
        TemplateEntity templateEntity=templatePreviewFragmentViewModel.getTemplateEntity().getValue();
        if(!templateEntity.isFavorite()){
            HashMap data = new HashMap();
            String userId = windowViewModel.getUserId();
            String templateId = templateEntity.getId();
            data.put(TemplateEntity.APIKEY_ISFAVORITE,!templateEntity.isFavorite());
            data.put(TemplateEntity.APIKEY_SEARCHTAGS,templateEntity.getSearchTags());
            data.put(TemplateEntity.APIKEY_IMGURL,templateEntity.getImageUrl());
            data.put(TemplateEntity.APIKEY_REGION_ID,windowViewModel.getRegionId());
            data.put(TemplateEntity.APIKEY_CATEGORY_ID,templateEntity.getCategoryId());
            if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
                data.put(TemplateEntity.APIKEY_ID,templateId);
                getFireFunctions().callApi(ApiUrls.FAVORITE_TEMPLATE,data,new FavApiListener(templateEntity,!templateEntity.isFavorite()));
            }else{
                data.put(TemplateEntity.APIKEY_ID,templateId+"_"+userId);
                getFireFunctions().callApi(ApiUrls.FAVORITE_TEMPLATE,data,new FavApiListener(templateEntity,!templateEntity.isFavorite()));
            }

        }else{
            HashMap data = new HashMap();
            String templateId = templateEntity.getId();
            if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
                //in fav templates the id having user key
                data.put(TemplateEntity.APIKEY_ID,templateId);
                getFireFunctions().callApi(ApiUrls.UNFAVORITE_TEMPLATE,data,new FavApiListener(templateEntity,!templateEntity.isFavorite()));
            }else{
                String userId = windowViewModel.getLoggedInUserEntity().getId();
                data.put(TemplateEntity.APIKEY_ID,templateId+"_"+userId);
                getFireFunctions().callApi(ApiUrls.UNFAVORITE_TEMPLATE,data,new FavApiListener(templateEntity,!templateEntity.isFavorite()));
            }
        }
    }
    private void deleteTemplate(){
        showLoadingDialog(getString(R.string.deleting_template_dots));
        HashMap hashMap=new HashMap();
        hashMap.put(TemplateEntity.APIKEY_ID,templateEntity.getId());
        getFireFunctions().callApi(ApiUrls.DELETE_TEMPLATE,hashMap,new DeleteTemplateApiListener());
    }
    private void reportTemplate(String reportStr){
        showLoadingDialog(getString(R.string.loading_dots));
        HashMap apiData=new HashMap();
        apiData.put(TemplateEntity.APIKEY_ID,templateEntity.getId());
        apiData.put(ApiConstants.KEY_REPORTTYPE,reportStr);
        getFireFunctions().callApi(ApiUrls.REPORT_TEMPLATE,apiData,null);
    }


    /*api methods end*/


    /*class start*/
    private class ViewHolder{
        ImageView previewImage,favIcon;
        View favoriteBtn;
        TextView favText,appNameText;
        View deleteBtn,editBtn,reportBtn;
        ConstraintLayout moreInfoContainer;
        View freezeLayer;
        View progressBar,templatePreviewImageParent, templateCreditsContainer;
        ImageView watermark;
        public ViewHolder(){
            previewImage=findViewById(R.id.templatePreviewImage);
            favoriteBtn=findViewById(R.id.bsAddToFavorites);
            favIcon=findViewById(R.id.bsAddToFavIcon);
            favText=findViewById(R.id.bsFavoriteText);
            appNameText=findViewById(R.id.previewFragmentAppNameTv);
            appNameText.setVisibility(View.GONE);
            deleteBtn=findViewById(R.id.bsTemplateDelete);
            editBtn=findViewById(R.id.bsTemplateEdit);
            reportBtn=findViewById(R.id.bsTemplateReport);
            moreInfoContainer=findViewById(R.id.moreInfoContainer);
            freezeLayer=findViewById(R.id.previewTemplateFreezeLayer);
            progressBar=findViewById(R.id.templatePreviewProgress);
            templatePreviewImageParent=findViewById(R.id.templatePreviewImageParent);
            watermark=findViewById(R.id.watermark);
            templateCreditsContainer =findViewById(R.id.previewTemplateCreditsContainer);
        }

    }
    private class FavApiListener implements FireFunctions.ApiListener{
        TemplateEntity beforeTemplateEntity;
        boolean isFavorite;

        public FavApiListener(TemplateEntity beforeTemplateEntity, boolean isFavorite) {
            this.beforeTemplateEntity = beforeTemplateEntity;
            this.isFavorite = isFavorite;
            setFavorite(isFavorite);
        }

        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            if(statusCode==HttpCodes.UNAUTHORIZED){
                unAuthorizeSignOut(appPrefsRepository,windowViewModel,R.id.action_templatePreviewFragment_to_signInFragment);
            }
            else if(statusCode== HttpCodes.SUCCESS){
                if(isFavorite){
                    HashMap resultMap = (HashMap)resultObject;
                    String id =(String) resultMap.get(TemplateEntity.APIKEY_ID);
                    TemplateEntity templateEntity = TemplateEntity.getEntity(resultObject);
                    List<TemplateEntity> templateEntities=new ArrayList<>();
                    beforeTemplateEntity.setFavorite(true);
                    if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
                        String[] ids = templateEntity.getId().split("_");
                        if(ids.length>0){
                            templateDbViewModel.updateIsFavorite(ids[0],templateEntity.isFavorite());
                        }

                    }else{
                        templateEntities.add(beforeTemplateEntity);
                    }
                    templateEntities.add(templateEntity);
                    templateDbViewModel.insertAllTemplateData(templateEntities);
                    /*insert only if fav templates already exists*/
                    if(getHasFavTemplatesInLocal()){
                        templateDbViewModel.insertSingleFavTemplateId(new FavTemplateIdsEntity(id,templateEntity.getCreatedTime()));
                    }
                }else{
                    String favId;
                    if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
                        favId = beforeTemplateEntity.getId();
                        String[] ids = favId.split("_");
                        if(ids.length>0){
                            templateDbViewModel.updateIsFavorite(ids[0],false);
                        }
                    }else{
                        favId = beforeTemplateEntity.getId()+"_"+windowViewModel.getLoggedInUserEntity().getId();
                        templateDbViewModel.updateIsFavorite(favId,false);
                        beforeTemplateEntity.setFavorite(false);
                    }
                    templateDbViewModel.deleteFavorite(favId);
                    templateDbViewModel.deleteSingleTemplateData(favId);
                    templateDbViewModel.updateIsFavorite(beforeTemplateEntity.getId(),false);
                }
                setSearchDbValues(beforeTemplateEntity,Constants.UPDATE_OPERATION);
            }else {
                setFavorite(!isFavorite);
                if(statusCode==HttpCodes.BLOCKED){
                    LoggedInUserEntity loggedInUserEntity=windowViewModel.getLoggedInUserEntity();
                    if(!Constants.USER_STATUS_BLOCKED.equals(loggedInUserEntity.getStatus())){
                        loggedInUserEntity.setStatus(Constants.USER_STATUS_BLOCKED);
                        windowViewModel.setLoggedInUserEntity(loggedInUserEntity);
                        loggedInUserDbViewModel.insert(loggedInUserEntity);
                    }
                    showMsg(R.string.user_blocked_msg);
                }else if(statusCode==HttpCodes.MESSAGE){
                    showMsg(getErrorMsgFromApiData(resultObject));
                }
                else{
                    showMsg(R.string.something_went_wrong_please_try_again);
                }

            }
        }

        @Override
        public void onFailure(Exception e) {
            setFavorite(!isFavorite);
            showMsg(R.string.something_went_wrong_please_try_again);
        }
    }

    private class DeleteTemplateApiListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            if(statusCode==HttpCodes.UNAUTHORIZED){
                hideLoadingDialog();
                unAuthorizeSignOut(appPrefsRepository,windowViewModel,R.id.action_templatePreviewFragment_to_signInFragment);
            }
            else if(statusCode==HttpCodes.SUCCESS){
                templateDbViewModel.deleteTemplate(templateEntity.getId(),new DeleteTemplateDbCallbackListener());
                setSearchDbValues(templateEntity,Constants.DELETE_OPERATION);
            }else{
                showMsg(R.string.cant_delete_template_now);
                hideLoadingDialog();
            }
        }

        @Override
        public void onFailure(Exception e) {
            showMsg(R.string.cant_delete_template_now);
            hideLoadingDialog();
        }
    }
    private class TemplateSaveListener implements SaveImageListener{
        @Override
        public void onSuccess(String uriString) {
            showMsgWithGravity(R.string.template_saved_successfully, Gravity.CENTER);
        }

        @Override
        public void onFailure() {
            showMsg(R.string.cant_save_template);
        }
    }

    private class DeleteTemplateDialogClickListener implements ConfirmationDialog.AlertDialogBtnClickListner{
        @Override
        public void onPositiveBtnClick(Dialog dialog) {
            if(!AppUtils.hasInternetConnection(getContext())){
                showMsg(R.string.no_internet_connection);
                return;
            }
            dialog.dismiss();
            deleteTemplate();
        }

        @Override
        public void onNegativeBtnClick(Dialog dialog) {
            dialog.dismiss();
        }
    }

    private class DeleteTemplateDbCallbackListener implements AppDatabase.DbOperationCallbackListener{
        @Override
        public void onSuccess() {
            hideLoadingDialog();
            showMsg(R.string.template_deleted_successfully);
            getMainActivity().onBackPressed();
        }
    }

    private class ReportTemplateClickListeners implements ReportTemplateDialog.DialogItemClickListener{
        @Override
        public void onSpamItemClick(Dialog dialog) {
            if(!AppUtils.hasInternetConnection(getContext())){
                showMsg(R.string.no_internet_connection);
                return;
            }
            dialog.dismiss();
            reportTemplate(Constants.REPORT_SPAM);
            showTemplateReportedMsg();
        }

        @Override
        public void onInappropriateItemClick(Dialog dialog) {
            if(!AppUtils.hasInternetConnection(getContext())){
                showMsg(R.string.no_internet_connection);
                return;
            }
            dialog.dismiss();
            reportTemplate(Constants.REPORT_INAPPROPRIATE);
            showTemplateReportedMsg();
        }

        @Override
        public void onCancelClick(Dialog dialog) {
            dialog.dismiss();
        }
    }

    private class FeedTemplateDeleteListener implements AppDatabase.DbOperationCallbackListener{
        @Override
        public void onSuccess() {
            hideLoadingDialog();
            getMainActivity().onBackPressed();
        }
    }

    private class TemplateCreditsHandshakes implements TemplateCreditsFragment.ParentHandShakes{
        @Override
        public void onData() {
            showCreditsContainer();
        }

        @Override
        public void onNoData() {
            hideCreditsContainer();
        }
    }
    /*class end*/

}
