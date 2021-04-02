package com.thugdroid.memeking.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.thugdroid.memeking.MediaFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.model.MyImage;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.SocialUsernameEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.room.repository.AppPrefsRepository;
import com.thugdroid.memeking.room.repository.MemesRepository;
import com.thugdroid.memeking.room.repository.SocialUsernameRepository;
import com.thugdroid.memeking.ui.AddTextDialog;
import com.thugdroid.memeking.ui.ConfirmationDialog;
import com.thugdroid.memeking.ui.LoadingDialog;
import com.thugdroid.memeking.ui.UsernameDialog;
import com.thugdroid.memeking.ui.data.SettingsMenuItemData;
import com.thugdroid.memeking.ui.data.SettingsMenuItemsData;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.utils.FileUtils;
import com.thugdroid.memeking.viewmodel.MemesFragmentViewModel;
import com.thugdroid.memeking.viewmodel.SettingsFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class SettingsFragment extends MediaFragment {
    private static final int PICKUP_LOGO=1;
    private AppPrefsRepository appPrefsRepository;
    private ViewHolder viewHolder;
    private LoadingDialog loadingDialog;
    private WindowViewModel windowViewModel;
    private SettingsFragmentViewModel settingsFragmentViewModel;
    private SocialUsernameRepository socialUsernameRepository;
    /*override start*/
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public SettingsFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
        initObservers();
        if(windowViewModel.getLoggedInUserEntity()!=null){
            AppUtils.setHTML(viewHolder.signedInAsTv,getString(R.string.signed_in_as,"<b>"+windowViewModel.getLoggedInUserEntity().getEmail()+"</b>"));
        }
    }




    @Override
    public void initVariables() {
        appPrefsRepository =new ViewModelProvider(this).get(AppPrefsRepository.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
        SettingsMenuFragment settingsMenuFragment = SettingsMenuFragment.newInstance(SettingsMenuItemsData.getSettingsMenuModel(),new MenuItemClickListener());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.settingsMenuContainerParent,settingsMenuFragment).commit();
        /*synchronizing commit fragment for getting view by tag*/
        getSupportFragmentManager().executePendingTransactions();
        viewHolder.setLogoTv((view.findViewWithTag(SettingsMenuFragment.getViewTag(R.id.settingsMenuItemTitle,
                SettingsMenuItemData.ID_SET_LOGO))));
        viewHolder.setLogoIv((view.findViewWithTag(SettingsMenuFragment.getViewTag(R.id.settingsMenuItemLogo,
                SettingsMenuItemData.ID_SET_LOGO))));
        viewHolder.setInstaUsernameTv((view.findViewWithTag(SettingsMenuFragment.getViewTag(R.id.settingsMenuItemTitle,
                SettingsMenuItemData.ID_INSTA_USERNAME))));
    }

    @Override
    public void initListeners() {
        viewHolder.back.setOnClickListener(this::onClick);
    }

    @Override
    public void initObservers() {
        appPrefsRepository.getPref(AppPrefsEntity.LOGO_PATH).observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                if(viewHolder.logoTv!=null && viewHolder.logoIv!=null){
                    if(s!=null){
                        Glide.with(getContext()).load(s).listener(new RequestListener<Drawable>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                appPrefsRepository.delete(AppPrefsEntity.LOGO_PATH);
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                viewHolder.logoTv.setText(R.string.change_logo);
                                return false;
                            }
                        }).into(viewHolder.logoIv);
                    }else{
                        viewHolder.logoTv.setText(R.string.set_logo);
                        viewHolder.logoIv.setImageResource(R.drawable.memeking_googleplay);
                    }
                }

            }
        });
        appPrefsRepository.getPref(AppPrefsEntity.INSTA_USERNAME).observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String instaUsername) {
                getSettingsFragmentViewModel().setInstaUsername(instaUsername);
                if(instaUsername==null){
                    viewHolder.instaUsernameTv.setText(R.string.instagram_username);
                }else{
                    viewHolder.instaUsernameTv.setText(AppUtils.getInstaDisplayUsername(instaUsername));
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.settingsBackParent:
                getMainActivity().onBackPressed();
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PICKUP_LOGO){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickLogo();
            }else{
                showMsg(R.string.allow_permission_to_choose_image);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case PICKUP_LOGO:
                if(resultCode==RESULT_OK && data!=null){
                    Uri uri=data.getData();
                    MyImage imageDetails = AppUtils.getDetailedImage(getContext(),uri);
                    String mimeType=imageDetails.getMimeType();
                    if(mimeType!=null && AppUtils.getIndexOf(Constants.IMAGE_ACCEPTABLE_MIME_TYPES,mimeType)!=-1){
                        appPrefsRepository.updateLogoUrl(uri.toString());
                    }else{
                        showMsg(R.string.jpg_png_images_allowed);
                    }
                }
                break;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        recordScreenView();
    }
    /*override end*/

    /*other methods start*/
    private void clearCache(){
        new ClearCache().execute();
    }
    private void showClearCacheDialog(){
        ConfirmationDialog confirmationDialog=new ConfirmationDialog(getContext());
        confirmationDialog.setTitle(getString(R.string.do_you_want_to_clear_cache));
        confirmationDialog.setOnClickListener(new ClearCacheConfirmationListener());
        confirmationDialog.show();
    }
    private void showFeedbackDialog(){
        AddTextDialog addTextDialog=new AddTextDialog(getMainActivity());
        addTextDialog.setPositiveBtnText(getString(R.string.send));
        addTextDialog.setPlaceHolder(getString(R.string.enter_feedback));
        addTextDialog.setDialogBtnClickListener(new FeedbackDialogListener());
        addTextDialog.show("");
    }
    private void recordScreenView() {
        recordScreen(FireConstants.SCREEN_SETTINGS);
    }
    private void signOut(){
        unSubscribeFCMTopic(windowViewModel.getRegionId());
        getGlobalMemesFeedDb().getMemes().clear();
        new ClearDataForSignOut().execute();
    }
    private void showLoadingDialog(int stringResId){
        if(loadingDialog==null){
            loadingDialog=new LoadingDialog(getMainActivity());
            loadingDialog.setLoadingText(getString(stringResId));
        }
        loadingDialog.show();
    }

    private void hideLoadingDialog(){
        if(loadingDialog!=null){
            loadingDialog.dismiss();
        }
    }

    private void pickLogo(){
        if(isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE,PICKUP_LOGO)){
            openGallery(PICKUP_LOGO);
        }
    }

    private void showSignOutAlertDialog(){
        ConfirmationDialog confirmationDialog=new ConfirmationDialog(getMainActivity());
        confirmationDialog.setTitle(getString(R.string.signout_confirmation));
        confirmationDialog.setOnClickListener(new SignOutConfirmationListener());
        confirmationDialog.show();

    }

    private void sendFeedback(String description){
        HashMap apiMap=new HashMap();
        apiMap.put(ApiConstants.KEY_DESCRIPTION,description);
        apiMap.put(TemplateEntity.APIKEY_REGION_ID,windowViewModel.getRegionId());
        getFireFunctions().callApi(ApiUrls.SEND_FEEDBACK,apiMap,null);
        showMsg(R.string.thanks_for_your_feedback);
    }


    private SocialUsernameRepository getSocialUsernameRepository(){
        if(socialUsernameRepository==null){
            socialUsernameRepository=new ViewModelProvider(this).get(SocialUsernameRepository.class);
        }
        return socialUsernameRepository;
    }

    private SettingsFragmentViewModel getSettingsFragmentViewModel(){
        if(settingsFragmentViewModel==null){
            settingsFragmentViewModel=new ViewModelProvider(this).get(SettingsFragmentViewModel.class);
        }
        return settingsFragmentViewModel;
    }

    private void showInstaUserNameDialog(){
        UsernameDialog usernameDialog=new UsernameDialog(getContext(),getSettingsFragmentViewModel().getInstaUsername(),new IUsernameDialogHandShakes());
        usernameDialog.show();
    }
    private void updateIUsernameInDb(String username){
        appPrefsRepository.insertPref(AppPrefsEntity.INSTA_USERNAME,username);
        getSocialUsernameRepository().insert(new SocialUsernameEntity(windowViewModel.getUserId(),username));
    }
    private class IUsernameDialogHandShakes implements UsernameDialog.ParentHandShakes{
        @Override
        public void onUnAuthorize() {

        }

        @Override
        public void onUpdateUsername(String username) {
            updateIUsernameInDb(username);
        }
    }
    /*other methods end*/

    /*classes start*/
    private class ViewHolder{
        View back;
        TextView signedInAsTv;
        ImageView logoIv;
        TextView logoTv,instaUsernameTv;
        public ViewHolder() {
            back=findViewById(R.id.settingsBackParent);
            signedInAsTv=findViewById(R.id.settingsSignedInAsTv);
        }

        public void setLogoIv(ImageView logoIv) {
            this.logoIv = logoIv;
        }

        public void setLogoTv(TextView logoTv) {
            this.logoTv = logoTv;
        }

        public void setInstaUsernameTv(TextView instaUsernameTv) {
            this.instaUsernameTv = instaUsernameTv;
        }
    }
    /*classes end*/

    private MemesFragmentViewModel getGlobalMemesFeedDb() {
        return new ViewModelProvider(requireActivity()).get(MemesFragmentViewModel.class);
    }

    private class ClearDataForSignOut extends AsyncTask {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingDialog(R.string.signing_out_dots);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            appPrefsRepository.clearAllTablesSync();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            hideLoadingDialog();
            windowViewModel.resetAll();
            clientSignOut();
            navigate(R.id.action_settingsFragment_to_signInFragment);
        }
    }
    private class SignOutConfirmationListener implements ConfirmationDialog.AlertDialogBtnClickListner{
        @Override
        public void onPositiveBtnClick(Dialog dialog) {
            dialog.dismiss();
            signOut();
        }

        @Override
        public void onNegativeBtnClick(Dialog dialog) {
            dialog.dismiss();
        }
    }
    private class FeedbackDialogListener implements AddTextDialog.DialogBtnClickListener{
        @Override
        public void onPositiveBtnClick(Dialog dialog, String text) {
            int minLength = getResources().getInteger(R.integer.feedbackDescriptionMinLength);
            if(text.trim().length()<minLength){
                showMsg(R.string.enter_atlease_x_letter,minLength);
                return;
            }
            if(!AppUtils.hasInternetConnection(getContext())){
                showMsg(R.string.no_internet_connection);
                return;
            }
            sendFeedback(text.trim());
            dialog.dismiss();
        }

        @Override
        public void onNegativeBtnClick(Dialog dialog) {
            dialog.dismiss();
        }

        @Override
        public void onDeleteBtnClick(Dialog dialog) {

        }
    }

    private class ClearCacheConfirmationListener implements ConfirmationDialog.AlertDialogBtnClickListner{
        @Override
        public void onPositiveBtnClick(Dialog dialog) {
            dialog.dismiss();
            clearCache();
        }

        @Override
        public void onNegativeBtnClick(Dialog dialog) {
            dialog.dismiss();
        }
    }

    private class ClearCache extends AsyncTask{
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingDialog(R.string.clearing_cache);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                FileUtils.deleteDir(getContext().getCacheDir());
            }catch (Exception e){
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            hideLoadingDialog();
            showMsg(R.string.cache_cleared_successfully);
        }
    }

    private class MenuItemClickListener implements SettingsMenuFragment.ListItemClickListener{
        @Override
        public void onListItemClick(int id) {
            switch (id){
                case SettingsMenuItemData.ID_SET_LOGO:
                    pickLogo();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_SETTINGS_USAGE,FireConstants.EVENT_SETTINGS_ADD_LOGO);
                    break;
                case SettingsMenuItemData.ID_CHANGE_REGION:
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_SETTINGS_USAGE,FireConstants.EVENT_SETTINGS_CHANGE_REGION);
                    SettingsFragmentDirections.ActionSettingsFragmentToSelectRegionFragment action=SettingsFragmentDirections.actionSettingsFragmentToSelectRegionFragment(Constants.MODE_CHANGE_REGION);
                    navigate(action);
                    break;
                case SettingsMenuItemData.ID_SIGNOUT:
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_SETTINGS_USAGE,FireConstants.EVENT_SETTINGS_SIGN_OUT);
                    showSignOutAlertDialog();
                    break;
                case SettingsMenuItemData.ID_SEND_FEEDBACK:
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_SETTINGS_USAGE,FireConstants.EVENT_SETTINGS_SEND_FEED_BACK);
                    showFeedbackDialog();
                    break;
                case SettingsMenuItemData.ID_CLEAR_CACHE:
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_SETTINGS_USAGE,FireConstants.EVENT_SETTINGS_CLEAR_CACHE);
                    showClearCacheDialog();
                    break;
                case SettingsMenuItemData.ID_INSTA_USERNAME:
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_SETTINGS_USAGE,FireConstants.EVENT_SETTINGS_INSTA_USERNAME);
                    showInstaUserNameDialog();
                    break;
            }
        }
    }
}
