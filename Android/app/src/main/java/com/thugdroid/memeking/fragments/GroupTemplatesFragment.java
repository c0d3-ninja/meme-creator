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
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.model.NoTemplatesFoundConfigurations;
import com.thugdroid.memeking.room.AppDatabase;
import com.thugdroid.memeking.room.entity.GTsLastApiCallTimeEntity;
import com.thugdroid.memeking.room.entity.GroupTemplatesIdsEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.room.repository.GroupTemplatesRepository;
import com.thugdroid.memeking.room.repository.NewTemplatesDataRepository;
import com.thugdroid.memeking.smalllibs.SpacesItemDecoration;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.utils.FavApiUtils;
import com.thugdroid.memeking.utils.TimeUtils;
import com.thugdroid.memeking.viewmodel.NewTemplatesFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import com.thugdroid.memeking.constants.Constants.ApiGetType;
import com.thugdroid.memeking.viewmodel.db.TemplateDbViewModel;

public class GroupTemplatesFragment extends MediaFragment {

    private GroupTemplatesRepository groupTemplatesRepository;
    private NewTemplatesDataRepository newTemplatesDataRepository;
    private int scrollDx, scrollDy;
    private TemplatesRecyclerAdapter templatesRecyclerAdapter;
    private NewTemplatesFragmentViewModel newTemplatesFragmentViewModel;
    private ParentHandShakes parentHandShakes;
    private WindowViewModel windowViewModel;
    private String searchStr;
    private ViewHolder viewHolder;
    private TemplateDbViewModel templateDbViewModel;

    /*to fix Caused by java.lang.NoSuchMethodException*/
    public GroupTemplatesFragment() {
    }
    public static GroupTemplatesFragment newInstance(String searchStr,ParentHandShakes parentHandShakes){
        GroupTemplatesFragment groupTemplatesFragment = new GroupTemplatesFragment();
        groupTemplatesFragment.setSearchStr(searchStr);
        groupTemplatesFragment.setParentHandShakes(parentHandShakes);
        return groupTemplatesFragment;

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_templates,container,false);
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
        windowViewModel = new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        groupTemplatesRepository = new ViewModelProvider(this).get(GroupTemplatesRepository.class);
        newTemplatesFragmentViewModel = new ViewModelProvider(this).get(NewTemplatesFragmentViewModel.class);
        newTemplatesDataRepository=new ViewModelProvider(this).get(NewTemplatesDataRepository.class);
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();

    }

    @Override
    public void initListeners() {
    }

    @Override
    public void initObservers() {
        LiveData<List<TemplateEntity>> liveData=groupTemplatesRepository.getAllGroupTemplatesAsLiveData(getSearchStr(),windowViewModel.getRegionId());
        liveData.observe(getViewLifecycleOwner(), new Observer<List<TemplateEntity>>() {
            @Override
            public void onChanged(List<TemplateEntity> templateEntities) {
                liveData.removeObservers(getViewLifecycleOwner());
                initTemplateData(templateEntities);
            }
        });
    }

    @Override
    public void onClick(View v) {

    }

    /*other methods start*/
    private TemplateDbViewModel getTemplateDbViewModel(){
        if(templateDbViewModel==null){
            templateDbViewModel=new ViewModelProvider(this).get(TemplateDbViewModel.class);
        }
        return templateDbViewModel;
    }
    private int getTemplateType(){
        return Constants.API_TYPE_TEMPLATES_FEED;
    }
    private void initTemplateData(List<TemplateEntity> templateEntities){
        /*on back press case handle*/
        newTemplatesFragmentViewModel.getTemplateList().clear();
        newTemplatesFragmentViewModel.getTemplateList().addAll(templateEntities);
        templatesRecyclerAdapter.notifyDataSetChanged();
        if(templateEntities.size()==0){
            getTemplates(null);
        }else{
            //TODO: silent call templates api
            LiveData<Long> silentCallTimeLiveData = groupTemplatesRepository.getApiCalledTimeAsLiveData(getSearchStr());
            silentCallTimeLiveData.observe(getViewLifecycleOwner(), new Observer<Long>() {
                @Override
                public void onChanged(Long time) {
                    silentCallTimeLiveData.removeObservers(getViewLifecycleOwner());
                    if(time!=null && TimeUtils.getDifferenceInHrs(time,new Date().getTime())>=Constants.GROUP_TEMPLATES_SILENT_CALL_THRESHOLD_IN_HOURS){
                        getTemplatesSilently();
                    }
                }
            });
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                viewHolder.templatesRecyclerView.scrollBy(scrollDx,scrollDy);
            }
        },1);

    }
    private void getTemplates(ApiGetType apiGetType){
        if(!AppUtils.hasInternetConnection(getContext())){
             setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE, HttpCodes.NOINTERNETCONNECTION, hasMinRequirementForFetchOnScroll());
            return;
        }
        HashMap apiData=new HashMap();
        apiData.put(ApiConstants.KEY_LIMIT,Constants.GROUP_TEMPLATES_API_LIMIT);
        apiData.put(ApiConstants.KEY_SEARCHSTR,getSearchStr());
        int templatesListSize = newTemplatesFragmentViewModel.getTemplateList().size();
        if(templatesListSize>0 && apiGetType!=null){
            if(apiGetType==ApiGetType.SCROLL_GET){
                Object object = newTemplatesFragmentViewModel.getTemplateList().get(templatesListSize-1);
                if(object instanceof TemplateEntity){
                    TemplateEntity templateEntity=(TemplateEntity) object;
                    apiData.put(TemplateEntity.APIKEY_FROM,templateEntity.getCreatedTime()+"_"+templateEntity.getId());
                }
            }

        }
        setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST, HttpCodes.IDLE,false);
        groupTemplatesRepository.insertGTsLastCalledTime(new GTsLastApiCallTimeEntity(getSearchStr(),new Date().getTime()));
        getFireFunctions().callApi(ApiUrls.GET_TEMPLATES,apiData,new GetTemplatesApiListener(apiData));

    }

    private boolean hasMinRequirementForFetchOnScroll(){
        int templatesSize = newTemplatesFragmentViewModel.getTemplateList().size();
        return templatesSize!=0 && templatesSize%Constants.GROUP_TEMPLATES_API_LIMIT == 0;
    }
    private void showRecyclerViewLoading(){
        if(!newTemplatesFragmentViewModel.getTemplateList().containsAll(newTemplatesFragmentViewModel.getLoadingItems())){
            newTemplatesFragmentViewModel.getTemplateList().addAll(newTemplatesFragmentViewModel.getLoadingItems());
            viewHolder.templatesRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    templatesRecyclerAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void hideRecyclerViewLoading(){
        if(newTemplatesFragmentViewModel.getTemplateList().containsAll(newTemplatesFragmentViewModel.getLoadingItems())){
            newTemplatesFragmentViewModel.getTemplateList().removeAll(newTemplatesFragmentViewModel.getLoadingItems());
            viewHolder.templatesRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    templatesRecyclerAdapter.notifyDataSetChanged();
                }
            });
        }
    }

    private void setTemplatesApiModel(int loadingState,int statusCode,boolean isShouldCallApi){
        if(loadingState==ApiModel.LOADINGSTATE_REQUEST){
            showTemplatesGrid();
            showRecyclerViewLoading();
        }else if(loadingState == ApiModel.LOADINGSTATE_REQUEST_SUCCESS){
            hideRecyclerViewLoading();
            if(statusCode==HttpCodes.NOCONTENT && newTemplatesFragmentViewModel.getTemplateList().size()==0){
                customShowNoContentFound();
            }
        }else if(loadingState==ApiModel.LOADINGSTATE_REQUEST_FAILURE){
            hideRecyclerViewLoading();
            if(statusCode==HttpCodes.NOINTERNETCONNECTION){
                customShowNoInternetConnection();
            }else{
                customShowSomethingWentWrong();
            }
        }
        newTemplatesFragmentViewModel.setTemplatesApiModel(loadingState, statusCode,isShouldCallApi);
    }
    private void showTemplatesGrid(){
        if(viewHolder.swipeRefreshLayout.getVisibility()!=View.VISIBLE){
            hideNoInternetConnection();
            hideSomethingWentWrong();
            hideNoContentFound();
            viewHolder.swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    }
    private void hideTemplatesGrid(){
        viewHolder.swipeRefreshLayout.setVisibility(View.GONE);
    }
    private void customShowNoInternetConnection(){
        hideTemplatesGrid();
        hideNoContentFound();
        hideSomethingWentWrong();
        showNoInternetConnection(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTemplatesGrid();
                getTemplates(null);
            }
        });
    }
    private void customShowSomethingWentWrong(){
        hideTemplatesGrid();
        hideNoInternetConnection();
        hideNoContentFound();
        showSomethingWentWrong(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTemplatesGrid();
                getTemplates(null);
            }
        },windowViewModel.getRegionId());
    }
    private void customShowNoContentFound(){
        /*getString method used*/
        if(isFragmentVisible()){
            hideTemplatesGrid();
            hideNoInternetConnection();
            hideSomethingWentWrong();
            NoTemplatesFoundConfigurations noTemplatesFoundConfigurations =
                    new NoTemplatesFoundConfigurations(getString(R.string.no_meme_templates_found), true, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(parentHandShakes!=null){
                                parentHandShakes.onUploadTemplateBtnClick();
                            }
                        }
                    });
            noTemplatesFoundConfigurations.setRegionId(windowViewModel.getRegionId());
            showNoContentFound(noTemplatesFoundConfigurations);
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
    /*other methods end*/

    /*setters start*/
    public void setSearchStr(String searchStr) {
        this.searchStr = searchStr;
    }

    public void setParentHandShakes(ParentHandShakes parentHandShakes) {
        this.parentHandShakes = parentHandShakes;
    }

    public void setScrollDx(int scrollDx) {
        this.scrollDx = scrollDx;
    }

    public void setScrollDy(int scrollDy) {
        this.scrollDy = scrollDy;
    }
    /*setters end*/

    /*getters start*/
    public String getSearchStr() {
        return searchStr;
    }

    public int getScrollDx() {
        return viewHolder.templatesRecyclerView.computeHorizontalScrollOffset();
    }

    public int getScrollDy() {
        return viewHolder.templatesRecyclerView.computeVerticalScrollOffset();
    }

    public ParentHandShakes getParentHandShakes() {
        return parentHandShakes;
    }
    /*getters end*/


    /*fav api start*/
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
    /*fav api end*/



    /*classes start*/
    private class ViewHolder{
        SwipeRefreshLayout swipeRefreshLayout;
        RecyclerView templatesRecyclerView;
        ViewHolder(){
            templatesRecyclerView=findViewById(R.id.templatesRecyclerView);
            swipeRefreshLayout=findViewById(R.id.templatesSwipeRefreshLayout);
            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
            swipeRefreshLayout.setEnabled(false);
            int totalItemCount=getContext().getResources().getInteger(R.integer.templatesGridSpanCount);
            templatesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(),totalItemCount));
            templatesRecyclerView.addItemDecoration(new SpacesItemDecoration(getContext().getResources().getInteger(R.integer.templatesGridSpace),totalItemCount));
            templatesRecyclerAdapter=new TemplatesRecyclerAdapter(getContext(), newTemplatesFragmentViewModel.getTemplateList(),templatesRecyclerView);
            templatesRecyclerAdapter.setScrollListener(new TemplatesScrollListener());
            templatesRecyclerAdapter.setListItemClickListener(new TemplateItemClickListener());
            templatesRecyclerAdapter.setHasActionBtns(true);
            templatesRecyclerView.setAdapter(templatesRecyclerAdapter);
        }
    }

    private class TemplateItemClickListener implements TemplatesRecyclerAdapter.ListItemClickListener{
        @Override
        public void onClick(TemplateEntity templateEntity, int position) {
            if(parentHandShakes!=null){
                parentHandShakes.onTemplateItemClick(templateEntity);
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
            templateEntity.setFavorite(!templateEntity.isFavorite());
            templatesRecyclerAdapter.notifyItemChanged(position);
            callFavApi(templateEntity,position);
        }
    }
    private class TemplatesScrollListener implements TemplatesRecyclerAdapter.ScrollListener{
        @Override
        public void onScrollEnd() {
            if(hasMinRequirementForFetchOnScroll()){
                ApiModel apiModel = newTemplatesFragmentViewModel.getTemplatesApiModel();
                if(apiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST && apiModel.isShouldCallApi()){
                    getTemplates(ApiGetType.SCROLL_GET);
                }
            }
        }
    }
    /*classes end*/

    /*api listeners start*/
    private class GetTemplatesApiListener implements FireFunctions.ApiListener{
        HashMap apiData;

        public GetTemplatesApiListener(HashMap apiData) {
            this.apiData = apiData;
        }

        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            Object apiDataFrom = apiData.get(ApiConstants.KEY_FROM);
            switch (statusCode){
                case HttpCodes.SUCCESS:
                    List<TemplateEntity> templateEntities = TemplateEntity.getEntityList(resultObject);
                    int limit = (int)apiData.get(ApiConstants.KEY_LIMIT);
                    setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,templateEntities.size()>=limit);
                    List<GroupTemplatesIdsEntity> groupTemplateIdsEntities=new ArrayList<>();
                    for (TemplateEntity templateEntity : templateEntities) {
                        groupTemplateIdsEntities.add(
                                new GroupTemplatesIdsEntity(getSearchStr(),templateEntity.getId())
                        );
                    }
                    if(apiDataFrom==null){
                        newTemplatesFragmentViewModel.getTemplateList().clear();
                    }
                    newTemplatesFragmentViewModel.getTemplateList().addAll(templateEntities);
                    templatesRecyclerAdapter.notifyDataSetChanged();
                    newTemplatesDataRepository.insertAllTemplateData(templateEntities);
                    groupTemplatesRepository.insert(groupTemplateIdsEntities);
                    break;
                case HttpCodes.NOCONTENT:
                    setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode,false);
                    break;
                default:
                    setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE, statusCode, false);
                    break;
            }
            exeTemplateSizeHandShake(newTemplatesFragmentViewModel.getTemplateList().size());

        }

        @Override
        public void onFailure(Exception e) {
            setTemplatesApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE, HttpCodes.INTERNALSERVERERROR, false);
            exeTemplateSizeHandShake(newTemplatesFragmentViewModel.getTemplateList().size());
        }
    }
    /*api listeners end*/

    public interface ParentHandShakes{
        void onDataEmpty();
        void onData(int size);
        void onRegionIdNull();
        void onUploadTemplateBtnClick();
        void onCreateMemeClick(TemplateEntity templateEntity);
        void onTemplateItemClick(TemplateEntity templateEntity);
    }


    /*silent calls start*/
    private void getTemplatesSilently(){
        if(!AppUtils.hasInternetConnection(getContext())){
            return;
        }
        HashMap apiData=new HashMap();
        apiData.put(ApiConstants.KEY_LIMIT,Constants.GROUP_TEMPLATES_API_LIMIT);
        apiData.put(ApiConstants.KEY_SEARCHSTR,getSearchStr());
        groupTemplatesRepository.insertGTsLastCalledTime(new GTsLastApiCallTimeEntity(getSearchStr(),new Date().getTime()));
        getFireFunctions().callApi(ApiUrls.GET_TEMPLATES,apiData,new GetTemplatesSilentCallListener());
    }
    private class GetTemplatesSilentCallListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            switch (statusCode){
                case HttpCodes.SUCCESS:
                    List<TemplateEntity> templateEntities = TemplateEntity.getEntityList(resultObject);
                    List<GroupTemplatesIdsEntity> groupTemplateIdsEntities=new ArrayList<>();
                    for (TemplateEntity templateEntity : templateEntities) {
                        groupTemplateIdsEntities.add(
                                new GroupTemplatesIdsEntity(getSearchStr(),templateEntity.getId())
                        );
                    }
                    groupTemplatesRepository.deleteAllBySearchStr(getSearchStr(),new GroupTemplatesDeleteOperationListener(templateEntities,groupTemplateIdsEntities));
                    break;
            }
        }

        @Override
        public void onFailure(Exception e) {

        }
    }
    private class GroupTemplatesDeleteOperationListener implements AppDatabase.DbOperationCallbackListener{
        List<TemplateEntity> templateEntities;
        List<GroupTemplatesIdsEntity> groupTemplateIdsEntities;

        public GroupTemplatesDeleteOperationListener(List<TemplateEntity> templateEntities, List<GroupTemplatesIdsEntity> groupTemplateIdsEntities) {
            this.templateEntities = templateEntities;
            this.groupTemplateIdsEntities = groupTemplateIdsEntities;
        }

        @Override
        public void onSuccess() {
            newTemplatesDataRepository.insertAllTemplateData(templateEntities);
            groupTemplatesRepository.insert(groupTemplateIdsEntities);
        }
    }
    /*silent calls end*/
}
