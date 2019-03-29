/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.ashlikun.photoview;

import android.animation.ValueAnimator;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.ashlikun.photoview.listener.OnMatrixChangedListener;
import com.ashlikun.photoview.listener.OnOutsidePhotoTapListener;
import com.ashlikun.photoview.listener.OnPhotoTapListener;
import com.ashlikun.photoview.listener.OnScaleChangedListener;
import com.ashlikun.photoview.listener.OnSingleFlingListener;
import com.ashlikun.photoview.listener.OnViewDragListener;
import com.ashlikun.photoview.listener.OnViewTapListener;

/**
 * The component of {@link PhotoView} which does the work allowing for zooming, scaling, panning, etc.
 * It is made public in case you need to subclass something other than {@link ImageView} and still
 * gain the functionality that {@link PhotoView} offers
 */
public class PhotoViewAttacher implements View.OnLayoutChangeListener {
    private static float DEFAULT_MAX_SCALE = 2.5f;
    private static float DEFAULT_GREATER_SCALE = 5f;
    private static float DEFAULT_MIN_SCALE = 0.5f;
    private static int DEFAULT_ZOOM_DURATION = 250;
    private static final int EDGE_NONE = -1;
    private static final int EDGE_LEFT = 0;
    private static final int EDGE_RIGHT = 1;
    private static final int EDGE_BOTH = 2;
    private static int SINGLE_TOUCH = 1;
    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private int mZoomDuration = DEFAULT_ZOOM_DURATION;
    //最小缩放, 会反弹回1
    private float mMinScale = DEFAULT_MIN_SCALE;
    //较大的缩放，会反弹回mMaxScale
    private float mGreaterScale = DEFAULT_GREATER_SCALE;
    //最大缩放
    private float mMaxScale = DEFAULT_MAX_SCALE;
    //在边缘的时候 是否允许父控件拦截触摸事件
    private boolean mAllowParentInterceptOnEdge = true;
    private ImageView mImageView;
    // Gesture Detectors
    private CustomGestureDetector mScaleDragDetector;
    // These are set so we don't keep allocating them on the heap
    private final Matrix mBaseMatrix = new Matrix();
    private final Matrix mDrawMatrix = new Matrix();
    private final Matrix mSuppMatrix = new Matrix();
    private final RectF mDisplayRect = new RectF();
    private final float[] mMatrixValues = new float[9];
    // 监听事件
    private OnMatrixChangedListener mMatrixChangeListener;
    private OnPhotoTapListener mPhotoTapListener;
    private OnOutsidePhotoTapListener mOutsidePhotoTapListener;
    private OnViewTapListener mViewTapListener;
    private View.OnClickListener mOnClickListener;
    private OnLongClickListener mLongClickListener;
    private OnScaleChangedListener mScaleChangeListener;
    private OnSingleFlingListener mSingleFlingListener;
    private OnViewDragListener mOnViewDragListener;


    private int mScrollEdge = EDGE_BOTH;
    private float mBaseRotation;

    private boolean mZoomEnabled = true;
    private ScaleType mScaleType = ScaleType.FIT_CENTER;

    private OnGestureListener onGestureListener = new OnGestureListener() {
        @Override
        public void onSingleClick() {
            if (mOnClickListener != null) {
                mOnClickListener.onClick(mImageView);
            }
        }

        @Override
        public void onViewTap(float x, float y) {
            if (mViewTapListener != null) {
                mViewTapListener.onViewTap(mImageView, x, y);
            }
        }

        @Override
        public void onPhotoTap(float x, float y) {
            if (mPhotoTapListener != null) {
                mPhotoTapListener.onPhotoTap(mImageView, x, y);
            }
        }

        @Override
        public void onOutsidePhotoTap() {
            if (mOutsidePhotoTapListener != null) {
                mOutsidePhotoTapListener.onOutsidePhotoTap(mImageView);
            }
        }

        @Override
        public boolean onDrag(float dx, float dy) {
            if (mOnViewDragListener != null) {
                mOnViewDragListener.onDrag(dx, dy);
            }
            mSuppMatrix.postTranslate(dx, dy);
            checkAndDisplayMatrix();
            if (mAllowParentInterceptOnEdge && !mScaleDragDetector.isScaling()) {
                if (mScrollEdge == EDGE_BOTH
                        || (mScrollEdge == EDGE_LEFT && dx >= 1f)
                        || (mScrollEdge == EDGE_RIGHT && dx <= -1f)) {
                    requestInterceptScaleFinishViewParentTouchEvent(false);
                } else {
                    requestInterceptScaleFinishViewParentTouchEvent(true);
                }
                return false;
            } else {
                requestInterceptScaleFinishViewParentTouchEvent(true);
                return true;
            }
        }

        @Override
        public void onDown(float dx, float dy) {
            requestInterceptTouchEvent(true);
            // 取消之前的Fling
            mScaleDragDetector.cancelFling();
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (mLongClickListener != null) {
                mLongClickListener.onLongClick(mImageView);
            }
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mSingleFlingListener != null) {
                if (getScale() > DEFAULT_MIN_SCALE) {
                    return false;
                }
                if (e1.getPointerCount() > SINGLE_TOUCH
                        || e2.getPointerCount() > SINGLE_TOUCH) {
                    return false;
                }

                return mSingleFlingListener.onFling(e1, e2, velocityX, velocityY);
            }
            return true;
        }

        @Override
        public void onScaleChange(float scaleFactor, float focusX, float focusY) {
            if (mScaleChangeListener != null) {
                mScaleChangeListener.onScaleChange(scaleFactor, focusX, focusY);
            }
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            try {
                float scale = getScale();
                float x = e.getX();
                float y = e.getY();

                if (scale != 1) {
                    setScale(1, x, y, true);
                } else {
                    setScale(getMaximumScale(), x, y, true);
                }
            } catch (ArrayIndexOutOfBoundsException ee) {
            }
            return true;
        }
    };

    public PhotoViewAttacher(ImageView imageView) {
        mImageView = imageView;
        imageView.addOnLayoutChangeListener(this);

        if (imageView.isInEditMode()) {
            return;
        }
        mBaseRotation = 0.0f;
        mScaleDragDetector = new CustomGestureDetector(imageView.getContext(), this, onGestureListener);
    }

    public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangeListener) {
        this.mScaleChangeListener = onScaleChangeListener;
    }

    public void setOnSingleFlingListener(OnSingleFlingListener onSingleFlingListener) {
        this.mSingleFlingListener = onSingleFlingListener;
    }


    public RectF getDisplayRect() {
        checkMatrixBounds();
        return getDisplayRect(getDrawMatrix());
    }

    public boolean setDisplayMatrix(Matrix finalMatrix) {
        if (finalMatrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null");
        }

        if (mImageView.getDrawable() == null) {
            return false;
        }

        mSuppMatrix.set(finalMatrix);
        checkAndDisplayMatrix();

        return true;
    }

    public void setBaseRotation(final float degrees) {
        mBaseRotation = degrees % 360;
        update();
        setRotationBy(mBaseRotation);
        checkAndDisplayMatrix();
    }

    public void setRotationTo(float degrees) {
        mSuppMatrix.setRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    public void setRotationBy(float degrees) {
        mSuppMatrix.postRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    public float getMinimumScale() {
        return mMinScale;
    }

    public float getGreaterScale() {
        return mGreaterScale;
    }

    public float getMaximumScale() {
        return mMaxScale;
    }

    public float getScale() {
        return (float) Math.sqrt((float) Math.pow(getValue(mSuppMatrix, Matrix.MSCALE_X), 2) + (float) Math.pow(getValue(mSuppMatrix, Matrix.MSKEW_Y), 2));
    }

    public ScaleType getScaleType() {
        return mScaleType;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (left != oldLeft || top != oldTop || right != oldRight || bottom != oldBottom) {
            updateBaseMatrix(mImageView.getDrawable());
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        boolean handled = false;

        return handled;
    }

    public boolean onTouch(MotionEvent ev) {
        boolean handled = false;
        if (mZoomEnabled && Util.hasDrawable(mImageView)) {
            switch (ev.getActionMasked()) {
                //离开屏幕的时候恢复图片缩放
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    //恢复全屏大小
                    if (getScale() < 1) {
                        PointF pointF = getTouchFocusPoint(ev);
                        startZoomAnim(getScale(), 1, pointF.x
                                , pointF.y);
                        handled = true;
                    }
                    //限制最大值
                    else if (getScale() > mMaxScale) {
                        PointF pointF = getTouchFocusPoint(ev);
                        startZoomAnim(getScale(), mMaxScale,
                                pointF.x, pointF.y);
                        handled = true;
                    }
                    break;
            }
            if (mScaleDragDetector != null) {
//                //保存上一次缩放或者拖拽
//                boolean wasScaling = mScaleDragDetector.isScaling();
//                boolean wasDragging = mScaleDragDetector.isDragging();
                handled = mScaleDragDetector.onTouchEvent(ev);
//                //是否拖拽或者缩放
//                boolean didntScale = !wasScaling && !mScaleDragDetector.isScaling();
//                boolean didntDrag = !wasDragging && !mScaleDragDetector.isDragging();
//                mBlockParentIntercept = didntScale && didntDrag;
            }
        }
        return handled;
    }

    /**
     * 获取触摸焦点的位置
     */
    private PointF getTouchFocusPoint(MotionEvent ev) {
        PointF point = new PointF();
        final int action = ev.getActionMasked();
        final boolean pointerUp = action == MotionEvent.ACTION_POINTER_UP;
        final int skipIndex = pointerUp ? ev.getActionIndex() : -1;
        final int count = ev.getPointerCount();
        float sumX = 0, sumY = 0;
        final int div = pointerUp ? count - 1 : count;
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) {
                continue;
            }
            try {
                sumX += ev.getX(i);
                sumY += ev.getY(i);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }
        point.set(sumX / div, sumY / div);
        return point;
    }

    //在边缘的时候 是否允许父控件拦截触摸事件
    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAllowParentInterceptOnEdge = allow;
    }

    public void setMinimumScale(float minimumScale) {
        Util.checkZoomLevels(minimumScale, mGreaterScale, mMaxScale);
        mMinScale = minimumScale;
    }

    public void setGreaterScale(float greaterScale) {
        Util.checkZoomLevels(mMinScale, greaterScale, mMaxScale);
        mGreaterScale = greaterScale;
    }

    public void setMaximumScale(float maximumScale) {
        Util.checkZoomLevels(mMinScale, mGreaterScale, maximumScale);
        mMaxScale = maximumScale;
    }

    public void setScaleLevels(float minimumScale, float greaterScale, float maximumScale) {
        Util.checkZoomLevels(minimumScale, greaterScale, maximumScale);
        mMinScale = minimumScale;
        mGreaterScale = greaterScale;
        mMaxScale = maximumScale;
    }

    public void setOnLongClickListener(OnLongClickListener listener) {
        mLongClickListener = listener;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }

    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        mMatrixChangeListener = listener;
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mPhotoTapListener = listener;
    }

    public void setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener mOutsidePhotoTapListener) {
        this.mOutsidePhotoTapListener = mOutsidePhotoTapListener;
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        mViewTapListener = listener;
    }

    public void setOnViewDragListener(OnViewDragListener listener) {
        mOnViewDragListener = listener;
    }

    public void setScale(float scale) {
        setScale(scale, false);
    }

    public void setScale(float scale, boolean animate) {
        setScale(scale,
                (mImageView.getRight()) / 2,
                (mImageView.getBottom()) / 2,
                animate);
    }

    public void setScale(float scale, float focalX, float focalY,
                         boolean animate) {
        // Check to see if the scale is within bounds
        if (scale < mMinScale || scale > mMaxScale) {
            throw new IllegalArgumentException("Scale must be within the range of minScale and maxScale");
        }
        if (animate) {
            startZoomAnim(getScale(), scale,
                    focalX, focalY);
        } else {
            mSuppMatrix.setScale(scale, scale, focalX, focalY);
            checkAndDisplayMatrix();
        }
    }

    /**
     * Set the zoom interpolator
     *
     * @param interpolator the zoom interpolator
     */
    public void setZoomInterpolator(Interpolator interpolator) {
        mInterpolator = interpolator;
    }

    public void setScaleType(ScaleType scaleType) {
        if (Util.isSupportedScaleType(scaleType) && scaleType != mScaleType) {
            mScaleType = scaleType;
            update();
        }
    }

    public boolean isZoomable() {
        return mZoomEnabled;
    }

    public void setZoomable(boolean zoomable) {
        mZoomEnabled = zoomable;
        update();
    }

    public void update() {
        if (mZoomEnabled) {
            // Update the base matrix using the current drawable
            updateBaseMatrix(mImageView.getDrawable());
        } else {
            // Reset the Matrix...
            resetMatrix();
        }
    }

    /**
     * Get the display matrix
     *
     * @param matrix target matrix to copy to
     */
    public void getDisplayMatrix(Matrix matrix) {
        matrix.set(getDrawMatrix());
    }

    /**
     * Get the current support matrix
     */
    public void getSuppMatrix(Matrix matrix) {
        matrix.set(mSuppMatrix);
    }

    protected Matrix getSuppMatrix() {
        return mSuppMatrix;
    }

    private Matrix getDrawMatrix() {
        mDrawMatrix.set(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        return mDrawMatrix;
    }

    public Matrix getImageMatrix() {
        return mDrawMatrix;
    }

    public void setZoomTransitionDuration(int milliseconds) {
        this.mZoomDuration = milliseconds;
    }

    /**
     * Helper method that 'unpacks' a Matrix and returns the required value
     *
     * @param matrix     Matrix to unpack
     * @param whichValue Which value from Matrix.M* to return
     * @return returned value
     */
    private float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }

    /**
     * Resets the Matrix back to FIT_CENTER, and then displays its contents
     */
    private void resetMatrix() {
        mSuppMatrix.reset();
        setRotationBy(mBaseRotation);
        setImageViewMatrix(getDrawMatrix());
        checkMatrixBounds();
    }

    private void setImageViewMatrix(Matrix matrix) {
        mImageView.setImageMatrix(matrix);

        // Call MatrixChangedListener if needed
        if (mMatrixChangeListener != null) {
            RectF displayRect = getDisplayRect(matrix);
            if (displayRect != null) {
                mMatrixChangeListener.onMatrixChanged(displayRect);
            }
        }
    }

    /**
     * 设置imageview的矩阵显示
     */
    protected void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageViewMatrix(getDrawMatrix());
        }
    }

    /**
     * Helper method that maps the supplied Matrix to the current Drawable
     *
     * @param matrix - Matrix to map Drawable against
     * @return RectF - Displayed Rectangle
     */
    private RectF getDisplayRect(Matrix matrix) {
        Drawable d = mImageView.getDrawable();
        if (d != null) {
            mDisplayRect.set(0, 0, d.getIntrinsicWidth(),
                    d.getIntrinsicHeight());
            matrix.mapRect(mDisplayRect);
            return mDisplayRect;
        }
        return null;
    }

    /**
     * 根据ScaleType 跟新矩阵
     *
     * @param drawable - Drawable being displayed
     */
    private void updateBaseMatrix(Drawable drawable) {
        if (drawable == null) {
            return;
        }

        final float viewWidth = Util.getImageViewWidth(mImageView);
        final float viewHeight = Util.getImageViewHeight(mImageView);
        final int drawableWidth = drawable.getIntrinsicWidth();
        final int drawableHeight = drawable.getIntrinsicHeight();

        mBaseMatrix.reset();

        final float widthScale = viewWidth / drawableWidth;
        final float heightScale = viewHeight / drawableHeight;
        //长图浏览开始的位置为0
        boolean isStartToTop = drawableHeight > viewHeight;

        if (mScaleType == ScaleType.CENTER) {
            mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2F,
                    isStartToTop ? 0 : (viewHeight - drawableHeight) / 2F);

        } else if (mScaleType == ScaleType.CENTER_CROP) {
            float scale = Math.max(widthScale, heightScale);
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    isStartToTop ? 0 : (viewHeight - drawableHeight * scale) / 2F);

        } else if (mScaleType == ScaleType.CENTER_INSIDE) {
            float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    isStartToTop ? 0 : (viewHeight - drawableHeight * scale) / 2F);

        } else {
            RectF mTempSrc = new RectF(0, 0, drawableWidth, drawableHeight);
            RectF mTempDst = new RectF(0, 0, viewWidth, viewHeight);

            if ((int) mBaseRotation % 180 != 0) {
                mTempSrc = new RectF(0, 0, drawableHeight, drawableWidth);
            }

            switch (mScaleType) {
                case FIT_CENTER:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER);
                    break;
                case FIT_START:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.START);
                    break;
                case FIT_END:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.END);
                    break;
                case FIT_XY:
                    mBaseMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.FILL);
                    break;
                default:
                    break;
            }
        }
        resetMatrix();
    }

    /**
     * 检测矩阵边界
     *
     * @return
     */
    private boolean checkMatrixBounds() {
        final RectF rect = getDisplayRect(getDrawMatrix());
        if (rect == null) {
            return false;
        }
        final float height = rect.height(), width = rect.width();
        float deltaX = 0, deltaY = 0;
        final int viewHeight = Util.getImageViewHeight(mImageView);
        if (height <= viewHeight) {
            switch (mScaleType) {
                case FIT_START:
                    deltaY = -rect.top;
                    break;
                case FIT_END:
                    deltaY = viewHeight - height - rect.top;
                    break;
                default:
                    deltaY = (viewHeight - height) / 2 - rect.top;
                    break;
            }
        } else if (rect.top > 0) {
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) {
            deltaY = viewHeight - rect.bottom;
        }

        final int viewWidth = Util.getImageViewWidth(mImageView);
        if (width <= viewWidth) {
            switch (mScaleType) {
                case FIT_START:
                    deltaX = -rect.left;
                    break;
                case FIT_END:
                    deltaX = viewWidth - width - rect.left;
                    break;
                default:
                    deltaX = (viewWidth - width) / 2 - rect.left;
                    break;
            }
            mScrollEdge = EDGE_BOTH;
        } else if (rect.left > 0) {
            mScrollEdge = EDGE_LEFT;
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
            mScrollEdge = EDGE_RIGHT;
        } else {
            mScrollEdge = EDGE_NONE;
        }

        // Finally actually translate the matrix
        mSuppMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    /**
     * 检查缩放边界
     *
     * @param scaleFactor 缩放因子
     */
    protected boolean checkScaleBound(float scaleFactor) {
        return (getScale() < getGreaterScale() || scaleFactor < 1f) &&
                (getScale() > getMinimumScale() || scaleFactor > 1f);
    }

    protected ImageView getImageView() {
        return mImageView;
    }


    /**
     * 开始缩放动画
     */
    private void startZoomAnim(float currentZoom, float targetZoom,
                               final float focalX, final float focalY) {
        ValueAnimator autoZoomAnim = ValueAnimator.ofFloat(currentZoom, targetZoom);
        autoZoomAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float deltaScale = ((Float) animation.getAnimatedValue()) / getScale();
                if (checkScaleBound(deltaScale)) {
                    onGestureListener.onScaleChange(deltaScale, focalX, focalY);
                    getSuppMatrix().postScale(deltaScale, deltaScale, focalX, focalY);
                    checkAndDisplayMatrix();
                }
            }
        });
        autoZoomAnim.setDuration(mZoomDuration);
        autoZoomAnim.setInterpolator(mInterpolator);
        autoZoomAnim.start();
    }

    //请求ScaleFinishView的上一级是否能拦截事件
    protected void requestInterceptScaleFinishViewParentTouchEvent(boolean disallowIntercept) {
        ViewParent parent = mImageView.getParent();
        if (parent != null) {
            ViewParent parent2 = parent.getParent();
            if (parent2 != null) {
                parent2.requestDisallowInterceptTouchEvent(disallowIntercept);
            }
        }
    }

    protected void requestInterceptTouchEvent(boolean disallowIntercept) {
        ViewParent parent = mImageView.getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }
}
