package com.thugdroid.memeking.viewmodel;

import android.graphics.Bitmap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.thugdroid.libs.collagegrid.constants.GridNameConstants;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.model.BorderModel;

public class CreateMemeFragmentViewModel extends ViewModel {
    MutableLiveData<Boolean> isMemeTextSelected;
    MutableLiveData<Boolean> isWatermarkHideable;
    Bitmap singleGridBitmap;
    String currentGridName;
    String selectedFontName;
    String  logoPath;
    boolean isModified;
    boolean isShowSingleGridWalkThrough;

    /*used in back press operation*/
    boolean isWalkThroughShowing;

    MutableLiveData<Boolean> isBorderOptionAvailable;
    MutableLiveData<Boolean> isBorderColorAvailable;
    MutableLiveData<Boolean> isPostMemeAvailable;
    BorderModel borderModel;
    String borderColor;

    public MutableLiveData<Boolean> getIsMemeTextSelected() {
        if(isMemeTextSelected==null){
            isMemeTextSelected=new MutableLiveData<>(false);
        }
        return isMemeTextSelected;
    }

    public String getSelectedFontName() {
        if(selectedFontName==null){
            selectedFontName= Constants.DEFAULT_FONTNAME;
        }
        return selectedFontName;
    }

    public void setSelectedFontName(String selectedFontName) {
        this.selectedFontName = selectedFontName;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }

    public boolean isShowSingleGridWalkThrough() {
        return isShowSingleGridWalkThrough;
    }

    public void setShowSingleGridWalkThrough(boolean showSingleGridWalkThrough) {
        isShowSingleGridWalkThrough = showSingleGridWalkThrough;
    }


    public boolean isWalkThroughShowing() {
        return isWalkThroughShowing;
    }

    public void setWalkThroughShowing(boolean walkThroughShowing) {
        isWalkThroughShowing = walkThroughShowing;
    }

    public MutableLiveData<Boolean> getIsBorderOptionAvailable() {
        if(isBorderOptionAvailable==null){
            isBorderOptionAvailable=new MutableLiveData<>(false);
        }
        return isBorderOptionAvailable;
    }

    public MutableLiveData<Boolean> getIsBorderColorAvailable() {
        if(isBorderColorAvailable==null){
            isBorderColorAvailable=new MutableLiveData<>(false);
        }
        return isBorderColorAvailable;
    }

    public MutableLiveData<Boolean> getIsPostMemeAvailable() {
        if(isPostMemeAvailable==null){
            isPostMemeAvailable=new MutableLiveData<>(false);
        }
        return isPostMemeAvailable;
    }

    public BorderModel getBorderModel() {
        if(borderModel==null){
            borderModel=new BorderModel(0,0,0,0);
        }
        return borderModel;
    }

    public void setBorderModel(BorderModel borderModel) {
        this.borderModel = borderModel;
    }

    public String getCurrentGridName() {
        if(currentGridName==null){
            currentGridName= GridNameConstants.L1;
        }
        return currentGridName;
    }

    public void setCurrentGridName(String currentGridName) {
        this.currentGridName = currentGridName;
    }

    public Bitmap getSingleGridBitmap() {
        return singleGridBitmap;
    }

    public void setSingleGridBitmap(Bitmap singleGridBitmap) {
        this.singleGridBitmap = singleGridBitmap;
    }

    public String getBorderColor() {
        if(borderColor==null){
            borderColor="#000000";
        }
        return borderColor;
    }

    public void setBorderColor(String borderColor) {
        this.borderColor = borderColor;
    }

    public MutableLiveData<Boolean> getIsWatermarkHideable() {
        if(isWatermarkHideable==null){
            isWatermarkHideable=new MutableLiveData<>(false);
        }
        return isWatermarkHideable;
    }

    public boolean isWaterMarkHideable(){
        return getIsWatermarkHideable().getValue();
    }




}
