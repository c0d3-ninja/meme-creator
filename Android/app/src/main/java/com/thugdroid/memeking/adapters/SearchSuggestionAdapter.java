package com.thugdroid.memeking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.thugdroid.memeking.R;
import com.thugdroid.memeking.model.FacetModel;
import com.thugdroid.memeking.utils.AppUtils;

import java.util.List;

public class SearchSuggestionAdapter extends BaseAdapter {
    private Context context;
    private List<FacetModel> searchSuggestionList;
    private LayoutInflater layoutInflater;
    private ListItemListener listItemListener;
    private boolean advancedSearchEnabled;
    public SearchSuggestionAdapter(Context context, List<FacetModel> searchSuggestionList,boolean advancedSearchEnabled) {
        this.context = context;
        this.searchSuggestionList = searchSuggestionList;
        this.advancedSearchEnabled=advancedSearchEnabled;
        layoutInflater=LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return searchSuggestionList.size();
    }

    @Override
    public Object getItem(int position) {
        return searchSuggestionList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    private FacetModel getCurrentItem(int position){
        return searchSuggestionList.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView=layoutInflater.inflate(R.layout.item_searchsuggestion,parent,false);
        FacetModel currentItem = getCurrentItem(position);
        boolean isAdvanceSearchEle = isAdvanceSearchElement(position);
        AppUtils.setHTML((convertView.findViewById(R.id.searchItemTitle)),
                getHighlightedText(currentItem.getHighlighted(),(!isAdvanceSearchEle?R.color.colorPrimary:R.color.roundedRectangle3)));
        if(listItemListener!=null){
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listItemListener.onItemClick(currentItem.getValue());
                }
            });
            (convertView.findViewById(R.id.searchSuggestionPreview)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listItemListener.onSearchPreviewClick(currentItem.getValue());
                }
            });

        }
        if(isAdvanceSearchEle){
            (convertView.findViewById(R.id.searchSuggestionPreview)).setVisibility(View.GONE);
            (convertView.findViewById(R.id.searchSuggestionSearchIcon)).setVisibility(View.GONE);
        }
        return convertView;
    }

    public void setListItemListener(ListItemListener listItemListener) {
        this.listItemListener = listItemListener;
    }

    private String removeAlpha(String colorString){
        colorString=colorString.substring(3);
        return "#"+colorString;
    }

    public String getHighlightedText(String str,int color){
        return str.replaceAll("<em>","<strong><font color='"+removeAlpha(AppUtils.getColorString(context,color).toLowerCase())+"'>").replaceAll("</em>","</font></strong>");
    }

    public boolean isAdvanceSearchElement(int position){
        return (isAdvancedSearchEnabled() && position==searchSuggestionList.size()-1);
    }

    public boolean isAdvancedSearchEnabled() {
        return advancedSearchEnabled;
    }

    public interface ListItemListener{
        void onItemClick(String searchStr);
        void onSearchPreviewClick(String searchStr);
    }
}
