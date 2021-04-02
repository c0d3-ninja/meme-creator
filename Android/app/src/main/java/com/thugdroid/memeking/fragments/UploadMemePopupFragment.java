package com.thugdroid.memeking.fragments;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.storage.UploadTask;
import com.thugdroid.memeking.MediaFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.ApiUrls;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.HttpCodes;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.firebasepack.storage.FireStorage;
import com.thugdroid.memeking.model.ApiModel;
import com.thugdroid.memeking.model.MyImage;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.MemesDataEntity;
import com.thugdroid.memeking.room.entity.SocialUsernameEntity;
import com.thugdroid.memeking.room.repository.AppPrefsRepository;
import com.thugdroid.memeking.room.repository.MemesRepository;
import com.thugdroid.memeking.room.repository.SocialUsernameRepository;
import com.thugdroid.memeking.ui.LoadingDialog;
import com.thugdroid.memeking.ui.UsernameDialog;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.viewmodel.UploadMemePopupFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;

import java.util.Date;
import java.util.HashMap;

public class UploadMemePopupFragment extends MediaFragment {

    private MyImage myImage;
    private ViewHolder viewHolder;
    private FireStorage fireStorage;
    private WindowViewModel windowViewModel;
    private UploadMemePopupFragmentViewModel uploadMemePopupFragmentViewModel;
    private ParentHandShakes  parentHandShakes;
    private NotificationManagerCompat  notificationManager;
    private NotificationCompat.Builder notificationBuilder;
    private LoadingDialog loadingDialog;
    private AppPrefsRepository appPrefsRepository;
    private SocialUsernameRepository socialUsernameRepository;


    private String memeUploading,memeUploadSuccess,memeUploadFailure,blockedUserString,noInternetConnectionMsg,memeUploadSuccesContent;
    public UploadMemePopupFragment() {
    }

    public static UploadMemePopupFragment newInstance(MyImage myImage,ParentHandShakes parentHandShakes){
        UploadMemePopupFragment uploadMemePopupFragment=new UploadMemePopupFragment();
        uploadMemePopupFragment.setMyImage(myImage);
        uploadMemePopupFragment.setParentHandShakes(parentHandShakes);
        return uploadMemePopupFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_uploadmeme_popup,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
        initObservers();
        showLoading();
        Glide.with(getContext()).asBitmap().load(myImage.getUri()).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                /*back press before image loadeded case*/
                if(isFragmentVisible()){
                 Glide.with(getContext()).load(resource).into(viewHolder.imageView);
                 showImage();
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    @Override
    public void initVariables() {
        memeUploading=getString(R.string.posting_meme);
        memeUploadSuccess=getString(R.string.meme_verification_under_progress)+" 🕵️‍♀️";
        memeUploadFailure=getString(R.string.post_meme_failure);
        blockedUserString=getString(R.string.user_blocked_msg);
        noInternetConnectionMsg=getString(R.string.no_internet_connection);
        memeUploadSuccesContent=getString(R.string.you_will_be_notified_once_your_meme_got_verified);
        fireStorage=new FireStorage(getContext());
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        uploadMemePopupFragmentViewModel=new ViewModelProvider(this).get(UploadMemePopupFragmentViewModel.class);
        appPrefsRepository=new ViewModelProvider(this).get(AppPrefsRepository.class);
        initNotification();
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder = new ViewHolder();

    }

    @Override
    public void initListeners() {
        viewHolder.positiveBtn.setOnClickListener(this::onClick);
        viewHolder.uploadMemeBackParent.setOnClickListener(this::onClick);
        viewHolder.iUsernameParent.setOnClickListener(this::onClick);
    }

    @Override
    public void initObservers() {
        appPrefsRepository.getPref(AppPrefsEntity.INSTA_USERNAME).observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String username) {
                uploadMemePopupFragmentViewModel.setiUsername(username);
                if(username==null){
                    viewHolder.usernameTv.setText(R.string.enter_instagram_username);
                    viewHolder.usernameTv.setSelected(true);
                }else{
                    viewHolder.usernameTv.setText("@"+username);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.uploadMemePositiveBtn:
                if(!AppUtils.hasInternetConnection(getContext())){
                    showMsg(R.string.no_internet_connection);
                }else{
                    uploadMemePopupFragmentViewModel.setUploadButtonClicked(true);
                    if(AppUtils.bytesToKb(myImage.getSize())> Constants.UPLOAD_IMAGE_MAX_SIZE_IN_KB){
                        showLoadingDialog(getString(R.string.preparing));
                        Bitmap bitmap= com.thugdroid.memeking.utils.AppUtils.getBitmapFromView(findViewById(R.id.itemMemeImage));
                        new SaveImage(Constants.SHARE_MEME_FOLDER_NAME,bitmap,true,new MemeSaveListener()).execute();
                        return;
                    }
                    postMeme();
                }
                break;
            case R.id.uploadMemeBackParent:
                goBack();
                break;
            case R.id.memeItemInstaUsernameParent:
                showInstaUserNameDialog();
                break;
        }
    }

    public void setMyImage(MyImage myImage) {
        this.myImage = myImage;
    }

    public void setParentHandShakes(ParentHandShakes parentHandShakes) {
        this.parentHandShakes = parentHandShakes;
    }

    private void showLoadingDialog(String loadingTxt){
        if(loadingDialog==null){
            loadingDialog=new LoadingDialog(getContext());
            loadingDialog.setLoadingText(loadingTxt);
        }
        loadingDialog.show();
    }
    private void hideLoadingDialog(){
        if(loadingDialog!=null){
            loadingDialog.dismiss();
        }
    }
    private void postMeme(){
        showUploadNotificationProgress();
        ApiModel imageUploadApiModel =uploadMemePopupFragmentViewModel.getImageUploadApiModel();
        if(imageUploadApiModel.getLoadingState()==ApiModel.LOADINGSTATE_REQUEST_SUCCESS){
            addMeme();
        }else if(imageUploadApiModel.getLoadingState()!=ApiModel.LOADINGSTATE_REQUEST){
            uploadImage();
        }
        showMsgWithGravity(R.string.posting_see_notification_for_uploading_status, Gravity.CENTER);
        goBack();
    }

    private void initNotification(){
        int notificationId = (int)new Date().getTime();
        uploadMemePopupFragmentViewModel.setNotificationId(notificationId);
        windowViewModel.getPendingNotificationIds().add(notificationId);
        notificationManager= NotificationManagerCompat.from(getContext());
        notificationBuilder=new NotificationCompat.Builder(getContext(),Constants.POST_NOTIFICATION_CHANNEL);
        notificationBuilder.setContentTitle(getString(R.string.app_name_with_space))
                .setContentText(memeUploading)
                .setSmallIcon(R.drawable.ic_notification)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.upload_meme);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(Constants.POST_NOTIFICATION_CHANNEL, name, importance);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager.createNotificationChannel(channel);
        }
        Glide.with(getContext()).asBitmap().load(myImage.getUri()).override(100,100).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                notificationBuilder.setLargeIcon(resource);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    private void showUploadNotificationProgress(){
        notificationBuilder
                .setContentText(memeUploading)
                .setProgress(0,0,true);
        notificationManager.notify(uploadMemePopupFragmentViewModel.getNotificationId(),notificationBuilder.build());
    }
    private void showUploadNotificationSuccess(){
        notificationBuilder
                .setContentTitle(memeUploadSuccess)
                .setContentText(memeUploadSuccesContent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(memeUploadSuccesContent))
                .setProgress(0,0,false);
        notificationManager.notify(uploadMemePopupFragmentViewModel.getNotificationId(),notificationBuilder.build());
    }
    private void showUploadNotificationFailure(String failureMsg){
        notificationBuilder
                .setContentText(failureMsg)
                .setProgress(0,0,false);
        notificationManager.notify(uploadMemePopupFragmentViewModel.getNotificationId(),notificationBuilder.build());
    }
    private void setImageUploadApiModel(int loadingState,int statusCode){
        ApiModel apiModel= uploadMemePopupFragmentViewModel.getImageUploadApiModel();
        apiModel.setLoadingState(loadingState);
        apiModel.setStatusCode(statusCode);
    }
    private void uploadImage(){
        if(AppUtils.hasInternetConnection(getContext())){
            setImageUploadApiModel(ApiModel.LOADINGSTATE_REQUEST, HttpCodes.IDLE);
            fireStorage.uploadImage(myImage.getUri(),Constants.MEMES_FIRESTOREPATH+"/"+windowViewModel.getLoggedInUserEntity().getId(),
                    myImage.getExtension(),new UploadImageListener());
        }else{
            showUploadNotificationFailure(noInternetConnectionMsg);
        }
    }
    private void addMeme(){
        HashMap data = new HashMap();
        data.put(MemesDataEntity.APIKEY_REGION_ID,windowViewModel.getRegionId());
        data.put(MemesDataEntity.APIKEY_IMGURL,myImage.getWebUri().toString());
            getFireFunctions().callApi(ApiUrls.ADD_MEME,
                    data
                    ,new  AddMemeApiListener());
    }
    private void goBack(){
        if(parentHandShakes!=null){
            parentHandShakes.onHide();
        }
    }
    private void showLoading(){
        viewHolder.progressBar.setVisibility(View.VISIBLE);
        viewHolder.imageView.setVisibility(View.GONE);
        viewHolder.positiveBtn.setVisibility(View.GONE);
    }
    private void showImage(){
        viewHolder.progressBar.setVisibility(View.GONE);
        viewHolder.imageView.setVisibility(View.VISIBLE);
        viewHolder.positiveBtn.setVisibility(View.VISIBLE);
    }


    /*insta username start*/

    private SocialUsernameRepository getSocialUsernameRepository(){
        if(socialUsernameRepository==null){
            socialUsernameRepository=new ViewModelProvider(this).get(SocialUsernameRepository.class);
        }
        return socialUsernameRepository;
    }

    private void showInstaUserNameDialog(){
        UsernameDialog usernameDialog=new UsernameDialog(getContext(),uploadMemePopupFragmentViewModel.getiUsername(),new IUsernameDialogHandShakes());
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
    /*insta username end*/


    private class ViewHolder{
        ImageView imageView;
        Button positiveBtn;
        View uploadMemeBackParent,usernameTvParent;
        View progressBar,iUsernameParent;
        TextView usernameTv;
        public ViewHolder() {
            imageView=findViewById(R.id.itemMemeImage);
            imageView.setVisibility(View.GONE);
            positiveBtn=findViewById(R.id.uploadMemePositiveBtn);
            positiveBtn.setVisibility(View.GONE);
            uploadMemeBackParent=findViewById(R.id.uploadMemeBackParent);
            progressBar=findViewById(R.id.memeItemLoading);
            progressBar.setVisibility(View.GONE);
            usernameTv=findViewById(R.id.memeItemInstaUsernameTv);
            usernameTvParent=findViewById(R.id.memeItemInstaUsernameTvParent);
            iUsernameParent=findViewById(R.id.memeItemInstaUsernameParent);
            findViewById(R.id.itemMemeHighlightView1).setVisibility(View.GONE);
            findViewById(R.id.memeItemBottomControls).setVisibility(View.GONE);
        }
    }
    private class UploadImageListener implements FireStorage.FileUploadListener{
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot, Uri uri) {
            myImage.setWebUri(uri);
            setImageUploadApiModel(ApiModel.LOADINGSTATE_REQUEST_SUCCESS, HttpCodes.SUCCESS);
            if(uploadMemePopupFragmentViewModel.isUploadButtonClicked()){
                addMeme();
            }
        }

        @Override
        public void onFail(Exception e) {
            setImageUploadApiModel(ApiModel.LOADINGSTATE_REQUEST_FAILURE, HttpCodes.INTERNALSERVERERROR);
            if(uploadMemePopupFragmentViewModel.isUploadButtonClicked()){
                showUploadNotificationFailure(memeUploadFailure);
            }
        }
    }

    private class AddMemeApiListener implements FireFunctions.ApiListener{
        @Override
        public void onSuccess(int statusCode, Object resultObject) {
            switch (statusCode){
                case HttpCodes.SUCCESS:
                    showUploadNotificationSuccess();
                    break;
                case HttpCodes.MESSAGE:
                    String message = getErrorMsgFromApiData(resultObject);
                    showUploadNotificationFailure(message==null?"":message);
                    break;
                case HttpCodes.BLOCKED:
                    showUploadNotificationFailure(blockedUserString);
                    break;
                default:
                    showUploadNotificationFailure(memeUploadFailure);
                    break;
            }
        }

        @Override
        public void onFailure(Exception e) {
            showUploadNotificationFailure(memeUploadFailure);
        }
    }

    private class MemeSaveListener implements MediaFragment.SaveImageListener{
        @Override
        public void onSuccess(String uriString) {
            try{
                Uri contentUri= Uri.parse(uriString);
                myImage = AppUtils.getDetailedImage(getContext(),contentUri);
                hideLoadingDialog();
                postMeme();
            }catch (Exception e){
                e.printStackTrace();
                hideLoadingDialog();
                showMsg(R.string.post_meme_failure);
            }

        }

        @Override
        public void onFailure() {
            hideLoadingDialog();
            showMsg(R.string.post_meme_failure);
        }
    }



    public interface ParentHandShakes{
        void onHide();
    }

}
