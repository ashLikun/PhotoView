/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.ashlikun.photoview;

import android.content.Context;
import android.graphics.RectF;
import androidx.core.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.OverScroller;

/**
 * Does a whole lot of gesture detecting.
 */
class CustomGestureDetector {
    private FlingRunnable mCurrentFlingRunnable;
    //缩放手势
    private final ScaleGestureDetector mDetector;
    private final GestureDetectorCompat detectorCompat;
    private OnGestureListener mListener;
    private boolean mIsDragging;
    private PhotoViewAttacher attacher;
    private Context mContext;

    CustomGestureDetector(Context context, final PhotoViewAttacher attacher, OnGestureListener listener) {
        this.attacher = attacher;
        this.mContext = context;
        mListener = listener;
        ScaleGestureDetector.OnScaleGestureListener mScaleListener = new ScaleGestureDetector.OnScaleGestureListener() {

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float scaleFactor = detector.getScaleFactor();
//                if (scaleFactor < 1) {
//                    scaleFactor = scaleFactor * 0.96f;
//                }
//                if (scaleFactor > 1) {
//                    scaleFactor = scaleFactor * 1.04f;
//                }
                if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) {
                    return false;
                }
                if (attacher.checkScaleBound(scaleFactor)) {
                    attacher.getSuppMatrix().postScale(scaleFactor, scaleFactor, detector.getFocusX(), detector.getFocusY());
                    attacher.checkAndDisplayMatrix();
                    mListener.onScaleChange(scaleFactor, detector.getFocusX(), detector.getFocusY());
                }
                return true;
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                return true;
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {

            }
        };
        mDetector = new ScaleGestureDetector(context, mScaleListener);
        detectorCompat = new GestureDetectorCompat(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                mIsDragging = true;
                return mListener.onDrag(-distanceX, -distanceY);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                mListener.onDown(e.getX(), e.getY());
                return true;
            }

            @Override
            public void onLongPress(MotionEvent e) {
                mListener.onLongPress(e);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                mCurrentFlingRunnable = new FlingRunnable(mContext);
                mCurrentFlingRunnable.fling(Util.getImageViewWidth(attacher.getImageView()),
                        Util.getImageViewHeight(attacher.getImageView()), (int) -velocityX, (int) -velocityY);
                attacher.getImageView().post(mCurrentFlingRunnable);
                return mListener.onFling(e1, e2, -velocityX, -velocityY);
            }


            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                mListener.onSingleClick();
                final RectF displayRect = attacher.getDisplayRect();
                final float x = e.getX(), y = e.getY();
                mListener.onViewTap(x, y);
                if (displayRect != null) {
                    //点击的是否是照片内部
                    if (displayRect.contains(x, y)) {
                        float xResult = (x - displayRect.left)
                                / displayRect.width();
                        float yResult = (y - displayRect.top)
                                / displayRect.height();
                        //照片点击事件
                        mListener.onPhotoTap(xResult, yResult);
                        return true;
                    } else {
                        mListener.onOutsidePhotoTap();
                    }
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                return mListener.onDoubleTap(e);
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });
    }

    public boolean isScaling() {
        return mDetector.isInProgress();
    }

    public boolean isDragging() {
        return mIsDragging;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        try {
            //先处理缩放
            mDetector.onTouchEvent(ev);
            mIsDragging = false;
            if (!mDetector.isInProgress()) {
                //再处理普通的手势
                detectorCompat.onTouchEvent(ev);
            }
            return true;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private class FlingRunnable implements Runnable {

        private final OverScroller mScroller;
        private int mCurrentX, mCurrentY;

        public FlingRunnable(Context context) {
            mScroller = new OverScroller(context);
        }

        public void cancelFling() {
            mScroller.forceFinished(true);
        }

        public void fling(int viewWidth, int viewHeight, int velocityX,
                          int velocityY) {
            final RectF rect = attacher.getDisplayRect();
            if (rect == null) {
                return;
            }

            final int startX = Math.round(-rect.left);
            final int minX, maxX, minY, maxY;

            if (viewWidth < rect.width()) {
                minX = 0;
                maxX = Math.round(rect.width() - viewWidth);
            } else {
                minX = maxX = startX;
            }

            final int startY = Math.round(-rect.top);
            if (viewHeight < rect.height()) {
                minY = 0;
                maxY = Math.round(rect.height() - viewHeight);
            } else {
                minY = maxY = startY;
            }

            mCurrentX = startX;
            mCurrentY = startY;

            // If we actually can move, fling the scroller
            if (startX != maxX || startY != maxY) {
                mScroller.fling(startX, startY, velocityX, velocityY, minX,
                        maxX, minY, maxY, 0, 0);
            }
        }

        @Override
        public void run() {
            if (mScroller.isFinished()) {
                return; // remaining post that should not be handled
            }
            if (mScroller.computeScrollOffset()) {
                final int newX = mScroller.getCurrX();
                final int newY = mScroller.getCurrY();
                attacher.getSuppMatrix().postTranslate(mCurrentX - newX, mCurrentY - newY);
                attacher.checkAndDisplayMatrix();
                mCurrentX = newX;
                mCurrentY = newY;
                Compat.postOnAnimation(attacher.getImageView(), this);
            }
        }
    }

    protected void cancelFling() {
        if (mCurrentFlingRunnable != null) {
            mCurrentFlingRunnable.cancelFling();
            mCurrentFlingRunnable = null;
        }
    }
}
