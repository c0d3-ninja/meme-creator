package com.thugdroid.memeking.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.thugdroid.memeking.MediaFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.adapters.MemesRecyclerAdapter;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.model.NoTemplatesFoundConfigurations;
import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.entity.MemesDataEntity;
import com.thugdroid.memeking.room.entity.MyMemesIdsEntity;
import com.thugdroid.memeking.room.entity.SocialUsernameEntity;
import com.thugdroid.memeking.room.repository.MemesRepository;
import com.thugdroid.memeking.room.repository.SocialUsernameRepository;
import com.thugdroid.memeking.smalllibs.SpacesItemDecoration;
import com.thugdroid.memeking.ui.ConfirmationDialog;
import com.thugdroid.memeking.ui.LoadingDialog;
import com.thugdroid.memeking.ui.ReportTemplateDialog;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.viewmodel.MemesFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class MemesFragment extends MediaFragment {
    private static final int PERMISSION_SAVE_MEME=1;
    private ViewHolder viewHolder;
    private MemesRecyclerAdapter memesRecyclerAdapter;
    private MemesFragmentViewModel memesFragmentViewModel;
    private MemesFragmentViewModel memesFeedDbViewModel;
    private WindowViewModel windowViewModel;
    private int apiType;
    private MemesRepository memesRepository;
    private ParentHandShakes parentHandShakes;
    private boolean isRefreshOnLoad;
    private int scrollDx, scrollDy;
    private LoadingDialog loadingDialog;
    private SocialUsernameRepository socialUsernameRepository;
    public MemesFragment() {
    }

    public static MemesFragment newInstance(
            int apiType,
            boolean isRefreshOnLoad,
            int scrollDx,
            int scrollDy,
            ParentHandShakes parentHandShakes
    ){
        MemesFragment memesFragment=new MemesFragment();
        memesFragment.setApiType(apiType);
        memesFragment.setRefreshOnLoad(isRefreshOnLoad);
        memesFragment.setScrollDx(scrollDx);
        memesFragment.setScrollDy(scrollDy);
        memesFragment.setParentHandShakes(parentHandShakes);
        return memesFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_templates,container,false);
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
        if(windowViewModel.getLoggedInUserEntity()==null){
            if(parentHandShakes!=null){
                parentHandShakes.onRegionIdNull();
            }
        }else{
            memesFragmentViewModel=new ViewModelProvider(this).get(MemesFragmentViewModel.class);
            memesFeedDbViewModel=new ViewModelProvider(requireActivity()).get(MemesFragmentViewModel.class);
            memesRepository =new ViewModelProvider(this).get(MemesRepository.class);
            initViews(getRootView());
            initListeners();
            initObservers();
        }
    }

    @Override
    public void initViews(View view) {
        viewHolder=new ViewHolder();
        memesRecyclerAdapter=
                new MemesRecyclerAdapter(getContext(),
                        windowViewModel.getUserId(),
                        memesFragmentViewModel.getMemes(),
                        viewHolder.memesRecyclerView);
        memesRecyclerAdapter.setListItemClickListener(new MemeItemClickListener());
        memesRecyclerAdapter.setScrollListener(new MemesScrollListener());
        viewHolder.memesRecyclerView.setAdapter(memesRecyclerAdapter);
    }

    @Override
    public void initListeners() {
        viewHolder.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshListener());
    }

    @Override
    public void initObservers() {
        memesFragmentViewModel.getMemesApiModel().observe(getViewLifecycleOwner(), new Observer<ApiModel>() {
            @Override
            public void onChanged(ApiModel apiModel) {
                if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST){
                    if(!viewHolder.swipeRefreshLayout.isRefreshing()){
                        showLoading();
                    }

                }else{
                    hideLoading();
                    if(viewHolder.swipeRefreshLayout.isRefreshing()){
                        viewHolder.swipeRefreshLayout.setRefreshing(false);
                    }
                    if(memesFragmentViewModel.getMemes().size()==0){
                        if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_SUCCESS
                        && apiModel.getStatusCode()==HttpCodes.NOCONTENT){
                            customShowNoContentFound();
                        }else if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_FAILURE){
                            if(apiModel.getStatusCode()==HttpCodes.NOINTERNETCONNECTION){
                                customShowNoInternetConnection();
                            }else{
                                customShowSomethingWentWrong();
                            }
                        }
                    }
                }
            }
        });
        if(isFromMyMemes()){
            LiveData<List<MemesDataEntity>> memesLiveData= memesRepository.getMyMemesAsLiveData();
            memesLiveData.observe(getViewLifecycleOwner(), new Observer<List<MemesDataEntity>>() {
                @Override
                public void onChanged(List<MemesDataEntity> memesDataEntities) {
                    memesLiveData.removeObservers(getViewLifecycleOwner());
                    initMemesData(memesDataEntities);
                }
            });
        }else{
            initMemesData(memesFeedDbViewModel.getMemes());
        }

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PERMISSION_SAVE_MEME){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveMeme();
            }else{
                showMsg(R.string.allow_permission_to_save_meme);
            }
        }
    }

    /*other methods start*/

    public SocialUsernameRepository getSocialUsernameRepository() {
        if(socialUsernameRepository==null){
            socialUsernameRepository=new ViewModelProvider(this).get(SocialUsernameRepository.class);
        }
        return socialUsernameRepository;
    }

    private boolean isFromMemesFeed(){
        return getApiType()==Constants.API_TYPE_MEMES_FEED;
    }

    private boolean isFromMyMemes(){
        return getApiType()==Constants.API_TYPE_MY_MEMES;
    }

    public void setApiType(int apiType) {
        this.apiType = apiType;
    }

    public void setParentHandShakes(ParentHandShakes parentHandShakes) {
        this.parentHandShakes = parentHandShakes;
    }

    private void showMemeReportedMsg(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showMsg(R.string.meme_reported_successfully);
                String id = memesFragmentViewModel.getCurrentItemMemesDataEntity().getId();
                memesRepository.deleteAMeme(id,new DeleteAMemeListener());
                memesFeedDbViewModel.deleteAMeme(id);
                int position = memesFragmentViewModel.deleteAMeme(id,memesFragmentViewModel.getCurrentItemPosition());
                if(position!=-1){
                    memesRecyclerAdapter.notifyItemRemoved(position);
                }
            }
        },300);
    }
    private void showReportMemeDialog(){
        ReportTemplateDialog reportTemplateDialog=new ReportTemplateDialog(getMainActivity());
        reportTemplateDialog.setDialogItemClickListener(new ReportMemeClickListener());
        reportTemplateDialog.show();
    }
    private void showDeleteMemeDialog(){
        ConfirmationDialog confirmationDialog=new ConfirmationDialog(getContext());
        confirmationDialog.setTitle(getString(R.string.do_you_want_to_delete_this_meme));
        confirmationDialog.setOnClickListener(new DeleteMemeConfirmationListener());
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
    public void triggerHardRefresh(){
        try{
            if(AppUtils.hasInternetConnection(getContext())){
                if(getApiType()==Constants.API_TYPE_MY_MEMES){
                    memesRepository.deleteAllMyMemesIds(new HardRefreshDeleteListener());
                }else{
                    memesRepository.deleteAllMemesFeedIds(new HardRefreshDeleteListener());
                }
            }else {
                showMsg(R.string.no_internet_connection);
            }
        }catch (Exception  e){
            e.printStackTrace();
        }

    }
    private void exeTemplateSizeHandShake(int size){
        if(parentHandShakes!=null && isFragmentVisible()){
            if(size==0){
                parentHandShakes.onDataEmpty();
            }else{
                parentHandShakes.onData(size);
            }
        }
    }
    private void initMemesData(List<MemesDataEntity> memesDataEntities){
        exeTemplateSizeHandShake(memesDataEntities.size());
        memesFragmentViewModel.getMemes().addAll(memesDataEntities);
        memesRecyclerAdapter.notifyDataSetChanged();
        ApiModel apiModel = memesFragmentViewModel.getMemesApiModel().getValue();
        if(apiModel.isShouldCallApi() && memesDataEntities.size()==0){
            getMemes(false);
        }else if(isRefreshOnLoad() && memesDataEntities.size()>0 && apiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST){
            viewHolder.swipeRefreshLayout.setRefreshing(true);
            getMemes(true);
        }
        /*scroll not setting properly*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewHolder.memesRecyclerView.scrollBy(scrollDx,scrollDy);
            }
        },1);
    }
    private void setMemesApiModel(int loadingState,int statusCode,boolean shouldCallApi){
        ApiModel apiModel=memesFragmentViewModel.getMemesApiModel().getValue();
        apiModel.setLoadingState(loadingState);
        apiModel.setStatusCode(statusCode);
        apiModel.setShouldCallApi(shouldCallApi);
        memesFragmentViewModel.getMemesApiModel().setValue(apiModel);
    }
    private void getMemes(boolean isHardRefresh){
        if(!AppUtils.hasInternetConnection(getContext())){
            setMemesApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE, HttpCodes.NOINTERNETCONNECTION,true);
            return;
        }
        HashMap apiData=new HashMap();
        if(memesFragmentViewModel.getMemes().size()!=0){
            Object object;
            if(isHardRefresh){
                object=memesFragmentViewModel.getMemes().get(0);
            }else{
                object = memesFragmentViewModel.getMemes().get(memesFragmentViewModel.getMemes().size()-1);
            }
            if(object instanceof MemesDataEntity){
                MemesDataEntity memesDataEntity = (MemesDataEntity)object;
                if(isHardRefresh){
                    apiData.put(ApiConstants.KEY_QUERY_TYPE,ApiConstants.VALUE_QUERY_TYPE_BACKWARD);
                }
                apiData.put(MemesDataEntity.APIKEY_FROM,memesDataEntity.getCreatedTime()+"_"+memesDataEntity.getId());
            }
        }
        setMemesApiModel(ApiModel.LOADINGSTATE_REQUEST, HttpCodes.IDLE,false);
        if(isFromMyMemes()){
            apiData.put(ApiConstants.KEY_LIMIT, Constants.MEMES_API_LIMIT);
            apiData.put(MemesDataEntity.APIKEY_CREATEDBY,windowViewModel.getUserId());
        }else{
            apiData.put(MemesDataEntity.APIKEY_REGION_ID,windowViewModel.getRegionId());
        }
        if(isFromMyMemes()){
            getFireFunctions().callApi(ApiUrls.GET_MEMES,apiData,new GetMemesApiListener(apiData));
        }else{
            getFireFunctions().callApi(ApiUrls.GET_MEMES,apiData,new GetMemesApiListener(apiData));
        }


    }
    private void showLoading(){
        if(!memesFragmentViewModel.getMemes().containsAll(memesFragmentViewModel.getLoadingItems())){
            memesFragmentViewModel.getMemes().addAll(memesFragmentViewModel.getLoadingItems());
            viewHolder.memesRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    memesRecyclerAdapter.notifyDataSetChanged();
                }
            });

        }
    }

    private void hideLoading(){
        if(memesFragmentViewModel.getMemes().containsAll(memesFragmentViewModel.getLoadingItems())){
            memesFragmentViewModel.getMemes().removeAll(memesFragmentViewModel.getLoadingItems());
            viewHolder.memesRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    memesRecyclerAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    public int getApiType() {
        return apiType;
    }

    public boolean isRefreshOnLoad() {
        return isRefreshOnLoad;
    }

    public void setRefreshOnLoad(boolean refreshOnLoad) {
        isRefreshOnLoad = refreshOnLoad;
    }

    private void showMemesGrid(){
        viewHolder.swipeRefreshLayout.setVisibility(View.VISIBLE);
        hideNoInternetConnection();
        hideNoContentFound();
        hideSomethingWentWrong();
    }
    private void hideMemesGrid(){
        viewHolder.swipeRefreshLayout.setVisibility(View.GONE);
    }
    private void customShowNoInternetConnection(){
        hideMemesGrid();
        hideSomethingWentWrong();
        hideNoContentFound();
        showNoInternetConnection(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMemesGrid();
                getMemes(false);
            }
        });
    }
    private void customShowNoContentFound(){
        if(isFragmentVisible()){
            hideMemesGrid();
            hideNoInternetConnection();
            hideSomethingWentWrong();
            NoTemplatesFoundConfigurations noTemplatesFoundConfigurations=
                    new NoTemplatesFoundConfigurations(getString(R.string.no_memes_found),false,null);
            showNoContentFound(noTemplatesFoundConfigurations);

        }
    }
    private void customShowSomethingWentWrong(){
        hideMemesGrid();
        hideNoInternetConnection();
        hideNoContentFound();
        showSomethingWentWrong(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMemesGrid();
                getMemes(false);
            }
        },windowViewModel.getRegionId());
    }

    public int getScrollDx() {
        return viewHolder.memesRecyclerView.computeHorizontalScrollOffset();
    }

    public void setScrollDx(int scrollDx) {
        this.scrollDx = scrollDx;
    }

    public int getScrollDy() {
        return viewHolder.memesRecyclerView.computeVerticalScrollOffset();
    }

    public void setScrollDy(int scrollDy) {
        this.scrollDy = scrollDy;
    }
    private void callMemesActionCountApi(String action,String id){
        if(AppUtils.hasInternetConnection(getContext())){
            HashMap apiData = new HashMap();
            apiData.put(ApiConstants.KEY_ACTION,action);
            apiData.put(MemesDataEntity.APIKEY_ID,id);
            getFireFunctions().callApi(ApiUrls.INCREASE_MEMES_ACTION_COUNT,apiData,null);
        }
    }
    private void saveMeme(){
        if(isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE,PERMISSION_SAVE_MEME)){
            showLoadingDialog(getString(R.string.saving));
            Glide.with(getContext()).asBitmap().load(memesFragmentViewModel.getCurrentItemMemesDataEntity().getImageUrl()).listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    hideLoadingDialog();
                    showMsg(R.string.cant_save_meme);
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    new SaveImage(Constants.MEMES_FOLDERNAME,resource,false,new SaveMemeListener(memesFragmentViewModel.getCurrentItemMemesDataEntity(),
                            memesFragmentViewModel.getCurrentItemPosition())).execute();
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
        }
    }

    private void shareMeme(){
        showLoadingDialog(getString(R.string.loading_dots));
        Glide.with(getContext()).asBitmap().load(memesFragmentViewModel.getCurrentItemMemesDataEntity().getImageUrl()).listener(new RequestListener<Bitmap>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                hideLoadingDialog();
                showMsg(R.string.cant_share_meme);
                return false;
            }

            @Override
            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        }).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                new SaveImage(Constants.SHARE_MEME_FOLDER_NAME,resource,true,
                        new MemeShareListener()).execute();
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }
    private void reportMeme(String id,String reportType){
        HashMap apiData = new HashMap();
        apiData.put(MemesDataEntity.APIKEY_ID,id);
        apiData.put(ApiConstants.KEY_REPORTTYPE,reportType);
        getFireFunctions().callApi(ApiUrls.REPORT_MEME,apiData,null);
    }
    private void deleteMeme(){
        showLoadingDialog(getString(R.string.deleting));
        HashMap apiData=new HashMap();
        apiData.put(MemesDataEntity.APIKEY_ID,memesFragmentViewModel.getCurrentItemMemesDataEntity().getId());
        getFireFunctions().callApi(ApiUrls.DELETE_MEME,apiData,new DeleteMemeApiListener());
    }
    /*other methods end*/


    private class ViewHolder{
        SwipeRefreshLayout swipeRefreshLayout;
        RecyclerView memesRecyclerView;
        ViewHolder(){
            memesRecyclerView =findViewById(R.id.templatesRecyclerView);
            memesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));
            memesRecyclerView.addItemDecoration(new SpacesItemDecoration(getContext().getResources().getInteger(R.integer.memesGridSpace),1));
            swipeRefreshLayout=findViewById(R.id.templatesSwipeRefreshLayout);
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
            if(isFromMemesFeed()){
                swipeRefreshLayout.setEnabled(false);
            }
        }
    }

    private class GetMemesApiListener implements FireFunctions.ApiListener{
        HashMap apiData;

        public GetMemesApiListener(HashMap apiData) {
            this.apiData = apiData;
        }

        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            Object apiDataFrom = apiData.get(MemesDataEntity.APIKEY_FROM);
            Object apiQueryType=apiData.get(ApiConstants.KEY_QUERY_TYPE);
            switch (statusCode){
                case HttpCodes.SUCCESS:
                    List listOfHashMaps = (ArrayList)resultObject;
                    if(apiQueryType!=null && ApiConstants.VALUE_QUERY_TYPE_BACKWARD.equals(apiQueryType)){
                        setMemesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,true);
                    }else{
                        setMemesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,listOfHashMaps.size()>= Constants.MEMES_API_LIMIT);
                    }

                    List<Object> currentIdsEntity=new ArrayList<>();
                    List<MemesDataEntity> currentMemeDataEntities=MemesDataEntity.getEntityList(resultObject);
                    if(isFromMyMemes()){
                        //For O(1)
                        HashMap<String,Boolean> userIdsMap = new HashMap<>();
                        List<SocialUsernameEntity> socialUsernameEntities = new ArrayList<>();
                        for (MemesDataEntity currentMemeDataEntity : currentMemeDataEntities) {
                            if(!userIdsMap.containsKey(currentMemeDataEntity.getCreatedBy())){
                                socialUsernameEntities.add(new SocialUsernameEntity(currentMemeDataEntity.getId(),currentMemeDataEntity.getInstaUsername()));
                                userIdsMap.put(currentMemeDataEntity.getId(),true);
                            }
                                currentIdsEntity.add(
                                        new MyMemesIdsEntity(currentMemeDataEntity.getId(),currentMemeDataEntity.getCreatedTime())
                                );
                        }
                        memesRepository.insertMemesDataEntity(MemesDataEntity.getEntityListForDb(resultObject));
                        memesRepository.insertMyMemesIdsEntity((ArrayList)currentIdsEntity);
                        getSocialUsernameRepository().insert(socialUsernameEntities);
                    }
                    if(apiDataFrom==null){
                        memesFragmentViewModel.getMemes().clear();
                    }
                    if(apiQueryType!=null && ApiConstants.VALUE_QUERY_TYPE_BACKWARD.equals(apiQueryType)){
                        Collections.reverse(currentMemeDataEntities);
                        memesFragmentViewModel.getMemes().addAll(0,currentMemeDataEntities);
                    }else{
                        memesFragmentViewModel.getMemes().addAll(currentMemeDataEntities);
                        if(isFromMemesFeed()){
                            memesFeedDbViewModel.getMemes().addAll(currentMemeDataEntities);
                        }
                    }
                    memesRecyclerAdapter.notifyDataSetChanged();
                    break;
                case HttpCodes.NOCONTENT:
                    if(apiQueryType!=null && ApiConstants.VALUE_QUERY_TYPE_BACKWARD.equals(apiQueryType)){
                        setMemesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,true);
                    }else{
                        setMemesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,false);
                    }
                    break;
                default:
                    setMemesApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,statusCode,false);
                    break;
            }
            exeTemplateSizeHandShake(memesFragmentViewModel.getMemes().size());
        }

        @Override
        public void onFailure(Exception e) {
            setMemesApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.INTERNALSERVERERROR,false);
            exeTemplateSizeHandShake(memesFragmentViewModel.getMemes().size());
        }
    }

    private class MemesScrollListener implements MemesRecyclerAdapter.ScrollListener{
        @Override
        public void onScrollEnd() {
            if(AppUtils.hasInternetConnection(getContext()) && isFromMyMemes()){
                ApiModel apiModel=memesFragmentViewModel.getMemesApiModel().getValue();
                if(apiModel.isShouldCallApi()
                && apiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST){
                    getMemes(false);
                }
            }
        }
    }

    private class SwipeRefreshListener implements SwipeRefreshLayout.OnRefreshListener{
        @Override
        public void onRefresh() {
            if(AppUtils.hasInternetConnection(getContext()) && isFromMyMemes()){
                ApiModel apiModel=memesFragmentViewModel.getMemesApiModel().getValue();
                if(apiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST){
                    viewHolder.swipeRefreshLayout.setRefreshing(true);
                    getMemes(true);
                }else{
                    viewHolder.swipeRefreshLayout.setRefreshing(false);
                }
            }else{
                viewHolder.swipeRefreshLayout.setRefreshing(false);
                showMsg(R.string.no_internet_connection);
            }
        }
    }

    private class HardRefreshDeleteListener implements AppDatabase.DbOperationCallbackListener{
        @Override
        public void onSuccess() {
            if(isFromMyMemes()){
                showMemesGrid();
                memesFragmentViewModel.getMemes().clear();
                memesRecyclerAdapter.notifyDataSetChanged();
                getMemes(false);
            }
        }
    }

    private class MemeItemClickListener implements MemesRecyclerAdapter.ListItemClickListener{
        @Override
        public void onDownloadClick(int position, MemesDataEntity memesDataEntity) {
            memesFragmentViewModel.setCurrentItemMemesDataEntity(memesDataEntity);
            memesFragmentViewModel.setCurrentItemPosition(position);
            saveMeme();
        }

        @Override
        public void onShareClick(int position, MemesDataEntity memesDataEntity) {
            memesFragmentViewModel.setCurrentItemMemesDataEntity(memesDataEntity);
            memesFragmentViewModel.setCurrentItemPosition(position);
            shareMeme();
        }

        @Override
        public void onReportClick(int position, MemesDataEntity memesDataEntity) {
            memesFragmentViewModel.setCurrentItemMemesDataEntity(memesDataEntity);
            memesFragmentViewModel.setCurrentItemPosition(position);
            showReportMemeDialog();
        }

        @Override
        public void onDeleteClick(int position, MemesDataEntity memesDataEntity) {
            memesFragmentViewModel.setCurrentItemMemesDataEntity(memesDataEntity);
            memesFragmentViewModel.setCurrentItemPosition(position);
            showDeleteMemeDialog();

        }

        @Override
        public void onIUsernameClick(String username) {
            getMainActivity().goToUrl(AppUtils.getInstaProfileUrl(username), false);
        }
    }

    private class ReportMemeClickListener implements ReportTemplateDialog.DialogItemClickListener{
        @Override
        public void onSpamItemClick(Dialog dialog) {
            if(!AppUtils.hasInternetConnection(getContext())){
                showMsg(R.string.no_internet_connection);
                return;
            }
            dialog.dismiss();
            showLoadingDialog(getString(R.string.reporting));
            String id = memesFragmentViewModel.getCurrentItemMemesDataEntity().getId();
            reportMeme(id,Constants.REPORT_SPAM);
            showMemeReportedMsg();
        }

        @Override
        public void onInappropriateItemClick(Dialog dialog) {
            if(!AppUtils.hasInternetConnection(getContext())){
                showMsg(R.string.no_internet_connection);
                return;
            }
            dialog.dismiss();
            showLoadingDialog(getString(R.string.reporting));
            String id = memesFragmentViewModel.getCurrentItemMemesDataEntity().getId();
            reportMeme(id,Constants.REPORT_INAPPROPRIATE);
            showMemeReportedMsg();
        }

        @Override
        public void onCancelClick(Dialog dialog) {
            dialog.dismiss();
        }
    }

    private class SaveMemeListener implements SaveImageListener{
        MemesDataEntity  memesDataEntity;
        int position;

        public SaveMemeListener(MemesDataEntity memesDataEntity, int position) {
            this.memesDataEntity = memesDataEntity;
            this.position = position;
        }

        @Override
        public void onSuccess(String uriString) {
            hideLoadingDialog();
            showMsgWithGravity(R.string.meme_saved_successfully, Gravity.CENTER);
            memesDataEntity.setDownloads(memesDataEntity.getDownloads()+1);
            memesRecyclerAdapter.notifyItemChanged(position);
            memesRepository.updateDownloadsCount(memesDataEntity.getId(),memesDataEntity.getDownloads());
            callMemesActionCountApi(MemesDataEntity.APIKEY_DOWNLOADS,memesDataEntity.getId());
        }

        @Override
        public void onFailure() {
            hideLoadingDialog();
            showMsgWithGravity(R.string.cant_save_meme, Gravity.CENTER);
        }
    }
    private class MemeShareListener implements SaveImageListener{
        @Override
        public void onSuccess(String uriString) {
            hideLoadingDialog();
            MemesDataEntity memesDataEntity=memesFragmentViewModel.getCurrentItemMemesDataEntity();
            memesDataEntity.setShares(memesDataEntity.getShares()+1);
            memesRecyclerAdapter.notifyItemChanged(memesFragmentViewModel.getCurrentItemPosition());
            memesRepository.updateSharesCount(memesDataEntity.getId(),memesDataEntity.getShares());
            callMemesActionCountApi(MemesDataEntity.APIKEY_SHARES,memesDataEntity.getId());
            Uri contentUri= Uri.parse(uriString);
            if(contentUri!=null){
                Intent shareIntent=new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri,getContext().getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM,contentUri);
                startActivity(Intent.createChooser(shareIntent,getResources().getString(R.string.share)));
            }
        }

        @Override
        public void onFailure() {
            hideLoadingDialog();
            showMsg(R.string.cant_share_meme);
        }
    }

    private class DeleteMemeConfirmationListener implements ConfirmationDialog.AlertDialogBtnClickListner{
        @Override
        public void onPositiveBtnClick(Dialog dialog) {
            if(!AppUtils.hasInternetConnection(getContext())){
                showMsg(R.string.no_internet_connection);
                return;
            }
            dialog.dismiss();
            deleteMeme();
        }

        @Override
        public void onNegativeBtnClick(Dialog dialog) {
            dialog.dismiss();
        }
    }

    private class DeleteMemeApiListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            hideLoadingDialog();
            switch (statusCode){
                case HttpCodes.SUCCESS:
                    showMsg(R.string.meme_deleted_successfully);
                    String id = memesFragmentViewModel.getCurrentItemMemesDataEntity().getId();
                    memesRepository.deleteAMeme(id,new DeleteAMemeListener());
                    int position = memesFragmentViewModel.deleteAMeme(id,memesFragmentViewModel.getCurrentItemPosition());
                    if(position!=-1){
                        memesRecyclerAdapter.notifyItemRemoved(position);
                    }
                    memesFeedDbViewModel.deleteAMeme(id);
                    break;
                default:
                    hideLoadingDialog();
                    showMsg(R.string.cant_delete_this_meme);
                    break;
            }
        }

        @Override
        public void onFailure(Exception e) {
            hideLoadingDialog();
            showMsg(R.string.cant_delete_this_meme);
        }
    }

    private class DeleteAMemeListener implements AppDatabase.DbOperationCallbackListener{

        @Override
        public void onSuccess() {
            hideLoadingDialog();
        }
    }

    public interface ParentHandShakes{
        void onDataEmpty();
        void onData(int size);
        void onRegionIdNull();

    }

}
