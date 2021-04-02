package com.thugdroid.memeking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thugdroid.libs.myseekbar.BubbleSeekBar;
import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.model.BorderModel;


public class BorderFragment extends CustomFragment {
    private static final int TOP=1;
    private static final int BOTTOM=2;
    private static final int LEFT=3;
    private static final int RIGHT=4;
    private BorderModel borderModel;
    private ViewHolder viewHolder;
    private ActionButtonClickListener actionButtonClickListener;
    /*to fix Caused by java.lang.NoSuchMethodException*/
    public BorderFragment(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_border,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        initListeners();
    }


    @Override
    public void initVariables() {
    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
        viewHolder.topBsb.setProgress(getBorderModel().getTop());
        viewHolder.bottomBsb.setProgress(getBorderModel().getBottom());
        viewHolder.leftBsb.setProgress(getBorderModel().getLeft());
        viewHolder.rightBsb.setProgress(getBorderModel().getRight());

    }

    @Override
    public void initListeners() {
        viewHolder.topBsb.setOnProgressChangedListener(new BorderChangeListener(TOP));
        viewHolder.bottomBsb.setOnProgressChangedListener(new BorderChangeListener(BOTTOM));
        viewHolder.leftBsb.setOnProgressChangedListener(new BorderChangeListener(LEFT));
        viewHolder.rightBsb.setOnProgressChangedListener(new BorderChangeListener(RIGHT));
        (findViewById(R.id.dialogBorderPositiveBtn)).setOnClickListener(this::onClick);
        (findViewById(R.id.dialogBorderNegativeBtn)).setOnClickListener(this::onClick);
    }

    @Override
    public void initObservers() {

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.dialogBorderPositiveBtn:
                actionButtonClickListener.onPositiveButtonClick(borderModel);
                break;
            case R.id.dialogBorderNegativeBtn:
                actionButtonClickListener.onNegativeButtonClick();
                break;
        }

    }

    public BorderModel getBorderModel() {
        if(borderModel==null){
            borderModel=new BorderModel(0,0,0,0);
        }
        return borderModel;
    }

    public void setActionButtonClickListener(ActionButtonClickListener actionButtonClickListener) {
        this.actionButtonClickListener = actionButtonClickListener;
    }

    public void setBorderModel(BorderModel borderModel) {
        this.borderModel = new BorderModel(borderModel.getTop(),borderModel.getBottom(),borderModel.getLeft(),borderModel.getRight());
    }

    public interface ActionButtonClickListener{
        void onPositiveButtonClick(BorderModel borderModel);
        void onNegativeButtonClick();
    }

    private class ViewHolder{
        BubbleSeekBar topBsb,bottomBsb,leftBsb,rightBsb;
        public ViewHolder() {
            topBsb=findViewById(R.id.dialogBorderTopBsb);
            bottomBsb=findViewById(R.id.dialogBorderBottomBsb);
            leftBsb=findViewById(R.id.dialogBorderLeftBsb);
            rightBsb=findViewById(R.id.dialogBorderRightBsb);
        }
    }

    class BorderChangeListener implements BubbleSeekBar.OnProgressChangedListener{
        int type;

        public BorderChangeListener(int type) {
            this.type = type;
        }

        @Override
        public void onProgressChanged(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {

        }

        @Override
        public void getProgressOnActionUp(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat) {

        }

        @Override
        public void getProgressOnFinally(BubbleSeekBar bubbleSeekBar, int progress, float progressFloat, boolean fromUser) {
            switch (type){
                case TOP:
                    borderModel.setTop(progress);
                    break;
                case BOTTOM:
                    borderModel.setBottom(progress);
                    break;
                case LEFT:
                    borderModel.setLeft(progress);
                    break;
                case RIGHT:
                    borderModel.setRight(progress);
                    break;
            }
        }
    }
}
