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
import com.thugdroid.memeking.model.MenuModel;

import java.util.ArrayList;
import java.util.List;

public class MenuItemsFragment extends CustomFragment {

    private List<MenuModel> menuModels;
    private ViewHolder viewHolder;
    private ListItemClickListener listItemClickListener;

    public MenuItemsFragment(){

    }

    public static MenuItemsFragment newInstance(List<MenuModel> menuModels,ListItemClickListener listItemClickListener){
        MenuItemsFragment menuItemsFragment = new MenuItemsFragment();
        menuItemsFragment.setMenuModels(menuModels);
        menuItemsFragment.setListItemClickListener(listItemClickListener);
        return  menuItemsFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menuitems,container,false);
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

    public void setMenuModels(List<MenuModel> menuModels) {
        getMenuModels().clear();
        getMenuModels().addAll(menuModels);
    }

    public List<MenuModel> getMenuModels() {
        if(this.menuModels==null){
            this.menuModels=new ArrayList<>();
        }
        return this.menuModels;
    }

    public void setListItemClickListener(ListItemClickListener listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
    }

    private void renderItems(){
        viewHolder.parentContainer.removeAllViews();
        for(int i = 0; i< getMenuModels().size(); i++){
            MenuModel menuModel = getMenuModels().get(i);
            View convertView=getLayoutInflater().inflate(R.layout.item_menu,viewHolder.parentContainer,false);
            ImageView logo=convertView.findViewById(R.id.navDrawerItemLogo);
            TextView logoPlaceHolder=convertView.findViewById(R.id.navDrawerLogoPlaceholder);
            TextView title=convertView.findViewById(R.id.navDrawerItemTitle);
            if(menuModel.getLogoResId()!=0){
                logoPlaceHolder.setVisibility(View.GONE);
                logo.setVisibility(View.VISIBLE);
                logo.setImageResource(menuModel.getLogoResId());
            }
            title.setText(getString(menuModel.getTitleResId()));
            if(listItemClickListener!=null){
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listItemClickListener.onListItemClick(menuModel.getId());
                    }
                });
            }
            viewHolder.parentContainer.addView(convertView);
        }
    }

    private class ViewHolder{
        LinearLayout parentContainer;
        public ViewHolder() {
            parentContainer=findViewById(R.id.menuItemsFragmentContainer);
        }
    }

    public interface ListItemClickListener{
        void onListItemClick(int id);
    }
}
