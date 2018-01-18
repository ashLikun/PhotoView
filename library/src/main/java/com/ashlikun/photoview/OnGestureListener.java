/*******************************************************************************
 * Copyright 2011, 2012 Chris Banes.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.ashlikun.photoview;

import android.view.MotionEvent;

interface OnGestureListener {
    //拖拽
    boolean onDrag(float dx, float dy);

    void onDown(float dx, float dy);

    public void onLongPress(MotionEvent e);

    boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                    float velocityY);

    void onScaleChange(float scaleFactor, float focusX, float focusY);


    boolean onDoubleTap(MotionEvent e);

    /**
     * 单击事件
     */
    void onSingleClick();

    /**
     * view点击事件
     */
    void onViewTap(float x, float y);

    /**
     * 照片点击事件
     *
     * @param x
     * @param y
     */
    void onPhotoTap(float x, float y);

    /**
     * 外围点击事件
     */
    void onOutsidePhotoTap();
}