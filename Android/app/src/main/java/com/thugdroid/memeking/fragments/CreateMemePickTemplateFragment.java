package com.thugdroid.memeking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FragmentTags;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.viewmodel.CreateMemePickTemplateViewModel;
import com.thugdroid.memeking.viewmodel.db.SearchTemplateFragmentVariableDb;

public class CreateMemePickTemplateFragment extends CustomFragment {

    private ViewHolder viewHolder;
    private int templateType;
    private CategoryEntity categoryEntity;
    private CreateMemePickTemplateViewModel createMemePickTemplateViewModel;
    private OnBackPressedCallback onBackPressedCallback;
    private SearchTemplateFragmentVariableDb searchTemplateFragmentVariableDb;
    private TemplateItemClickListener templateItemClickListener;
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public CreateMemePickTemplateFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_creatememe_templates,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
        initObservers();
        SearchSuggestionFragment searchSuggestionFragment=SearchSuggestionFragment.newInstance(templateType,
                categoryEntity,
                SearchSuggestionFragment.MODE_FRAGMENT,
                0,
                true,
                false,
                new SearchSuggestionListener());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.creatMemeTemplatesSearchSuggFragmentContainer,searchSuggestionFragment).commit();
        showTemplates();

    }



    @Override
    public void initVariables() {
        createMemePickTemplateViewModel =new ViewModelProvider(this).get(CreateMemePickTemplateViewModel.class);
        searchTemplateFragmentVariableDb=new ViewModelProvider(requireActivity()).get(SearchTemplateFragmentVariableDb.class);
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
    }

    @Override
    public void initListeners() {
        viewHolder.search.setOnClickListener(this::onClick);
        viewHolder.back.setOnClickListener(this::onClick);
        onBackPressedCallback=new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(viewHolder.searchSuggestionParent.getVisibility()==View.VISIBLE){
                    hideSearchFragment();
                }else{
                    onBackPressedCallback.setEnabled(false);
                    getMainActivity().onBackPressed();
                }
            }
        };
        getMainActivity().getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }

    @Override
    public void initObservers() {
        createMemePickTemplateViewModel.getSearchStr().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String searchStr) {
                if("".equals(searchStr)){
                    if(getTemplateType()==Constants.API_TYPE_MY_TEMPLATES){
                        viewHolder.title.setText(getString(R.string.my_templates));
                    }else if(getTemplateType()==Constants.API_TYPE_FAV_TEMPLATES){
                        viewHolder.title.setText(getString(R.string.favourites));
                    }else{
                        if(categoryEntity!=null){
                            viewHolder.title.setText(categoryEntity.getName());
                        }
                    }
                }else{
                    viewHolder.title.setText(searchStr);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.createMemeTemplatesSearchBtn:
                showSearchFragment();
                break;
            case R.id.createMemeTemplatesBack:
                getMainActivity().onBackPressed();
                break;
        }
    }

    public void setTemplateType(int templateType) {
        this.templateType = templateType;
    }

    public void setCategoryEntity(CategoryEntity categoryEntity) {
        this.categoryEntity = categoryEntity;
    }

    public void setTemplateItemClickListener(TemplateItemClickListener templateItemClickListener) {
        this.templateItemClickListener = templateItemClickListener;
    }

    private void showTemplates(){
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(FragmentTags.CREATE_MEME_PICK_TEMPLATE_LIST_FRAGMENT);
        if(fragment!=null){
            searchTemplateFragmentVariableDb.clearAll();
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
        if("".equals(createMemePickTemplateViewModel.getSearchStr().getValue())){
            TemplatesFragment templatesFragment=TemplatesFragment.newInstance(getTemplateType(),
                    categoryEntity,
                    new TemplateActionListener(),
                    new CreateMemePickToTemplateHandShake(),
                    false,
                    false);
            getSupportFragmentManager().beginTransaction().replace(R.id.createMemeTemplatesListParent,templatesFragment,
                    FragmentTags.CREATE_MEME_PICK_TEMPLATE_LIST_FRAGMENT).commit();
        }else {
            TemplatesSearchFragment templatesSearchFragment=TemplatesSearchFragment.newInstance(
                    createMemePickTemplateViewModel.getSearchStr().getValue(),
                    getTemplateType(),
                    categoryEntity,
                    false,
                    null,
                    new TemplateActionListener()
            );
            getSupportFragmentManager().beginTransaction().replace(R.id.createMemeTemplatesListParent,templatesSearchFragment,
                    FragmentTags.CREATE_MEME_PICK_TEMPLATE_LIST_FRAGMENT).commit();
        }
    }

    private void showSearchFragment(){
        viewHolder.searchSuggestionParent.setVisibility(View.VISIBLE);
    }

    private void hideSearchFragment(){
        viewHolder.searchSuggestionParent.setVisibility(View.GONE);
    }

    private  int getTemplateType() {
        return templateType;
    }

    private class ViewHolder{
        ConstraintLayout back,search;
        View searchSuggestionParent;
        TextView title;
        public ViewHolder() {
            back=findViewById(R.id.createMemeTemplatesBack);
            search=findViewById(R.id.createMemeTemplatesSearchBtn);
            searchSuggestionParent=findViewById(R.id.creatMemeTemplatesSearchSuggFragmentContainer);
            searchSuggestionParent.setVisibility(View.GONE);
            title =findViewById(R.id.createMemeTemplatesTitle);
        }
    }

    private void setSearchStrAndShowTemplates(String searchStr){
        searchStr=searchStr.trim();
        String prevSearchStr = createMemePickTemplateViewModel.getSearchStr().getValue();
        if(!searchStr.equals(prevSearchStr)){
            createMemePickTemplateViewModel.getSearchStr().setValue(searchStr);
            showTemplates();
        }
        hideSearchFragment();
    }

    @Override
    public void onDestroyView() {
        onBackPressedCallback.setEnabled(false);
        super.onDestroyView();
    }

    private class SearchSuggestionListener implements SearchSuggestionFragment.Listeners{
        @Override
        public void onSearchSuggestionClick(String str) {
            setSearchStrAndShowTemplates(str);
        }

        @Override
        public void onSearchEnterClick(String str) {
            setSearchStrAndShowTemplates(str);
        }

        @Override
        public void onBackClick() {
            hideSearchFragment();
        }

    }

    private class TemplateActionListener implements TemplatesFragment.ActionsListener,TemplatesSearchFragment.ActionsListener{
        @Override
        public void onTemplateItemClick(TemplateEntity templateEntity, int templateType) {
            if(templateItemClickListener!=null){
                templateItemClickListener.onClick(templateEntity);
            }
        }
    }

    private class CreateMemePickToTemplateHandShake implements TemplatesFragment.ParentHandShakes{
        @Override
        public void onDataEmpty() {

        }

        @Override
        public void onData(int size) {

        }

        @Override
        public void onRegionIdNull() {
            showMsg(R.string.session_expired_login_again);
            getMainActivity().onBackPressed();

        }

        @Override
        public void onCreateMemeClick(TemplateEntity templateEntity) {

        }
    }

    public interface TemplateItemClickListener{
        void onClick(TemplateEntity templateEntity);
    }
}
