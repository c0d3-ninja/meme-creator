package com.thugdroid.memeking.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.adapters.TemplatesGroupAdapter;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.model.NoTemplatesFoundConfigurations;
import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.TemplatesGroupDataEntity;
import com.thugdroid.memeking.room.repository.AppPrefsRepository;
import com.thugdroid.memeking.room.repository.TemplatesGroupRepository;
import com.thugdroid.memeking.smalllibs.SpacesItemDecoration;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.utils.TimeUtils;
import com.thugdroid.memeking.viewmodel.TemplatesGroupFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class TemplatesGroupFragment extends CustomFragment implements SwipeRefreshLayout.OnRefreshListener{
    private ViewHolder viewHolder;
    private TemplatesGroupFragmentViewModel templatesGroupFragmentViewModel;
    private WindowViewModel windowViewModel;
    private TemplatesGroupRepository templatesGroupRepository;
    private ParentHandShakes parentHandShakes;
    private TemplatesGroupAdapter templatesGroupAdapter;
    private int scrollDx, scrollDy;
    private boolean refreshOnLoad;
    private ItemClickListener itemClickListener;
    private AppPrefsRepository appPrefsRepository;
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public TemplatesGroupFragment(){

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
        templatesGroupRepository=new ViewModelProvider(this).get(TemplatesGroupRepository.class);
        templatesGroupFragmentViewModel =new ViewModelProvider(this).get(TemplatesGroupFragmentViewModel.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        appPrefsRepository=new ViewModelProvider(this).get(AppPrefsRepository.class);
        if(windowViewModel.getRegionId()!=null){
            initViews(getRootView());
            initListeners();
            initObservers();
        }else{
            if(parentHandShakes!=null){
                parentHandShakes.onRegionIdNull();
            }
        }
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
        templatesGroupAdapter=new TemplatesGroupAdapter(getContext(), templatesGroupFragmentViewModel.getTemplatesGroup(),viewHolder.templatesRecyclerView);
        templatesGroupAdapter.setScrollListener(new ScrollListener());
        templatesGroupAdapter.setListItemClickListener(new TemplatesGroupItemClickListener());
        viewHolder.templatesRecyclerView.setAdapter(templatesGroupAdapter);

    }

    @Override
    public void initListeners() {
        viewHolder.swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
    }

    @Override
    public void initObservers() {
        LiveData<List<TemplatesGroupDataEntity>> liveData = templatesGroupRepository.getAllTemplatesGroupsAsLiveData(windowViewModel.getRegionId());
        liveData.observe(getViewLifecycleOwner(), new Observer<List<TemplatesGroupDataEntity>>() {
            @Override
            public void onChanged(List<TemplatesGroupDataEntity> templatesGroupDataEntities) {
                liveData.removeObservers(getViewLifecycleOwner());
                initTemplatesGroupData(templatesGroupDataEntities);
            }
        });
        LiveData<String> scrollLiveData=appPrefsRepository.getPref(AppPrefsEntity.TEMPLATES_GROUP_SCROLL_EMPTY_TIME);
        scrollLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String time) {
                scrollLiveData.removeObservers(getViewLifecycleOwner());
                if(time!=null && TimeUtils.getDifferenceInHrs(Long.valueOf(time),new Date().getTime())<=Constants.TEMPLATES_GROUP_SCROLL_CALL_THRESHOLD_IN_DAYS){
                    templatesGroupFragmentViewModel.setScrollDisabled(true);
                }
            }
        });
    }
    @Override
    public void onClick(View v) {

    }

    @Override
    public void onRefresh() {
        if(AppUtils.hasInternetConnection(getContext())){
            if(templatesGroupFragmentViewModel.getTemplatesGroup().size()>=0
                    && templatesGroupFragmentViewModel.getTemplatesGroupApiModel().getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST){
                viewHolder.swipeRefreshLayout.setRefreshing(true);
                getTemplatesGroup(true);
            }
        }else{
            showMsg(R.string.no_internet_connection);
            viewHolder.swipeRefreshLayout.setRefreshing(false);
        }

    }

    /*other methods start*/
    private void showTemplatesGrid(){
        viewHolder.swipeRefreshLayout.setVisibility(View.VISIBLE);
        hideNoInternetConnection();
        hideSomethingWentWrong();
        hideNoContentFound();
    }
    private void hideTemplateGrid(){
        viewHolder.swipeRefreshLayout.setVisibility(View.GONE);
    }
    private void customShowNoInternetConnection(){
        hideTemplateGrid();
        hideSomethingWentWrong();
        hideNoContentFound();
        showNoInternetConnection(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTemplatesGrid();
                getTemplatesGroup(false);
            }
        });
    }
    private void customShowSomethingWentWrong(){
        hideNoInternetConnection();
        hideTemplateGrid();
        hideNoContentFound();
        showSomethingWentWrong(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTemplatesGrid();
                getTemplatesGroup(false);
            }
        },windowViewModel.getRegionId());
    }

    private void customShowNoContentFound(){
        /*getString method used*/
        if(isFragmentVisible()){
            hideNoInternetConnection();
            hideTemplateGrid();
            hideSomethingWentWrong();
            NoTemplatesFoundConfigurations configurations=new NoTemplatesFoundConfigurations(getString(R.string.no_meme_templates_found),
                    false, null);
            configurations.setRegionId(windowViewModel.getRegionId());
            showNoContentFound(configurations);
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
    private void initTemplatesGroupData(List<TemplatesGroupDataEntity> templatesGroupDataEntities){
        exeTemplateSizeHandShake(templatesGroupDataEntities.size());
        templatesGroupFragmentViewModel.getTemplatesGroup().addAll(templatesGroupDataEntities);
        templatesGroupAdapter.notifyDataSetChanged();
        if(templatesGroupFragmentViewModel.getTemplatesGroupApiModel().isShouldCallApi()
        && templatesGroupDataEntities.size()==0){
            getTemplatesGroup(false);
        }
        /*scroll not setting properly*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewHolder.templatesRecyclerView.scrollBy(scrollDx,scrollDy);
            }
        },1);
    }

    private void getTemplatesGroup(boolean isHardRefresh){
        if(!AppUtils.hasInternetConnection(getContext())){
            templatesGroupFragmentViewModel.setTemplatesGroupLoading(ApiModel.LOADINGSTATE_REQUEST_FAILURE,
                    HttpCodes.NOINTERNETCONNECTION,true);
            showErrMsgs();
            return;
        }
        HashMap apiMap = new HashMap();
        if(templatesGroupFragmentViewModel.getTemplatesGroup().size()>0 && !isHardRefresh){
            Long createdTime = getLastCreatedTime();
            if(createdTime!=0L){
                apiMap.put(ApiConstants.KEY_FROM, createdTime);
            }

        }
        apiMap.put(ApiConstants.KEY_LIMIT, Constants.TEMPLATES_GROUP_API_LIMIT);
        apiMap.put(TemplatesGroupDataEntity.APIKEY_REGION_ID,windowViewModel.getRegionId());
        showTemplatesGrid();
        templatesGroupFragmentViewModel.setTemplatesGroupLoading(ApiModel.LOADINGSTATE_REQUEST, HttpCodes.IDLE,false);
        if(!isHardRefresh){
            showRecyclerViewLoading();
        }
        getFireFunctions().callApi(ApiUrls.GET_TEMPLATES_GROUP,apiMap,new GetTemplatesGroupListener(apiMap));

    }

    private void showRecyclerViewLoading(){
        if(!templatesGroupFragmentViewModel.getTemplatesGroup().containsAll(templatesGroupFragmentViewModel.getLoadingItems())){
            templatesGroupFragmentViewModel.getTemplatesGroup().addAll(templatesGroupFragmentViewModel.getLoadingItems());
            viewHolder.templatesRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    templatesGroupAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void hideRecyclerViewLoading(){
        if(templatesGroupFragmentViewModel.getTemplatesGroup().containsAll(templatesGroupFragmentViewModel.getLoadingItems())){
            templatesGroupFragmentViewModel.getTemplatesGroup().removeAll(templatesGroupFragmentViewModel.getLoadingItems());
            viewHolder.templatesRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    templatesGroupAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void hideTemplatesGroupLoading(){
        if(viewHolder.swipeRefreshLayout.isRefreshing()){
            viewHolder.swipeRefreshLayout.setRefreshing(false);
        }
        hideRecyclerViewLoading();
    }

    private void showErrMsgs(){
        if(templatesGroupFragmentViewModel.getTemplatesGroup().size()>0){
            return;
        }
        ApiModel apiModel = templatesGroupFragmentViewModel.getTemplatesGroupApiModel();
        if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_FAILURE){
            switch (apiModel.getStatusCode()){
                case HttpCodes.NOINTERNETCONNECTION:
                    customShowNoInternetConnection();
                    break;
                default:
                    customShowSomethingWentWrong();
                    break;
            }
        }
    }

    private Long getLastCreatedTime(){
        List templatesGroupDataEntities =
                templatesGroupFragmentViewModel.getTemplatesGroup();
        Long largestCreatedTime=0L;
        for (int i = 0; i < templatesGroupDataEntities.size(); i++) {
            Object currItem = templatesGroupDataEntities.get(i);
            if(currItem instanceof  TemplatesGroupDataEntity){
                TemplatesGroupDataEntity templatesGroupDataEntity = (TemplatesGroupDataEntity) currItem;
                if(largestCreatedTime<templatesGroupDataEntity.getCreatedTime()){
                    largestCreatedTime=templatesGroupDataEntity.getCreatedTime();
                }
            }
        }
        return largestCreatedTime;
    }

    /*other methods end*/

    /*setters start*/
    public void setParentHandShakes(ParentHandShakes parentHandShakes) {
        this.parentHandShakes = parentHandShakes;
    }

    public void setScrollDx(int scrollDx) {
        this.scrollDx = scrollDx;
    }

    public void setScrollDy(int scrollDy) {
        this.scrollDy = scrollDy;
    }

    public void setRefreshOnLoad(boolean refreshOnLoad) {
        this.refreshOnLoad = refreshOnLoad;
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    /*setters end*/

    /*getters start*/
    public int getScrollDx() {
        return viewHolder.templatesRecyclerView.computeHorizontalScrollOffset();
    }
    public int getScrollDy() {
        return viewHolder.templatesRecyclerView.computeVerticalScrollOffset();
    }
    public boolean isRefreshOnLoad() {
        return refreshOnLoad;
    }
    /*getters end*/



    /*api listeners start*/
    private class GetTemplatesGroupListener implements FireFunctions.ApiListener{
        HashMap apiData;

        public GetTemplatesGroupListener(HashMap apiData) {
            this.apiData = apiData;
        }

        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            Long apiFrom = (Long) apiData.get(ApiConstants.KEY_FROM);
            hideTemplatesGroupLoading();
            switch (statusCode){
                case HttpCodes.SUCCESS:
                    List<TemplatesGroupDataEntity> templatesGroupDataEntities
                            = TemplatesGroupDataEntity.getEntityList(resultObject);
                    if(apiFrom==null){
                        templatesGroupFragmentViewModel.getTemplatesGroup().clear();
                        appPrefsRepository.delete(AppPrefsEntity.TEMPLATES_GROUP_SCROLL_EMPTY_TIME);
                        templatesGroupRepository.deleteAll(new TemplatesGroupsApiDeleteAllListener(templatesGroupDataEntities));
                    }else{
                        templatesGroupRepository.insert(templatesGroupDataEntities);
                    }
                    templatesGroupFragmentViewModel.getTemplatesGroup().addAll(templatesGroupDataEntities);
                    templatesGroupAdapter.notifyDataSetChanged();
                    templatesGroupFragmentViewModel.setTemplatesGroupLoading(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,
                            HttpCodes.SUCCESS,templatesGroupDataEntities.size()>=((int)apiData.get(ApiConstants.KEY_LIMIT)));
                    break;
                case HttpCodes.NOCONTENT:
                    if(apiFrom==null){
                        templatesGroupFragmentViewModel.getTemplatesGroup().clear();
                        templatesGroupAdapter.notifyDataSetChanged();
                        templatesGroupRepository.deleteAll(new TemplatesGroupsApiDeleteAllListener(new ArrayList<>()));
                        customShowNoContentFound();
                    }else{
                        appPrefsRepository.insertPref(AppPrefsEntity.TEMPLATES_GROUP_SCROLL_EMPTY_TIME,String.valueOf(new Date().getTime()));
                    }
                    templatesGroupFragmentViewModel.setTemplatesGroupLoading(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,false);
                    break;
                default:
                    templatesGroupFragmentViewModel.setTemplatesGroupLoading(ApiModel.LOADINGSTATE_REQUEST_FAILURE,statusCode,false);
                    showErrMsgs();
                    break;
            }
            exeTemplateSizeHandShake(templatesGroupFragmentViewModel.getTemplatesGroup().size());
        }

        @Override
        public void onFailure(Exception e) {
            hideTemplatesGroupLoading();
            templatesGroupFragmentViewModel.setTemplatesGroupLoading(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.INTERNALSERVERERROR,false);
            exeTemplateSizeHandShake(templatesGroupFragmentViewModel.getTemplatesGroup().size());
            showErrMsgs();
        }
    }
    /*api listeners end*/


    /*db listeners start*/
    private class TemplatesGroupsApiDeleteAllListener implements AppDatabase.DbOperationCallbackListener{
        List<TemplatesGroupDataEntity> templatesGroupDataEntities;

        public TemplatesGroupsApiDeleteAllListener(List<TemplatesGroupDataEntity> templatesGroupDataEntities) {
            this.templatesGroupDataEntities = templatesGroupDataEntities;
        }

        @Override
        public void onSuccess() {
            templatesGroupRepository.insert(templatesGroupDataEntities);
        }
    }
    /*db listeners end*/


    /*other listeners start*/
    private class ScrollListener implements TemplatesGroupAdapter.ScrollListener{
        @Override
        public void onScrollEnd() {
            if(AppUtils.hasInternetConnection(getContext()) && !templatesGroupFragmentViewModel.isScrollDisabled()){
                ApiModel apiModel = templatesGroupFragmentViewModel.getTemplatesGroupApiModel();
                if(apiModel.isShouldCallApi() &&
                        apiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST){
                    getTemplatesGroup(false);
                }
            }
        }
    }
    private class TemplatesGroupItemClickListener implements TemplatesGroupAdapter.ListItemClickListener{
        @Override
        public void onClick(TemplatesGroupDataEntity templatesGroupDataEntity) {
            if(itemClickListener!=null){
                itemClickListener.onClick(templatesGroupDataEntity);
            }
        }
    }
    /*other listeners end*/


    private class ViewHolder{
        SwipeRefreshLayout swipeRefreshLayout;
        RecyclerView templatesRecyclerView;
        ViewHolder(){
            templatesRecyclerView=findViewById(R.id.templatesRecyclerView);
            templatesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),1));
            templatesRecyclerView.addItemDecoration(new SpacesItemDecoration(getContext().getResources().getInteger(R.integer.templatesGroupGridSpace),1));
            swipeRefreshLayout=findViewById(R.id.templatesSwipeRefreshLayout);
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
            swipeRefreshLayout.setEnabled(false);
        }
    }

    public interface ParentHandShakes{
        void onDataEmpty();
        void onData(int size);
        void onRegionIdNull();

    }

    public interface ItemClickListener{
        void onClick(TemplatesGroupDataEntity templatesGroupDataEntity);
    }
}
