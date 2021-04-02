package com.thugdroid.memeking.viewmodel;

import androidx.lifecycle.ViewModel;

public class StartFragmentViewModel extends ViewModel {
    boolean loggedInUserObserverExecuted,selectedCategoryObserverExecuted, selectedNavDrawerMenuExecuted;

    public boolean isLoggedInUserObserverExecuted() {
        return loggedInUserObserverExecuted;
    }

    public void setLoggedInUserObserverExecuted(boolean loggedInUserObserverExecuted) {
        this.loggedInUserObserverExecuted = loggedInUserObserverExecuted;
    }

    public boolean isSelectedCategoryObserverExecuted() {
        return selectedCategoryObserverExecuted;
    }

    public void setSelectedCategoryObserverExecuted(boolean selectedCategoryObserverExecuted) {
        this.selectedCategoryObserverExecuted = selectedCategoryObserverExecuted;
    }

    public boolean isSelectedNavDrawerMenuExecuted() {
        return selectedNavDrawerMenuExecuted;
    }

    public void setSelectedNavDrawerMenuExecuted(boolean selectedNavDrawerMenuExecuted) {
        this.selectedNavDrawerMenuExecuted = selectedNavDrawerMenuExecuted;
    }

}
