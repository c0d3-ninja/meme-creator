package com.thugdroid.memeking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.room.entity.RegionEntity;


import java.util.List;

public class RegionGridAdapter extends BaseAdapter {

    private Context context;
    private List regions;
    private LayoutInflater layoutInflater;
    private ListItem listItemListener;
    private String selectedRegionId;
    public RegionGridAdapter(Context context, List regions) {
        this.context = context;
        this.regions = regions;
        layoutInflater=LayoutInflater.from(context);
    }


    @Override
    public int getCount() {
        return regions.size();
    }

    @Override
    public Object getItem(int position) {
        return regions.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Object object=regions.get(position);
        if(object instanceof LoadingItem){
            convertView=layoutInflater.inflate(R.layout.shimmer_region,parent,false);
            ((ShimmerFrameLayout)convertView.findViewById(R.id.shimmerRegion)).startShimmer();
        }else{
            convertView=layoutInflater.inflate(R.layout.item_region,parent,false);
            RegionEntity regionEntity =(RegionEntity)getItem(position);
            TextView name=convertView.findViewById(R.id.regionItemTitle);
            TextView language=convertView.findViewById(R.id.regionItemLanguage);
            name.setText(regionEntity.getName());
            language.setText(regionEntity.getLanguage());
            if(regionEntity.getId().equals(selectedRegionId)){
                convertView.setBackgroundColor(ContextCompat.getColor(context,R.color.cardPrimarySelected));
                name.setTextColor(ContextCompat.getColor(context,R.color.onCardPrimarySelected));
                language.setTextColor(ContextCompat.getColor(context,R.color.onCardPrimarySelected));
            }else{
                convertView.setBackgroundColor(ContextCompat.getColor(context,R.color.cardPrimary));
                name.setTextColor(ContextCompat.getColor(context,R.color.onCardPrimary));
                language.setTextColor(ContextCompat.getColor(context,R.color.onCardPrimary));
            }
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listItemListener!=null){
                        listItemListener.onClick(regionEntity);
                    }
                }
            });
        }

        return convertView;
    }

    public void setListItemListener(ListItem listItemListener) {
        this.listItemListener = listItemListener;
    }

    public void setSelectedRegionId(String id){
        if(id==null || !id.equals(selectedRegionId)){
            this.selectedRegionId=id;
            notifyDataSetChanged();
        }

    }

    public interface ListItem{
        void onClick(RegionEntity regionEntity);
    }

    public static class LoadingItem{

    }
}
