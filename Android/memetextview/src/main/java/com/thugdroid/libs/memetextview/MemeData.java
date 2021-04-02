package com.thugdroid.libs.memetextview;

import android.view.Gravity;

public class MemeData {
    public String text="";
    public  int textSize=11;
    public float strokeWidth=1f;
    public String fontName;
    public String textColor=ColorConstants.WHITE;
    public String strokeColor=ColorConstants.BLACK;
    public String bgColor=ColorConstants.ALPHA;
    public boolean isStrokeColorModified,isBgColorModified;
    private int alignment= Gravity.CENTER;

public MemeData(){

}
    public MemeData(String text, int textSize, float strokeWidth, String fontName, String textColor, String strokeColor, String bgColor, boolean isStrokeColorModified,
                    boolean isBgColorModified,int alignment) {
        this.text = text;
        this.textSize = textSize;
        this.strokeWidth = strokeWidth;
        this.fontName = fontName;
        this.textColor = textColor;
        this.strokeColor = strokeColor;
        this.bgColor = bgColor;
        this.isStrokeColorModified = isStrokeColorModified;
        this.isBgColorModified = isBgColorModified;
        this.alignment=alignment;
    }

    public static MemeData getClonedMemeDataForNewText(MemeData memeData){
    if(memeData==null){
        return new MemeData();
    }
    return (new MemeData("",
            memeData.getTextSize(),
            memeData.getStrokeWidth(),
            memeData.getFontName(),
            memeData.getTextColor(),
            memeData.getStrokeColor(),
            memeData.getBgColor(),
            memeData.isStrokeColorModified(),
            memeData.isBgColorModified(),
            memeData.getAlignment()));
    }

    public String getText() {
        return text;
    }

    public int getTextSize() {
        return textSize;
    }

    public float getStrokeWidth() {
        return strokeWidth;
    }

    public String getFontName() {
        return fontName;
    }

    public String getTextColor() {
        return textColor;
    }

    public String getStrokeColor() {
        return strokeColor;
    }

    public String getBgColor() {
        return bgColor;
    }

    public boolean isStrokeColorModified() {
        return isStrokeColorModified;
    }

    public boolean isBgColorModified() {
        return isBgColorModified;
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }
}
