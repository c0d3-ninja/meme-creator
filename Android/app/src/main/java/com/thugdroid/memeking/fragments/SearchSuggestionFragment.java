package com.thugdroid.memeking.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.adapters.SearchSuggestionAdapter;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.model.FacetModel;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.viewmodel.SearchSuggestionFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SearchSuggestionFragment extends CustomFragment {
    public static final int MODE_FULLSCREEN=1;
    public static final int MODE_FRAGMENT=2;

    private ViewHolder viewHolder;
    private SearchSuggestionFragmentViewModel searchSuggestionFragmentViewModel;
    private SearchSuggestionAdapter searchSuggestionAdapter;
    private WindowViewModel windowViewModel;
    private int templateType;
    private Handler searchTimeOut;
    private Listeners listeners;
    private CategoryEntity categoryEntity;
    private boolean autoFocus;
    private int mode;
    private int minChar;
    private boolean deeperSearchEnabled;
    private String deeperSearchHighlightedStr;
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public SearchSuggestionFragment(){

    }

    public static SearchSuggestionFragment newInstance(int templateType,
                                                       CategoryEntity categoryEntity,
                                                       int mode,
                                                       int minChar,
                                                       boolean deeperSearchEnabled,
                                                       boolean autoFocus,
                                                       Listeners  listeners
                                                       ){
        SearchSuggestionFragment searchSuggestionFragment =new SearchSuggestionFragment();
        searchSuggestionFragment.setTemplateType(templateType);
        searchSuggestionFragment.setCategoryEntity(categoryEntity);
        searchSuggestionFragment.setMode(mode);
        searchSuggestionFragment.setMinChar(minChar);
        searchSuggestionFragment.setDeeperSearchEnabled(deeperSearchEnabled);
        searchSuggestionFragment.setAutoFocus(autoFocus);
        searchSuggestionFragment.setListeners(listeners);
        return searchSuggestionFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
        initObservers();
        if(autoFocus){
            AppUtils.focusEditText(getContext(),viewHolder.searchEditText);
        }

    }

    @Override
    public void initVariables() {
        searchSuggestionFragmentViewModel =new ViewModelProvider(this).get(SearchSuggestionFragmentViewModel.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        deeperSearchHighlightedStr=getString(R.string.try_advanced_search_part1)+" <em>"+getString(R.string.try_advanced_search_part2)+"</em>";
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
        searchSuggestionAdapter=new SearchSuggestionAdapter(getContext(),
                searchSuggestionFragmentViewModel.getSearchSuggestionList()
                ,isDeeperSearchEnabled());
        searchSuggestionAdapter.setListItemListener(new SuggestionClickListener());
        viewHolder.searchSuggestion.setAdapter(searchSuggestionAdapter);
        viewHolder.searchEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH){
                    String currStr = viewHolder.searchEditText.getText().toString().trim();
                    if(currStr.length()<minChar){
                        showMsg(R.string.enter_atlease_x_letter,minChar);
                        return false;
                    }else{
                        if(listeners!=null){
                            viewHolder.searchEditText.clearFocus();
                            AppUtils.hideKeyboard(getContext(),viewHolder.searchEditText);
                            listeners.onSearchEnterClick(currStr);
                        }
                    }

                    return true;
                }
                return false;
            }
        });
        if(templateType==Constants.API_TYPE_TEMPLATES_FEED){
            if(categoryEntity!=null && !AppUtils.isAllTypeCategory(categoryEntity.getId(),windowViewModel.getRegionId())){
                viewHolder.searchEditText.setHint(getString(R.string.search_in_x,categoryEntity.getName()));
            }else{
                viewHolder.searchEditText.setHint(R.string.search_movies_dialogues);
            }
        }else if(getTemplateType() == Constants.API_TYPE_TEMPLATES_GROUP){
            viewHolder.searchEditText.setHint(R.string.search_movies_dialogues);
        }else if(templateType==Constants.API_TYPE_MY_TEMPLATES){
            viewHolder.searchEditText.setHint(R.string.search_my_templates);
        }else if(templateType==Constants.API_TYPE_FAV_TEMPLATES){
            viewHolder.searchEditText.setHint(R.string.seach_favorite_templates);
        }
    }

    @Override
    public void initListeners() {
        viewHolder.searchClose.setOnClickListener(this::onClick);
        viewHolder.searchBack.setOnClickListener(this::onClick);
        viewHolder.searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(searchTimeOut==null){
                    searchTimeOut=new Handler();
                }
                searchTimeOut.removeCallbacksAndMessages(null);
                String currentStr = s.toString();
                if(currentStr.trim().length()==0){
                    setApiModel(ApiModel.LOADINGSTATE_IDLE,HttpCodes.IDLE);
                    searchSuggestionFragmentViewModel.clearSearchSuggestionList();
                    searchSuggestionAdapter.notifyDataSetChanged();
                }else{
                    searchTimeOut.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getSearchSuggestions();
                        }
                    },Constants.SEARCH_TIMEOUT);

                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public void initObservers() {
        searchSuggestionFragmentViewModel.getSuggestionRequest().observe(getViewLifecycleOwner(), new Observer<ApiModel>() {
            @Override
            public void onChanged(ApiModel apiModel) {
                switch (apiModel.getLoadingState()){
                    case ApiModel.LOADINGSTATE_REQUEST:
                        viewHolder.searchProgress.setVisibility(View.VISIBLE);
                        viewHolder.searchClose.setVisibility(View.GONE);
                        break;
                    default:
                        viewHolder.searchProgress.setVisibility(View.GONE);
                        if(viewHolder.searchEditText.getText().toString().trim().equals("")){
                            viewHolder.searchClose.setVisibility(View.GONE);
                        }else{
                            viewHolder.searchClose.setVisibility(View.VISIBLE);
                        }
                        break;

                }
            }
        });
    }

    public void setAutoFocus(boolean autoFocus) {
        this.autoFocus = autoFocus;
    }

    public void setMinChar(int minChar) {
        this.minChar = minChar;
    }

    public int getTemplateType() {
        return templateType;
    }


    private class ViewHolder{
        ListView searchSuggestion;
        ImageView searchClose;
        ProgressBar searchProgress;
        EditText searchEditText;
        ConstraintLayout searchBack;
        ViewHolder(){
            searchSuggestion=findViewById(R.id.searchSuggestion);
            searchClose=findViewById(R.id.searchClose);
            searchProgress=findViewById(R.id.searchProgressBar);
            searchEditText=findViewById(R.id.searchEditText);
            searchBack=findViewById(R.id.searchBack);
        }
    }


    private void setApiModel(int loadingState,int httpCode){
        ApiModel apiModel = searchSuggestionFragmentViewModel.getSuggestionRequest().getValue();
        apiModel.setLoadingState(loadingState);
        apiModel.setStatusCode(httpCode);
        searchSuggestionFragmentViewModel.getSuggestionRequest().setValue(apiModel);
    }

    private void getSearchSuggestions(){
        setApiModel(ApiModel.LOADINGSTATE_REQUEST,HttpCodes.IDLE);
        String prevSearchStr= viewHolder.searchEditText.getText().toString();
        HashMap hashMap = new HashMap();
        hashMap.put(ApiConstants.KEY_SEARCHSTR,prevSearchStr);
        if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
            getFireFunctions().callApi(ApiUrls.GET_FAVORITES_SEARCH_SUGGESTION,hashMap,new SearchSuggestionListener(prevSearchStr));
        }else{
            if(getTemplateType()==Constants.API_TYPE_MY_TEMPLATES){
                hashMap.put(TemplateEntity.APIKEY_CREATEDBY,windowViewModel.getUserId());
            }else if(getTemplateType()==Constants.API_TYPE_TEMPLATES_FEED){
                if(categoryEntity!=null){
                    hashMap.put(TemplateEntity.APIKEY_CATEGORY_ID,categoryEntity.getId());
                }
                hashMap.put(LoggedInUserEntity.KEY_REGION_ID,windowViewModel.getRegionId());
            }else if(getTemplateType()==Constants.API_TYPE_TEMPLATES_GROUP){
                hashMap.put(LoggedInUserEntity.KEY_REGION_ID,windowViewModel.getRegionId());
            }
            getFireFunctions().callApi(ApiUrls.GET_FEED_SEARCH_SUGGESTION,hashMap,new SearchSuggestionListener(prevSearchStr));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.searchClose:
                viewHolder.searchEditText.setText("");
                break;
            case R.id.searchBack:
                if(listeners!=null){
                    viewHolder.searchEditText.clearFocus();
                    AppUtils.hideKeyboard(getContext(),viewHolder.searchEditText);
                    listeners.onBackClick();
                }
                break;
        }
    }



    public void setListeners(Listeners listeners) {
        this.listeners = listeners;
    }

    public void setDeeperSearchEnabled(boolean deeperSearchEnabled) {
        this.deeperSearchEnabled = deeperSearchEnabled;
    }

    public void setTemplateType(int templateType) {
        this.templateType = templateType;
    }

    public void setCategoryEntity(CategoryEntity categoryEntity) {
        this.categoryEntity = categoryEntity;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    private class SearchSuggestionListener implements FireFunctions.ApiListener{
        String searchStr;

        public SearchSuggestionListener(String searchStr) {
            this.searchStr = searchStr;
        }

        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            if(statusCode== HttpCodes.SUCCESS){
                setApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS,statusCode);
                if(searchStr!=null && searchStr.equals(viewHolder.searchEditText.getText().toString())){
                    HashMap resultMap =(HashMap) resultObject;
                    List<HashMap> resultList = (ArrayList<HashMap>) resultMap.get(FacetModel.API_KEY_HITS);
                    List<FacetModel> facetModels = new ArrayList<>();
                    for (HashMap facetMap : resultList) {
                        facetModels.add(new FacetModel(
                                (String)facetMap.get(FacetModel.API_KEY_VALUE),
                                (String)facetMap.get(FacetModel.API_KEY_HIGHLIGHTED),
                                (int)facetMap.get(FacetModel.API_KEY_COUNT)
                        ));
                    }
                    if(isDeeperSearchEnabled()){
                        FacetModel facetModel = new FacetModel(searchStr,deeperSearchHighlightedStr,1);
                        facetModels.add(facetModel);
                    }
                    searchSuggestionFragmentViewModel.setSearchSuggestionList(facetModels);
                    searchSuggestionAdapter.notifyDataSetChanged();
                }
            }else{
                setApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,statusCode);
            }

        }

        @Override
        public void onFailure(Exception e) {
            setApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE,HttpCodes.INTERNALSERVERERROR);
        }
    }

    public boolean isDeeperSearchEnabled() {
        return deeperSearchEnabled;
    }

    private class SuggestionClickListener implements SearchSuggestionAdapter.ListItemListener{
        @Override
        public void onItemClick(String searchStr) {
            if(listeners!=null){
                viewHolder.searchEditText.clearFocus();
                AppUtils.hideKeyboard(getContext(),viewHolder.searchEditText);
                listeners.onSearchSuggestionClick(searchStr);
            }
        }

        @Override
        public void onSearchPreviewClick(String searchStr) {
            viewHolder.searchEditText.setText(searchStr);
            viewHolder.searchEditText.setSelection(searchStr.length());
        }
    }

    public interface Listeners{
        void onSearchSuggestionClick(String str);
        void onSearchEnterClick(String str);
        void onBackClick();
    }
}
