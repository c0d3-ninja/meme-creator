package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.ViewModel;

public class SettingsFragmentViewModel extends ViewModel {
    private String instaUsername;

    public String getInstaUsername() {
        return instaUsername;
    }

    public void setInstaUsername(String instaUsername) {
        this.instaUsername = instaUsername;
    }
}
