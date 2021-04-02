package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.ViewModel;

public class CreateCollageFragmentViewModel extends ViewModel {
    private String currentGridName;
    private boolean isModified;


    public String getCurrentGridName() {
        return currentGridName;
    }

    public void setCurrentGridName(String currentGridName) {
        this.currentGridName = currentGridName;
    }

    public boolean isModified() {
        return isModified;
    }

    public void setModified(boolean modified) {
        isModified = modified;
    }
}
