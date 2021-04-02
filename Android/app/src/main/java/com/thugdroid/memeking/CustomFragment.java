package com.thugdroid.memeking;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessaging;
import com.thugdroid.memeking.constants.ApiConstants;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.constants.ImageAssetConstants;
import com.thugdroid.memeking.firebasepack.analytics.FireAnalytics;
import com.thugdroid.memeking.firebasepack.auth.FireSignIn;
import com.thugdroid.memeking.firebasepack.functions.FireFunctions;
import com.thugdroid.memeking.interfaces.CommonFragmentInterface;
import com.thugdroid.memeking.model.NoTemplatesFoundConfigurations;
import com.thugdroid.memeking.room.repository.AppPrefsRepository;
import com.thugdroid.memeking.ui.LoadingDialog;
import com.thugdroid.memeking.viewmodel.MemesFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;

import java.io.File;
import java.util.HashMap;

public abstract class CustomFragment extends Fragment implements CommonFragmentInterface,View.OnClickListener {
    View rootView;
    FireAnalytics fireAnalytics;
    private FireFunctions fireFunctions;
    private MemesFragmentViewModel globalMemesFeedDb;
    protected MainActivity getMainActivity() {
        return (MainActivity) getActivity();
    }

    protected View getRootView() {
        return rootView;
    }

    protected void setRootView(View rootView) {
        this.rootView = rootView;
    }

    protected  <T extends View> T findViewById(int id){
        return getRootView().findViewById(id);
    }

    private NavController findNavController(){
        return Navigation.findNavController(getRootView());
    }

    protected void navigate(int navGraphId){
        try{
            findNavController().navigate(navGraphId);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected void navigate(NavDirections navDirections){
        try{
            findNavController().navigate(navDirections);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    protected FireFunctions getFireFunctions(){
        if(fireFunctions==null){
            fireFunctions=new FireFunctions(getContext());
        }
        return fireFunctions;
    }

    protected void initFireAnalytics(){
        fireAnalytics=new FireAnalytics(getContext(), FireConstants.CUSTOM_EVENT);
    }

    protected FireAnalytics getFireAnalytics(){
        if(fireAnalytics==null){
            initFireAnalytics();
        }
        return fireAnalytics;
    }

    protected void clientSignOut(){
        if(isFragmentVisible()){
            FireSignIn fireSignIn = new FireSignIn(getMainActivity(),getString(R.string.default_web_client_id));
            fireSignIn.signOut();
        }
    }

    protected void recordScreen(String screenName){
        getFireAnalytics().setCurrentScreen(getMainActivity(),screenName);
    }

    protected FragmentManager getSupportFragmentManager(){
        return getMainActivity().getSupportFragmentManager();
    }

    protected void showMsg(String  msg){
        if(getMainActivity()!=null && isAdded()){
            Toast.makeText(getMainActivity(),msg,Toast.LENGTH_SHORT).show();
        }
    }
    protected void showMsg(int resourceId){
        if(getMainActivity()!=null && isAdded()){
            Toast.makeText(getMainActivity(),getString(resourceId),Toast.LENGTH_SHORT).show();
        }
    }
    protected void showMsgWithGravity(int resourceId,int gravity){
        if(getMainActivity()!=null && isAdded()){
            Toast toast =Toast.makeText(getMainActivity(),getString(resourceId),Toast.LENGTH_SHORT);
            toast.setGravity(gravity,0,0);
            toast.show();
        }
    }

    protected void showMsg(int resourceId,@Nullable Object... formatArgs){
        if(getMainActivity()!=null && isAdded()){
            Toast.makeText(getMainActivity(),getString(resourceId,formatArgs),Toast.LENGTH_SHORT).show();
        }
    }

    protected boolean isFragmentVisible(){
        return getMainActivity()!=null && isAdded();
    }

    private MemesFragmentViewModel getGlobalMemesFeedDb() {
        return new ViewModelProvider(requireActivity()).get(MemesFragmentViewModel.class);
    }

    protected void unAuthorizeSignOut(AppPrefsRepository appPrefsDbViewModel, WindowViewModel windowViewModel,
                                      int navigationId){
        if(isFragmentVisible()){
            getGlobalMemesFeedDb().getMemes().clear();
            new UnAuthorizeSignOut(appPrefsDbViewModel,windowViewModel,navigationId).execute();
        }
    }


    private String getImageAssetPath(String imageName,String regionId){
        return (Constants.ASSET_PATH+ File.separator+ImageAssetConstants.PREFIX_FOLDER+File.separator+regionId+File.separator+imageName);
    }

    private void loadImageAsset(ImageView imageView,String imagePath,int backupDrawable){
        if(isFragmentVisible()){
            RequestManager glide = Glide.with(getMainActivity());
            glide.asBitmap().load(imagePath).listener(new RequestListener<Bitmap>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                    imageView.setImageResource(backupDrawable);
                    return false;
                }

                @Override
                public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        imageView.getLayoutParams().width=resource.getWidth();
                        imageView.getLayoutParams().height=resource.getHeight();
                        glide.load(resource).into(imageView);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
        }

    }
    protected void showNoContentFound(NoTemplatesFoundConfigurations configurations){
        if(isFragmentVisible()){
            ConstraintLayout  parent=findViewById(R.id.noContentFound);
            if(parent!=null){
                parent.setVisibility(View.VISIBLE);
                if(parent.getChildCount()==0){
                    View noContentFoundView=getLayoutInflater().inflate(R.layout.layout_notemplatesfound,parent,false);
                    View uploadBtn = noContentFoundView.findViewById(R.id.noTemplatesUploadTemplateBtn);
                    if(configurations.getTitle()!=null){
                        ((TextView)noContentFoundView.findViewById(R.id.noTemplatesTitle)).setText(configurations.getTitle());
                    }
                    if(configurations.getRegionId()!=null){
                        loadImageAsset(noContentFoundView.findViewById(R.id.noTemplatesIcon),
                                getImageAssetPath(ImageAssetConstants.NOCONTENT_IMAGE,configurations.getRegionId()),
                                R.drawable.ic_mytemplates_grey_24dp);
                    }
                    if(!configurations.isHasActionBtn()){
                        uploadBtn.setVisibility(View.GONE);
                    }else{
                        if(configurations.getUploadBtnClickListener()!=null){
                            uploadBtn.setOnClickListener(configurations.getUploadBtnClickListener());
                        }
                    }
                    parent.addView(noContentFoundView);
                }
            }
        }
    }
    protected void hideNoContentFound(){
        ConstraintLayout  parent=findViewById(R.id.noContentFound);
        if(parent!=null){
            parent.setVisibility(View.GONE);
        }
    }
    protected void showNoInternetConnection(View.OnClickListener onRetryBtnClick){
        if(isFragmentVisible()){
            ConstraintLayout parent=findViewById(R.id.noInternetConnectionParent);
            if(parent!=null){
                parent.setVisibility(View.VISIBLE);
                if(parent.getChildCount()==0){
                    View noConnectionView=getLayoutInflater().inflate(R.layout.layout_nointernetconnection,parent,false);
                    if(onRetryBtnClick!=null){
                        (noConnectionView.findViewById(R.id.noConnectionRetryBtn)).setOnClickListener(onRetryBtnClick);
                    }
                    parent.addView(noConnectionView);
                }
            }
        }
    }
    protected void hideNoInternetConnection(){
        if(isFragmentVisible()){
            View parent=findViewById(R.id.noInternetConnectionParent);
            if(parent!=null){
                parent.setVisibility(View.GONE);
            }
        }

    }
    protected void showSomethingWentWrong(View.OnClickListener onRetryBtnClick,String regionId){
        if(isFragmentVisible()){
            ConstraintLayout parent=findViewById(R.id.somethingWentWrongParent);
            if(parent!=null){
                parent.setVisibility(View.VISIBLE);
                if(parent.getChildCount()==0){
                    View noConnectionView=getLayoutInflater().inflate(R.layout.layout_somethingwentwrong,parent,false);
                    if(onRetryBtnClick!=null){
                        (noConnectionView.findViewById(R.id.swwRetryBtn)).setOnClickListener(onRetryBtnClick);
                    }
                    if(regionId!=null){
                        loadImageAsset(noConnectionView.findViewById(R.id.swwIcon),getImageAssetPath(ImageAssetConstants.SOMETHING_WENT_WRONG_IMAGE,regionId),R.drawable.ic_mood_bad_grey_24dp);
                    }
                    parent.addView(noConnectionView);
                }
            }
        }

    }
    protected void hideSomethingWentWrong(){
        View parent=findViewById(R.id.somethingWentWrongParent);
        if(parent!=null){
            parent.setVisibility(View.GONE);
        }
    }
    protected int[] getSpotLightLocations(View view){
        int locations[] = new int[2];
        view.getLocationOnScreen(locations);
        locations[0]=locations[0]+(view.getWidth()/2);
        locations[1]=locations[1]+(view.getHeight()/2);
        return locations;
    }

    protected String getErrorMsgFromApiData(Object resultObj){
        String msg="";
        if(getMainActivity()!=null && isAdded()){
            msg = getString(R.string.something_went_wrong_please_try_again);
            if(resultObj!=null){
                HashMap resultMap=(HashMap)resultObj;
                if(resultMap.containsKey(ApiConstants.KEY_MESSAGE)){
                    msg=(String)resultMap.get(ApiConstants.KEY_MESSAGE);
                }
            }
        }
        return msg;
    }

    protected void unSubscribeFCMTopic(String topic){
        if(topic!=null){
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic);
        }
    }
    protected void subscribeFCMTopic(String topic){
        if(topic!=null){
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
        }
    }

    class  UnAuthorizeSignOut extends AsyncTask{
        AppPrefsRepository appPrefsRepository;
        WindowViewModel windowViewModel;
        int navigationId;
        LoadingDialog loadingDialog;
        public UnAuthorizeSignOut(AppPrefsRepository appPrefsRepository, WindowViewModel windowViewModel,
                                  int navigationId) {
            this.appPrefsRepository = appPrefsRepository;
            this.navigationId=navigationId;
            this.windowViewModel=windowViewModel;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            loadingDialog=new LoadingDialog(getContext());
            loadingDialog.setLoadingText(getString(R.string.signing_out_dots));
            loadingDialog.show();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            unSubscribeFCMTopic(windowViewModel.getRegionId());
            appPrefsRepository.clearAllTablesSync();
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            loadingDialog.dismiss();
            windowViewModel.resetAll();
            clientSignOut();
            showMsg(R.string.session_expired_login_again);
            if(isFragmentVisible()){
               navigate(navigationId);
            }
        }
    }

}
