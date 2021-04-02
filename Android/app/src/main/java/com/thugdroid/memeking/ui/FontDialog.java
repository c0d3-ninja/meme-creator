package com.thugdroid.memeking.ui;

import android.app.Activity;
import android.app.Dialog;
import android.widget.GridView;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.adapters.FontAdapter;
import com.thugdroid.memeking.model.DefaultFont;
import com.thugdroid.memeking.utils.DialogUtils;

import java.util.List;

public class FontDialog {
    Activity activity;
    Dialog dialog;
    List<DefaultFont> defaultFonts;
    String selectedFont;
    FontDialogItemClickListener fontItemClickListener;
    public FontDialog(Activity activity) {
        this.activity = activity;
        dialog=new Dialog(activity);
    }

    public void setDefaultFonts(List<DefaultFont> defaultFonts) {
        this.defaultFonts = defaultFonts;
    }

    public void setSelectedFont(String selectedFont) {
        this.selectedFont = selectedFont;
    }

    public void setFontItemClickListener(FontDialogItemClickListener fontItemClickListener) {
        this.fontItemClickListener = fontItemClickListener;
    }

    public void show(){
        dialog.setContentView(R.layout.dialog_fonts);
        GridView gridView=dialog.findViewById(R.id.fontsGridView);
        FontAdapter fontAdapter=new FontAdapter(activity,defaultFonts,selectedFont);
        fontAdapter.setFontItemClickListener(new FontItemClickListener());
        gridView.setAdapter(fontAdapter);
        dialog.setCancelable(true);
        dialog.show();
        DialogUtils.setFullWidth(dialog);
    }

    public interface FontDialogItemClickListener{
        void onClick(Dialog dialog,String fontName);
    }

    class FontItemClickListener implements FontAdapter.FontItemClickListener{
        @Override
        public void onClick(String fontName) {
            if(fontItemClickListener!=null){
                fontItemClickListener.onClick(dialog,fontName);
            }
        }
    }
}
