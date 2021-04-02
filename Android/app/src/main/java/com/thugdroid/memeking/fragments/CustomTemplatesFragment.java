package com.thugdroid.memeking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.thugdroid.libs.collagegrid.constants.GridNameConstants;
import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.constants.FragmentTags;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.viewmodel.WindowViewModel;
import com.thugdroid.memeking.viewmodel.db.SearchTemplateFragmentVariableDb;

public class CustomTemplatesFragment extends CustomFragment {

    private ViewHolder viewHolder;
    private WindowViewModel windowViewModel;
    private OnBackPressedCallback onBackPressedCallback;
    private SearchTemplateFragmentVariableDb searchTemplateFragmentVariableDb;
    private CustomTemplatesFragmentArgs args;

    /*preserve scroll from search fragment*/
    private int templateScrollX,templateScrollY;

    /*to fix Caused by java.lang.NoSuchMethodException*/
    public CustomTemplatesFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_customtemplates,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
        if(args.getIsFromTemplatesGroup()){
            renderGroupTemplatesFragment();
        }else{
            renderSearchTemplatesFragment();
        }

    }


    @Override
    public void initVariables() {
        args=CustomTemplatesFragmentArgs.fromBundle(getArguments());
        searchTemplateFragmentVariableDb=new ViewModelProvider(requireActivity()).get(SearchTemplateFragmentVariableDb.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
        viewHolder.title.setText(args.getSearchStr());
    }

    @Override
    public void initListeners() {
        viewHolder.backBtn.setOnClickListener(this::onClick);
        onBackPressedCallback= new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                onBackPressedCallback.setEnabled(false);
                searchTemplateFragmentVariableDb.clearAll();
                getMainActivity().onBackPressed();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);
    }

    @Override
    public void initObservers() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.customTemplatesBackParent:
                getMainActivity().onBackPressed();
                break;
        }

    }

    @Override
    public void onDestroyView() {
        /*hook to remove observers from templates fragment, commit allowing state loss is used for main activity destroy workaround*/
        try{
            Fragment fragment = getSupportFragmentManager().findFragmentByTag(FragmentTags.SEARCH_TEMPLATES_FRAGMENT);
            if(fragment!=null){
                if(fragment instanceof TemplatesSearchFragment){
                    TemplatesSearchFragment templatesFragment =(TemplatesSearchFragment) fragment;
                    setTemplateScrollX(templatesFragment.getScrollDx());
                    setTemplateScrollY(templatesFragment.getScrollDy());
                    getSupportFragmentManager().beginTransaction().remove(templatesFragment).commitAllowingStateLoss();
                }else if(fragment instanceof GroupTemplatesFragment){
                    GroupTemplatesFragment groupTemplatesFragment = (GroupTemplatesFragment) fragment;
                    setTemplateScrollX(groupTemplatesFragment.getScrollDx());
                    setTemplateScrollY(groupTemplatesFragment.getScrollDy());
                    getSupportFragmentManager().beginTransaction().remove(groupTemplatesFragment).commitAllowingStateLoss();
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        onBackPressedCallback.setEnabled(false);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        recordScreenView();
    }

    private void navigateToTemplatePreview(TemplateEntity templateEntity){
        CustomTemplatesFragmentDirections.ActionCustomTemplatesFragmentToTemplatePreviewFragment action =
                CustomTemplatesFragmentDirections.actionCustomTemplatesFragmentToTemplatePreviewFragment(templateEntity.getId(),templateEntity.getSearchTags()
                        ,templateEntity.getImageUrl(),templateEntity.getCategoryId(),templateEntity.isFavorite(),
                        templateEntity.getCreatedBy(),templateEntity.getCreatedTime(),templateEntity.getRegionId(), templateEntity.getAuthorId(),
                        args.getTemplateType(),true);
        navigate(action);
    }

    private void navigateToUploadTemplate(){
        CustomTemplatesFragmentDirections.ActionCustomTemplatesFragmentToUploadTemplateFragment action =
                CustomTemplatesFragmentDirections.actionCustomTemplatesFragmentToUploadTemplateFragment(null,null,null,
                        null,windowViewModel.getRegionId(),
                        false,
                        Constants.API_TYPE_TEMPLATES_FEED);
        navigate(action);
    }

    private void renderSearchTemplatesFragment(){
        TemplatesSearchFragment templatesFragment=TemplatesSearchFragment.newInstance(
                args.getSearchStr(),
                args.getSearchTemplateType(),
                windowViewModel.getSelectedCategoryEntityAsLiveData().getValue(),
                args.getSearchTemplateType()!= Constants.API_TYPE_FAV_TEMPLATES,
                new TemplatesSearchParentHandShakes(),
                new ActionsListener()
        );
        /*maintaining previous scroll*/
        templatesFragment.setScrollDx(getTemplateScrollX());
        templatesFragment.setScrollDy(getTemplateScrollY());
        getSupportFragmentManager().beginTransaction().replace(R.id.customTemplateContentsParent,templatesFragment,FragmentTags.SEARCH_TEMPLATES_FRAGMENT).commit();
    }
    private void renderGroupTemplatesFragment(){
        GroupTemplatesFragment groupTemplatesFragment = GroupTemplatesFragment.newInstance(
                args.getSearchStr(),
                new GroupTemplatesParentHandShake()
        );
        groupTemplatesFragment.setScrollDx(getTemplateScrollX());
        groupTemplatesFragment.setScrollDy(getTemplateScrollY());
        getSupportFragmentManager().beginTransaction().replace(R.id.customTemplateContentsParent,groupTemplatesFragment,FragmentTags.SEARCH_TEMPLATES_FRAGMENT).commit();


    }
    private void recordScreenView() {
        recordScreen(FireConstants.SCREEN_SEARCH_TEMPLATES);
    }


    public int getTemplateScrollX() {
        return templateScrollX;
    }

    public void setTemplateScrollX(int templateScrollX) {
        this.templateScrollX = templateScrollX;
    }

    public int getTemplateScrollY() {
        return templateScrollY;
    }

    public void setTemplateScrollY(int templateScrollY) {
        this.templateScrollY = templateScrollY;
    }

    private class ViewHolder{
        TextView title;
        View backBtn;
        public ViewHolder() {
            title=findViewById(R.id.customTemplatesTitle);
            backBtn=findViewById(R.id.customTemplatesBackParent);
        }
    }

    private void navigateToCreateMeme(TemplateEntity templateEntity){
        CustomTemplatesFragmentDirections.ActionCustomTemplatesFragmentToCreateMemeFragment action=
                CustomTemplatesFragmentDirections.actionCustomTemplatesFragmentToCreateMemeFragment(GridNameConstants.L1,templateEntity.getImageUrl(),templateEntity.getAuthorId());
        navigate(action);
    }

    private class TemplatesSearchParentHandShakes implements TemplatesSearchFragment.ParentHandShakes{
        @Override
        public void onCreateMemeClick(TemplateEntity templateEntity) {
            navigateToCreateMeme(templateEntity);
        }
    }
    private class GroupTemplatesParentHandShake implements GroupTemplatesFragment.ParentHandShakes{
        @Override
        public void onDataEmpty() {

        }

        @Override
        public void onData(int size) {

        }

        @Override
        public void onRegionIdNull() {

        }

        @Override
        public void onUploadTemplateBtnClick() {
            navigateToUploadTemplate();
        }

        @Override
        public void onTemplateItemClick(TemplateEntity templateEntity) {
            navigateToTemplatePreview(templateEntity);
        }

        @Override
        public void onCreateMemeClick(TemplateEntity templateEntity) {
            navigateToCreateMeme(templateEntity);
        }
    }
    private class ActionsListener implements TemplatesSearchFragment.ActionsListener {
        @Override
        public void onTemplateItemClick(TemplateEntity templateEntity, int templateType) {
            navigateToTemplatePreview(templateEntity);
        }

    }
}
