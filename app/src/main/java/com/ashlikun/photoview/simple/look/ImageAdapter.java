package com.ashlikun.photoview.simple.look;

import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

/**
 * 作者　　: 李坤
 * 创建时间: 2018/1/12　14:34
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public class ImageAdapter extends PagerAdapter {
    List<String> datas;
    protected PvViewHolderCreator holderCreator;

    public ImageAdapter(List<String> datas, PvViewHolderCreator holderCreator) {
        this.datas = datas;
        this.holderCreator = holderCreator;
    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = getView(position, null, container);
        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        View view = (View) object;
        container.removeView(view);
    }

    protected void hintView(View view) {
        Holder holder = (Holder) view.getTag(view.getId());
        if (holder != null) {
            holder.hintView(view.getContext());
        }
    }

    public View getView(int position, View view, ViewGroup container) {
        Holder holder = null;
        if (view == null) {
            holder = (Holder) holderCreator.createHolder();
            view = holder.createView(container.getContext());
            view.setTag(view.getId(), holder);
        } else {
            holder = (Holder) view.getTag(view.getId());
        }
        if (datas != null && !datas.isEmpty()) {
            holder.updateUI(container.getContext(), position, datas.get(position));
        }
        return view;
    }
}
