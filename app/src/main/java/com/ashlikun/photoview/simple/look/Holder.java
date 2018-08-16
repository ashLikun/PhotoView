package com.ashlikun.photoview.simple.look;

/**
 * Created by Sai on 15/12/14.
 *
 * @param <T> 任何你指定的对象
 */

import android.content.Context;
import android.view.View;

public interface Holder {
    View createView(Context context);

    void hintView(Context context);

    void updateUI(Context context, int position, String data);
}