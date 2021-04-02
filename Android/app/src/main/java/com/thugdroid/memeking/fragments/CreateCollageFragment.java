package com.thugdroid.memeking.fragments;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.RelativeLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.thugdroid.libs.collagegrid.constants.GridNameConstants;
import com.thugdroid.libs.collagegrid.fragments.GridFragment;
import com.thugdroid.memeking.MediaFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.constants.FragmentTags;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.room.entity.TemplateEntity;
import com.thugdroid.memeking.ui.ConfirmationDialog;
import com.thugdroid.memeking.ui.LoadingDialog;
import com.thugdroid.memeking.ui.data.MenuItemsData;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.viewmodel.CreateCollageFragmentViewModel;

public class CreateCollageFragment extends MediaFragment {

    private ViewHolder viewHolder;
    private GridFragment gridFragment;
    private LayoutListener layoutListener;
    private CreateCollageFragmentViewModel createCollageFragmentViewModel;
    private CreateCollageFragmentArgs args;
    private OnBackPressedCallback onBackPressedCallback;
    private LoadingDialog loadingDialog;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_createcollage,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
        initObservers();
        createCollageFragmentViewModel.setCurrentGridName(args.getGridName());
        layoutListener=new LayoutListener();
        viewHolder.memeCreateBody.getViewTreeObserver().addOnGlobalLayoutListener(layoutListener);
        /*lazy load*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadCategories();
            }
        },300);

    }

    @Override
    public void initVariables() {
        args=CreateCollageFragmentArgs.fromBundle(getArguments());
        createCollageFragmentViewModel=new ViewModelProvider(this).get(CreateCollageFragmentViewModel.class);
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
        viewHolder.freezeLayer.setOnClickListener(this::onClick);
        viewHolder.createCollageBack.setOnClickListener(this::onClick);
        viewHolder.createCollageNextFab.setOnClickListener(this::onClick);
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
        onBackPressedCallback=new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {


                if(viewHolder.templatesFragmentParent.getVisibility()==View.VISIBLE){
                    hideTemplates();
                }
                else if(viewHolder.chooseImageBsBehavior.getState()!=BottomSheetBehavior.STATE_HIDDEN){
                    hideChooseImageBottomSheet();
                }
                else if(viewHolder.cropFragmentContainer.getVisibility()==View.VISIBLE){
                    hideCropFragment();
                }
                else if(createCollageFragmentViewModel.isModified()) {
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

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.createCollageBack:
            case R.id.createCollageFreezeLayer:
                getMainActivity().onBackPressed();
                break;
            case R.id.createCollageNextFab:
                saveAndNavigate();
                break;
        }
    }

    @Override
    public void onDestroyView() {
        onBackPressedCallback.setEnabled(false);
        super.onDestroyView();
    }

    /*setters start*/
    private void setModified(boolean modified){
        createCollageFragmentViewModel.setModified(modified);
    }
    /*setters end*/


    /*other methods start*/
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
    private void saveAndNavigate(){
        if(gridFragment.getTotalGridCount()  ==  gridFragment.getImageAddedGridCount()){
            showLoadingDialog();
            Bitmap bitmap = AppUtils.getBitmapFromView(viewHolder.collageImageFragmentContainer);
            new SaveImage(Constants.SHARE_MEME_FOLDER_NAME,bitmap,true,new CollageSaveListener()).execute();
        }else {
            showMsg(R.string.add_all_images_to_create_meme);
        }
    }
    private void loadCategories(){
        CategoryFragment categoryFragment=new CategoryFragment();
        categoryFragment.setListItemListener(new ChooseImageBSCategoryClickListener());
        getSupportFragmentManager().beginTransaction().replace(R.id.chooseImageCategoryFragmentContainer,categoryFragment).commit();
    }

    private void showChooseImageBottomSheet(){
        showFreezeLayer();
        viewHolder.chooseImageBsBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
    private void hideChooseImageBottomSheet(){
        viewHolder.chooseImageBsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
    private void showFreezeLayer(){
        viewHolder.freezeLayer.setVisibility(View.VISIBLE);
    }
    private void hideFreezeLayer(){
        viewHolder.freezeLayer.setVisibility(View.GONE);
    }
    private void showCropFragment(Uri imageUri,int gridNo){
        viewHolder.cropFragmentContainer.setVisibility(View.VISIBLE);
        CropFragment cropFragment=new CropFragment();
        cropFragment.setUri(imageUri);
        cropFragment.setListener(new CropListener(gridNo));
        getSupportFragmentManager().beginTransaction().replace(R.id.createCollageCropFragmentContainer,cropFragment).commit();
    }
    private void hideCropFragment(){
        viewHolder.cropFragmentContainer.setVisibility(View.GONE);
    }
    private void setCropImage(Uri uri,int gridNo){
        gridFragment.setImage(uri,gridNo,true);
        hideCropFragment();
        setModified(true);
    }
    private void showTemplates(int templateType,CategoryEntity categoryEntity){
        viewHolder.templatesFragmentParent.setVisibility(View.VISIBLE);
        CreateMemePickTemplateFragment createMemePickTemplateFragment =new CreateMemePickTemplateFragment();
        createMemePickTemplateFragment.setTemplateType(templateType);
        createMemePickTemplateFragment.setCategoryEntity(categoryEntity);
        createMemePickTemplateFragment.setTemplateItemClickListener(new PickTemplateItemClickListener());
        getSupportFragmentManager().beginTransaction().replace(R.id.createCollagePickTemplatesParent, createMemePickTemplateFragment,
                FragmentTags.CREATE_MEME_PICK_TEMPLATE_FRAGMENT).commit();
    }
    private void hideTemplates(){
        viewHolder.templatesFragmentParent.setVisibility(View.GONE);
        Fragment fragment=getSupportFragmentManager().findFragmentByTag(FragmentTags.CREATE_MEME_PICK_TEMPLATE_FRAGMENT);
        if(fragment!=null){
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        }
    }
    /*other methods end*/


    /*other listeners start*/
    private class ImagePickListener implements GridFragment.ImagePickInterceptionListener{
        @Override
        public void onImagePick() {
            showChooseImageBottomSheet();
        }
    }
    private class ImagePickedListener implements GridFragment.ImageInterceptionListener{
        @Override
        public void onImagePicked(Uri uri, int gridNo) {
            showCropFragment(uri,gridNo);
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
    private class GridLayoutMeasurementChangeListener implements GridFragment.LayoutMeasurementChangeListener{
        @Override
        public void onLayoutMeasurementChanged(int width, int height) {
            viewHolder.collageImageFragmentContainer.getLayoutParams().width=width;
            viewHolder.collageImageFragmentContainer.getLayoutParams().height=height;
        }
    }
    private class ChooseImageBSCategoryClickListener implements CategoryFragment.ListItemClickListener{
        @Override
        public void onListItemClick(CategoryEntity categoryEntity) {
            hideChooseImageBottomSheet();
            showTemplates(Constants.API_TYPE_TEMPLATES_FEED,categoryEntity);
        }
    }

    private class PickTemplateItemClickListener implements CreateMemePickTemplateFragment.TemplateItemClickListener{
        @Override
        public void onClick(TemplateEntity templateEntity) {
            hideTemplates();
            showCropFragment(Uri.parse(templateEntity.imageUrl),gridFragment.getClickedGridNo());
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

    private class CollageSaveListener implements SaveImageListener{
        @Override
        public void onSuccess(String uriString) {
            hideLoadingDialog();
            CreateCollageFragmentDirections.ActionCreateCollageFragmentToCreateMemeFragment action =
                    CreateCollageFragmentDirections.actionCreateCollageFragmentToCreateMemeFragment(GridNameConstants.L1,uriString,null);
            navigate(action);
        }

        @Override
        public void onFailure() {
            hideLoadingDialog();
            showMsg(R.string.something_went_wrong_while_creating_collage);
        }
    }
    /*other listeners end*/
    private class LayoutListener implements ViewTreeObserver.OnGlobalLayoutListener{
        @Override
        public void onGlobalLayout() {
            viewHolder.memeCreateBody.getViewTreeObserver().removeOnGlobalLayoutListener(layoutListener);
            int width = viewHolder.memeCreateBody.getWidth();
            int height= viewHolder.memeCreateBody.getHeight();
            String gridName = createCollageFragmentViewModel.getCurrentGridName();
            gridFragment=GridFragment.newInstance(gridName,width,height);
            gridFragment.setImagePickInterceptionListener(new ImagePickListener());
            gridFragment.setImageInterceptionListener(new ImagePickedListener());
            gridFragment.setLayoutMeasurementChangeListener(new GridLayoutMeasurementChangeListener());
            getSupportFragmentManager().beginTransaction().replace(R.id.collageImageFragmentContainer,gridFragment).commit();
        }
    }
    private class ViewHolder{
        BottomSheetBehavior chooseImageBsBehavior;
        RelativeLayout memeCreateBody;
        ConstraintLayout freezeLayer;
        ConstraintLayout collageImageFragmentContainer;
        View cropFragmentContainer,templatesFragmentParent,createCollageBack;
        FloatingActionButton createCollageNextFab;
        public ViewHolder() {
            memeCreateBody=findViewById(R.id.createCollageBody);
            freezeLayer=findViewById(R.id.createCollageFreezeLayer);
            cropFragmentContainer=findViewById(R.id.createCollageCropFragmentContainer);
            collageImageFragmentContainer=findViewById(R.id.collageImageFragmentContainer);
            templatesFragmentParent=findViewById(R.id.createCollagePickTemplatesParent);
            createCollageBack=findViewById(R.id.createCollageBack);
            createCollageNextFab=findViewById(R.id.createCollageNextFab);
            chooseImageBsBehavior=BottomSheetBehavior.from(findViewById(R.id.chooseImageBottomSheet));
            chooseImageBsBehavior.setHideable(true);
            chooseImageBsBehavior.setPeekHeight((int)getResources().getDimension(R.dimen.chooseImageOptionsBsHeight));
            chooseImageBsBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }
}
