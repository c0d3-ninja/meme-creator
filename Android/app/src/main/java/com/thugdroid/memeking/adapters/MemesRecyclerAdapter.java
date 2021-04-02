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
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.room.entity.MemesDataEntity;
import com.thugdroid.memeking.utils.AppUtils;
import com.thugdroid.memeking.utils.NumberUtils;

import java.util.List;

public class MemesRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_LOADING=1;
    private static final int VIEW_TYPE_MEMES=2;
    private Context context;
    private List memesFeedDataEntities;
    private ScrollListener scrollListener;
    private final int scrollThreshold=1;
    String loggedInUserId;
    private ListItemClickListener listItemClickListener;
    public MemesRecyclerAdapter(Context context,String userId, List memesFeedDataEntities,RecyclerView recyclerView) {
        this.context = context;
        this.loggedInUserId=userId;
        this.memesFeedDataEntities = memesFeedDataEntities;
        GridLayoutManager gridLayoutManager=(GridLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int totalItemCount = gridLayoutManager.getItemCount();
                int lastVisibleItemPos = gridLayoutManager.findLastVisibleItemPosition();
                if(lastVisibleItemPos+scrollThreshold==totalItemCount &&
                        scrollListener!=null && memesFeedDataEntities.size()!=0){
                    scrollListener.onScrollEnd();
                }
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if(memesFeedDataEntities.get(position)instanceof MemesDataEntity){
            return VIEW_TYPE_MEMES;
        }else{
            return VIEW_TYPE_LOADING;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        if(viewType==VIEW_TYPE_MEMES){
            View view = LayoutInflater.from(context).inflate(R.layout.item_meme,parent,false);
            viewHolder = new MemeViewHolder(view);
        }else{
            View view = LayoutInflater.from(context).inflate(R.layout.shimmer_memes,parent,false);
            viewHolder = new LoadingViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof MemeViewHolder){
            MemeViewHolder memeViewHolder = (MemeViewHolder) holder;
            MemesDataEntity memesDataEntity = getMemesFeedData(position);
            if(memesDataEntity.getInstaUsername()==null){
                memeViewHolder.highlightView1.setVisibility(View.VISIBLE);
                memeViewHolder.memeItemInstaUsernameParent.setVisibility(View.GONE);
                int colorForPosition =AppUtils.getColorForPosition(context,position);
                memeViewHolder.highlightView1.setBackgroundColor(colorForPosition);
            }else{
                memeViewHolder.highlightView1.setVisibility(View.GONE);
                memeViewHolder.memeItemInstaUsernameParent.setVisibility(View.VISIBLE);
                memeViewHolder.instaUsernameTv.setText(AppUtils.getInstaDisplayUsername(memesDataEntity.getInstaUsername()));
                if(listItemClickListener!=null){
                    memeViewHolder.memeItemInstaUsernameTvParent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            listItemClickListener.onIUsernameClick(memesDataEntity.getInstaUsername());
                        }
                    });
                }
            }

            if(memesDataEntity.getDownloads()==0){
                memeViewHolder.downloadsTv.setVisibility(View.GONE);
            }else{
                memeViewHolder.downloadsTv.setVisibility(View.VISIBLE);
                memeViewHolder.downloadsTv.setText(NumberUtils.getCountString(memesDataEntity.getDownloads()));
            }
            if(memesDataEntity.getShares()==0){
                memeViewHolder.sharesTv.setVisibility(View.GONE);
            }else{
                memeViewHolder.sharesTv.setVisibility(View.VISIBLE);
                memeViewHolder.sharesTv.setText(NumberUtils.getCountString(memesDataEntity.getShares()));
            }
            if(isOwnPost(memesDataEntity.getCreatedBy())){
                memeViewHolder.reportIcon.setImageResource(R.drawable.ic_delete_red_24dp);
            }else {
                memeViewHolder.reportIcon.setImageResource(R.drawable.ic_report_red_24dp);
            }
            if(listItemClickListener!=null){
                memeViewHolder.downloadBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                     listItemClickListener.onDownloadClick(position,memesDataEntity);
                    }
                });
                memeViewHolder.shareBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listItemClickListener.onShareClick(position,memesDataEntity);
                    }
                });
                memeViewHolder.reportBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(isOwnPost(memesDataEntity.getCreatedBy())){
                            listItemClickListener.onDeleteClick(position,memesDataEntity);
                        }else{
                            listItemClickListener.onReportClick(position,memesDataEntity);
                        }
                    }
                });
            }
            memeViewHolder.memeItemLoading.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(memesDataEntity.getImageUrl())
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            memeViewHolder.memeItemLoading.setVisibility(View.GONE);
                            return false;
                        }
                    })
                    .into(memeViewHolder.memeImage);
        }else{
            ((LoadingViewHolder)holder).shimmerFrameLayout.startShimmer();
        }
    }

    @Override
    public int getItemCount() {
        return memesFeedDataEntities.size();
    }

    private boolean isOwnPost(String createdBy){
        return loggedInUserId.equals(createdBy);
    }

    public void setListItemClickListener(ListItemClickListener listItemClickListener) {
        this.listItemClickListener = listItemClickListener;
    }

    public void setScrollListener(ScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    class MemeViewHolder extends RecyclerView.ViewHolder{
        ImageView memeImage;
        View highlightView1;
        ImageView reportIcon;
        TextView downloadsTv,sharesTv,instaUsernameTv;
        View downloadBtn,shareBtn,reportBtn;
        View memeItemLoading,memeItemInstaUsernameParent,memeItemInstaUsernameTvParent;
        public MemeViewHolder(@NonNull View itemView) {
            super(itemView);
            memeImage=itemView.findViewById(R.id.itemMemeImage);
            highlightView1 =itemView.findViewById(R.id.itemMemeHighlightView1);
            reportIcon=itemView.findViewById(R.id.memeItemReportImageView);
            downloadsTv=itemView.findViewById(R.id.memeItemDownloadCountTv);
            sharesTv=itemView.findViewById(R.id.memeItemShareCountTv);
            downloadBtn=itemView.findViewById(R.id.memeItemDownloadMemeBtn);
            shareBtn=itemView.findViewById(R.id.memeItemShareMemeBtn);
            reportBtn=itemView.findViewById(R.id.memeItemReportMemeBtn);
            memeItemLoading=itemView.findViewById(R.id.memeItemLoading);
            memeItemInstaUsernameParent=itemView.findViewById(R.id.memeItemInstaUsernameParent);
            memeItemInstaUsernameTvParent=itemView.findViewById(R.id.memeItemInstaUsernameTvParent);
            instaUsernameTv=itemView.findViewById(R.id.memeItemInstaUsernameTv);

        }
    }
    class LoadingViewHolder extends RecyclerView.ViewHolder{
        ShimmerFrameLayout shimmerFrameLayout;
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            shimmerFrameLayout=itemView.findViewById(R.id.shimmerMemes);
        }
    }
    private MemesDataEntity getMemesFeedData(int position){
        return (MemesDataEntity) (memesFeedDataEntities.get(position));
    }

    public static class LoadingItem{

    }

    public interface ScrollListener{
        void onScrollEnd();
    }

    public interface ListItemClickListener{
        void onDownloadClick(int position,MemesDataEntity memesDataEntity);
        void onShareClick(int position,MemesDataEntity memesDataEntity);
        void onReportClick(int position,MemesDataEntity memesDataEntity);
        void onDeleteClick(int position,MemesDataEntity memesDataEntity);
        void onIUsernameClick(String username);
    }
}
