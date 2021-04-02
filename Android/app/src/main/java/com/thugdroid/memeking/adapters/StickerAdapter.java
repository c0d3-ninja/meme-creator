package com.thugdroid.memeking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.model.DefaultSticker;
import com.thugdroid.memeking.utils.AppUtils;

import java.util.List;

public class StickerAdapter extends BaseAdapter {
    private Context context;
    private List<DefaultSticker> list;
    private LayoutInflater layoutInflater;
    private RequestManager glide;
    private StickerClickListener stickerClickListener;
    public StickerAdapter(Context context, List<DefaultSticker> list) {
        this.context = context;
        this.list = list;
        layoutInflater=LayoutInflater.from(context);
        glide=Glide.with(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }
    public DefaultSticker getCurrentItem(int position){
        return  list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView=layoutInflater.inflate(R.layout.item_sticker,parent,false);
        }
        ImageView imageView=convertView.findViewById(R.id.itemStickerImage);
        DefaultSticker currentDefaultSticker =getCurrentItem(position);
        glide.load(AppUtils.getAssetPath(AppUtils.ASSET_STICKERS)+"/"+ currentDefaultSticker.getName()).into(imageView);
        if(stickerClickListener!=null){
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stickerClickListener.onClick(currentDefaultSticker);
                }
            });
        }
        return convertView;
    }

    public void setStickerClickListener(StickerClickListener stickerClickListener) {
        this.stickerClickListener = stickerClickListener;
    }

    public interface StickerClickListener{
        void onClick(DefaultSticker defaultSticker);
    }
}
