package com.ashlikun.photoview.simple.look;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.ashlikun.photoview.simple.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/28　10:56
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public class PhotoViewPager extends FrameLayout {
    // PhotoView photoView;
    private ImageAdapter imageAdapter;
    ViewPager viewPager;
    LinearLayout loPageTurningPoint;
    private List<String> pathDatas = new ArrayList<>();
    private ArrayList<ImageView> mPointViews = new ArrayList<ImageView>();
    private int[] page_indicatorId = new int[2];
    private int space = 8;

    public PhotoViewPager(@NonNull Context context) {
        this(context, null);
    }

    public PhotoViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PhotoViewPager(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setData(List<String> lists, PvViewHolderCreator<Holder> holder) {
        if (lists != null && !lists.isEmpty()) {
            pathDatas.clear();
            pathDatas.addAll(lists);
        }
        viewPager.setAdapter(imageAdapter = new ImageAdapter(pathDatas, holder));
        setPageIndicator(new int[]{R.drawable.photo_circle_default, R.drawable.photo_circle_select});
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.pv_fragment_photo, this);
        space = px2dip(space);
        viewPager = findViewById(R.id.PvViewPager);
        loPageTurningPoint = findViewById(R.id.PvLoPageTurningPoint);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < viewPager.getChildCount(); i++) {
                    imageAdapter.hintView(viewPager.getChildAt(i));
                }
                for (int i = 0; i < mPointViews.size(); i++) {
                    mPointViews.get(i).setImageResource(i == position ? page_indicatorId[1] : page_indicatorId[0]);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    /**
     * 底部指示器资源图片
     *
     * @param page_indicatorId
     */
    public void setPageIndicator(int[] page_indicatorId) {
        loPageTurningPoint.removeAllViews();
        mPointViews.clear();
        this.page_indicatorId = page_indicatorId;
        if (imageAdapter == null) {
            return;
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(space, 0, space, 0);
        for (int count = 0; count < imageAdapter.getCount(); count++) {
            // 翻页指示的点
            ImageView pointView = new ImageView(getContext());
            pointView.setImageResource(viewPager.getCurrentItem() == count ? page_indicatorId[1] : page_indicatorId[0]);
            mPointViews.add(pointView);
            loPageTurningPoint.addView(pointView, params);
        }
    }

    public void addPath(List<String> lists) {
        if (lists == null || lists.isEmpty() || imageAdapter == null) {
            return;
        }
        pathDatas.clear();
        pathDatas.addAll(lists);
        imageAdapter.notifyDataSetChanged();
        setPageIndicator(page_indicatorId);
    }

    public int px2dip(float pxValue) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}

