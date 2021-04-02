package com.thugdroid.memeking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.adapters.RegionGridAdapter;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;
import com.thugdroid.memeking.room.entity.RegionEntity;
import com.thugdroid.memeking.ui.LoadingDialog;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.viewmodel.MemesFragmentViewModel;
import com.thugdroid.memeking.viewmodel.SelectRegionFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;
import com.thugdroid.memeking.viewmodel.db.LoggedInUserDbViewModel;
import com.thugdroid.memeking.viewmodel.db.RegionDbViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SelectRegionFragment extends CustomFragment {

    private ViewHolder viewHolder;
    private RegionGridAdapter regionGridAdapter;
    private LoggedInUserDbViewModel loggedInUserDbViewModel;
    private SelectRegionFragmentViewModel selectRegionFragmentViewModel;
    private RegionDbViewModel regionDbViewModel;
    private WindowViewModel windowViewModel;
    private LoadingDialog loadingDialog;
    private MemesFragmentViewModel globalMemesFragmentViewModel;

    /*to fix Caused by java.lang.NoSuchMethodException*/
    public SelectRegionFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_selectregion,container,false);
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
        selectRegionFragmentViewModel =new ViewModelProvider(this).get(SelectRegionFragmentViewModel.class);
        regionDbViewModel=new ViewModelProvider(this).get(RegionDbViewModel.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
        regionGridAdapter=new RegionGridAdapter(getMainActivity(), selectRegionFragmentViewModel.getRegions());
        regionGridAdapter.setListItemListener(new RegionListItemListener());
        viewHolder.regions.setAdapter(regionGridAdapter);
    }

    @Override
    public void initListeners() {
        viewHolder.nextBtn.setOnClickListener(this::onClick);
    }

    @Override
    public void initObservers() {
        regionDbViewModel.getRegionsAsLiveData().observe(getViewLifecycleOwner(), new Observer<List<RegionEntity>>() {
            @Override
            public void onChanged(List<RegionEntity> regionEntities) {
                if(regionEntities==null || regionEntities.size()==0){
                    if(selectRegionFragmentViewModel.getApiModel().getValue().getLoadingState()== ApiModel.LOADINGSTATE_IDLE){
                        getRegions();
                    }
                }else{
                    selectRegionFragmentViewModel.regions.clear();
                    selectRegionFragmentViewModel.regions.addAll(regionEntities);
                    regionGridAdapter.notifyDataSetChanged();
                    selectRegionFragmentViewModel.getShowNextBtn().setValue(true);
                }
            }
        });
        selectRegionFragmentViewModel.getShowNextBtn().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean showNextBtn) {
                viewHolder.nextBtn.setVisibility(showNextBtn?View.VISIBLE:View.GONE);
            }
        });
        loggedInUserDbViewModel.getLoggedInUserAsLiveData().observe(getViewLifecycleOwner(), loggedInUserEntity -> {
            if(loggedInUserEntity !=null ){
                if(loggedInUserEntity.regionId!=null){
                    selectRegionFragmentViewModel.setUserHasRegion(true);
                    selectRegionFragmentViewModel.getSelectedRegionId().setValue(loggedInUserEntity.getRegionId());
                }
            }
        });

        selectRegionFragmentViewModel.getApiModel().observe(getViewLifecycleOwner(), new Observer<ApiModel>() {
            @Override
            public void onChanged(ApiModel apiModel) {
                if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST){
                    showLoading();
                }else  if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_SUCCESS){

                }
                else if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_FAILURE){
                    if(apiModel.getStatusCode()==HttpCodes.NOINTERNETCONNECTION){
                        showCustomNoConnection();
                    }else{
                        showCustomSomethingWentWrong();
                    }
                }
            }
        });
        selectRegionFragmentViewModel.getSelectedRegionId().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String selectedRegionId) {
                if(selectedRegionId==null){
                    viewHolder.nextBtn.setEnabled(false);
                }else{
                    viewHolder.nextBtn.setEnabled(true);
                }
                regionGridAdapter.setSelectedRegionId(selectedRegionId);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        recordScreenView();
    }

    private void recordScreenView(){
        recordScreen(FireConstants.SCREEN_SELECT_REGION);
    }

    private class ViewHolder{
        GridView regions;
        Button nextBtn;
        ViewHolder(){
            regions=findViewById(R.id.regionsGrid);
            nextBtn =findViewById(R.id.selectRegionPositiveBtn);
        }
    }

    private void updateRegion(String regionId){
        HashMap apiData = new HashMap();
        apiData.put(LoggedInUserEntity.KEY_REGION_ID,regionId);
        getFireFunctions().callApi(ApiUrls.SET_REGION,apiData,null);
    }



    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.selectRegionPositiveBtn:
                String selectedRegionId= selectRegionFragmentViewModel.getSelectedRegionId().getValue();
                if(selectedRegionId!=null){
                    LoggedInUserEntity loggedInUserEntity=windowViewModel.getLoggedInUserEntity();
                    loggedInUserEntity.setRegionId(selectedRegionId);
                    updateRegion(selectedRegionId);
                    String mode = SelectRegionFragmentArgs.fromBundle(getArguments()).getMode();
                    if(AppUtils.hasInternetConnection(getContext())){
                        if(Constants.MODE_CHANGE_REGION.equals(mode)){
                            if(loadingDialog==null){
                                loadingDialog=new LoadingDialog(getMainActivity());
                            }
                            loadingDialog.show();
                            getGlobalMemesFragmentViewModel().getMemes().clear();
                            regionDbViewModel.readyDataForChangeRegion(new ClearTemplateDataListener(loggedInUserEntity));
                        }else{
                            windowViewModel.setLoggedInUserEntity(loggedInUserEntity);
                            loggedInUserDbViewModel.updateRegion(selectedRegionId);
                            navigate(R.id.action_selectRegionFragment_to_initializingFragment);
                        }
                    }else{
                        showMsg(R.string.no_internet_connection);
                    }
                }
                break;
            case R.id.swwRetryBtn:
            case R.id.noConnectionRetryBtn:
                getRegions();
                break;
        }
    }

    private MemesFragmentViewModel getGlobalMemesFragmentViewModel(){
        if(globalMemesFragmentViewModel==null){
            globalMemesFragmentViewModel=new ViewModelProvider(requireActivity()).get(MemesFragmentViewModel.class);
        }
        return globalMemesFragmentViewModel;
    }
    private void setRegionLoading(){
        int loadingCount=6;
        RegionGridAdapter.LoadingItem loadingItem= new RegionGridAdapter.LoadingItem();
        for (int i = 0; i < loadingCount; i++) {
            selectRegionFragmentViewModel.regions.add(loadingItem);
        }
    }

    private void showLoading(){
        hideNoInternetConnection();
        showRegionsGrid();
        selectRegionFragmentViewModel.regions.clear();
        setRegionLoading();
        regionGridAdapter.notifyDataSetChanged();
    }


    private void  getRegions(){
        selectRegionFragmentViewModel.getShowNextBtn().setValue(false);
        ApiModel apiModel= selectRegionFragmentViewModel.getApiModel().getValue();
        if(AppUtils.hasInternetConnection(getContext())){
            apiModel.setLoadingState(ApiModel.LOADINGSTATE_REQUEST);
            apiModel.setStatusCode(HttpCodes.IDLE);
            getFireFunctions().callApi(ApiUrls.GET_REGIONS,null,new RegionsGetApiListener());
        }else{
            apiModel.setLoadingState(ApiModel.LOADINGSTATE_REQUEST_FAILURE);
            apiModel.setStatusCode(HttpCodes.NOINTERNETCONNECTION);
        }
        selectRegionFragmentViewModel.getApiModel().setValue(apiModel);
    }

    private void showCustomNoConnection(){
        hideRegionsGrid();
        hideSomethingWentWrong();
        showNoInternetConnection(this::onClick);
    }

    private void showCustomSomethingWentWrong(){
        hideRegionsGrid();
        hideNoInternetConnection();
        showSomethingWentWrong(this::onClick,null);
    }

    private void showRegionsGrid(){
        hideNoInternetConnection();
        hideSomethingWentWrong();
        viewHolder.regions.setVisibility(View.VISIBLE);
    }
    private void hideRegionsGrid(){
        viewHolder.regions.setVisibility(View.GONE);
    }


    private class RegionsGetApiListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            ApiModel apiModel= selectRegionFragmentViewModel.getApiModel().getValue();
            apiModel.setStatusCode(statusCode);
            if(statusCode== HttpCodes.SUCCESS){
                List<HashMap> dataList=(List<HashMap>) resultObject;
                List<RegionEntity> regionEntities=new ArrayList<>();
                for (HashMap hashMap : dataList) {
                    RegionEntity regionEntity=new RegionEntity((String) hashMap.get(RegionEntity.APIKEY_ID),
                            (String)hashMap.get(RegionEntity.APIKEY_NAME),
                            (String)hashMap.get(RegionEntity.APIKEY_LANGUAGE));
                    regionEntities.add(regionEntity);
                }
                regionDbViewModel.insertRegions(regionEntities);
                apiModel.setLoadingState(ApiModel.LOADINGSTATE_REQUEST_SUCCESS);
            }else{
                apiModel.setLoadingState(ApiModel.LOADINGSTATE_REQUEST_FAILURE);
                selectRegionFragmentViewModel.getApiModel().setValue(apiModel);
            }
            selectRegionFragmentViewModel.getApiModel().setValue(apiModel);
        }

        @Override
        public void onFailure(Exception e) {
            ApiModel apiModel= selectRegionFragmentViewModel.getApiModel().getValue();
            apiModel.setLoadingState(ApiModel.LOADINGSTATE_REQUEST_FAILURE);
            apiModel.setStatusCode(HttpCodes.INTERNALSERVERERROR);
            selectRegionFragmentViewModel.getApiModel().setValue(apiModel);
        }
    }

    private class RegionListItemListener implements RegionGridAdapter.ListItem{
        @Override
        public void onClick(RegionEntity regionEntity) {
            selectRegionFragmentViewModel.getSelectedRegionId().setValue(regionEntity.getId());
        }
    }

    private class ClearTemplateDataListener implements AppDatabase.DbOperationCallbackListener{
        LoggedInUserEntity loggedInUserEntity;

        public ClearTemplateDataListener(LoggedInUserEntity loggedInUserEntity) {
            this.loggedInUserEntity = loggedInUserEntity;
        }

        @Override
        public void onSuccess() {
            if(loadingDialog!=null){
                loadingDialog.dismiss();
                windowViewModel.setLoggedInUserEntity(loggedInUserEntity);
                loggedInUserDbViewModel.updateRegion(loggedInUserEntity.getRegionId());
                navigate(R.id.action_updateRegionFragment_to_initializingFragment);
            }
        }
    }
}
