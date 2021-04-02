package com.thugdroid.libs.stickerimageview;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;


public class StickerImageView extends StickerView implements StickerView.StickerViewTouchListener {
    private ImageView iv_main;
    private int parentWidth,parentHeight;
    StickerImageViewTouchListener stickerImageViewTouchListener;
    public StickerImageView(Context context, int parentWidth, int parentHeight) {
        super(context,parentWidth,parentHeight);
        this.parentWidth=parentWidth;
        this.parentHeight=parentHeight;
        this.iv_main = new ImageView(getContext());
        this.iv_main.setAdjustViewBounds(true);
        setStickerViewTouchListener(this);
    }

    @Override
    public View getMainView() {
        return iv_main;
    }

    public void setStickerImageViewTouchListener(StickerImageViewTouchListener stickerImageViewTouchListener) {
        this.stickerImageViewTouchListener = stickerImageViewTouchListener;
    }

    public void setImageBitmap(Bitmap bmp){
        bmp = scaleBitmap(bmp);
        Glide.with(getContext()).load(bmp).into(this.iv_main);
        redraw(bmp.getWidth(),bmp.getHeight());
    }

    public Bitmap getImageBitmap(){ return ((BitmapDrawable)this.iv_main.getDrawable()).getBitmap() ; }
    private Bitmap scaleBitmap(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        if (width > height) {
            // landscape
            float ratio = (float) width / parentWidth;
            width = parentWidth;
            height = (int)(height / ratio);
        } else if (height > width) {
            // portrait
            float ratio = (float) height / parentHeight;
            height = parentHeight;
            width = (int)(width / ratio);
        } else if(width>parentWidth || height>parentHeight){
            // square
            height = parentHeight;
            width = parentWidth;
        }

        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }

    @Override
    public void onTouch() {
        if(stickerImageViewTouchListener!=null){
            stickerImageViewTouchListener.onTouch(this);
        }
    }

    @Override
    public void onModify() {
        if(stickerImageViewTouchListener!=null){
            stickerImageViewTouchListener.onModify();
        }
    }

    public static long getAnimationDuration(){
        return StickerView.ANIMATE_DURATION;
    }

    public interface StickerImageViewTouchListener{
        void onTouch(StickerImageView stickerImageView);
        void onModify();
    }

}