package com.thugdroid.memeking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.room.entity.LoggedInUserEntity;
import com.thugdroid.memeking.room.entity.SocialUsernameEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.room.repository.SocialUsernameRepository;
import com.thugdroid.memeking.utils.AppUtils;

import java.util.HashMap;

public class TemplateCreditsFragment extends CustomFragment {
    private String instaUsername;
    private ViewHolder viewHolder;
    private String authorId;
    private SocialUsernameRepository socialUsernameRepository;
    private ParentHandShakes parentHandShakes;
    public TemplateCreditsFragment() {
    }

    public static TemplateCreditsFragment newInstance(String authorId,ParentHandShakes parentHandShakes){
        TemplateCreditsFragment templateCreditsFragment=new TemplateCreditsFragment();
        templateCreditsFragment.setAuthorId(authorId);
        templateCreditsFragment.setParentHandShakes(parentHandShakes);
        return templateCreditsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.layout_template_credits,container,false);
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

    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
    }

    @Override
    public void initListeners() {
        viewHolder.instaUsernameTv.setOnClickListener(this::onClick);
    }

    @Override
    public void initObservers() {
        if(getAuthorId()!=null){
            getSocialUsernameRepository().get(getAuthorId()).observe(getViewLifecycleOwner(), new Observer<String>() {
                @Override
                public void onChanged(String instaUsername) {
                    setInstaUsername(instaUsername);
                    notifyParent(instaUsername);
                    if(instaUsername==null){
                        getTemplateCredits();
                    }else{
                        if(!LoggedInUserEntity.isInstaUsernameNull(instaUsername)){
                            setUsernameText();
                        }
                    }
                }
            });
        }else{
            notifyParent(null);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.templateCreditsUsernameTv:
                getMainActivity().goToUrl(AppUtils.getInstaProfileUrl(getInstaUsername()),false);
                break;
        }
    }

    public String getInstaUsername() {
        return instaUsername;
    }

    private void setUsernameText(){
        viewHolder.instaUsernameTv.setText(AppUtils.getInstaDisplayUsername(getInstaUsername()));
    }

    public void setInstaUsername(String instaUsername) {
        this.instaUsername = instaUsername;
    }

    public SocialUsernameRepository getSocialUsernameRepository() {
        if(socialUsernameRepository==null){
            socialUsernameRepository=new ViewModelProvider(this).get(SocialUsernameRepository.class);
        }
        return socialUsernameRepository;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public ParentHandShakes getParentHandShakes() {
        return parentHandShakes;
    }

    public void setParentHandShakes(ParentHandShakes parentHandShakes) {
        this.parentHandShakes = parentHandShakes;
    }

    private void notifyParent(String data){
        if(getParentHandShakes()!=null){
            if(LoggedInUserEntity.isInstaUsernameNull(data)){
                getParentHandShakes().onNoData();
            }    else {
                getParentHandShakes().onData();
            }
        }

    }

    private class ViewHolder{
        TextView instaUsernameTv;
        public ViewHolder() {
            instaUsernameTv=findViewById(R.id.templateCreditsUsernameTv);
        }
    }

    private void getTemplateCredits(){
        HashMap apiData = new HashMap();
        apiData.put(TemplateEntity.APIKEY_AUTHOR_ID,getAuthorId());
        getFireFunctions().callApi(ApiUrls.GET_TEMPLATE_CREDITS,apiData,new InstaUsernameApiListener());
    }
    private class InstaUsernameApiListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            switch (statusCode){
                case HttpCodes.SUCCESS:
                    String instaUsername = LoggedInUserEntity.getInstaUserNameFromResponse(resultObject);
                    getSocialUsernameRepository().insert(new SocialUsernameEntity(getAuthorId(),instaUsername));
                    break;
                default:
                    notifyParent(null);
            }
        }

        @Override
        public void onFailure(Exception e) {
            e.printStackTrace();
            notifyParent(null);
        }
    }

    public interface ParentHandShakes{
        void onData();
        void onNoData();
    }

}
