package com.thugdroid.memeking.utils;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.fragments.TemplatesFragment;
import com.thugdroid.memeking.room.entity.FavTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.viewmodel.WindowViewModel;
import com.thugdroid.memeking.viewmodel.db.TemplateDbViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FavApiUtils {
    private FireFunctions fireFunctions;
    private LifecycleOwner viewLifecycleOwner;
    private TemplateEntity templateEntity;
    private int templateType;
    private int viewPosition;
    private WindowViewModel windowViewModel;
    private TemplateDbViewModel templateDbViewModel;
    private ApiStateListener apiStateListener;
    private boolean isFavorite;

    public FavApiUtils(FireFunctions fireFunctions, LifecycleOwner viewLifecycleOwner, TemplateEntity templateEntity, int templateType, int viewPosition, WindowViewModel windowViewModel, TemplateDbViewModel templateDbViewModel, ApiStateListener apiStateListener) {
        this.fireFunctions = fireFunctions;
        this.viewLifecycleOwner = viewLifecycleOwner;
        this.templateEntity = templateEntity;
        this.templateType = templateType;
        this.viewPosition = viewPosition;
        this.windowViewModel = windowViewModel;
        this.templateDbViewModel = templateDbViewModel;
        this.apiStateListener = apiStateListener;
        this.isFavorite=templateEntity.isFavorite();
    }

    public FireFunctions getFireFunctions() {
        return fireFunctions;
    }

    public TemplateEntity getTemplateEntity() {
        return templateEntity;
    }

    public int getTemplateType() {
        return templateType;
    }

    public int getViewPosition() {
        return viewPosition;
    }

    public WindowViewModel getWindowViewModel() {
        return windowViewModel;
    }

    public TemplateDbViewModel getTemplateDbViewModel() {
        return templateDbViewModel;
    }

    public ApiStateListener getApiStateListener() {
        return apiStateListener;
    }

    public LifecycleOwner getViewLifecycleOwner() {
        return viewLifecycleOwner;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void callFavApi(){
        HashMap data = new HashMap();
        if(templateEntity.isFavorite()){
            data.put(TemplateEntity.APIKEY_SEARCHTAGS,getTemplateEntity().getSearchTags());
            data.put(TemplateEntity.APIKEY_IMGURL,getTemplateEntity().getImageUrl());
            data.put(TemplateEntity.APIKEY_REGION_ID,getWindowViewModel().getRegionId());
            data.put(TemplateEntity.APIKEY_CATEGORY_ID,getTemplateEntity().getCategoryId());
            data.put(TemplateEntity.APIKEY_ID,getTemplateEntity().getId()+"_"+getWindowViewModel().getLoggedInUserEntity().getId());
            getFireFunctions().callApi(ApiUrls.FAVORITE_TEMPLATE,data,new FavApiListener());
        }else{
            if(getTemplateType()== Constants.API_TYPE_FAV_TEMPLATES){
                data.put(TemplateEntity.APIKEY_ID,getTemplateEntity().getId());
            }else{
                data.put(TemplateEntity.APIKEY_ID,getTemplateEntity().getId()+"_"+getWindowViewModel().getLoggedInUserEntity().getId());
            }
            getFireFunctions().callApi(ApiUrls.UNFAVORITE_TEMPLATE,data,new FavApiListener());
        }
    }


    private class FavApiListener implements FireFunctions.ApiListener{


        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            switch (statusCode){
                case HttpCodes.SUCCESS:
                    if(isFavorite){
                        TemplateEntity resTemplateEntity =
                                TemplateEntity.getEntity(resultObject);
                        List<TemplateEntity> templateEntities=new ArrayList<>();
                        templateEntity.setFavorite(true);
                        templateEntities.add(resTemplateEntity);
                        templateEntities.add(templateEntity);
                        templateDbViewModel.insertAllTemplateData(templateEntities);
                        /*insert only if fav templates already exists*/
                        LiveData<List<TemplateEntity>> favTemplateLiveData = templateDbViewModel.getFavoriteTemplatesAsLiveData(1);
                        favTemplateLiveData.observe(getViewLifecycleOwner(), new Observer<List<TemplateEntity>>() {
                            @Override
                            public void onChanged(List<TemplateEntity> templateEntities) {
                                favTemplateLiveData.removeObservers(getViewLifecycleOwner());
                                if(templateEntities.size()>0){
                                    templateDbViewModel.insertSingleFavTemplateId(new FavTemplateIdsEntity(resTemplateEntity.getId(),resTemplateEntity.getCreatedTime()));
                                }
                            }
                        });
                    }else{
                        String favId;
                        if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
                            favId = templateEntity.getId();
                            String[] ids = favId.split("_");
                            if(ids.length>0){
                                templateDbViewModel.updateIsFavorite(ids[0],false);
                            }
                        }else{
                            favId = templateEntity.getId()+"_"+windowViewModel.getLoggedInUserEntity().getId();
                            templateDbViewModel.updateIsFavorite(templateEntity.getId(),false);
                        }
                        templateDbViewModel.deleteFavorite(favId);
                        templateDbViewModel.deleteSingleTemplateData(favId);
                    }
                default:
                    if(getApiStateListener()!=null){
                        getApiStateListener().onApiEnd(statusCode,resultObject,getTemplateEntity(),isFavorite(),getViewPosition());
                    }
            }
        }
        @Override
        public void onFailure(Exception e) {
            if(getApiStateListener()!=null){
                getApiStateListener().onApiEnd(HttpCodes.INTERNALSERVERERROR,null,getTemplateEntity(),isFavorite(),getViewPosition());
            }
        }
    }





    public interface ApiStateListener{
        void onApiEnd(int statusCode,Object resultObject,TemplateEntity templateEntity,boolean isFavorite,int position);
    }


}


