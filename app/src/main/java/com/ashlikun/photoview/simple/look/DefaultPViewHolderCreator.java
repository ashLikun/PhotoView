package com.ashlikun.photoview.simple.look;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.ashlikun.glideutils.GlideLoad;
import com.ashlikun.glideutils.GlideUtils;
import com.ashlikun.photoview.PhotoView;
import com.ashlikun.photoview.ScaleFinishView;
import com.ashlikun.photoview.simple.R;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;


/**
 * 作者　　: 李坤
 * 创建时间: 2018/1/17　17:37
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public class DefaultPViewHolderCreator implements PvViewHolderCreator<Holder> {
    Activity activity;
    public int height = 0;
    DisplayMetrics displayMetrics = new DisplayMetrics();

    public DefaultPViewHolderCreator(Activity activity) {
        this.activity = activity;
        activity.getWindow().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    }

    @Override
    public Holder createHolder() {
        return new Holder() {
            PhotoView imageView;
            ScaleFinishView finishView;

            @Override
            public View createView(Context context) {
                View view = LayoutInflater.from(context).inflate(R.layout.pv_item_photo, null);
                imageView = view.findViewById(R.id.photo_view);
                finishView = view.findViewById(R.id.scaleView);
                finishView.setOnSwipeListener(new ScaleFinishView.OnSwipeListener() {
                    @Override
                    public void onOverSwipe(boolean isFinish) {
                        if (isFinish) {
                            activity.finish();
                        }
                    }

                    @Override
                    public boolean onSwiping(float offsetY, float alpha) {
                        return false;
                    }
                });
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.finish();
                    }
                });
                return view;
            }

            @Override
            public void hintView(Context context) {
                imageView.update();
            }

            @Override
            public void updateUI(Context context, int position, final String data) {
                final RequestOptions options = new RequestOptions();
                options.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                GlideLoad.with(activity).load(data).options(options).show(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition transition) {
                        if (resource.getIntrinsicHeight() > displayMetrics.heightPixels) {
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        }
                        GlideUtils.show(imageView, data, options);
                    }
                });
            }
        };
    }
}
