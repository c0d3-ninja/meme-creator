package com.thugdroid.memeking.ui;

import androidx.fragment.app.FragmentActivity;

import com.thugdroid.libs.colorpicker.ColorPickerDialog;
import com.thugdroid.libs.colorpicker.ColorPickerDialogListener;

public class MyColorPickerDialog {
    FragmentActivity activity;
    ColorPickerDialog colorPickerDialog;
    ColorPickerDialogListener colorPickerDialogListener;
    public MyColorPickerDialog(int id, FragmentActivity activity) {
        this.activity = activity;
        colorPickerDialog=ColorPickerDialog.newBuilder()
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowPresets(false)
                .setShowAlphaSlider(true)
                .setDialogId(id).create();
    }
    public  void setColorPickerDialogListener(ColorPickerDialogListener colorPickerDialogListener){
        this.colorPickerDialogListener=colorPickerDialogListener;
    }
    public void show(){
        colorPickerDialog.setColorPickerDialogListener(this.colorPickerDialogListener);
        colorPickerDialog.show(activity.getSupportFragmentManager(),"ColorPicker");
    }

}
