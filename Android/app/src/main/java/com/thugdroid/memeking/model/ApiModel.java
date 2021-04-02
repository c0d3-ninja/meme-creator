package com.thugdroid.memeking.model;

import com.thugdroid.memeking.constants.HttpCodes;

public class ApiModel {
    public static final int LOADINGSTATE_IDLE=0;
    public static final int LOADINGSTATE_REQUEST=1;
    public static final int LOADINGSTATE_REQUEST_SUCCESS=2;
    public static final int LOADINGSTATE_REQUEST_FAILURE=3;
    private int loadingState;
    private int statusCode;
    private boolean shouldCallApi;
    private String errorMessage;

    public ApiModel() {
        this.loadingState=LOADINGSTATE_IDLE;
        this.statusCode= HttpCodes.IDLE;
        this.shouldCallApi=true;
    }


    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getLoadingState() {
        return loadingState;
    }

    public void setLoadingState(int loadingState) {
        this.loadingState = loadingState;
    }

    public boolean isShouldCallApi() {
        return shouldCallApi;
    }

    public void setShouldCallApi(boolean shouldCallApi) {
        this.shouldCallApi = shouldCallApi;
    }

    public String getErrorMessage() {
        if(errorMessage==null){
            errorMessage="";
        }
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
