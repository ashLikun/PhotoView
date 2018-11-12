package com.ashlikun.photoview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/28　10:25
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：仿照微信下拉销毁
 */

public class ScaleFinishView extends FrameLayout {
    /**
     * 滑动到多少地方可以回掉销毁接口
     */
    private float finishElement = 3.5f;

    public ScaleFinishView(@NonNull Context context) {
        this(context, null);
    }

    public ScaleFinishView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScaleFinishView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop() * 2;
    }

    private int mTouchSlop;
    private float mDisplacementX;
    private float mDisplacementY;
    private float mInitialTy;
    private float mInitialTx;
    /**
     * 是否正在移动
     */
    private boolean mTracking;
    /**
     * 父控件的透明度
     */
    private float mParentAlpha;

    public void setFinishElement(float finishElement) {
        this.finishElement = finishElement;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                //偏移量
                float deltaX = event.getRawX() - mDisplacementX;
                float deltaY = event.getRawY() - mDisplacementY;
                if (isTouchToSwipe(deltaX, deltaY)) {
                    setSwiping(deltaX, deltaY);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_POINTER_UP:
                boolean isFinish = false;
                if (mTracking) {
                    mTracking = false;
                    float currentTranslateY = getTranslationY();
                    if (currentTranslateY > getHeight() / finishElement) {
                        if (onSwipeListener != null) {
                            isFinish = true;
                            setParentBackground(0);
                        }
                    }
                }
                if (!isFinish) {
                    setViewDefault();
                }
                if (onSwipeListener != null) {
                    onSwipeListener.onOverSwipe(isFinish);
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        super.onInterceptTouchEvent(event);
        boolean handle = false;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDisplacementX = event.getRawX();
                mDisplacementY = event.getRawY();
                mInitialTy = getTranslationY();
                mInitialTx = getTranslationX();
                break;
            case MotionEvent.ACTION_MOVE:
                //偏移量
                float deltaX = event.getRawX() - mDisplacementX;
                float deltaY = event.getRawY() - mDisplacementY;
                if (isTouchToSwipe(deltaX, deltaY)) {
                    if (deltaY > mTouchSlop * 3) {
                        mDisplacementX = event.getRawX();
                        mDisplacementY = event.getRawY();
                        mInitialTy = getTranslationY();
                        mInitialTx = getTranslationX();
                    }
                    handle = true;
                }
                break;
        }
        return handle;
    }

    public boolean isTouchToSwipe(float offsetX, float offsetY) {
        return offsetY > 0 && Math.abs(offsetY) > mTouchSlop && Math.abs(offsetX) < Math.abs(offsetY)
                || mTracking;
    }


    private void setViewDefault() {
        final float orginTransX = getTranslationX();
        final float orginTransY = getTranslationY();
        final float scaleX = getScaleX();
        final float scaleY = getScaleY();
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                Float value = (Float) animation.getAnimatedValue();
                //恢复默认
                setScaleX(scaleX + (1 - scaleX) * value);
                setScaleY(scaleY + (1 - scaleY) * value);
                setParentBackground(mParentAlpha + (1 - mParentAlpha) * value);
                setTranslationX(orginTransX - orginTransX * value);
                setTranslationY(orginTransY - orginTransY * value);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                setScaleX(1);
                setScaleY(1);
                setParentBackground(1);
                setTranslationX(0);
                setTranslationY(0);
            }
        });
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(200);
        animator.start();
    }

    private void setSwiping(float offsetX, float offsetY) {
        mTracking = true;
        setTranslationY(mInitialTy + offsetY);
        setTranslationX(mInitialTx + offsetX);
        float scale = 1 - offsetY / getWidth();
        if (scale > 1) {
            scale = 1;
        }
        if (scale < 0.1) {
            scale = 0.1f;
        }
        float alpha = scale;
        if (scale < 0.3) {
            scale = 0.3f;
        }
        setScaleX(scale);
        setScaleY(scale);

        if (onSwipeListener != null) {
            if (!onSwipeListener.onSwiping(offsetY, alpha)) {
                setParentBackground(alpha);
            }
        } else {
            setParentBackground(alpha);
        }
    }

    private void setParentBackground(float alpha) {
        View view = (View) getParent();
        mParentAlpha = alpha;
        if (view != null) {
            Drawable parentDrawable = view.getBackground();
            if (parentDrawable != null) {
                //防止改变drawable后影响全局的对应的drawable
                parentDrawable = DrawableCompat.wrap(parentDrawable).mutate();
                parentDrawable.setAlpha((int) (alpha * 255));
                view.setBackground(parentDrawable);
            }
        }
    }

    public interface OnSwipeListener {

        /**
         * 结束
         *
         * @param isFinish 是否可以销毁
         */
        void onOverSwipe(boolean isFinish);

        /**
         * 正在滑动,如果返回true，代表内部不做背景色变化操作
         *
         * @param offsetY
         * @param alpha
         * @return
         */
        boolean onSwiping(float offsetY, float alpha);
    }


    private OnSwipeListener onSwipeListener;

    public void setOnSwipeListener(OnSwipeListener onSwipeListener) {
        this.onSwipeListener = onSwipeListener;
    }

}
