package com.liyi.viewer.dragger;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.FloatEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.liyi.viewer.ImageViewerState;
import com.liyi.viewer.ViewData;
import com.liyi.viewer.widget.ScaleImageView;
import com.nineoldandroids.view.ViewHelper;

public class WxImageDragger extends ImageDragger {
    // 恢复原样的动画时间
    protected final int BACK_ANIM_DURATION = 200;
    // 退出预览的动画时间
    private final int EXIT_ANIM_DURATION = 280;
    // 默认的最小缩放比例
    private final float MIN_SCALE_WEIGHT = 0.25f;

    // imageView 的当前缩放比例
    private float mCurScale;
    private float mCurImgX, mCurImgY;
    private float mAdjustScale;
    private float mAdjustImgWidth, mAdjustImgHeight;
    // 图片的原始宽高
    private float mOriImg_width = 0, mOriImg_height = 0;
    private FrameLayout.LayoutParams mImageParams;

    public WxImageDragger() {

    }

    @Override
    public void bindScaleImageView(ScaleImageView scaleImageView) {
        super.bindScaleImageView(scaleImageView);
        final ViewData viewData = scaleImageView.getViewData();
        final ImageView imageView = scaleImageView.getImageView();
        mImageParams = (FrameLayout.LayoutParams) imageView.getLayoutParams();
        Drawable drawable = imageView.getDrawable();
        if (drawable != null) {
            mOriImg_width = drawable.getIntrinsicWidth();
            mOriImg_height = drawable.getIntrinsicHeight();
        } else if (viewData.getImageWidth() != 0 && viewData.getImageHeight() != 0) {
            mOriImg_width = viewData.getImageWidth();
            mOriImg_height = viewData.getImageHeight();
        }
    }

    @Override
    public void onReady(float width, float height) {
        super.onReady(width, height);
        mAdjustScale = Math.min(mPreviewWidth / mOriImg_width, mPreviewHeight / mOriImg_height);
    }

    @Override
    public void onDragging(float x1, float y1, float x2, float y2) {
        super.onDragging(x1, y1, x2, y2);
        View imageView = scaleImageView.getImageView();
        // 计算 view 的坐标
        final float diffX = x2 - x1;
        final float diffY = y2 - y1;

        float scale = 1f;
        if (diffY > 0) {
            scale = 1 - Math.abs(diffY) / mPreviewHeight;
        }

        ViewHelper.setTranslationX(imageView, diffX);
        ViewHelper.setTranslationY(imageView, diffY);
        setupScale(scale, imageView);
        if (diffY <= 0) {// 向上
            mBackgroundAlpha = DEF_BACKGROUND_ALPHA;
        } else {
            mBackgroundAlpha = (1 - Math.abs(diffY) / (mPreviewHeight)) * DEF_BACKGROUND_ALPHA;
        }
        setBackgroundAlpha((int) mBackgroundAlpha);
    }

    private void setupScale(float scale, View view) {
        scale = Math.min(Math.max(scale, MIN_SCALE_WEIGHT), 1);
        ViewHelper.setScaleX(view, scale);
        ViewHelper.setScaleY(view, scale);
    }

    @Override
    public void onRelease(MotionEvent event, float downX, float downY) {
        super.onRelease(event, downX, downY);
        if (!scaleImageView.isImageAnimRunning()) {
            View imageView = scaleImageView.getImageView();
            final float viewX = imageView.getX();
            final float viewY = imageView.getY();
            mAdjustImgWidth = mOriImg_width * mAdjustScale;
            mAdjustImgHeight = mOriImg_height * mAdjustScale;
            // 图片在预览界面中的当前坐标
            mCurImgX = viewX + (mPreviewWidth - mAdjustImgWidth) / 2 * mCurScale;
            mCurImgY = viewY + (mPreviewHeight - mAdjustImgHeight) / 2 * mCurScale;
            if (viewY <= mMaxDisOnY) {
                setupReback(event.getRawX(), event.getRawY(), downX, downY);
            } else {
                exit();
            }
        }
    }

    private void setupReback(final float mUpX, final float mUpY, final float mDownX, final float mDownY) {
        setImageDraggerState(ImageDraggerState.DRAG_STATE_BEGIN_REBACK);
        setPreviewStatus(ImageViewerState.STATE_READY_REBACK, scaleImageView);
        if (mUpY != mDownY) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(mUpY, mDownY);
            valueAnimator.setDuration(BACK_ANIM_DURATION);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float mY = (float) animation.getAnimatedValue();
                    float percent = (mY - mDownY) / (mUpY - mDownY);
                    float mX = percent * (mUpX - mDownX) + mDownX;
                    setupMoving(mX, mY, mDownX, mDownY);
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mBackgroundAlpha = DEF_BACKGROUND_ALPHA;
                    setImageDraggerState(ImageDraggerState.DRAG_STATE_END_REBACK);
                    if (checkAttacherNotNull()) {
                        mAttacher.setViewPagerScrollable(true);
                        setPreviewStatus(ImageViewerState.STATE_COMPLETE_REBACK, scaleImageView);
                        setPreviewStatus(ImageViewerState.STATE_WATCHING, scaleImageView);
                    }
                }
            });
            valueAnimator.start();
        } else if (mUpX != mDownX) {
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(mUpX, mDownX);
            valueAnimator.setDuration(BACK_ANIM_DURATION);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float mX = (float) animation.getAnimatedValue();
                    float percent = (mX - mDownX) / (mUpX - mDownX);
                    float mY = percent * (mUpY - mDownY) + mDownY;
                    setupMoving(mX, mY, mDownX, mDownY);
                }
            });

            valueAnimator.addListener(new AnimatorListenerAdapter() {

                @Override
                public void onAnimationEnd(Animator animation) {
                    mBackgroundAlpha = DEF_BACKGROUND_ALPHA;
                    setImageDraggerState(ImageDraggerState.DRAG_STATE_END_REBACK);
                    if (checkAttacherNotNull()) {
                        mAttacher.setViewPagerScrollable(true);
                        setPreviewStatus(ImageViewerState.STATE_COMPLETE_REBACK, scaleImageView);
                        setPreviewStatus(ImageViewerState.STATE_WATCHING, scaleImageView);
                    }
                }
            });
            valueAnimator.start();
        }
    }

    private void setupMoving(float movingX, float movingY, float mDownX, float mDownY) {
        ImageView currentShowView = scaleImageView.getImageView();
        if (currentShowView == null)
            return;
        float deltaX = movingX - mDownX;
        float deltaY = movingY - mDownY;
        float scale = 1f;
        ViewHelper.setTranslationX(currentShowView, deltaX);
        ViewHelper.setTranslationY(currentShowView, deltaY);
        setupScale(scale, currentShowView);
        if (deltaY > 0) {
            mBackgroundAlpha = (1 - Math.abs(deltaY) / (mPreviewHeight)) * DEF_BACKGROUND_ALPHA;
        }
        setBackgroundAlpha((int) mBackgroundAlpha);
    }

    /**
     * 退出预览
     */
    private void exit() {
        //  turbo 略修改
        if (scaleImageView.getViewData().getImageWidth() == 0) {
            setPreviewStatus(ImageViewerState.STATE_COMPLETE_CLOSE, scaleImageView);
        } else {
            exitOld();
        }
    }

    private void exitOld() {
        setImageDraggerState(ImageDraggerState.DRAG_STATE_BEGIN_EXIT);
        setPreviewStatus(ImageViewerState.STATE_READY_CLOSE, scaleImageView);
        final ImageView imageView = scaleImageView.getImageView();
        final ViewData viewData = scaleImageView.getViewData();
        final float from_x = mCurImgX;
        final float from_y = mCurImgY;
        final float toX = viewData.getTargetX();
        final float toY = viewData.getTargetY();
        final float old_width = mAdjustImgWidth * mCurScale;
        final float old_height = mAdjustImgHeight * mCurScale;
        final float new_width = viewData.getTargetWidth();
        final float new_height = viewData.getTargetHeight();
        // 是否需要改变 imageView 的尺寸
        final boolean needChangeImageSize;
        if ((mCurImgX + mAdjustImgWidth * mCurScale) <= 0 || mCurImgX >= mPreviewWidth || mCurImgY >= mPreviewHeight) {
            needChangeImageSize = false;
        } else {
            needChangeImageSize = true;
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(EXIT_ANIM_DURATION);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            FloatEvaluator evaluator = new FloatEvaluator();
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) imageView.getLayoutParams();

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                final float progress = (float) animation.getAnimatedValue();
                if (needChangeImageSize) {
                    final float x = evaluator.evaluate(progress, from_x, toX);
                    final float y = evaluator.evaluate(progress, from_y, toY);
                    final float width = evaluator.evaluate(progress, old_width, new_width);
                    final float height = evaluator.evaluate(progress, old_height, new_height);
                    imageView.setX(x);
                    imageView.setY(y);
                    layoutParams.width = (int) width;
                    layoutParams.height = (int) height;
                    imageView.setLayoutParams(layoutParams);
                }
                final float alpha = evaluator.evaluate(progress, mBackgroundAlpha, 0);
                setBackgroundAlpha((int) alpha);
                setImageDraggerState(ImageDraggerState.DRAG_STATE_EXITTING);
                setPreviewStatus(ImageViewerState.STATE_CLOSING, scaleImageView);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (checkAttacherNotNull()) mAttacher.exit();
                mBackgroundAlpha = DEF_BACKGROUND_ALPHA;
                setImageDraggerState(ImageDraggerState.DRAG_STATE_END_EXIT);
                imageView.setX(0);
                imageView.setY(0);
                mImageParams.width = (int) mPreviewWidth;
                mImageParams.height = (int) mPreviewHeight;
                imageView.setLayoutParams(mImageParams);
                setPreviewStatus(ImageViewerState.STATE_COMPLETE_CLOSE, scaleImageView);
                setPreviewStatus(ImageViewerState.STATE_SILENCE, null);
            }
        });
        animator.start();
    }
}
