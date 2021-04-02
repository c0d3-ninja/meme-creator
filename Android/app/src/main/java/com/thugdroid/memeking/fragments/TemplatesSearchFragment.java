package com.thugdroid.memeking.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.model.NoTemplatesFoundConfigurations;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.smalllibs.SpacesItemDecoration;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.utils.FavApiUtils;
import com.thugdroid.memeking.viewmodel.SearchTemplatesFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;
import com.thugdroid.memeking.viewmodel.db.SearchTemplateFragmentVariableDb;
import com.thugdroid.memeking.viewmodel.db.TemplateDbViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TemplatesSearchFragment extends MediaFragment implements SwipeRefreshLayout.OnRefreshListener{

    private ViewHolder viewHolder;
    private TemplatesRecyclerAdapter templatesRecyclerAdapter;
    private WindowViewModel windowViewModel;
    private ActionsListener actionsListener;
    private SearchTemplatesFragmentViewModel templateFragmentViewModel;
    private SearchTemplateFragmentVariableDb searchTemplateFragmentVariableDb;
    private int templateType;
    private String searchStr;
    private boolean isFirstTime;
    private int scrollDx, scrollDy;
    private CategoryEntity categoryEntity;
    private boolean showUploadBtn;
    private boolean isFromTemplateGroup;
    private TemplateDbViewModel templateDbViewModel;
    private ParentHandShakes parentHandShakes;

    /*to fix Caused by java.lang.NoSuchMethodException*/
    public TemplatesSearchFragment(){

    }

    public static TemplatesSearchFragment newInstance(String searchStr,
                                                      int templateType,
                                                      CategoryEntity  categoryEntity,
                                                      boolean showUploadBtn,
                                                      ParentHandShakes parentHandShakes,
                                                      ActionsListener actionsListener){
        TemplatesSearchFragment templatesSearchFragment = new TemplatesSearchFragment();
        templatesSearchFragment.setSearchStr(searchStr);
        templatesSearchFragment.setTemplateType(templateType);
        templatesSearchFragment.setCategoryEntity(categoryEntity);
        templatesSearchFragment.setParentHandShakes(parentHandShakes);
        templatesSearchFragment.setActionsListener(actionsListener);
        templatesSearchFragment.setShowUploadBtn(showUploadBtn);
        return templatesSearchFragment;
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
        initVariables();
        initViews(view);
        initListeners();
        initObservers();
    }

    @Override
    public void initVariables() {
        isFirstTime=true;
        templateFragmentViewModel = new ViewModelProvider(this).get(SearchTemplatesFragmentViewModel.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        searchTemplateFragmentVariableDb=new ViewModelProvider(requireActivity()).get(SearchTemplateFragmentVariableDb.class);
        searchTemplateFragmentVariableDb.setSearchStr(searchStr);
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
        templatesRecyclerAdapter.setHasActionBtns(true);
        viewHolder.templatesRecyclerView.setAdapter(templatesRecyclerAdapter);
    }

    @Override
    public void initListeners() {
        viewHolder.swipeRefreshLayout.setOnRefreshListener(this::onRefresh);
    }

    @Override
    public void initObservers() {
        searchTemplateFragmentVariableDb.getSearchData().observe(getViewLifecycleOwner(), new Observer<HashMap<String, List<TemplateEntity>>>() {
            @Override
            public void onChanged(HashMap<String, List<TemplateEntity>> templateEntityHashMap) {
                if(isFirstTime){
                    if(templateEntityHashMap==null){
                        initTemplateData(new ArrayList<>());
                    }else{
                        Object object = templateEntityHashMap.get(searchStr);
                        if(object==null || ((ArrayList)object).size()==0 ){
                            setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,HttpCodes.NOCONTENT,false);
                        }else{
                            initTemplateData((ArrayList)object);
                        }
                    }
                }
            }
        });

        templateFragmentViewModel.getTemplatesApiModel().observe(getViewLifecycleOwner(), new Observer<ApiModel>() {
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

    public void setParentHandShakes(ParentHandShakes parentHandShakes) {
        this.parentHandShakes = parentHandShakes;
    }

    public ParentHandShakes getParentHandShakes() {
        return parentHandShakes;
    }

    public boolean isFromTemplateGroup() {
        return isFromTemplateGroup;
    }

    public void setFromTemplateGroup(boolean fromTemplateGroup) {
        isFromTemplateGroup = fromTemplateGroup;
    }

    public void setCategoryEntity(CategoryEntity categoryEntity) {
        this.categoryEntity = categoryEntity;
    }

    public boolean isShowUploadBtn() {
        return showUploadBtn;
    }

    public void setShowUploadBtn(boolean showUploadBtn) {
        this.showUploadBtn = showUploadBtn;
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
        hideNoInternetConnection();
        hideTemplateGrid();
        hideSomethingWentWrong();
        NoTemplatesFoundConfigurations configurations=new NoTemplatesFoundConfigurations(getString(R.string.no_meme_templates_found), isShowUploadBtn(), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CustomTemplatesFragmentDirections.ActionCustomTemplatesFragmentToUploadTemplateFragment action =
                        CustomTemplatesFragmentDirections.actionCustomTemplatesFragmentToUploadTemplateFragment(null,null,null,
                                null,windowViewModel.getRegionId(),
                                false,
                                Constants.API_TYPE_TEMPLATES_FEED);
                navigate(action);
            }
        });
        if(windowViewModel.getLoggedInUserEntity()!=null){
            configurations.setRegionId(windowViewModel.getRegionId());
        }
        showNoContentFound(configurations);
    }

    public void setActionsListener(ActionsListener actionsListener) {
        this.actionsListener = actionsListener;
    }
    public ActionsListener getActionsListener() {
        return actionsListener;
    }
    private void initTemplateData(List<TemplateEntity> templateEntities){
        if(isFirstTime){
            isFirstTime=false;
            for (TemplateEntity templateEntity : templateEntities) {
                if(templateEntity!=null){
                    templateFragmentViewModel.getTemplateList().add(templateEntity);
                }
            }
            templatesRecyclerAdapter.notifyDataSetChanged();
            if(templateFragmentViewModel.getTemplatesApiModel().getValue().isShouldCallApi() &&  templateEntities.size()==0){
                getTemplates(false);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    viewHolder.templatesRecyclerView.scrollBy(scrollDx,scrollDy);
                }
            },1);

        }
    }
    public void setTemplateType(int type){
        templateType=type;
    }
    public int getTemplateType() {
        return templateType;
    }

    public String getSearchStr() {
        return searchStr==null?"":searchStr;
    }

    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }
    /*others end*/


    private class ViewHolder{
        SwipeRefreshLayout swipeRefreshLayout;
        RecyclerView templatesRecyclerView;
        ViewHolder(){
            templatesRecyclerView=findViewById(R.id.templatesRecyclerView);
            swipeRefreshLayout=findViewById(R.id.templatesSwipeRefreshLayout);
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
            swipeRefreshLayout.setEnabled(false);
        }
    }

    @Override
    public void onClick(View v) {

    }


    @Override
    public void onRefresh() {
        if(AppUtils.hasInternetConnection(getContext())){
            ApiModel apiModel=templateFragmentViewModel.getTemplatesApiModel().getValue();
            if(apiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST){
                viewHolder.swipeRefreshLayout.setRefreshing(true);
                getTemplates(true);
            }
        }else {
            viewHolder.swipeRefreshLayout.setRefreshing(false);
            showMsg(R.string.no_internet_connection);
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
        apiData.put(ApiConstants.KEY_LIMIT,Constants.TEMPLATES_API_LIMIT);
        apiData.put(ApiConstants.KEY_SEARCHSTR,getSearchStr());
        if(templateFragmentViewModel.getTemplateList().size()>0){
            Object object = templateFragmentViewModel.getTemplateList().get(templateFragmentViewModel.getTemplateList().size()-1);
            if(object instanceof  TemplateEntity && !isHardRefresh){
                TemplateEntity templateEntity=(TemplateEntity) object;
                if(isFromTemplateGroup()){
                    apiData.put(TemplateEntity.APIKEY_FROM,templateEntity.getCreatedTime()+"_"+templateEntity.getId());
                }else{
                    apiData.put(TemplateEntity.APIKEY_FROM,templateEntity.getCreatedTime());
                }

            }
        }
        setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST, HttpCodes.IDLE,false);
        if(isFromTemplateGroup()){
            getFireFunctions().callApi(ApiUrls.GET_TEMPLATES,apiData,new GetAllTemplatesListener(apiData));
        }else{
            if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
                apiData.put(TemplateEntity.APIKEY_CREATEDBY,windowViewModel.getUserId());
                getFireFunctions().callApi(ApiUrls.SEARCH_FAV_TEMPLATES,apiData,new GetAllTemplatesListener(apiData));
            }else{
                if(getTemplateType()==Constants.API_TYPE_TEMPLATES_FEED){
                    if(categoryEntity!=null){
                        apiData.put(TemplateEntity.APIKEY_CATEGORY_ID,categoryEntity.getId());
                    }
                    apiData.put(TemplateEntity.APIKEY_REGION_ID,windowViewModel.getRegionId());
                }else if(getTemplateType()==Constants.API_TYPE_TEMPLATES_GROUP){
                    apiData.put(ApiConstants.IS_FROM_TEMPLATES_GROUP,true);
                    apiData.put(TemplateEntity.APIKEY_REGION_ID,windowViewModel.getRegionId());
                }else{
                    apiData.put(TemplateEntity.APIKEY_CREATEDBY,windowViewModel.getUserId());
                }
                getFireFunctions().callApi(ApiUrls.SEARCH_FEED_TEMPLATES,apiData,new GetAllTemplatesListener(apiData));
            }
        }


    }

    private void setTemplatesApiModel(int loadingStatus,int statusCode,boolean shouldCallApi){
        ApiModel apiModel=templateFragmentViewModel.getTemplatesApiModel().getValue();
        apiModel.setLoadingState(loadingStatus);
        apiModel.setStatusCode(statusCode);
        apiModel.setShouldCallApi(shouldCallApi);
        templateFragmentViewModel.getTemplatesApiModel().setValue(apiModel);
    }

    private class GetAllTemplatesListener implements FireFunctions.ApiListener{
        HashMap apiData;

        public GetAllTemplatesListener(HashMap apiData) {
            this.apiData = apiData;
        }

        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            switch (statusCode){
                case HttpCodes.SUCCESS:
                    List<TemplateEntity> currentTemplateEntities=TemplateEntity.getEntityList(resultObject);
                    setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,currentTemplateEntities.size()>= Constants.TEMPLATES_API_LIMIT);
                    Object apiDataFrom = apiData.get(ApiConstants.KEY_FROM);
                    HashMap<String,List<TemplateEntity>> searchTemplateEntities=searchTemplateFragmentVariableDb.getSearchData().getValue();
                    if(searchTemplateEntities==null){
                        searchTemplateEntities=new HashMap<>();
                    }
                    if(apiDataFrom==null){
                        templateFragmentViewModel.getTemplateList().clear();
                        searchTemplateEntities.remove(searchStr);
                    }
                    List<TemplateEntity> dbEntities = searchTemplateEntities.get(searchStr);
                    if(dbEntities==null){
                        dbEntities=new ArrayList<>();
                    }
                    dbEntities.addAll(currentTemplateEntities);
                    searchTemplateEntities.put(searchStr,dbEntities);
                    searchTemplateFragmentVariableDb.getSearchData().setValue(searchTemplateEntities);
                    templateFragmentViewModel.getTemplateList().addAll(currentTemplateEntities);
                    templatesRecyclerAdapter.notifyDataSetChanged();
                    break;
                case HttpCodes.NOCONTENT:
                    setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,false);
                    break;
                default:
                    setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,statusCode,false);
                    break;
            }
        }

        @Override
        public void onFailure(Exception e) {
            setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.INTERNALSERVERERROR,false);
        }
    }

    /*fav api start*/

    private TemplateDbViewModel getTemplateDbViewModel(){
        if(templateDbViewModel==null){
            templateDbViewModel=new ViewModelProvider(this).get(TemplateDbViewModel.class);
        }
        return templateDbViewModel;
    }
    public void callFavApi(TemplateEntity templateEntity,int position){
        FavApiUtils favApiUtils= new FavApiUtils(getFireFunctions(),
                requireActivity(),templateEntity,
                getTemplateType(),position,
                windowViewModel,getTemplateDbViewModel(),
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
    /*fav api end*/


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

    private class TemplatesScrollListener implements TemplatesRecyclerAdapter.ScrollListener{
        @Override
        public void onScrollEnd() {
            if(AppUtils.hasInternetConnection(getContext())){
                ApiModel apiModel = templateFragmentViewModel.getTemplatesApiModel().getValue();
                if(apiModel.isShouldCallApi()
                        && apiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST
                        && Constants.TEMPLATES_API_LIMIT-1<= templateFragmentViewModel.getTemplateList().size() /* template api call cache  */
                ){
                    getTemplates(false);
                }
            }
        }
    }

    public interface ParentHandShakes{
        void onCreateMemeClick(TemplateEntity templateEntity);
    }

}