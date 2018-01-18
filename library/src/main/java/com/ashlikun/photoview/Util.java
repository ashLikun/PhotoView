package com.ashlikun.photoview;

import android.view.MotionEvent;
import android.view.ViewParent;
import android.widget.ImageView;

class Util {

    static void checkZoomLevels(float minZoom, float greaterZoom,
                                float maxZoom) {
        if (minZoom >= greaterZoom) {
            throw new IllegalArgumentException(
                    "Minimum zoom has to be less than Greater zoom. Call setMinimumZoom() with a more appropriate value");
        } else if (greaterZoom <= maxZoom) {
            throw new IllegalArgumentException(
                    "Maximum zoom has to be less than Greater zoom. Call setMaximumZoom() with a more appropriate value");
        }
    }

    static boolean hasDrawable(ImageView imageView) {
        return imageView.getDrawable() != null;
    }

    static boolean isSupportedScaleType(final ImageView.ScaleType scaleType) {
        if (scaleType == null) {
            return false;
        }
        switch (scaleType) {
            case MATRIX:
                throw new IllegalStateException("Matrix scale type is not supported");
        }
        return true;
    }

    static int getPointerIndex(int action) {
        return (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    }

    public static int getImageViewWidth(ImageView imageView) {
        return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }

    public static int getImageViewHeight(ImageView imageView) {
        return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }

    /**
     * 作者　　: 李坤
     * 创建时间: 2018/1/11 15:50
     * 邮箱　　：496546144@qq.com
     * <p>
     * 方法功能：告诉父控件拦截触摸事件
     *
     * @param disallowIntercept true:不可以拦截
     */
    public static void requestDisallowInterceptTouchEvent(ImageView imageView, boolean disallowIntercept) {
        ViewParent parent = imageView.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }
}
