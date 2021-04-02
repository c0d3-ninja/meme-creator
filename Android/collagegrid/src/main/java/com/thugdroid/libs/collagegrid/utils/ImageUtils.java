package com.thugdroid.libs.collagegrid.utils;

import android.graphics.Bitmap;

public class ImageUtils {
    public static Bitmap getScaleBitmapForGrid(Bitmap bm, int maxWidth, int maxHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        if(width<height){
            float ratio = (float) width/maxWidth;
            width = maxWidth;
            height = (int)(height/ratio);
        }else if(width>height) {
            float ratio = (float) height/maxHeight;
            height = maxHeight;
            width = (int)(width/ratio);
        }else if(maxWidth>maxHeight){
            float ratio = (float) width/maxWidth;
            width = maxWidth;
            height = (int)(height/ratio);
        }else if(maxWidth<maxHeight){
            float ratio = (float) height/maxHeight;
            height = maxHeight;
            width = (int)(width/ratio);
        }else {
            width=maxWidth;
            height=maxHeight;
        }

        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }
    public static Bitmap scaleBitmap(Bitmap bm, int maxWidth, int maxHeight){
        int width = bm.getWidth();
        int height = bm.getHeight();
        /*display depends on width so we need to check minwidth=maxwidth*/
        if(width<maxWidth){
            float ratio = (float) maxWidth/width;
            width=maxWidth;
            height=(int)(height*ratio);
        }
        if(width>maxWidth){
            float ratio = (float) width/maxWidth;
            width = maxWidth;
            height = (int)(height/ratio);
        }
        if(height>maxHeight){
            float ratio = (float) height/maxHeight;
            height = maxHeight;
            width = (int)(width/ratio);
        }
        bm = Bitmap.createScaledBitmap(bm, width, height, true);
        return bm;
    }

    public static Size getImageMaxSize(Bitmap bm,int maxWidth,int maxHeight){
        int width = bm.getWidth();
        int height = bm.getHeight();
        /*display depends on width so we need to check minwidth=maxwidth*/
        if(width<maxWidth){
            float ratio = (float) maxWidth/width;
            width=maxWidth;
            height=(int)(height*ratio);
        }
        if(width>maxWidth){
            float ratio = (float) width/maxWidth;
            width = maxWidth;
            height = (int)(height/ratio);
        }
        if(height>maxHeight){
            float ratio = (float) height/maxHeight;
            height = maxHeight;
            width = (int)(width/ratio);
        }
        return (new Size(width,height));
    }

    public static class Size{
        private int width,height;
        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }
}
