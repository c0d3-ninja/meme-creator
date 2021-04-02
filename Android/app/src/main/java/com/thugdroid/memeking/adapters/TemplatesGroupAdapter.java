package com.thugdroid.memeking.adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.room.entity.TemplatesGroupDataEntity;

import java.util.List;

public class TemplatesGroupAdapter  extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_LOADING=1;
    private static final int VIEW_TYPE_MOVIES =2;
    private Context context;
    private List FeedDataEntities;
    private ScrollListener scrollListener;
    private final int scrollThreshold=1;
    private ListItemClickListener listItemClickListener;

    public TemplatesGroupAdapter(Context context, List FeedDataEntities, RecyclerView recyclerView) {
        this.context = context;
        this.FeedDataEntities = FeedDataEntities;
        GridLayoutManager gridLayoutManager=(GridLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = gridLayoutManager.getItemCount();
                int lastVisibleItemPos = gridLayoutManager.findLastVisibleItemPosition();
                if(lastVisibleItemPos+scrollThreshold==totalItemCount &&
                        scrollListener!=null && FeedDataEntities.size()!=0){
                    scrollListener.onScrollEnd();
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if(FeedDataEntities.get(position)instanceof TemplatesGroupDataEntity){
            return VIEW_TYPE_MOVIES;
        }else{
            return VIEW_TYPE_LOADING;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if(viewType== VIEW_TYPE_MOVIES){
            View view = LayoutInflater.from(context).inflate(R.layout.item_templategroup,parent,false);
            viewHolder = new MovieViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.shimmer_templategroup,parent,false);
            viewHolder = new LoadingViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MovieViewHolder){
            MovieViewHolder movieViewHolder = (MovieViewHolder) holder;
            TemplatesGroupDataEntity TemplatesGroupDataEntity = getMemesFeedData(position);
            movieViewHolder.title.setText(TemplatesGroupDataEntity.getName());
            Glide.with(context)
                    .load(TemplatesGroupDataEntity.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(movieViewHolder.memeImage);
            if(listItemClickListener!=null){
                movieViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listItemClickListener.onClick(TemplatesGroupDataEntity);
                    }
                });
            }
        }else{
            ((LoadingViewHolder)holder).shimmerFrameLayout.startShimmer();
        }
    }

    @Override
    public int getItemCount() {
        return FeedDataEntities.size();
    }

    private TemplatesGroupDataEntity getMemesFeedData(int position){
        return (TemplatesGroupDataEntity) (FeedDataEntities.get(position));
    }

    public void setListItemClickListener(ListItemClickListener listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
    }

    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    private class MovieViewHolder extends RecyclerView.ViewHolder{
        ImageView memeImage;
        TextView title;
        public MovieViewHolder(@NonNull View itemView) {
            super(itemView);
            memeImage=itemView.findViewById(R.id.templateGroupImage);
            title=itemView.findViewById(R.id.templateGroupTitle);
        }
    }
    private class LoadingViewHolder extends RecyclerView.ViewHolder{
        ShimmerFrameLayout shimmerFrameLayout;
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            shimmerFrameLayout=itemView.findViewById(R.id.shimmerTemplateGroup);
        }
    }

    public interface ScrollListener{
        void onScrollEnd();
    }
    public interface ListItemClickListener{
        void onClick(TemplatesGroupDataEntity TemplatesGroupDataEntity);
    }

    public static class LoadingItem{

    }
}