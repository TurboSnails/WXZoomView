package com.liyi.example.activity;

import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.widget.ImageView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.liyi.example.R;
import com.liyi.example.Utils;
import com.liyi.example.adapter.RecyclerAdp;
import com.liyi.example.glide.GlideUtil;
import com.liyi.viewer.ImageLoader;
import com.liyi.viewer.ImageViewerUtil;
import com.liyi.viewer.ViewData;
import com.liyi.viewer.listener.OnPreviewStatusListener;
import com.liyi.viewer.widget.ScaleImageView;
import com.liyi.viewer.widget.ImageViewer;

/**
 * 竖向列表页面
 */
public class VerticalListAty extends BaseActivity {
    private ImageViewer imagePreview;
    private RecyclerView recyclerView;
    private LinearLayoutManager mLinearManager;
    private RecyclerAdp mAdapter;

    private Point mScreenSize;

    @Override
    int onBindLayoutResID() {
        return R.layout.aty_vertical_list;
    }

    @Override
    void onInit(Bundle savedInstanceState) {
        initView();
        addListener();
    }

    private void initView() {
        imagePreview = findViewById(R.id.imagePreview);
        recyclerView = findViewById(R.id.recyclerview);

        mLinearManager = new LinearLayoutManager(this);
        mLinearManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(mLinearManager);

        mAdapter = new RecyclerAdp(1);
        mImageList = Utils.getImageList();
        mAdapter.setData(mImageList);
        initData();
        imagePreview.setImageData(mImageList);
        imagePreview.setImageLoader(new ImageLoader<String>() {
            @Override
            public void displayImage(final int position, String src, final ImageView imageView) {
                GlideUtil.loadImage(VerticalListAty.this, src, new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        imageView.setImageDrawable(resource);
                        mViewList.get(position).setImageWidth(resource.getIntrinsicWidth());
                        mViewList.get(position).setImageHeight(resource.getIntrinsicHeight());
                    }
                });
            }
        });
    }

    private void initData() {
        mScreenSize = ImageViewerUtil.getScreenSize(this);
        for (int i = 0, len = mViewList.size(); i < len; i++) {
            ViewData viewData = new ViewData();
            viewData.setTargetX(ImageViewerUtil.dp2px(this, 10));
            viewData.setTargetWidth(mScreenSize.x - ImageViewerUtil.dp2px(this, 20));
            viewData.setTargetHeight(ImageViewerUtil.dp2px(this, 200));
            mViewList.set(i, viewData);
        }
    }

    private void addListener() {
        mAdapter.setOnItemClickCallback(new RecyclerAdp.OnItemClickCallback() {
            @Override
            public void onItemClick(int position, ImageView view) {
                int[] location = new int[2];
                // 获取在整个屏幕内的绝对坐标
                view.getLocationOnScreen(location);
                ViewData viewData = mViewList.get(position);
                // 去掉状态栏的高度
                viewData.setTargetY(location[1]);
                mViewList.set(position, viewData);

                imagePreview.setStartPosition(position);
                imagePreview.setViewData(mViewList);
                imagePreview.watch();
            }
        });
        recyclerView.setAdapter(mAdapter);
        mLinearManager.scrollToPositionWithOffset(0, 0);

        imagePreview.setOnPreviewStatusListener(new OnPreviewStatusListener() {
            @Override
            public void onPreviewStatus(int state, ScaleImageView imagePager) {
                if (state == com.liyi.viewer.ImageViewerState.STATE_READY_CLOSE) {
                    int top = getTop(imagePreview.getCurrentPosition());
                    ViewData viewData = mViewList.get(imagePreview.getCurrentPosition());
                    viewData.setTargetY(top);
                    mViewList.set(imagePreview.getCurrentPosition(), viewData);
                    imagePreview.setViewData(mViewList);
                    mLinearManager.scrollToPositionWithOffset(imagePreview.getCurrentPosition(), top);
                }
            }
        });
    }

    private int getTop(int position) {
        int top = 0;
        // 当前图片的高度
        float imgH = Float.valueOf(mViewList.get(position).getTargetHeight());
        // 图片距离 imageViewer 的上下边距
        int dis = (int) ((imagePreview.getHeight() - imgH) / 2);
        // 如果图片高度大于等于 imageViewer 的高度
        if (dis <= 0) {
            return top + dis;
        } else {
            float th1 = 0;
            float th2 = 0;
            // 计算当前图片上方所有 Item 的总高度
            for (int i = 0; i < position; i++) {
                // ImageViewerUtil.dp2px(this, 210) 是 Item 的高度
                th1 += ImageViewerUtil.dp2px(this, 210);
            }
            // 计算当前图片下方所有 Item 的总高度
            for (int i = position + 1; i < mImageList.size(); i++) {
                // ImageViewerUtil.dp2px(this, 210) 是 Item 的高度
                th2 += ImageViewerUtil.dp2px(this, 210);
            }
            if (th1 >= dis && th2 >= dis) {
                return top + dis;
            } else if (th1 < dis) {
                return (int) (top + th1);
            } else if (th2 < dis) {
                return (int) (recyclerView.getHeight() - imgH);
            }
        }
        return 0;
    }

    /**
     * 监听返回键
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean b = imagePreview.onKeyDown(keyCode, event);
        if (b) {
            return b;
        }
        return super.onKeyDown(keyCode, event);
    }
}
