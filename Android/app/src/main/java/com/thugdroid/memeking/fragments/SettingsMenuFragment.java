package com.thugdroid.memeking.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.thugdroid.memeking.CustomFragment;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.model.SettingsMenuItemModel;
import com.thugdroid.memeking.model.SettingsMenuModel;
import com.thugdroid.memeking.ui.data.SettingsMenuItemsData;

import org.w3c.dom.Text;

import java.util.List;

public class SettingsMenuFragment extends CustomFragment {
    private List<SettingsMenuModel> settingsMenuModels;
    private ViewHolder viewHolder;
    private ListItemClickListener listItemClickListener;

    public SettingsMenuFragment(){

    }

    public static SettingsMenuFragment newInstance(List<SettingsMenuModel>  settingsMenuModels, ListItemClickListener listItemClickListener){
        SettingsMenuFragment settingsMenuFragment=new SettingsMenuFragment();
        settingsMenuFragment.setSettingsMenuModels(settingsMenuModels);
        settingsMenuFragment.setListItemClickListener(listItemClickListener);
        return settingsMenuFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settingsmenu_list,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initVariables();
        initViews(view);
        renderItems();
    }



    @Override
    public void initVariables() {

    }

    @Override
    public void initViews(View view) {
        setRootView(view);
        viewHolder=new ViewHolder();
    }

    @Override
    public void initListeners() {

    }

    @Override
    public void initObservers() {

    }

    @Override
    public void onClick(View v) {

    }

    public void setSettingsMenuModels(List<SettingsMenuModel> settingsMenuModels) {
        this.settingsMenuModels = settingsMenuModels;
    }

    public void setListItemClickListener(ListItemClickListener listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
    }

    private void renderItems(){
        viewHolder.parentContainer.removeAllViews();
        int settingsMenuModelSize=settingsMenuModels.size();
        for (int i = 0; i < settingsMenuModelSize; i++) {
            SettingsMenuModel settingsMenuModel = settingsMenuModels.get(i);
            View menuItems = getLayoutInflater().inflate(R.layout.fragment_settingsmenu,viewHolder.parentContainer,false);
            TextView menuItemsTitle = menuItems.findViewById(R.id.settingsMenuItemsTitle);
            menuItemsTitle.setText(getString(settingsMenuModel.getTitleResId()));
            LinearLayout menuItemsParent = menuItems.findViewById(R.id.settingsMenuItemsContainer);
            for (SettingsMenuItemModel menuItemModel : settingsMenuModel.getSettingsMenuItemModels()) {
                View menuItemView = getLayoutInflater().inflate(R.layout.item_settingsmenu,menuItemsParent,false);
                TextView itemTitle = menuItemView.findViewById(R.id.settingsMenuItemTitle);
                TextView itemDesc=menuItemView.findViewById(R.id.settingsMenuItemDesc);
                ImageView itemLogo = menuItemView.findViewById(R.id.settingsMenuItemLogo);
                itemDesc.setText(getString(menuItemModel.getDescResId()));

                int titleResId=menuItemModel.getTitleResId();
                if(titleResId!=0){
                    itemTitle.setText(getString(titleResId));
                }else{
                    itemTitle.setTag(getViewTag(R.id.settingsMenuItemTitle,menuItemModel.getId()));
                }
                int logoResId = menuItemModel.getLogoResId();
                if(logoResId!=0){
                    itemLogo.setImageResource(logoResId);
                }else{
                    itemLogo.setTag(getViewTag(R.id.settingsMenuItemLogo,menuItemModel.getId()));
                }
                if(listItemClickListener!=null){
                    menuItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            listItemClickListener.onListItemClick(menuItemModel.getId());
                        }
                    });
                }
                menuItemsParent.addView(menuItemView);
            }
            if(i!=settingsMenuModelSize-1){
                View dividerView = getLayoutInflater().inflate(R.layout.component_divider_static_height,menuItemsParent,false);
                menuItemsParent.addView(dividerView);
            }
            viewHolder.parentContainer.addView(menuItems);
        }
    }

    private class ViewHolder{
        LinearLayout parentContainer;
        public ViewHolder() {
            parentContainer=findViewById(R.id.settingsMenuListParentContainer);
        }
    }

    public interface ListItemClickListener{
        void onListItemClick(int id);
    }

    public static String getViewTag(int viewId,int viewManualId){
        return (viewId+"_"+viewManualId);
    }
}
