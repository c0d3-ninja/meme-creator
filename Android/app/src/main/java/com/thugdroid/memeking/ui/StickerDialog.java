package com.thugdroid.memeking.ui;

import android.app.Activity;
import android.app.Dialog;
import android.widget.GridView;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.adapters.StickerAdapter;
import com.thugdroid.memeking.model.DefaultSticker;
import com.thugdroid.memeking.utils.DialogUtils;

import java.util.ArrayList;
import java.util.List;

public class StickerDialog implements StickerAdapter.StickerClickListener {
    Activity activity;
    Dialog dialog;
    List<DefaultSticker> defaultStickers;
    StickerDialogItemClickListener stickerDialogItemClickListener;
    public StickerDialog(Activity activity) {
        this.activity = activity;
        dialog=new Dialog(activity);
    }

    public void setStickerDialogItemClickListener(StickerDialogItemClickListener stickerDialogItemClickListener) {
        this.stickerDialogItemClickListener = stickerDialogItemClickListener;
    }

    public void setDefaultStickers(List<DefaultSticker> defaultStickers) {
        this.defaultStickers = defaultStickers;
    }

    public List<DefaultSticker> getDefaultStickers() {
        if(defaultStickers ==null){
            defaultStickers =new ArrayList<>();
        }
        return defaultStickers;
    }

    public void show(){
        dialog.setContentView(R.layout.dialog_stickers);
        GridView gridView=dialog.findViewById(R.id.stickersGridView);
        StickerAdapter stickerAdapter=new StickerAdapter(activity, getDefaultStickers());
        stickerAdapter.setStickerClickListener(this::onClick);
        gridView.setAdapter(stickerAdapter);
        dialog.setCancelable(true);
        dialog.show();
        DialogUtils.setFullWidth(dialog);
    }

    @Override
    public void onClick(DefaultSticker defaultSticker) {
        if(stickerDialogItemClickListener!=null){
            stickerDialogItemClickListener.onClick(dialog,defaultSticker);
        }
    }

    public interface StickerDialogItemClickListener{
        void onClick(Dialog dialog,DefaultSticker defaultSticker);
    }
}
