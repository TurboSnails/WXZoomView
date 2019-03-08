package com.liyi.example.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.liyi.example.glide.GlideUtil;
import com.liyi.example.R;

import java.util.List;


public class RecyclerAdp extends RecyclerView.Adapter {
    // 0: 水平方向  1: 垂直方向
    private int mOrientation;
    private List<String> mImgList;
    private OnItemClickCallback mCallback;

    public RecyclerAdp(int orientation) {
        mOrientation = orientation;
    }

    public void setData(List<String> list) {
        this.mImgList = list;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (mOrientation == 0) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_horizontal, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vertical, parent, false);
        }
        return new ItemHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ItemHolder itemHolder = (ItemHolder) holder;
        GlideUtil.loadImage(itemHolder.iv_pic.getContext(),mImgList.get(position),itemHolder.iv_pic);
        itemHolder.iv_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.onItemClick(position, itemHolder.iv_pic);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mImgList != null ? mImgList.size() : 0;
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        private ImageView iv_pic;

        public ItemHolder(View itemView) {
            super(itemView);
            iv_pic = itemView.findViewById(R.id.iv_pic);
        }
    }

    public void setOnItemClickCallback(OnItemClickCallback clickCallback) {
        this.mCallback = clickCallback;
    }

    public interface OnItemClickCallback {
        void onItemClick(int position, ImageView view);
    }
}
