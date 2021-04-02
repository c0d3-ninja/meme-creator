package com.thugdroid.libs.collagegrid.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.thugdroid.libs.collagegrid.R;
import com.thugdroid.libs.collagegrid.constants.Constants;
import com.thugdroid.libs.collagegrid.utils.GridUtils;

public class CollageGridAdapter extends BaseAdapter {
    Context context;
    LayoutInflater layoutInflater;
    String[] fileNames;
    RequestManager glide;
    CollageGridClickListener collageGridClickListener;

    public CollageGridAdapter(Context context) {
        this.context = context;
        layoutInflater=LayoutInflater.from(context);
       fileNames=GridUtils.getGridNames();
    }
    @Override
    public int getCount() {
        return fileNames.length;
    }

    @Override
    public Object getItem(int position) {
        return fileNames[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView==null){
            convertView=layoutInflater.inflate(R.layout.gridimage,parent,false);
        }
        if(collageGridClickListener!=null){
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    collageGridClickListener.onClick(fileNames[position],position);
                }
            });
        }
        Glide.with(context).load(Constants.ASSETS_PATH +Constants.ASSET_COLLAGE_FOLDER +"/"+ fileNames[position]+".png")
                .priority(Priority.IMMEDIATE)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into((ImageView) convertView.findViewById(R.id.collageListImage));
        return convertView;
    }

    public void setCollageGridClickListener(CollageGridClickListener collageGridClickListener) {
        this.collageGridClickListener = collageGridClickListener;
    }

    public interface CollageGridClickListener{
        void onClick(String collageGridName,int position);
    }
}
