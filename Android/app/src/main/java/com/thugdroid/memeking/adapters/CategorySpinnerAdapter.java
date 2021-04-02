package com.thugdroid.memeking.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.room.entity.CategoryEntity;
import com.thugdroid.memeking.utils.AppUtils;

import java.util.List;

public class CategorySpinnerAdapter extends BaseAdapter {

    private Context context;
    private List<CategoryEntity> categoryEntities;
    private LayoutInflater layoutInflater;
    private RequestManager glide;
    public CategorySpinnerAdapter(Context context, List<CategoryEntity> categoryEntities) {
        this.context = context;
        this.categoryEntities = categoryEntities;
        layoutInflater=LayoutInflater.from(context);
        glide=Glide.with(context);
    }

    @Override
    public int getCount() {
        return categoryEntities.size();
    }

    @Override
    public Object getItem(int position) {
        return categoryEntities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public CategoryEntity getCategory(int position){
        return categoryEntities.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup viewGroup) {
        if(convertView==null){
            convertView=layoutInflater.inflate(R.layout.item_menu,viewGroup,false);
        }
        ImageView logo=convertView.findViewById(R.id.navDrawerItemLogo);
        TextView logoPlaceHolder=convertView.findViewById(R.id.navDrawerLogoPlaceholder);
        TextView title=convertView.findViewById(R.id.navDrawerItemTitle);
        CategoryEntity categoryEntity=(CategoryEntity)getItem(position);
        if(!CategoryEntity.DROPDOWN_NONE.equals(categoryEntity.getId())){
            if(categoryEntity.imageUrl==null){
                setCategoryText(logoPlaceHolder,logo,logoPlaceHolder,AppUtils.getShortenName(categoryEntity.name,1),position);
                logoPlaceHolder.setVisibility(View.VISIBLE);
                logo.setVisibility(View.GONE);
            }else{
                logoPlaceHolder.setVisibility(View.GONE);
                logo.setVisibility(View.VISIBLE);
                glide.asBitmap().load(categoryEntity.imageUrl).listener(new RequestListener<Bitmap>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                        setCategoryText(logoPlaceHolder,logo,logoPlaceHolder,AppUtils.getShortenName(categoryEntity.name,1),position);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                }).into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        glide.load(resource).apply(new RequestOptions().transform(new CenterCrop(),new RoundedCorners(16))).into(logo);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });

            }
        }else{
            logo.setVisibility(View.GONE);
            logoPlaceHolder.setVisibility(View.GONE);
        }

        title.setText(categoryEntity.name);
        return convertView;
    }
    private void setCategoryText(View visibleView, View hideableView, TextView textView,String str,int position){
        visibleView.setVisibility(View.VISIBLE);
        hideableView.setVisibility(View.GONE);
        textView.setText(str);
        textView.setBackgroundResource(AppUtils.getDrawableFrom(position));
    }
}
