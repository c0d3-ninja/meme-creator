package com.thugdroid.memeking.fragments;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.thugdroid.libs.collagegrid.constants.GridNameConstants;
import com.thugdroid.libs.collagegrid.fragments.GridFragment;
import com.thugdroid.libs.collagegrid.utils.GridUtils;
import com.thugdroid.libs.colorpicker.ColorPickerDialog;
import com.thugdroid.libs.colorpicker.ColorPickerDialogListener;
import com.thugdroid.libs.colorpicker.DrawingUtils;
import com.thugdroid.libs.memetextview.MemeData;
import com.thugdroid.libs.memetextview.MemeTextView;
import com.thugdroid.libs.myseekbar.BubbleSeekBar;
import com.thugdroid.libs.spotlight.OnSpotlightStateChangedListener;
import com.thugdroid.libs.spotlight.WalkThrough;
import com.thugdroid.libs.spotlight.shape.Circle;
import com.thugdroid.libs.spotlight.target.SimpleTarget;
import com.thugdroid.libs.stickerimageview.StickerImageView;
import com.thugdroid.memeking.MediaFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FireConstants;
import com.thugdroid.memeking.constants.FragmentTags;
import com.thugdroid.memeking.model.BorderModel;
import com.thugdroid.memeking.model.DefaultFont;
import com.thugdroid.memeking.model.DefaultSticker;
import com.thugdroid.memeking.model.MyImage;
import com.thugdroid.memeking.room.entity.AppPrefsEntity;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.room.repository.AppPrefsRepository;
import com.thugdroid.memeking.room.repository.SocialUsernameRepository;
import com.thugdroid.memeking.ui.AddTextDialog;
import com.thugdroid.memeking.ui.ConfirmationDialog;
import com.thugdroid.memeking.ui.FontDialog;
import com.thugdroid.memeking.ui.LoadingDialog;
import com.thugdroid.memeking.ui.StickerDialog;
import com.thugdroid.memeking.ui.data.MenuItemsData;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.utils.DisplayUtils;
import com.thugdroid.memeking.viewmodel.CreateMemeFragmentViewModel;
import com.thugdroid.memeking.viewmodel.WindowViewModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;


public class CreateMemeFragment extends MediaFragment implements ColorPickerDialogListener {

    private static final int TEXT_COLOR_PICKER=1;
    private static final int TEXT_BG_COLOR_PICKER=2;
    private static final int TEXT_OUTLINE_COLOR_PICKER=3;
    private static  final int TEXT_SIZE_BSB =4;
    private static final int TEXT_OUTLINE_SIZE_BSB =5;
    private static final int PICK_STICKERVIEW_IMAGE_REQUESTCODE=6;
    private static final int PERMISSION_SAVE_MEME = 7;
    private static final int PICK_LOGO=8;
    private static final int BORDER_COLOR_PICKER=9;


    private ViewHolder viewHolder;
    private LayoutListener layoutListener;
    private MemeTextView currentMemeTextView;
    private CreateMemeFragmentViewModel createMemeFragmentViewModel;
    private StickerImageView currentStickerImageView;
    private AppPrefsRepository appPrefsRepository;
    private OnBackPressedCallback onBackPressedCallback;
    private GridFragment gridFragment;
    private LoadingDialog loadingDialog;
    private CreateMemeFragmentArgs arguments;
    private MemeData prevMemeData;
    private WindowViewModel windowViewModel;
    private SocialUsernameRepository socialUsernameRepository;
    /*override methods start*/
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public CreateMemeFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_creatememe,container,false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
        initObservers();
        createMemeFragmentViewModel.setCurrentGridName(arguments.getGridName());
        if(!createMemeFragmentViewModel.getCurrentGridName().equals(GridNameConstants.L1)){
            hideBorderOptions();
        }
        layoutListener=new LayoutListener();
        viewHolder.memeCreateBody.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        /*lazy load*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadCategories();
            }
        },300);
        insertTemplateCreditsFragment();
    }


    @Override
    public void initVariables() {
        arguments=CreateMemeFragmentArgs.fromBundle(getArguments());
        createMemeFragmentViewModel=new ViewModelProvider(this).get(CreateMemeFragmentViewModel.class);
        windowViewModel=new ViewModelProvider(requireActivity()).get(WindowViewModel.class);
        appPrefsRepository =new ViewModelProvider(this).get(AppPrefsRepository.class);
        socialUsernameRepository=new ViewModelProvider(this).get(SocialUsernameRepository.class);
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
        MenuItemsFragment chooseImageItemsFragment =MenuItemsFragment.newInstance(MenuItemsData.getCreateMemeImagePickItems(),new PickImageStaticItemsClickListener());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.createMemePickImageMenuItemsFragment,chooseImageItemsFragment).commit();
    }

    @Override
    public void initListeners() {
        (findViewById(R.id.createMemeBack)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolAddNewText)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolEditText)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolDeleteText)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolFontFamily)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolTextColor)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolBgColor)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolOutlineColor)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolAddSticker)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolTextSize)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolOutlineSize)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolAddImage)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolSave)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolShare)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolAddLogo)).setOnClickListener(this::onClick);
        (findViewById(R.id.toolTextAlign)).setOnClickListener(this::onClick);
        findViewById(R.id.toolUpload).setOnClickListener(this::onClick);
        viewHolder.freezeLayer.setOnClickListener(this::onClick);
        /*choose image bottom sheet start*/

        BottomSheetBehavior.BottomSheetCallback callback = new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int state) {
                switch (state){
                    case BottomSheetBehavior.STATE_HIDDEN:
                        hideFreezeLayer();
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {

            }
        };
        viewHolder.chooseImageBsBehavior.setBottomSheetCallback(callback);
        /*choose image bottom sheet end*/

        /*text align bs start*/
        viewHolder.textAlignBsBehavior.setBottomSheetCallback(callback);
        (findViewById(R.id.textAlignBsLeft)).setOnClickListener(this::onClick);
        (findViewById(R.id.textAlignBsCenter)).setOnClickListener(this::onClick);
        (findViewById(R.id.textAlignBsRight)).setOnClickListener(this::onClick);
        /*text align bs end*/


        if(createMemeFragmentViewModel.getCurrentGridName().equals(GridNameConstants.L1)){
            (findViewById(R.id.toolAddBorder)).setOnClickListener(this::onClick);
            (findViewById(R.id.toolBorderColor)).setOnClickListener(this::onClick);
        }


        onBackPressedCallback=new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(createMemeFragmentViewModel.isWalkThroughShowing()){
                    return;
                }
                if(viewHolder.createMemeUploadMemeParent.getVisibility()==View.VISIBLE){
                    hideUploadMemeFragment();
                }
                else if(viewHolder.textAlignBsBehavior.getState()!=BottomSheetBehavior.STATE_HIDDEN){
                    hideTextAlignBottomSheet();
                }

                else if(viewHolder.templatesFragmentParent.getVisibility()==View.VISIBLE){
                    hideTemplates();
                }
                else if(viewHolder.chooseImageBsBehavior.getState()!=BottomSheetBehavior.STATE_HIDDEN){
                    hideChooseImageBottomSheet();
                }
                else if(viewHolder.borderFragmentContainer.getVisibility()==View.VISIBLE){
                    hideBorderDialog();
                }
                else if(viewHolder.cropFragmentContainer.getVisibility()==View.VISIBLE){
                    if(GridNameConstants.L1.equals(arguments.getGridName()) && (gridFragment.getImageAddedGridCount()==0)){
                        onBackPressedCallback.setEnabled(false);
                        getMainActivity().onBackPressed();
                    }else{
                        hideCropFragment();
                    }
                }
                else if(createMemeFragmentViewModel.isModified()) {
                    ConfirmationDialog confirmationDialog = new ConfirmationDialog(getContext());
                    confirmationDialog.setTitle(getString(R.string.there_are_some_unsaved_changes));
                    confirmationDialog.setOnClickListener(new BackConfirmDialogListener());
                    confirmationDialog.show();
                }else{
                    onBackPressedCallback.setEnabled(false);
                    getMainActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this,onBackPressedCallback);
    }

    @Override
    public void initObservers() {
        createMemeFragmentViewModel.getIsPostMemeAvailable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isAvailable) {
                if(isAvailable){
                    ((ImageView)findViewById(R.id.toolUploadIcon)).setImageResource(R.drawable.ic_send_black_24dp);
                }else{
                    ((ImageView)findViewById(R.id.toolUploadIcon)).setImageResource(R.drawable.ic_send_inactive_grey_24dp);
                }
            }
        });
        createMemeFragmentViewModel.getIsMemeTextSelected().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isMemeTextSelected) {
                if(isMemeTextSelected){
                    setTextEditToolsActive();
                }else{
                    setTextEditToolsInActive();
                    hideSizeBsb();
                }
            }
        });
        createMemeFragmentViewModel.getIsBorderOptionAvailable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isBorderOptionAvailable) {
                activeBorderOption(isBorderOptionAvailable);
            }
        });

        createMemeFragmentViewModel.getIsBorderColorAvailable().observe(getViewLifecycleOwner(), new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isBorderColorAvailable) {
                activeBorderColor(isBorderColorAvailable);
            }
        });

        appPrefsRepository.getPref(AppPrefsEntity.LOGO_PATH).observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String url) {
                if(url==null){
                    viewHolder.toolAddLogoIv.setImageResource(R.drawable.memeking_googleplay);
                    createMemeFragmentViewModel.setLogoPath(null);
                }else{
                    createMemeFragmentViewModel.setLogoPath(url);
                    Glide.with(getContext()).load(url).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            appPrefsRepository.delete(AppPrefsEntity.LOGO_PATH);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    }).into(viewHolder.toolAddLogoIv);
                }
            }
        });

        LiveData<String> signleGridLiveData= appPrefsRepository.getPref(AppPrefsEntity.WALKTHROUGH_SINGLE_GRID);
        signleGridLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                createMemeFragmentViewModel.setShowSingleGridWalkThrough(s==null);
                if(s!=null){
                    signleGridLiveData.removeObservers(getViewLifecycleOwner());
                }

            }
        });

//        LiveData<String> adminUserLiveData = appPrefsRepository.getPref(AppPrefsEntity.IS_ADMIN_USER);
//        adminUserLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(String value) {
//                adminUserLiveData.removeObservers(getViewLifecycleOwner());
//                if(value!=null){
//                    viewHolder.watermark.setImageAlpha(0);
//                    viewHolder.appNameTextView.setText("");
//                }
//            }
//        });

    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        if(currentMemeTextView!=null){
            setModified(true);
            String hexColor = DrawingUtils.getHexColor(color);
            switch (dialogId){
                case TEXT_COLOR_PICKER:
                    currentMemeTextView.setNewTextColor(hexColor);
                    break;
                case TEXT_BG_COLOR_PICKER:
                    currentMemeTextView.setNewBackgroundColor(hexColor);
                    break;
                case TEXT_OUTLINE_COLOR_PICKER:
                    currentMemeTextView.setNewStrokeColor(hexColor);
                    break;
            }
        }else if(dialogId==BORDER_COLOR_PICKER){
            setModified(true);
            String hexColor = DrawingUtils.getHexColor(color);
            createMemeFragmentViewModel.setBorderColor(hexColor);
            new SetBorderToBitMap(createMemeFragmentViewModel.getSingleGridBitmap(),createMemeFragmentViewModel.getBorderModel(),createMemeFragmentViewModel.getBorderColor()).execute();
        }

    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id != R.id.toolOutlineSize && id!=R.id.toolTextSize){
            hideSizeBsb();
        }
        switch (v.getId()){
            case R.id.createMemeFreezeLayer:
            case R.id.createMemeBack:
                getMainActivity().onBackPressed();
                break;
            case R.id.toolUpload:
                if(createMemeFragmentViewModel.getIsPostMemeAvailable().getValue()){
                    uploadMeme();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_POST_MEME);
                }else{
                    showMsg(R.string.create_meme_to_post_it);
                }
                break;
            case R.id.toolSave:
                saveImage();
                getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_SAVE_MEME);
                break;
            case R.id.toolShare:
                shareImage();
                getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_SHARE_MEME);
                break;
            case R.id.toolAddNewText:
                setCurrentMemeTextView(null);
                showAddTextDialog("");
                getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_ADD_TEXT);
                break;
            case R.id.toolEditText:
                if(currentMemeTextView!=null){
                    showAddTextDialog(currentMemeTextView.getMemeData().text);
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_EDIT_TEXT);
                }else{
                    showMsg(R.string.please_add_or_seletct_text);
                }

                break;
            case R.id.toolDeleteText:
               deleteCurrentMemeTextView();
                getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_DELETE_TEXT);
                break;
            case R.id.toolFontFamily:
                showFontDialog();
                getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_FONTS);
                break;
            case R.id.toolTextColor:
                showColorPicker(TEXT_COLOR_PICKER);
                getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_TEXT_COLOR);
                break;
            case R.id.toolBgColor:
                showColorPicker(TEXT_BG_COLOR_PICKER);
                getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_BG_COLOR);
                break;
            case R.id.toolOutlineColor:
                showColorPicker(TEXT_OUTLINE_COLOR_PICKER);
                getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_OUTLINE_COLOR);
                break;
            case R.id.toolAddSticker:
                showStickerDialog();
                getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_ADD_STICKER);
                break;
            case R.id.toolTextSize:
                if(viewHolder.sizeParent.getVisibility()==View.GONE){
                    showSizeBsb(TEXT_SIZE_BSB);
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_TEXT_SIZE);
                }else{
                    hideSizeBsb();
                }
                break;
            case R.id.toolOutlineSize:
                if(viewHolder.sizeParent.getVisibility()==View.GONE){
                    showSizeBsb(TEXT_OUTLINE_SIZE_BSB);
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_OUTLINE_SIZE);
                }else{
                    hideSizeBsb();
                }
                break;
            case R.id.toolAddImage:
                pickStickerViewImage();
                getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_ADD_IMAGE);
                break;
            case R.id.toolAddLogo:
                if(createMemeFragmentViewModel.getLogoPath()==null){
                    pickLogo();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_PICK_LOGO);
                }else{
                    addLogo();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_ADD_LOGO);
                }

                break;
            case R.id.toolAddBorder:
                if(createMemeFragmentViewModel.getIsBorderOptionAvailable().getValue()){
                   showBorderDialog();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_ADD_IMAGE_BORDER);
                }else{
                    showMsg(R.string.add_image_to_choose_border);
                }
                break;
            case R.id.toolBorderColor:
                if(createMemeFragmentViewModel.getIsBorderColorAvailable().getValue()){
                    showBorderColorPicker();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_IMAGE_BORDER_COLOR);
                }else{
                    showBorderDialog();
                    showMsg(R.string.set_border_to_choose_border_color);
                }
                break;
            case R.id.toolTextAlign:
                if(currentMemeTextView!=null){
                    showTextAlignBottomSheet();
                    getFireAnalytics().logSingleEvent(FireConstants.EVENT_ID_MEME_TOOLS_USAGE,FireConstants.EVENT_MEME_TOOL_TEXT_ALIGN);
                }else{
                    showMsg(R.string.please_add_or_seletct_text);
                }
                break;

            /*text align bottomsheet start*/
            case R.id.textAlignBsLeft:
                hideTextAlignBottomSheet();
                setAlignment(Gravity.LEFT);
                break;
            case  R.id.textAlignBsCenter:
                hideTextAlignBottomSheet();
                setAlignment(Gravity.CENTER);
                break;
            case R.id.textAlignBsRight:
                hideTextAlignBottomSheet();
                setAlignment(Gravity.RIGHT);
                break;
            /*text align bottomsheet end*/

        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==PICK_STICKERVIEW_IMAGE_REQUESTCODE){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                pickStickerViewImage();
            }else{
                showMsg(R.string.allow_permission_to_choose_image);
            }
        }else if(requestCode==PERMISSION_SAVE_MEME){
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                saveImage();
            }else{
                showMsg(R.string.allow_permission_to_save_meme);
            }
        }else if(requestCode==PICK_LOGO){
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
            case PICK_STICKERVIEW_IMAGE_REQUESTCODE:
                if(resultCode==RESULT_OK && data!=null){
                    Uri uri=data.getData();
                    MyImage imageDetails = AppUtils.getDetailedImage(getContext(),uri);
                    String mimeType=imageDetails.getMimeType();
                    if(mimeType!=null && mimeType.startsWith("image")){
                        addStickerViewImage(uri);
                    }else{
                        showMsg(R.string.unsupported_file_format);
                    }
                }
                break;
            case PICK_LOGO:
                if(resultCode==RESULT_OK && data!=null){
                    Uri uri=data.getData();
                    MyImage imageDetails =AppUtils.getDetailedImage(getContext(),uri);
                    String mimeType=imageDetails.getMimeType();
                    if(mimeType!=null && com.thugdroid.memeking.utils.AppUtils.getIndexOf(Constants.IMAGE_ACCEPTABLE_MIME_TYPES,mimeType)!=-1){
                        appPrefsRepository.updateLogoUrl(uri.toString());
                        addStickerViewImage(uri);
                    }else{
                        showMsg(R.string.jpg_png_images_allowed);
                    }
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        onBackPressedCallback.setEnabled(false);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        recordScreenView();

    }


    /*override methods end*/

    /*other methods start*/
    private void showCreditsContainer(){
        viewHolder.templateCreditsFragment.setVisibility(View.VISIBLE);
    }
    private void hideCreditsContainer(){
        viewHolder.templateCreditsFragment.setVisibility(View.GONE);
    }
    private void insertTemplateCreditsFragment(){
        TemplateCreditsFragment templateCreditsFragment=TemplateCreditsFragment.newInstance(arguments.getAuthorId(),new TemplateCreditsHandshakes());
        getSupportFragmentManager().beginTransaction().replace(R.id.createMemeTemplateCreditsFragment,templateCreditsFragment)
                .commit();

    }
    private class TemplateCreditsHandshakes implements TemplateCreditsFragment.ParentHandShakes{
        @Override
        public void onData() {
            showCreditsContainer();
        }

        @Override
        public void onNoData() {
            hideCreditsContainer();
        }
    }



    private boolean isSingleGrid(){
        return createMemeFragmentViewModel.getCurrentGridName().equals(GridNameConstants.L1);
    }
    private void recordScreenView() {
        recordScreen(FireConstants.SCREEN_CREATE_MEME);
    }

    private void showTemplates(int templateType,CategoryEntity categoryEntity){
        viewHolder.templatesFragmentParent.setVisibility(View.VISIBLE);
        CreateMemePickTemplateFragment createMemePickTemplateFragment =new CreateMemePickTemplateFragment();
        createMemePickTemplateFragment.setTemplateType(templateType);
        createMemePickTemplateFragment.setCategoryEntity(categoryEntity);
        createMemePickTemplateFragment.setTemplateItemClickListener(new PickTemplateItemClickListener());
        getSupportFragmentManager().beginTransaction().replace(R.id.createMemePickTemplatesParent, createMemePickTemplateFragment,
                FragmentTags.CREATE_MEME_PICK_TEMPLATE_FRAGMENT).commit();
    }

    private void showUploadMemeFragment(MyImage myImage){
        if(myImage==null){
            showMsg(R.string.can_t_load_image);
            return;
        }
        UploadMemePopupFragment uploadMemePopupFragment= UploadMemePopupFragment.newInstance(myImage,new UploadMemeHandShake());
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.createMemeUploadMemeParent,uploadMemePopupFragment).commit();
        viewHolder.createMemeUploadMemeParent.setVisibility(View.VISIBLE);
    }
    private void hideUploadMemeFragment(){
        viewHolder.createMemeUploadMemeParent.setVisibility(View.GONE);
    }
    private void hideTemplates(){
        viewHolder.templatesFragmentParent.setVisibility(View.GONE);
        Fragment fragment=getSupportFragmentManager().findFragmentByTag(FragmentTags.CREATE_MEME_PICK_TEMPLATE_FRAGMENT);
        if(fragment!=null){
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }
    private void showFreezeLayer(){
        viewHolder.freezeLayer.setVisibility(View.VISIBLE);
    }
    private void hideFreezeLayer(){
        viewHolder.freezeLayer.setVisibility(View.GONE);
    }



    private void showTextAlignBottomSheet(){
        showFreezeLayer();
        viewHolder.textAlignBsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
    private void hideTextAlignBottomSheet(){
        viewHolder.textAlignBsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
    private void showChooseImageBottomSheet(){
        showFreezeLayer();
        viewHolder.chooseImageBsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
    private void hideChooseImageBottomSheet(){
        viewHolder.chooseImageBsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
    private void loadCategories(){
        CategoryFragment categoryFragment=new CategoryFragment();
        categoryFragment.setListItemListener(new ChooseImageBSCategoryClickListener());
        getSupportFragmentManager().beginTransaction().replace(R.id.chooseImageCategoryFragmentContainer,categoryFragment).commit();
    }
    private void showCropFragment(Uri imageUri,int gridNo){
        viewHolder.cropFragmentContainer.setVisibility(View.VISIBLE);
        CropFragment cropFragment=new CropFragment();
        cropFragment.setUri(imageUri);
        cropFragment.setListener(new CropListener(gridNo));
        getSupportFragmentManager().beginTransaction().replace(R.id.createMemeCropFragmentContainer,cropFragment).commit();

    }
    private void hideCropFragment(){
        viewHolder.cropFragmentContainer.setVisibility(View.GONE);
    }
    private void activeBorderOption(boolean isActive){
        ((ImageView)findViewById(R.id.toolAddBorderIv)).setImageResource(isActive?R.drawable.ic_image_border_black_24dp:R.drawable.ic_image_border_grey_24dp);
    }

    private void activeBorderColor(boolean isActive){
        ((ImageView)findViewById(R.id.toolBorderColorIv)).setImageResource(isActive?R.drawable.ic_image_border_teal_24dp:R.drawable.ic_image_border_grey_24dp);
    }

    private void hideBorderOptions(){
        (findViewById(R.id.toolAddBorder)).setVisibility(View.GONE);
        (findViewById(R.id.toolBorderColor)).setVisibility(View.GONE);
    }


    private void pickStickerViewImage(){
        if(isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE,PICK_STICKERVIEW_IMAGE_REQUESTCODE)){
            openGallery(PICK_STICKERVIEW_IMAGE_REQUESTCODE);
        }
    }

    private void addStickerViewImage(Uri uri){
        if(currentStickerImageView!=null){
            currentStickerImageView.setControlItemsHidden(true);
        }
        Glide.with(getContext()).asBitmap().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).load(uri).priority(Priority.IMMEDIATE).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                StickerImageView stickerImageView=new StickerImageView(getContext(),viewHolder.memeImageFragmentContainer.getWidth()-10,viewHolder.memeImageFragmentContainer.getHeight()-10);
                stickerImageView.setStickerImageViewTouchListener(new StickerImageViewListener());
                stickerImageView.setImageBitmap(resource);
                viewHolder.memeImageFragmentContainer.addView(stickerImageView);
                setCurrentStickerImageView(stickerImageView);
                setModified(true);
                createMemeFragmentViewModel.getIsPostMemeAvailable().setValue(true);
            }
            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }
    private void showAddTextDialog(String str) {
        AddTextDialog addTextDialog=new AddTextDialog(getMainActivity());
        addTextDialog.setDialogBtnClickListener(new AddTextDialogBtnClickListener());
        addTextDialog.show(str);
    }
    private void showStickerDialog(){
        List<DefaultSticker> defaultDefaultStickers =new ArrayList<>();
        try {
            String[] stickers=getContext().getAssets().list(Constants.STICKER_ASSET_PATH);
            for (String stickerName : stickers) {
                DefaultSticker defaultSticker =new DefaultSticker(stickerName);
                defaultDefaultStickers.add(defaultSticker);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        StickerDialog stickerDialog=new StickerDialog(getMainActivity());
        stickerDialog.setDefaultStickers(defaultDefaultStickers);
        stickerDialog.setStickerDialogItemClickListener(new DefaultStickerClickListener());
        stickerDialog.show();
    }
    private void showColorPicker(int dialogId){
        if(currentMemeTextView==null){
            showMsg(R.string.please_add_or_seletct_text);
            return;
        }
        String color="#FF000000";
        int title=R.string.select_text_color;
        switch (dialogId){
            case TEXT_COLOR_PICKER:
                title=R.string.select_text_color;
                color=currentMemeTextView.getMemeData().textColor;
                break;
            case TEXT_BG_COLOR_PICKER:
                title=R.string.select_background_color;
                if(currentMemeTextView.getMemeData().isBgColorModified){
                    color=currentMemeTextView.getMemeData().bgColor;
                }else{
                    color="#FF000000";
                }
                break;
            case TEXT_OUTLINE_COLOR_PICKER:
                title=R.string.select_outline_color;
                if(currentMemeTextView.getMemeData().isStrokeColorModified){
                    color=currentMemeTextView.getMemeData().strokeColor;
                }else{
                    color="#FF000000";
                }
                break;
        }

        ColorPickerDialog.newBuilder()
                .setDialogTitle(title)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(true)
                .setDialogId(dialogId)
                .setColor(Color.parseColor(color))
                .setShowAlphaSlider(true)
                .setColorPickerListener(this)
                .show(getMainActivity());
    }
    private void showBorderColorPicker(){
        int title=R.string.select_border_color;
        ColorPickerDialog.newBuilder()
                .setDialogTitle(title)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(true)
                .setDialogId(BORDER_COLOR_PICKER)
                .setColor(Color.parseColor(createMemeFragmentViewModel.getBorderColor()))
                .setShowAlphaSlider(false)
                .setColorPickerListener(this)
                .show(getMainActivity());
    }
    private void showFontDialog(){
        if(currentMemeTextView==null){
            showMsg(R.string.please_add_or_seletct_text);
            return;
        }
        List<DefaultFont> defaultFonts=new ArrayList<>();
        try {
            String[] fonts=getContext().getAssets().list(Constants.FONT_ASSET_PATH);
            for (String font : fonts) {
                DefaultFont defaultFont=new DefaultFont("abcd",font);
                defaultFonts.add(defaultFont);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        FontDialog fontDialog=new FontDialog(getMainActivity());
        fontDialog.setFontItemClickListener(new FontDialogItemClickListener());
        fontDialog.setDefaultFonts(defaultFonts);
        fontDialog.setSelectedFont(currentMemeTextView.getMemeData().fontName);
        fontDialog.show();
    }
    private void showSizeBsb(int type){
        if(currentMemeTextView==null){
            showMsg(R.string.please_add_or_seletct_text);
            return;
        }

        TextView bsbTitle = viewHolder.sizeParent.findViewById(R.id.createMemeBsbTitle);
        switch (type){
            case TEXT_SIZE_BSB:
                bsbTitle.setText(getString(R.string.text_size));
                viewHolder.outlineBsb.setVisibility(View.GONE);
                viewHolder.sizeBsb.setVisibility(View.VISIBLE);
                viewHolder.sizeBsb.setProgress(currentMemeTextView.getMemeData().textSize);
                break;
            case TEXT_OUTLINE_SIZE_BSB:
                bsbTitle.setText(getString(R.string.text_outline_size));
                viewHolder.outlineBsb.setVisibility(View.VISIBLE);
                viewHolder.sizeBsb.setVisibility(View.GONE);
                viewHolder.outlineBsb.setProgress(currentMemeTextView.getMemeData().strokeWidth);
                break;
        }
        if(viewHolder.sizeBsb.getOnProgressChangedListener()==null){
            viewHolder.sizeBsb.setOnProgressChangedListener(new TextSizeBsbProgressListener());
        }
        if(viewHolder.outlineBsb.getOnProgressChangedListener()==null){
            viewHolder.outlineBsb.setOnProgressChangedListener(new OutlineSizeBsbProgressListener());
        }
        viewHolder.sizeParent.setVisibility(View.VISIBLE);
    }
    private void hideSizeBsb(){
        viewHolder.sizeParent.setVisibility(View.GONE);
    }

    private void setAlignment(int gravity){
        if(this.currentMemeTextView!=null){
            this.currentMemeTextView.setNewGravity(gravity);
        }
    }

    private void addMemeTextView(String text){
        MemeTextView memeTextView=new MemeTextView(getContext());
       memeTextView.setElevation(getResources().getDimension(R.dimen.memeTextElevation));
        memeTextView.setOnTouchListener(new CaptionDragListener());
        ConstraintLayout.LayoutParams layoutParams=new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.startToStart=ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.endToEnd=ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.topToTop=ConstraintLayout.LayoutParams.PARENT_ID;
        layoutParams.bottomToBottom=ConstraintLayout.LayoutParams.PARENT_ID;
        /*null check handled inside the function*/
        MemeData memeData=MemeData.getClonedMemeDataForNewText(prevMemeData);
        memeData.text=text;
        memeTextView.setMemeData(memeData);
        /*need to do after setmemedata*/
        memeTextView.setNewGravity(memeData.getAlignment());
        if(memeData.getFontName()!=null){
            setTypeFace(memeData.getFontName(),memeTextView);
            createMemeFragmentViewModel.setSelectedFontName(memeData.getFontName());
        }else {
            setTypeFace(createMemeFragmentViewModel.getSelectedFontName(),memeTextView);
        }

        //not working after redraw
        memeTextView.setPadding(DisplayUtils.convertDpToPixel(getResources().getInteger(R.integer.memeTextViewLeftRightPadding),getContext()) ,0, DisplayUtils.convertDpToPixel(getResources().getInteger(R.integer.memeTextViewLeftRightPadding),getContext()),0);
        memeTextView.setLayoutParams(layoutParams);
        viewHolder.memeImageFragmentContainer.addView(memeTextView);
        setCurrentMemeTextView(memeTextView);
        setModified(true);
        showWaterMark();
    }
    private void deleteCurrentMemeTextView(){
        if(this.currentMemeTextView!=null){
            viewHolder.memeImageFragmentContainer.removeView(this.currentMemeTextView);
            setCurrentMemeTextView(null);
            setModified(true);
        }else{
            showMsg(R.string.please_add_or_seletct_text);
        }
    }

    private void setCurrentMemeTextView(MemeTextView memeTextView){
        if(this.currentMemeTextView!=null){
            this.currentMemeTextView.setSelected(false);
        }
        if(memeTextView!=null){
            memeTextView.setSelected(true);
            setPrevMemeData(memeTextView.getMemeData());
        }
        this.currentMemeTextView=memeTextView;
        createMemeFragmentViewModel.getIsMemeTextSelected().setValue(memeTextView==null?false:true);
    }

    private void setPrevMemeData(MemeData memeData){
        prevMemeData=memeData;
    }

    private void setCurrentStickerImageView(StickerImageView stickerImageView){
        if(this.currentStickerImageView!=null){
            this.currentStickerImageView.setControlItemsHidden(true);
        }
        if(stickerImageView!=null){
            stickerImageView.setControlItemsHidden(false);
        }
        this.currentStickerImageView=stickerImageView;
    }

    private void setTextEditToolsActive(){
        ((ImageView)findViewById(R.id.editTextIv)).setImageResource(R.drawable.ic_edit_black_24dp);
        ((ImageView)findViewById(R.id.deleteTextIv)).setImageResource(R.drawable.ic_delete_black_24dp);
        ((ImageView)findViewById(R.id.fontFamilyIv)).setImageResource(R.drawable.ic_font_black_24dp);
        ((ImageView)findViewById(R.id.textColorIv)).setImageResource(R.drawable.ic_textcolor_teal_24dp);
        ((ImageView)findViewById(R.id.bgColorIv)).setImageResource(R.drawable.ic_bgcolor_teal_24dp);
        ((ImageView)findViewById(R.id.strokeColorIv)).setImageResource(R.drawable.ic_strokecolor_teal_24dp);
        ((ImageView)findViewById(R.id.textSizeIv)).setImageResource(R.drawable.ic_textsize_black_24dp);
        ((ImageView)findViewById(R.id.strokeSizeIv)).setImageResource(R.drawable.ic_strokesize_black_24dp);
        ((ImageView)findViewById(R.id.toolTextAlignIv)).setImageResource(R.drawable.ic_format_align_center_black_24dp);

    }
    private void setTextEditToolsInActive(){
        ((ImageView)findViewById(R.id.editTextIv)).setImageResource(R.drawable.ic_edit_inactive_grey_24dp);
        ((ImageView)findViewById(R.id.deleteTextIv)).setImageResource(R.drawable.ic_delete_grey_24dp);
        ((ImageView)findViewById(R.id.fontFamilyIv)).setImageResource(R.drawable.ic_font_grey_24dp);
        ((ImageView)findViewById(R.id.textColorIv)).setImageResource(R.drawable.ic_textcolor_grey_24dp);
        ((ImageView)findViewById(R.id.bgColorIv)).setImageResource(R.drawable.ic_bgcolor_grey_24dp);
        ((ImageView)findViewById(R.id.strokeColorIv)).setImageResource(R.drawable.ic_strokecolor_grey_24dp);
        ((ImageView)findViewById(R.id.textSizeIv)).setImageResource(R.drawable.ic_textsize_grey_24dp);
        ((ImageView)findViewById(R.id.strokeSizeIv)).setImageResource(R.drawable.ic_strokesize_grey_24dp);
        ((ImageView)findViewById(R.id.toolTextAlignIv)).setImageResource(R.drawable.ic_format_align_center_inactive_grey_24dp);
    }

    private void setTypeFace(String fontName,MemeTextView memeTextView){
        try{
            if(memeTextView!=null){
                Typeface typeface=Typeface.createFromAsset(getContext().getAssets(),Constants.FONT_ASSET_PATH+"/"+fontName);
                memeTextView.setNewTypeface(typeface,fontName);
            }

        }catch (Exception e){
            if(memeTextView!=null){
                Typeface typeface=Typeface.createFromAsset(getContext().getAssets(),Constants.FONT_ASSET_PATH+"/"+Constants.DEFAULT_FONTNAME);
                memeTextView.setNewTypeface(typeface,fontName);
            }
        }finally {
            setModified(true);
        }

    }

    private void prepareSaveAndShare(){
        ConstraintLayout memeContainer = findViewById(R.id.memeImageFragmentContainer);
        if( createMemeFragmentViewModel.isWaterMarkHideable()){
            hideWaterMark();
        }else{
            boolean isHideWaterMark=true;
            for(int i=memeContainer.getChildCount()-1;i>=0;i--){
                Object object = memeContainer.getChildAt(i);
                if(object instanceof  StickerImageView){
                    isHideWaterMark=false;
                    ((StickerImageView)object).setControlItemsHidden(true);
                }else if(object instanceof MemeTextView){
                    isHideWaterMark=false;
                    ((MemeTextView)object).setSelected(false);
                }
            }
            if(isHideWaterMark){
                if(isSingleGrid() && arguments.getImageUrl()==null){
                    hideWaterMark();
                }
            }
        }
    }

    private void uploadMeme(){
        prepareSaveAndShare();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap= com.thugdroid.memeking.utils.AppUtils.getBitmapFromView(findViewById(R.id.memeImageFragmentContainer));
                    new SaveImage(Constants.SHARE_MEME_FOLDER_NAME,bitmap,true,new UploadMemeImageSaveListener()).execute();
                }
            },StickerImageView.getAnimationDuration()+100);

    }
    private void saveImage(){
        prepareSaveAndShare();
        if(isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE,PERMISSION_SAVE_MEME)){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap= com.thugdroid.memeking.utils.AppUtils.getBitmapFromView(findViewById(R.id.memeImageFragmentContainer));
                    new SaveImage(Constants.MEMES_FOLDERNAME,bitmap,false,new MemeSaveListener()).execute();
                }
            },StickerImageView.getAnimationDuration()+100);

        }
    }

    private void shareImage(){
        prepareSaveAndShare();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Bitmap bitmap= com.thugdroid.memeking.utils.AppUtils.getBitmapFromView(findViewById(R.id.memeImageFragmentContainer));
                    new SaveImage(Constants.SHARE_MEME_FOLDER_NAME,bitmap,true,new MemeShareListener()).execute();
                }
            },StickerImageView.getAnimationDuration()+100);

    }
    private void pickLogo(){
        if(isPermissionGranted(Manifest.permission.WRITE_EXTERNAL_STORAGE,PICK_LOGO)){
            openGallery(PICK_LOGO);
        }
    }

    private void addLogo(){
        addStickerViewImage(Uri.parse(createMemeFragmentViewModel.getLogoPath()));
        setModified(true);
    }

    private void setModified(boolean isModified){
        createMemeFragmentViewModel.setModified(isModified);
    }

    private void showSingleGridWalkThrough(View view){
            SimpleTarget changeImage = getChangeImageTarget(view);
            SimpleTarget addText = getAddTextTarget();
            WalkThrough.with(getMainActivity())
                    .setOverlayColor(R.color.background)
                    .setDuration(getResources().getInteger(R.integer.walkthroughSpotAnimationDuration))
                    .setTargets(changeImage,addText)
                    .setClosedOnTouchedOutside(true)
                    .setAnimation(new DecelerateInterpolator(1f))
                    .setOnSpotlightStateListener(new OnSpotlightStateChangedListener() {
                        @Override
                        public void onStarted() {
                            createMemeFragmentViewModel.setWalkThroughShowing(true);
                        }

                        @Override
                        public void onEnded() {
                            createMemeFragmentViewModel.setWalkThroughShowing(false);
                            showSmoothScrollOption();
                            appPrefsRepository.insertPref(AppPrefsEntity.WALKTHROUGH_SINGLE_GRID,String.valueOf(new Date().getTime()));
                        }
                    })
                    .start();
    }


    private SimpleTarget getAddTextTarget(){
        int[] locations = getSpotLightLocations(findViewById(R.id.toolAddNewText));
        int x = getContext().getResources().getInteger(R.integer.spotlightX);
     return  new SimpleTarget.Builder(getMainActivity())
             .setPoint(locations[0],locations[1])
             .setOverlayPoint(x,locations[1]-getContext().getResources().getInteger(R.integer.spotlightBottomToolbarIncrementY))
             .setShape(new Circle(getResources().getInteger(R.integer.walkthroughCircleRadius)))
             .setTitle(getString(R.string.walkthrough_add_caption_title))
             .setDescription(getString(R.string.walkthrough_add_caption_desc))
             .setImageResource(R.drawable.ic_click_gesture_white)
             .setDuration(getResources().getInteger(R.integer.walkthroughNextFrameDuration))
             .build();
    }

    private SimpleTarget getChangeImageTarget(View view){
        int locations[] = getSpotLightLocations(view);
        int x = getContext().getResources().getInteger(R.integer.spotlightX);
        int incrementY = getContext().getResources().getInteger(R.integer.spotlightIncrementY);
        return new SimpleTarget.Builder(getMainActivity())
                .setPoint(locations[0],locations[1])
                .setOverlayPoint(x,locations[1]+incrementY)
                .setShape(new Circle(getResources().getInteger(R.integer.walkthroughCircleRadius)))
                .setTitle(getString(R.string.walkthrough_change_image_title))
                .setDescription(getString(R.string.walkthrough_change_image_desc))
                .setImageResource(R.drawable.ic_longpress_gesture)
                .setDuration(getResources().getInteger(R.integer.walkthroughNextFrameDuration))
                .build();
    }

    private void showBorderDialog(){
        hideSizeBsb();
        viewHolder.borderFragmentContainer.setVisibility(View.VISIBLE);
        viewHolder.freezeLayer.setVisibility(View.VISIBLE);
        BorderFragment borderFragment=new BorderFragment();
        borderFragment.setActionButtonClickListener(new BorderActionBtnClickListener());
        borderFragment.setBorderModel(createMemeFragmentViewModel.getBorderModel());
        getSupportFragmentManager().beginTransaction().replace(R.id.borderFragmentContainer,borderFragment).commit();
    }
    private void hideBorderDialog(){
        viewHolder.borderFragmentContainer.setVisibility(View.GONE);
        viewHolder.freezeLayer.setVisibility(View.GONE);
        viewHolder.borderFragmentContainer.removeAllViews();
    }

    private void showLoadingDialog(){
        if(loadingDialog==null){
            loadingDialog=new LoadingDialog(getContext());
        }
        loadingDialog.show();
    }
    private void hideLoadingDialog(){
        if(loadingDialog!=null){
            loadingDialog.dismiss();
        }
    }
    private void showWaterMark(){
        viewHolder.watermark.setVisibility(View.VISIBLE);
        viewHolder.appNameTextView.setVisibility(View.VISIBLE);
    }
    private void hideWaterMark(){
        (findViewById(R.id.watermark)).setVisibility(View.GONE);
        viewHolder.appNameTextView.setVisibility(View.GONE);
    }
    private void setCropImage(Uri uri,int gridNo){
        gridFragment.setImage(uri,gridNo,true);
        hideCropFragment();
        if(isSingleGrid() && arguments.getImageUrl()!=null){
            showWaterMark();
        }
        if(GridNameConstants.L1.equals(arguments.getGridName())){
            Glide.with(getContext()).asBitmap().load(uri).into(new CustomTarget<Bitmap>() {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    createMemeFragmentViewModel.setSingleGridBitmap(resource);
                }

                @Override
                public void onLoadCleared(@Nullable Drawable placeholder) {

                }
            });
            if(createMemeFragmentViewModel.isShowSingleGridWalkThrough()){
                showSingleGridWalkThrough(findViewById(GridUtils.getGridId(gridNo)));
            }

            createMemeFragmentViewModel.setBorderModel(new BorderModel(0,0,0,0));
            createMemeFragmentViewModel.getIsBorderOptionAvailable().setValue(true);
            createMemeFragmentViewModel.getIsBorderColorAvailable().setValue(false);
        }
        if(arguments.getGridName()!=GridNameConstants.L1){
            setModified(true);
        }

    }
    private void scrollToolsTo(int position){
        viewHolder.createMemeToolsScroll.post(new Runnable() {
            @Override
            public void run() {
                viewHolder.createMemeToolsScroll.smoothScrollTo(position,viewHolder.createMemeToolsScroll.getScrollY());
            }
        });
    }
    private void showSmoothScrollOption(){
        try{
            scrollToolsTo(Constants.SCROLL_MAX_POSITION);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    scrollToolsTo(0);
                }
            },Constants.SCROLL_TOOLS_TIMEOUT);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            showMsg(R.string.scroll_for_more_option);
        }

    }
    /*other methods end*/

    /*class start*/

    private class ViewHolder{
        RelativeLayout memeCreateBody;
        ConstraintLayout memeImageFragmentContainer;
        ConstraintLayout sizeParent,borderFragmentContainer,freezeLayer;
        ImageView toolAddLogoIv;
        View cropFragmentContainer;
        BottomSheetBehavior chooseImageBsBehavior,textAlignBsBehavior;
        ConstraintLayout chooseImageCategoryFragmentContainer;
        View templatesFragmentParent,createMemeUploadMemeParent, templateCreditsFragment;
        HorizontalScrollView createMemeToolsScroll;
        BubbleSeekBar sizeBsb,outlineBsb;
        ImageView watermark;
        TextView appNameTextView;
        public ViewHolder() {
            memeCreateBody=findViewById(R.id.memeCreateBody);
            memeImageFragmentContainer=findViewById(R.id.memeImageFragmentContainer);
            sizeParent=findViewById(R.id.createMemeSizeSliderParent);
            sizeBsb = sizeParent.findViewById(R.id.createMemeSizeBsb);
            outlineBsb=sizeParent.findViewById(R.id.createMemeOutlineBsb);
            toolAddLogoIv=findViewById(R.id.toolAddLogoIv);
            borderFragmentContainer=findViewById(R.id.borderFragmentContainer);
            freezeLayer=findViewById(R.id.createMemeFreezeLayer);
            cropFragmentContainer=findViewById(R.id.createMemeCropFragmentContainer);
            chooseImageBsBehavior=BottomSheetBehavior.from(findViewById(R.id.chooseImageBottomSheet));
            chooseImageBsBehavior.setHideable(true);
            chooseImageBsBehavior.setPeekHeight((int)getResources().getDimension(R.dimen.chooseImageOptionsBsHeight));
            chooseImageBsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);


            textAlignBsBehavior=BottomSheetBehavior.from(findViewById(R.id.textAlignBottomSheet));
            textAlignBsBehavior.setHideable(true);
            textAlignBsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

            chooseImageCategoryFragmentContainer=findViewById(R.id.chooseImageCategoryFragmentContainer);
            templatesFragmentParent=findViewById(R.id.createMemePickTemplatesParent);
            createMemeUploadMemeParent=findViewById(R.id.createMemeUploadMemeParent);
            createMemeUploadMemeParent.setVisibility(View.GONE);

            createMemeToolsScroll=findViewById(R.id.createMemeToolsScroll);
            appNameTextView=findViewById(R.id.createMemeFragmentAppNameTv);
            watermark=findViewById(R.id.watermark);
            watermark.setVisibility(View.GONE);
            appNameTextView.setVisibility(View.GONE);
            if(!AppUtils.isMemesEnabled(windowViewModel.getRegionId())){
                findViewById(R.id.toolUpload).setVisibility(View.GONE);
            }
            templateCreditsFragment =findViewById(R.id.createMemeTemplateCreditsFragment);
            templateCreditsFragment.setVisibility(View.GONE);
        }
    }

    private class AddTextDialogBtnClickListener implements AddTextDialog.DialogBtnClickListener{
        @Override
        public void onPositiveBtnClick(Dialog dialog, String text) {
            if(text.trim().length()==0){
                showMsg(R.string.enter_atlease_x_letter,1);
                return;
            }
            dialog.dismiss();
            if(currentMemeTextView==null){
                addMemeTextView(text);
            }else{
                currentMemeTextView.setNewText(text);
            }
            setModified(true);
            createMemeFragmentViewModel.getIsPostMemeAvailable().setValue(true);

        }

        @Override
        public void onNegativeBtnClick(Dialog dialog) {
            dialog.dismiss();
        }

        @Override
        public void onDeleteBtnClick(Dialog dialog) {

        }
    }


    private class CaptionDragListener implements View.OnTouchListener{
        float move_orgX,move_orgY;
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if(currentMemeTextView!=null){
                        currentMemeTextView.setSelected(false);
                    }
                    MemeTextView memeTextView=(MemeTextView) v;
                    setCurrentMemeTextView(memeTextView);
                    viewHolder.sizeBsb.setProgress(memeTextView.getMemeData().getTextSize());
                    viewHolder.outlineBsb.setProgress(memeTextView.getMemeData().getStrokeWidth());
                    move_orgX=event.getRawX();
                    move_orgY=event.getRawY();

                    break;
                case MotionEvent.ACTION_MOVE:
                    float offsetX=(event.getRawX()-move_orgX);
                    float offsetY=(event.getRawY()-move_orgY);
                    v.setX(v.getX()+offsetX);
                    v.setY(v.getY()+offsetY);
                    move_orgX=event.getRawX();
                    move_orgY=event.getRawY();
                    if(currentMemeTextView!=null){
                        currentMemeTextView.setSelected(false);
                    }
                    setModified(true);
                    break;
                case MotionEvent.ACTION_UP:
                    if(currentMemeTextView!=null){
                        currentMemeTextView.setSelected(true);
                        currentMemeTextView.setText(currentMemeTextView.getMemeData().text);
                    }
                    break;
            }
            return true;
        }
    }

    private class LayoutListener implements ViewTreeObserver.OnGlobalLayoutListener{
        @Override
        public void onGlobalLayout() {
            viewHolder.memeCreateBody.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
            int width = viewHolder.memeCreateBody.getWidth();
            int height= viewHolder.memeCreateBody.getHeight();
            String gridName = createMemeFragmentViewModel.getCurrentGridName();
            String bundleImageUri = arguments.getImageUrl();
            gridFragment=GridFragment.newInstance(gridName,width,height);
            gridFragment.setImagePickInterceptionListener(new ImagePickListener());
            gridFragment.setImageInterceptionListener(new ImagePickedListener());
            gridFragment.setLayoutMeasurementChangeListener(new GridLayoutMeasurementChangeListener());
            gridFragment.setGridTouchListener(new GridTouchListener());
            getSupportFragmentManager().beginTransaction().replace(R.id.memeImageFragmentContainer,gridFragment).commit();
            if(bundleImageUri!=null){
             showCropFragment(Uri.parse(bundleImageUri),1);
            }
        }
    }
    private class GridLayoutMeasurementChangeListener implements GridFragment.LayoutMeasurementChangeListener{
        @Override
        public void onLayoutMeasurementChanged(int width, int height) {
            viewHolder.memeImageFragmentContainer.getLayoutParams().width=width;
            viewHolder.memeImageFragmentContainer.getLayoutParams().height=height;
        }
    }
    private class GridTouchListener implements GridFragment.GridTouchListener{
        @Override
        public void onTouch() {
            setCurrentMemeTextView(null);
            setCurrentStickerImageView(null);
        }
    }
    private class FontDialogItemClickListener implements FontDialog.FontDialogItemClickListener{
        @Override
        public void onClick(Dialog dialog, String fontName) {
            dialog.dismiss();
            createMemeFragmentViewModel.setSelectedFontName(fontName);
            setTypeFace(fontName,currentMemeTextView);
            setModified(true);
        }
    }
    private class TextSizeBsbProgressListener implements BubbleSeekBar.OnProgressChangedListener{
        @Override
        public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
            if(currentMemeTextView!=null){
                currentMemeTextView.setNewSize(progress);
                setModified(true);
            }
        }

        @Override
        public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

        }

        @Override
        public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

        }
    }
    private class OutlineSizeBsbProgressListener implements BubbleSeekBar.OnProgressChangedListener{
        @Override
        public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
            if(currentMemeTextView!=null){
                currentMemeTextView.setNewStrokeWidth(progressFloat);
                setModified(true);
            }
        }

        @Override
        public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

        }

        @Override
        public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

        }
    }
    private class StickerImageViewListener implements StickerImageView.StickerImageViewTouchListener{
        @Override
        public void onTouch(StickerImageView stickerImageView) {
            setCurrentStickerImageView(stickerImageView);
        }

        @Override
        public void onModify() {
            setModified(true);
        }
    }
    private class DefaultStickerClickListener implements StickerDialog.StickerDialogItemClickListener{
        @Override
        public void onClick(Dialog dialog, DefaultSticker defaultSticker) {
            String uriString=Constants.ASSET_PATH+"/"+Constants.STICKER_ASSET_PATH+"/"+defaultSticker.getName();
            addStickerViewImage(Uri.parse(uriString));
            dialog.dismiss();
            setModified(true);
        }
    }
    private class MemeSaveListener implements SaveImageListener{
        @Override
        public void onSuccess(String  uriString) {
            showMsgWithGravity(R.string.meme_save_success,Gravity.CENTER);
            setModified(false);
        }

        @Override
        public void onFailure() {
            showMsg(R.string.cant_save_meme);
        }
    }

    private class MemeShareListener implements SaveImageListener{
        @Override
        public void onSuccess(String uriString) {
            Uri contentUri= Uri.parse(uriString);
            if(contentUri!=null){
                Intent shareIntent=new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri,getContext().getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM,contentUri);
                startActivity(Intent.createChooser(shareIntent,getResources().getString(R.string.share)));
            }
        }

        @Override
        public void onFailure() {
            showMsg(R.string.cant_share_meme);
        }
    }

    private class UploadMemeImageSaveListener implements SaveImageListener{
        @Override
        public void onSuccess(String uriString) {
            Uri contentUri= Uri.parse(uriString);
            MyImage myImage = AppUtils.getDetailedImage(getContext(),contentUri);
            showUploadMemeFragment(myImage);
        }

        @Override
        public void onFailure() {

        }
    }

    private class BackConfirmDialogListener implements ConfirmationDialog.AlertDialogBtnClickListner{
        @Override
        public void onPositiveBtnClick(Dialog dialog) {
            dialog.dismiss();
            setModified(false);
            onBackPressedCallback.setEnabled(false);
            getMainActivity().onBackPressed();
        }

        @Override
        public void onNegativeBtnClick(Dialog dialog) {
            dialog.dismiss();
        }
    }


    private class BorderActionBtnClickListener implements BorderFragment.ActionButtonClickListener{
        @Override
        public void onPositiveButtonClick(BorderModel borderModel) {
            createMemeFragmentViewModel.setBorderModel(borderModel);
            if(borderModel.getTop()==0 && borderModel.getBottom()==0 && borderModel.getRight()==0 && borderModel.getLeft()==0){
                createMemeFragmentViewModel.getIsBorderColorAvailable().setValue(false);
                gridFragment.setImage(createMemeFragmentViewModel.getSingleGridBitmap(),1,true);
            }else{
                createMemeFragmentViewModel.getIsBorderColorAvailable().setValue(true);
                new SetBorderToBitMap(createMemeFragmentViewModel.getSingleGridBitmap(),borderModel,createMemeFragmentViewModel.getBorderColor()).execute();
            }
            getMainActivity().onBackPressed();
        }

        @Override
        public void onNegativeButtonClick() {
            getMainActivity().onBackPressed();
        }
    }

    private class SetBorderToBitMap extends AsyncTask{
        /*If 100% is 100px then 250px of image width/height is good*/
        private  static final int scaleFactoringWidth=250;
        Bitmap resultBitmap;
        Bitmap bitmap;
        BorderModel borderModel;
        String color;
        public SetBorderToBitMap(Bitmap bitmap, BorderModel borderModel,String color) {
            this.bitmap = bitmap;
            this.borderModel = borderModel;
            this.color=color;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoadingDialog();
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            try{
                int width = bitmap.getWidth();
                int height = bitmap.getHeight();
                int topFactor  = (int)(((double)height/scaleFactoringWidth)*borderModel.getTop());
                int bottomFactor = (int)(((double)height/scaleFactoringWidth)*borderModel.getBottom());
                int leftFactor = (int)(((double)width/scaleFactoringWidth)*borderModel.getLeft());
                int rightFactor = (int)(((double)width/scaleFactoringWidth)*borderModel.getRight());
                resultBitmap = Bitmap.createBitmap(width+(leftFactor+rightFactor),
                        height+(topFactor+bottomFactor),Bitmap.Config.ARGB_8888);
                Bitmap tempBitmap=Bitmap.createScaledBitmap(bitmap,width,height,false);
                /*coping bitmap*/
                Canvas canvas=new Canvas(resultBitmap);
                Paint paint=new Paint();
                paint.setAntiAlias(true);
                paint.setColor(Color.parseColor(color));
                canvas.drawPaint(paint);
                canvas.drawBitmap(tempBitmap,leftFactor,topFactor,paint);
            }catch (Exception e){
            }
            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            gridFragment.setImage(resultBitmap,1,false);
            hideLoadingDialog();
        }
    }

    private class CropListener implements CropFragment.Listener{
        int gridNo;

        public CropListener(int gridNo) {
            this.gridNo = gridNo;
        }

        @Override
        public void onBackClick() {
            getMainActivity().onBackPressed();
        }

        @Override
        public void onCrop(Uri uri) {
            setCropImage(uri,gridNo);
        }

        @Override
        public void onCropFailed(Uri uri) {
            setCropImage(uri,gridNo);
        }

        @Override
        public void onDontCropClick(Uri uri) {
            setCropImage(uri,gridNo);
        }
    }

    private class ImagePickedListener implements GridFragment.ImageInterceptionListener{
        @Override
        public void onImagePicked(Uri uri, int gridNo) {
            showCropFragment(uri,gridNo);
        }
    }
    private class ImagePickListener implements GridFragment.ImagePickInterceptionListener{
        @Override
        public void onImagePick() {
            showChooseImageBottomSheet();
        }
    }

    private class PickTemplateItemClickListener implements CreateMemePickTemplateFragment.TemplateItemClickListener{
        @Override
        public void onClick(TemplateEntity templateEntity) {
            hideTemplates();
            showCropFragment(Uri.parse(templateEntity.imageUrl),gridFragment.getClickedGridNo());
        }
    }
    private class ChooseImageBSCategoryClickListener implements CategoryFragment.ListItemClickListener{
        @Override
        public void onListItemClick(CategoryEntity categoryEntity) {
            hideChooseImageBottomSheet();
            showTemplates(Constants.API_TYPE_TEMPLATES_FEED,categoryEntity);
        }
    }
    private class PickImageStaticItemsClickListener implements MenuItemsFragment.ListItemClickListener{
        @Override
        public void onListItemClick(int id) {
            switch (id){
                case MenuItemsData.ID_GALLERY:
                    hideChooseImageBottomSheet();
                    gridFragment.pickImage();
                    break;
                case MenuItemsData.ID_MY_TEMPLATES:
                    hideChooseImageBottomSheet();
                    showTemplates(Constants.API_TYPE_MY_TEMPLATES,null);
                    break;
                case MenuItemsData.ID_FAV_TEMPLATES:
                    hideChooseImageBottomSheet();
                    showTemplates(Constants.API_TYPE_FAV_TEMPLATES,null);
                    break;

            }
        }
    }

    private class UploadMemeHandShake implements UploadMemePopupFragment.ParentHandShakes{
        @Override
        public void onHide() {
            hideUploadMemeFragment();
        }
    }
    /*class end*/

}
