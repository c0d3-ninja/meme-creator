package com.thugdroid.memeking.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.room.entity.TemplateEntity;

import java.util.List;

public class TemplatesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int LOADING_VIEWTYPE=0;
    private static final int TEMPLATE_VIEWTYPE=1;
    private Context context;
    private List templateList;
    private RecyclerView recyclerView;
    private LayoutInflater layoutInflater;
    private ListItemClickListener listItemClickListener;
    private final int scrollThreshold=1;
    private ScrollListener scrollListener;
    private boolean hasActionBtns;

    public TemplatesRecyclerAdapter(Context context, List templateList, RecyclerView recyclerView) {
        this.context = context;
        this.templateList = templateList;
        this.recyclerView = recyclerView;
        layoutInflater=LayoutInflater.from(context);
        GridLayoutManager gridLayoutManager=(GridLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = gridLayoutManager.getItemCount();
                int lastVisibleItemPos = gridLayoutManager.findLastVisibleItemPosition();
                if(lastVisibleItemPos+scrollThreshold==totalItemCount &&
                        scrollListener!=null && templateList.size()!=0){
                    scrollListener.onScrollEnd();
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return templateList.get(position) instanceof  TemplateEntity?TEMPLATE_VIEWTYPE:LOADING_VIEWTYPE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if(viewType==TEMPLATE_VIEWTYPE){
            View view=layoutInflater.inflate(R.layout.item_template,parent,false);
            viewHolder=new TemplateViewHolder(view);

        }else {
            View view=layoutInflater.inflate(R.layout.shimmer_template,parent,false);
            viewHolder=new LoadingViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof TemplateViewHolder){
            final TemplateEntity templateEntity=getTemplate(position);
            TemplateViewHolder templateViewHolder = (TemplateViewHolder)holder;
            showTemplateLoading(templateViewHolder);
            hideActionBtns(templateViewHolder);
            Glide.with(context).load(templateEntity.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            hideTemplateLoading(templateViewHolder);
                            showActionBtns(templateViewHolder,templateEntity,position);
                            return false;
                        }
                    })
                    .into(templateViewHolder.imageView);
            if(listItemClickListener!=null){
                templateViewHolder.imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listItemClickListener.onClick(templateEntity,position);
                    }
                });
            }

        }else{
            ((LoadingViewHolder)holder).shimmerFrameLayout.startShimmer();
        }

    }

    @Override
    public int getItemCount() {
        return templateList.size();
    }

    private void showTemplateLoading(TemplateViewHolder templateViewHolder){
        templateViewHolder.itemTemplateLoading.setVisibility(View.VISIBLE);
    }

    private void hideTemplateLoading(TemplateViewHolder templateViewHolder){
        templateViewHolder.itemTemplateLoading.setVisibility(View.GONE);
    }
    private void showActionBtns(TemplateViewHolder templateViewHolder,TemplateEntity templateEntity,int position){
        if(hasActionBtns()){
            templateViewHolder.actionBtnsParent.setVisibility(View.VISIBLE);
            if(templateEntity.isFavorite()){
                templateViewHolder.favBtnIv.setImageResource(R.drawable.ic_star_white_24dp);
            }else{
                templateViewHolder.favBtnIv.setImageResource(R.drawable.ic_star_border_white_24dp);
            }
            if(listItemClickListener!=null){
                templateViewHolder.createMemeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listItemClickListener.onCreateMemeClick(templateEntity);
                    }
                });
                templateViewHolder.favBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listItemClickListener.onFavClick(templateEntity,position);
                    }
                });
            }
        }
    }
    private void hideActionBtns(TemplateViewHolder templateViewHolder){
        templateViewHolder.actionBtnsParent.setVisibility(View.GONE);
    }
    public boolean hasActionBtns() {
        return hasActionBtns;
    }

    public void setHasActionBtns(boolean hasActionBtns) {
        this.hasActionBtns = hasActionBtns;
    }

    public TemplateEntity getTemplate(int position){
        return  (TemplateEntity) templateList.get(position);
    }

    public void setListItemClickListener(ListItemClickListener listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
    }

    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    class TemplateViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        View itemTemplateLoading;
        View actionBtnsParent;
        View createMemeBtn,favBtn;
        ImageView favBtnIv;
        public TemplateViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.templateIv);
            itemTemplateLoading=itemView.findViewById(R.id.itemTemplateLoading);
            actionBtnsParent=itemView.findViewById(R.id.templateActionBtnsParent);
            createMemeBtn=itemView.findViewById(R.id.itemTemplateCreateMemeParent);
            favBtn=itemView.findViewById(R.id.itemTemplateFavParent);
            favBtnIv=itemView.findViewById(R.id.itemTemplateFavBtnIv);

        }
    }
    class LoadingViewHolder extends RecyclerView.ViewHolder{
        ShimmerFrameLayout shimmerFrameLayout;
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            shimmerFrameLayout=itemView.findViewById(R.id.shimmerTemplates);
        }
    }

    public static class LoadingItem{

    }

    public interface ListItemClickListener{
        void onClick(TemplateEntity templateEntity,int position);
        void onCreateMemeClick(TemplateEntity templateEntity);
        void onFavClick(TemplateEntity templateEntity,int position);

    }

    public interface ScrollListener{
        void onScrollEnd();
    }
}
