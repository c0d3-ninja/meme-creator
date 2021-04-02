package com.thugdroid.memeking.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.constants.Constants;
import com.thugdroid.memeking.model.DefaultFont;

import java.util.ArrayList;
import java.util.List;

public class FontAdapter extends BaseAdapter {

    private Context context;
    private List<DefaultFont> list;
    private LayoutInflater layoutInflater;
    private String selectedFontName;
    private FontItemClickListener fontItemClickListener;
    public FontAdapter(Context context, List<DefaultFont> list,String selectedFontName) {
        this.context = context;
        this.list = list;
        this.selectedFontName=selectedFontName;
        layoutInflater=LayoutInflater.from(context);
    }

    public void setFontItemClickListener(FontItemClickListener fontItemClickListener) {
        this.fontItemClickListener = fontItemClickListener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    public DefaultFont getCurrentItem(int position){
        return  list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView==null){
            convertView=layoutInflater.inflate(R.layout.item_font,parent,false);
        }
        TextView smallLetters=convertView.findViewById(R.id.itemFontSmallLetter);
        TextView capitalLetters=convertView.findViewById(R.id.itemFontCapitalLetter);
        DefaultFont currentItem=getCurrentItem(position);
        Typeface typeface = Typeface.createFromAsset(context.getAssets(), Constants.FONT_ASSET_PATH+"/"+currentItem.getName());
        smallLetters.setTypeface(typeface);
        capitalLetters.setTypeface(typeface);
        if(currentItem.getName()!=null && currentItem.getName().equals(selectedFontName)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                convertView.setBackground(context.getResources().getDrawable(R.drawable.button_primary,null));
                smallLetters.setTextColor(context.getResources().getColor(R.color.onColorPrimary,null));
                capitalLetters.setTextColor(context.getResources().getColor(R.color.onColorPrimary,null));
            }
            else{
                convertView.setBackground(context.getResources().getDrawable(R.drawable.button_primary));
                smallLetters.setTextColor(ContextCompat.getColor(context,R.color.onColorPrimary));
                capitalLetters.setTextColor(ContextCompat.getColor(context,R.color.onColorPrimary));
            }
        }else{

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                convertView.setBackground(context.getResources().getDrawable(R.drawable.ripple_fontitem_bg,null));
                smallLetters.setTextColor(context.getResources().getColor(R.color.textColorPrimary,null));
                capitalLetters.setTextColor(context.getResources().getColor(R.color.textColorPrimary,null));
            }
            else{
                convertView.setBackground(context.getResources().getDrawable(R.drawable.ripple_fontitem_bg));
                smallLetters.setTextColor(ContextCompat.getColor(context,R.color.textColorPrimary));
                capitalLetters.setTextColor(ContextCompat.getColor(context,R.color.textColorPrimary));
            }
        }
        if(fontItemClickListener!=null){
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fontItemClickListener.onClick(currentItem.getName());
                }
            });
        }

        return convertView;
    }

    public interface FontItemClickListener{
        void onClick(String fontName);
    }
}
