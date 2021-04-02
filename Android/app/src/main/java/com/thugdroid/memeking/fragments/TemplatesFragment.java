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
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.thugdroid.memeking.MediaFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.adapters.TemplatesRecyclerAdapter;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.model.NoTemplatesFoundConfigurations;
import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.entity.AllCategoryTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.FavTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.MyTemplateIdsEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.room.entity.FeedTemplateIdsEntity;
import com.thugdroid.memeking.smalllibs.SpacesItemDecoration;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.utils.FavApiUtils;
import com.thugdroid.memeking.viewmodel.TemplateFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;
import com.thugdroid.memeking.viewmodel.db.TemplateDbViewModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class TemplatesFragment extends MediaFragment implements SwipeRefreshLayout.OnRefreshListener{

    private ViewHolder viewHolder;
    private TemplateDbViewModel templateDbViewModel;
    private TemplatesRecyclerAdapter templatesRecyclerAdapter;
    private TemplateFragmentViewModel templateFragmentViewModel;
    private WindowViewModel windowViewModel;
    private ActionsListener actionsListener;
    private int templateType;
    private int scrollDx, scrollDy;
    private CategoryEntity categoryEntity;
    private boolean showUploadBtn;
    private boolean refreshOnLoad;
    private ParentHandShakes  parentHandShakes;
    private boolean hasTemplateListActionBtns;

    /*to fix Caused by java.lang.NoSuchMethodException
     *#link https://stackoverflow.com/questions/56668934/java-lang-nosuchmethodexception-for-oncreate*/
    public TemplatesFragment(){

    }

    public static TemplatesFragment newInstance(int templateType,
                                                CategoryEntity categoryEntity,
                                                ActionsListener actionsListener,
                                                ParentHandShakes parentHandShakes,
                                                boolean hasTemplateListActionBtns,
                                                boolean showUploadBtn){
        TemplatesFragment templatesFragment = new TemplatesFragment();
        templatesFragment.setTemplateType(templateType);
        templatesFragment.setCategoryEntity(categoryEntity);
        templatesFragment.setActionsListener(actionsListener);
        templatesFragment.setParentHandShakes(parentHandShakes);
        templatesFragment.setShowUploadBtn(showUploadBtn);
        templatesFragment.setHasTemplateListActionBtns(hasTemplateListActionBtns);
        return templatesFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_templates,container,false);
    }

    /*override start*/
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setRootView(view);
        initVariables();

    }

    @Override
    public void initVariables() {
        templateFragmentViewModel = new ViewModelProvider(this).get(TemplateFragmentViewModel.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        templateDbViewModel=new ViewModelProvider(this).get(TemplateDbViewModel.class);
        String regionId = windowViewModel.getRegionId();
        /*Caused by java.lang.NullPointerException: Attempt to read from field 'java.lang.String b.e.b.f.c.f.d' on a null object reference
         fix try*/
        if(regionId!=null){
            templateFragmentViewModel.setRegionId(regionId);
            if(getTemplateType()==Constants.API_TYPE_TEMPLATES_FEED){
                templateFragmentViewModel.setCategoryId((categoryEntity!=null)?categoryEntity.getId():"");
                templateFragmentViewModel.setAllCategoryType(AppUtils.isAllTypeCategory(templateFragmentViewModel.getCategoryId(),templateFragmentViewModel.getRegionId()));
            }
            initViews(getRootView());
            initListeners();
            initObservers();
        }else{
            if(getParentHandShakes()!=null){
                getParentHandShakes().onRegionIdNull();
            }
        }


    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
        int totalItemCount=getContext().getResources().getInteger(R.integer.templatesGridSpanCount);
        viewHolder.templatesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),totalItemCount));
        viewHolder.templatesRecyclerView.addItemDecoration(new SpacesItemDecoration(getContext().getResources().getInteger(R.integer.templatesGridSpace),totalItemCount));
        templatesRecyclerAdapter=new TemplatesRecyclerAdapter(getContext(), templateFragmentViewModel.getTemplateList(),viewHolder.templatesRecyclerView);
        templatesRecyclerAdapter.setListItemClickListener(new TemplateListItemClickListener());
        templatesRecyclerAdapter.setScrollListener(new TemplatesScrollListener());
        templatesRecyclerAdapter.setHasActionBtns(hasTemplateListActionBtns());
        viewHolder.templatesRecyclerView.setAdapter(templatesRecyclerAdapter);
    }

    @Override
    public void initListeners() {
        viewHolder.swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
    }

    @Override
    public void initObservers() {
        if(getTemplateType()==Constants.API_TYPE_TEMPLATES_FEED){
            if(templateFragmentViewModel.isAllCategoryType()){
                templateDbViewModel.getAllCategoryTemplatesAsLiveData().observe(getViewLifecycleOwner(), new Observer<List<TemplateEntity>>() {
                    @Override
                    public void onChanged(List<TemplateEntity> templateEntities) {
                        templateDbViewModel.getAllCategoryTemplatesAsLiveData().removeObservers(getViewLifecycleOwner());
                        initTemplateData(templateEntities);
                    }
                });
            }else{
                templateDbViewModel.getFeedTemplatesAsLiveData(templateFragmentViewModel.getCategoryId()).observe(getViewLifecycleOwner(), new Observer<List<TemplateEntity>>() {
                    @Override
                    public void onChanged(List<TemplateEntity> templateEntities) {
                        templateDbViewModel.getFeedTemplatesAsLiveData(templateFragmentViewModel.getCategoryId()).removeObservers(getViewLifecycleOwner());
                        initTemplateData(templateEntities);
                    }
                });
            }

        }else if(getTemplateType()==Constants.API_TYPE_MY_TEMPLATES){
            templateDbViewModel.getMyTemplatesAsLiveData().observe(getViewLifecycleOwner(), new Observer<List<TemplateEntity>>() {
                @Override
                public void onChanged(List<TemplateEntity> templateEntities) {
                    templateDbViewModel.getMyTemplatesAsLiveData().removeObservers(getViewLifecycleOwner());
                    initTemplateData(templateEntities);

                }
            });
        }else if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
            templateDbViewModel.getFavoriteTemplatesAsLiveData().observe(getViewLifecycleOwner(), new Observer<List<TemplateEntity>>() {
                @Override
                public void onChanged(List<TemplateEntity> templateEntities) {
                    templateDbViewModel.getFavoriteTemplatesAsLiveData().removeObservers(getViewLifecycleOwner());
                    initTemplateData(templateEntities);

                }
            });
        }
        templateFragmentViewModel.getTemplatesApiModelAsLiveData().observe(getViewLifecycleOwner(), new Observer<ApiModel>() {
            @Override
            public void onChanged(ApiModel apiModel) {
                if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST){
                    if(!viewHolder.swipeRefreshLayout.isRefreshing()){
                        if(!templateFragmentViewModel.getTemplateList().containsAll(templateFragmentViewModel.getLoadingItems())){
                            templateFragmentViewModel.getTemplateList().addAll(templateFragmentViewModel.getLoadingItems());
                        }
                        viewHolder.templatesRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                templatesRecyclerAdapter.notifyDataSetChanged();
                            }
                        });
                    }
                }else{
                    templateFragmentViewModel.getTemplateList().removeAll(templateFragmentViewModel.getLoadingItems());
                    templatesRecyclerAdapter.notifyDataSetChanged();
                    if(viewHolder.swipeRefreshLayout.isRefreshing()){
                        viewHolder.swipeRefreshLayout.setRefreshing(false);
                    }
                    if(templateFragmentViewModel.getTemplateList().size()==0){
                        if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_SUCCESS
                                && apiModel.getStatusCode() == HttpCodes.NOCONTENT ){
                            /*show no content*/
                            customShowNoContentFound();
                        }else if(apiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_FAILURE
                        ){
                            if(apiModel.getStatusCode()==HttpCodes.NOINTERNETCONNECTION){
                                customShowNoInternetConnection();
                            }else {
                                customShowSomethingWentWrong();
                            }

                        }
                    }

                }

            }
        });
    }
    /*override end*/



    /*others start*/

    public void setTemplateType(int templateType) {
        this.templateType = templateType;
    }

    public void setCategoryEntity(CategoryEntity categoryEntity) {
        this.categoryEntity = categoryEntity;
    }

    public boolean hasTemplateListActionBtns() {
        return hasTemplateListActionBtns;
    }

    public void setHasTemplateListActionBtns(boolean hasTemplateListActionBtns) {
        this.hasTemplateListActionBtns = hasTemplateListActionBtns;
    }

    public ParentHandShakes getParentHandShakes() {
        return this.parentHandShakes;
    }

    public void setParentHandShakes(ParentHandShakes parentHandShakes) {
        this.parentHandShakes = parentHandShakes;
    }

    public void setShowUploadBtn(boolean showUploadBtn) {
        this.showUploadBtn = showUploadBtn;
    }

    public void setRefreshOnLoad(boolean refreshOnLoad) {
        this.refreshOnLoad = refreshOnLoad;
    }

    public boolean isRefreshOnLoad() {
        return refreshOnLoad;
    }

    public boolean isShowUploadBtn() {
        return showUploadBtn;
    }

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
                getTemplates(false);
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
                getTemplates(false);
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
                    isShowUploadBtn(), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainNewFragmentDirections.ActionMainNewFragmentToUploadTemplateFragment action=
                            MainNewFragmentDirections.actionMainNewFragmentToUploadTemplateFragment(null,
                                    null,
                                    null,
                                    null,
                                    templateFragmentViewModel.getRegionId(),
                                    false,
                                    Constants.API_TYPE_TEMPLATES_FEED);
                    navigate(action);
                }
            });
            configurations.setRegionId(templateFragmentViewModel.getRegionId());
            showNoContentFound(configurations);
        }
    }

    public void setActionsListener(ActionsListener actionsListener) {
        this.actionsListener = actionsListener;
    }
    private void exeTemplateSizeHandShake(int size){
        if(getParentHandShakes()!=null && isFragmentVisible()){
            if(size==0){
                getParentHandShakes().onDataEmpty();
            }else{
                getParentHandShakes().onData(size);
            }
        }
    }
    public ActionsListener getActionsListener() {
        return actionsListener;
    }
    private void initTemplateData(List<TemplateEntity> templateEntities){
        exeTemplateSizeHandShake(templateEntities.size());
        for (TemplateEntity templateEntity : templateEntities) {
            if(templateEntity!=null){
                templateFragmentViewModel.getTemplateList().add(templateEntity);
            }
        }
        templatesRecyclerAdapter.notifyDataSetChanged();
        ApiModel apiModel = templateFragmentViewModel.getTemplatesApiModelAsLiveData().getValue();
        if(apiModel.isShouldCallApi() &&  templateEntities.size()==0){
            getTemplates(false);
        }else if(isRefreshOnLoad() && templateEntities.size()>0 && apiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST){
            /*hard refresh to more templates when user enters first time*/
            viewHolder.swipeRefreshLayout.setRefreshing(true);
            getTemplates(true);
        }
        /*scroll not setting properly*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewHolder.templatesRecyclerView.scrollBy(scrollDx,scrollDy);
            }
        },1);

    }
    public int getTemplateType() {
        return templateType;
    }

    /*others end*/


    /*fav templates api start*/
    public void callFavApi(TemplateEntity templateEntity,int position){
        FavApiUtils favApiUtils= new FavApiUtils(getFireFunctions(),
                requireActivity(),templateEntity,
                getTemplateType(),position,
                windowViewModel,templateDbViewModel,
                new FavApiEndListener());
        favApiUtils.callFavApi();
    }

    private class FavApiEndListener implements FavApiUtils.ApiStateListener{

        @Override
        public void onApiEnd(int statusCode, Object resultObject,TemplateEntity templateEntity, boolean isFavorite, int position) {
            if(statusCode==HttpCodes.SUCCESS){
                return;
            }
            switch (statusCode){
                case HttpCodes.UNAUTHORIZED:
                    if(getParentHandShakes()!=null){
                        getParentHandShakes().onRegionIdNull();
                    }
                    break;
                case HttpCodes.MESSAGE:
                    showMsg(getErrorMsgFromApiData(resultObject));
                    triggerFavFailure(templateEntity,isFavorite,position,false);
                    break;
                default:
                    triggerFavFailure(templateEntity,isFavorite,position,true);
            }
        }
    }
    private void triggerFavFailure(TemplateEntity templateEntity,boolean isFavorite,int position,boolean showMsg){
        templateEntity.setFavorite(!isFavorite);
        templatesRecyclerAdapter.notifyItemChanged(position);
        if(showMsg){
            if(isFavorite){
                showMsg(R.string.can_t_favorite_template_try_again);
            }else{
                showMsg(R.string.can_t_unfavorite_template_try_again);
            }
        }

    }
    /*fav templates api end*/



    private class ViewHolder{
        SwipeRefreshLayout swipeRefreshLayout;
        RecyclerView templatesRecyclerView;
        ViewHolder(){
            templatesRecyclerView=findViewById(R.id.templatesRecyclerView);
            swipeRefreshLayout=findViewById(R.id.templatesSwipeRefreshLayout);
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        }
    }

    @Override
    public void onClick(View v) {

    }


    @Override
    public void onRefresh() {
        getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_TEMPLATE_LIST_USAGE,FireConstants.EVENT_SWIPE_REFRESH);
        if(AppUtils.hasInternetConnection(getContext())){
            ApiModel apiModel=templateFragmentViewModel.getTemplatesApiModelAsLiveData().getValue();
            if(apiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST){
                viewHolder.swipeRefreshLayout.setRefreshing(true);
                getTemplates(true);
            }
        }else {
            viewHolder.swipeRefreshLayout.setRefreshing(false);
            showMsg(R.string.no_internet_connection);
        }

    }

    public void triggerHardRefresh(){
        try{
            if(AppUtils.hasInternetConnection(getContext())){
                switch (getTemplateType()){
                    case Constants.API_TYPE_TEMPLATES_FEED:
                        if(AppUtils.isAllTypeCategory(templateFragmentViewModel.getCategoryId(),templateFragmentViewModel.getRegionId())){
                            templateDbViewModel.deleteAllCategoryTemplateIds(new HardRefreshDeleteListener());
                        }else{
                            templateDbViewModel.deleteAllFeedTemplateIdsByCategoryId(templateFragmentViewModel.getCategoryId(),new HardRefreshDeleteListener());
                        }
                        break;
                    case Constants.API_TYPE_MY_TEMPLATES:
                        templateDbViewModel.deleteAllMyTemplateIds(new HardRefreshDeleteListener());
                        break;
                    case Constants.API_TYPE_FAV_TEMPLATES:
                        templateDbViewModel.deleteAllFavTemplateIds(new HardRefreshDeleteListener());
                        break;
                }
                templateFragmentViewModel.getTemplateList().clear();
                templatesRecyclerAdapter.notifyDataSetChanged();
            }else{
                showMsg(R.string.no_internet_connection);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public int getScrollDx() {
        return viewHolder.templatesRecyclerView.computeHorizontalScrollOffset();
    }

    public int getScrollDy() {
        return viewHolder.templatesRecyclerView.computeVerticalScrollOffset();
    }

    public void setScrollDx(int scrollDx) {
        this.scrollDx = scrollDx;
    }

    public void setScrollDy(int scrollDy) {
        this.scrollDy = scrollDy;
    }

    private void getTemplates(boolean isHardRefresh){

        if(!AppUtils.hasInternetConnection(getContext()) && !isHardRefresh){
            setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.NOINTERNETCONNECTION,true);
            return;
        }
        HashMap apiData=new HashMap();
        if(templateFragmentViewModel.getTemplateList().size()!=0){
            Object object;
            if(isHardRefresh){
                object=templateFragmentViewModel.getTemplateList().get(0);
            }else{
                object = templateFragmentViewModel.getTemplateList().get(templateFragmentViewModel.getTemplateList().size()-1);
            }
            if(object instanceof  TemplateEntity){
                TemplateEntity templateEntity=(TemplateEntity) object;
                if(isHardRefresh){
                    apiData.put(ApiConstants.KEY_QUERY_TYPE,ApiConstants.VALUE_QUERY_TYPE_BACKWARD);
                }
                apiData.put(TemplateEntity.APIKEY_FROM,templateEntity.getCreatedTime()+"_"+templateEntity.getId());
            }
        }
        apiData.put(ApiConstants.KEY_LIMIT,Constants.TEMPLATES_API_LIMIT);
        setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST, HttpCodes.IDLE,false);
        if(getTemplateType()==Constants.API_TYPE_TEMPLATES_FEED){
            apiData.put(TemplateEntity.APIKEY_REGION_ID,templateFragmentViewModel.getRegionId());
            apiData.put(TemplateEntity.APIKEY_CATEGORY_ID,categoryEntity.getId());
            getFireFunctions().callApi(ApiUrls.GET_TEMPLATES,apiData,new GetAllTemplatesListener(apiData));
        }else if(getTemplateType()==Constants.API_TYPE_MY_TEMPLATES){
            apiData.put(TemplateEntity.APIKEY_CREATEDBY,windowViewModel.getUserId());
            getFireFunctions().callApi(ApiUrls.GET_TEMPLATES,apiData,new GetAllTemplatesListener(apiData));
        }else if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
            getFireFunctions().callApi(ApiUrls.GET_FAV_TEMPLATES,apiData,new GetAllTemplatesListener(apiData));
        }
    }

    private void setTemplatesApiModel(int loadingStatus,int statusCode,boolean shouldCallApi){
        ApiModel apiModel=templateFragmentViewModel.getTemplatesApiModelAsLiveData().getValue();
        apiModel.setLoadingState(loadingStatus);
        apiModel.setStatusCode(statusCode);
        apiModel.setShouldCallApi(shouldCallApi);
        templateFragmentViewModel.getTemplatesApiModelAsLiveData().setValue(apiModel);
    }

    private void insertTemplateIds(List<Object> categoryIdEntities){
        if(getTemplateType()==Constants.API_TYPE_TEMPLATES_FEED){
            if(templateFragmentViewModel.isAllCategoryType()){
                templateDbViewModel.insertAllCategoryTemplateIds((ArrayList)categoryIdEntities);
            }else{
                templateDbViewModel.insertAllFeedTemplateIds((ArrayList)categoryIdEntities);
            }
        }else if(getTemplateType()==Constants.API_TYPE_MY_TEMPLATES){
            templateDbViewModel.insertAllMyTemplateIds((ArrayList)categoryIdEntities);
        }else if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
            templateDbViewModel.insertAllFavTemplateIds((ArrayList)categoryIdEntities);
        }
    }
    private class GetAllTemplatesListener implements FireFunctions.ApiListener{
        HashMap apiData;

        public GetAllTemplatesListener(HashMap apiData) {
            this.apiData = apiData;
        }

        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            Object apiDataFrom = apiData.get(ApiConstants.KEY_FROM);
            Object apiQueryType=apiData.get(ApiConstants.KEY_QUERY_TYPE);
            switch (statusCode){
                case HttpCodes.SUCCESS:
                    List listOfHashMaps = (ArrayList)resultObject;
                    if(apiQueryType!=null && ApiConstants.VALUE_QUERY_TYPE_BACKWARD.equals(apiQueryType)){
                        setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,true);
                    }else{
                        setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,listOfHashMaps.size()>= Constants.TEMPLATES_API_LIMIT);
                    }

                    List<TemplateEntity> currentTemplateEntities=TemplateEntity.getEntityList(resultObject);
                    List<Object> currentTemplateEntityIds=new ArrayList<>();
                    for (TemplateEntity templateEntity : currentTemplateEntities) {
                        if(getTemplateType()==Constants.API_TYPE_TEMPLATES_FEED){
                            if(templateFragmentViewModel.isAllCategoryType()){
                                currentTemplateEntityIds.add(new AllCategoryTemplateIdsEntity(templateEntity.getId(),templateEntity.getCreatedTime()));
                            }else{
                                currentTemplateEntityIds.add(new FeedTemplateIdsEntity(templateEntity.getId(),templateEntity.getCreatedTime(),templateEntity.getCategoryId()));
                            }

                        }else if(getTemplateType()==Constants.API_TYPE_MY_TEMPLATES){
                            currentTemplateEntityIds.add(new MyTemplateIdsEntity(templateEntity.getId(),templateEntity.getCreatedTime()));
                        }else if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
                            currentTemplateEntityIds.add(new FavTemplateIdsEntity(templateEntity.getId(),templateEntity.getCreatedTime()));
                        }
                    }
                    if(apiDataFrom==null){
                        templateFragmentViewModel.getTemplateList().clear();
                    }
                    if(apiQueryType!=null && ApiConstants.VALUE_QUERY_TYPE_BACKWARD.equals((String)apiQueryType)){
                        Collections.reverse(currentTemplateEntities);
                        templateFragmentViewModel.getTemplateList().addAll(0,currentTemplateEntities);
                    }else{
                        templateFragmentViewModel.getTemplateList().addAll(currentTemplateEntities);
                    }
                    templatesRecyclerAdapter.notifyDataSetChanged();
                    templateDbViewModel.insertAllTemplateData(currentTemplateEntities);
                    insertTemplateIds(currentTemplateEntityIds);
                    break;
                case HttpCodes.NOCONTENT:
                    if(apiDataFrom==null){
                        templateFragmentViewModel.getTemplateList().clear();
                        switch (getTemplateType()){
                            case Constants.API_TYPE_TEMPLATES_FEED:
                                if(templateFragmentViewModel.isAllCategoryType()){
                                    templateDbViewModel.deleteAllCategoryTemplateIds(null);
                                }else{
                                    templateDbViewModel.deleteAllFeedTemplateIdsByCategoryId((String)apiData.get(TemplateEntity.APIKEY_CATEGORY_ID),
                                            null);
                                }
                                break;
                            case Constants.API_TYPE_MY_TEMPLATES:
                                templateDbViewModel.deleteAllMyTemplateIds(null);
                                break;
                            case Constants.API_TYPE_FAV_TEMPLATES:
                                templateDbViewModel.deleteAllFavTemplateIds(null);
                                break;
                        }
                        templatesRecyclerAdapter.notifyDataSetChanged();
                    }

                    if(apiQueryType!=null && ApiConstants.VALUE_QUERY_TYPE_BACKWARD.equals(apiQueryType)){
                        setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,true);
                    }else{
                        setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,false);
                    }

                    break;
                default:
                    setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,statusCode,false);
                    break;
            }
            exeTemplateSizeHandShake(templateFragmentViewModel.getTemplateList().size());
        }

        @Override
        public void onFailure(Exception e) {
            setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.INTERNALSERVERERROR,false);
            exeTemplateSizeHandShake(templateFragmentViewModel.getTemplateList().size());
        }
    }

    private class TemplateListItemClickListener implements TemplatesRecyclerAdapter.ListItemClickListener{
        @Override
        public void onClick(TemplateEntity templateEntity,int position) {
            if(getActionsListener()!=null){
                getActionsListener().onTemplateItemClick(templateEntity,getTemplateType());
            }
        }

        @Override
        public void onCreateMemeClick(TemplateEntity templateEntity) {
            if(getParentHandShakes()!=null){
                getParentHandShakes().onCreateMemeClick(templateEntity);
            }
        }

        @Override
        public void onFavClick(TemplateEntity templateEntity, int position) {
            if(!AppUtils.hasInternetConnection(getContext())){
                showMsg(R.string.no_internet_connection);
                return;
            }
            //don't need to do in fav templates because it will removed immediately from ui after unfavorite
            if(templateEntity.isFavorite() || getTemplateType()!=Constants.API_TYPE_FAV_TEMPLATES){
                templateEntity.setFavorite(!templateEntity.isFavorite());
                if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
                int index = templateFragmentViewModel.getIndexById(templateEntity.getId());
                if(index!=-1){
                    templateFragmentViewModel.getTemplateList().remove(index);
                    templatesRecyclerAdapter.notifyItemRemoved(index);
                    if(templateFragmentViewModel.getTemplateList().size()==0){
                        customShowNoContentFound();
                    }else {
                        viewHolder.templatesRecyclerView.post(new Runnable() {
                            @Override
                            public void run() {
                                new TemplatesScrollListener().onScrollEnd();
                            }
                        });

                    }
                }
                }else{
                    templatesRecyclerAdapter.notifyItemChanged(position);
                }

                callFavApi(templateEntity,position);
            }
        }
    }

    public interface ActionsListener {
        void onTemplateItemClick(TemplateEntity templateEntity, int templateType);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private boolean hasMinRequirementForScrollApi(){
        return (templateFragmentViewModel.getTemplateList().size() >= Constants.TEMPLATES_API_LIMIT-1);/* template api call cache  */
    }

    private class TemplatesScrollListener implements TemplatesRecyclerAdapter.ScrollListener{
        @Override
        public void onScrollEnd() {
            if(AppUtils.hasInternetConnection(getContext())){
                ApiModel apiModel = templateFragmentViewModel.getTemplatesApiModelAsLiveData().getValue();
                if(apiModel.isShouldCallApi()
                        && apiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST && hasMinRequirementForScrollApi()){
                    getTemplates(false);
                }
            }
        }
    }


    private class HardRefreshDeleteListener implements AppDatabase.DbOperationCallbackListener{
        @Override
        public void onSuccess() {
            showTemplatesGrid();
            templateFragmentViewModel.getTemplateList().clear();
            templatesRecyclerAdapter.notifyDataSetChanged();
            getTemplates(false);
        }
    }

    public interface ParentHandShakes{
        void onDataEmpty();
        void onData(int size);
        void onRegionIdNull();
        void onCreateMemeClick(TemplateEntity templateEntity);
    }

}
