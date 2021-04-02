package com.thugdroid.memeking.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.thugdroid.memeking.R;
import com.thugdroid.memeking.model.UploadTemplatePreview;

import java.util.ArrayList;

public class UploadTemplatePreviewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context context;
    private ArrayList<UploadTemplatePreview> list;
    private LayoutInflater layoutInflater;
    private RequestManager glide;

    public UploadTemplatePreviewAdapter(Context context, ArrayList<UploadTemplatePreview> list) {
        this.context = context;
        this.list = list;
        layoutInflater=LayoutInflater.from(context);
        glide= Glide.with(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=layoutInflater.inflate(R.layout.item_uploadtemplatepreview,parent,false);
        ViewHolder viewHolder=new ViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        glide.load(getItem(position).getImgUrl()).into(((ViewHolder)holder).imageView);
    }

    public UploadTemplatePreview getItem(int position){
        return list.get(position);
    }
    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        ImageView imageView;
        Button deleteBtn;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView=itemView.findViewById(R.id.uploadTemplatePreviewImage);
            deleteBtn=itemView.findViewById(R.id.uploadTemplatePreviewDelete);
        }
    }
}
