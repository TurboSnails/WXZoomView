package com.liyi.example.activity;

import android.app.SharedElementCallback;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.liyi.example.R;
import com.liyi.example.Utils;
import com.liyi.example.glide.GlideUtil;
import com.liyi.viewer.ImageLoader;
import com.liyi.viewer.ImageViewerState;
import com.liyi.viewer.ViewData;
import com.liyi.viewer.dragger.ImageDraggerType;
import com.liyi.viewer.listener.OnItemClickListener;
import com.liyi.viewer.listener.OnPreviewStatusListener;
import com.liyi.viewer.widget.ImageViewer;
import com.liyi.viewer.widget.ScaleImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VPActivity extends FragmentActivity {

    ImageViewer imagePreview;

    private int index = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_vp);
        imagePreview = findViewById(R.id.vp_iver);
        index = getIntent().getIntExtra("index", 0);
        prepareData();
        initView();
        initTransition();
    }

    protected List<String> mImageList = new ArrayList<>();
    protected List<ViewData> mViewList = new ArrayList<>();

    private void prepareData() {
        mImageList = Utils.getImageList();
        for (int i = 0, len = mImageList.size(); i < len; i++) {
            ViewData viewData = new ViewData();
            mViewList.add(viewData);
        }
    }

    private void initTransition() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setEnterSharedElementCallback(new SharedElementCallback() {
                @Override
                public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                    String url = mImageList.get(imagePreview.getCurrentPosition());
                    sharedElements.clear();
                    // 动态设置 key
                    View sharedView = ((ScaleImageView) imagePreview.getCurrentView()).getImageView();
                    sharedElements.put(url, sharedView);
                }
            });
        }
    }

    private void initView() {

        supportPostponeEnterTransition();
        imagePreview.doDrag(true);
        imagePreview.setDragType(ImageDraggerType.DRAG_TYPE_WX);
        imagePreview.setImageData(mImageList);
        imagePreview.setImageLoader(new ImageLoader<String>() {

            @Override
            public void displayImage(final int position, String src, final ImageView imageView) {
                final ScaleImageView scaleImageView = (ScaleImageView) imageView.getParent();
                GlideUtil.loadImage(VPActivity.this, src, new SimpleTarget<Drawable>() {

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        scaleImageView.showProgess();
                        imageView.setImageDrawable(placeholder);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        scaleImageView.removeProgressView();
                        imageView.setImageDrawable(errorDrawable);
                    }

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        scaleImageView.removeProgressView();
                        imageView.setImageDrawable(resource);
                    }
                });
            }
        });
        imagePreview.setStartPosition(index);
        imagePreview.setViewData(mViewList);
        imagePreview.watch();
        imagePreview.postOnAnimation(new Runnable() {
            @Override
            public void run() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ((ScaleImageView) imagePreview.getCurrentView()).getImageView().setTransitionName(getString(R.string.Test));
                    startPostponedEnterTransition();
                }
            }
        });

        imagePreview.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public boolean onItemClick(int position, View view) {
                Intent intent = new Intent().putExtra("index", position).putExtra("url", mImageList.get(position));
                setResult(RESULT_OK, intent);
                supportFinishAfterTransition();
                return false;
            }
        });

        // 设置图片预览器的状态监听
        imagePreview.setOnPreviewStatusListener(new OnPreviewStatusListener() {
            @Override
            public void onPreviewStatus(int state, ScaleImageView imagePager) {
                if (state == ImageViewerState.STATE_COMPLETE_OPEN) {

                } else if (state == ImageViewerState.STATE_COMPLETE_CLOSE) {
                    int index = imagePreview.getCurrentPosition();
                    Intent intent = new Intent().putExtra("index", index).putExtra("url", mImageList.get(index));
                    setResult(RESULT_OK, intent);
                    supportFinishAfterTransition();
                }
            }
        });
    }


}
