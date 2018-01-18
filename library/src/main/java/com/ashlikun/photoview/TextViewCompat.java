package com.ashlikun.photoview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatTextView;
import android.text.Layout;
import android.util.AttributeSet;

/**
 * 作者　　: 李坤
 * 创建时间:2017/9/2 0002　14:53
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：textView的兼容
 * 1：lineSpacingExtra   兼容
 */

public class TextViewCompat extends AppCompatTextView {
    float mSpacingAdd;

    public TextViewCompat(Context context) {
        this(context, null);
    }

    public TextViewCompat(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TextViewCompat(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs);
    }

    private void initView(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, new int[]{android.R.attr.lineSpacingExtra});
        mSpacingAdd = a.getDimensionPixelSize(0, 0);
        setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), getPaddingBottom());
        a.recycle();

    }

//    @Override
//    public void setPadding(int left, int top, int right, int bottom) {
//        int compatpaddingButton = bottom;
//        if (isNeedCompat()) {
//            compatpaddingButton = (int) (bottom - mSpacingAdd);
//        }
//        super.setPadding(left, top, right, compatpaddingButton);
//    }

//    //手机厂商定制后的bug，原生的只是5.0
//    public boolean isNeedCompat() {
//        Log.e("aaaa", Build.BRAND + "   Build.MODEL=" + Build.MODEL);
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
//            return true;
//        } else if (Build.BRAND != null) {
//            String BRAND = Build.BRAND.toUpperCase();
//            if (BRAND.contains("VIVO") && Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
//                return true;
//            }
//            if (BRAND.contains("OPPO")) {
//                return true;
//            }
//            //m1 note m3 note   m1 metal  M3s  U20  MX5正常
////            if (BRAND.contains("MEIZU") && !"m1 metal".equals(Build.MODEL)) {
////                return true;
////            }
//        }
//
//        return false;
//    }

    @Override
    public void setLineSpacing(float add, float mult) {
        super.setLineSpacing(add, mult);
        mSpacingAdd = add;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredHeight() - calculateExtraSpace());
    }

    //计算需要兼容的底部多余的高度
    public int calculateExtraSpace() {
        int result = 0;
        int lastLineIndex = getLineCount() - 1;

        if (lastLineIndex >= 0) {
            Layout layout = getLayout();
            if (getMeasuredHeight() - getPaddingTop() - getPaddingBottom() == getLayout().getHeight()) {
                Rect mRect = new Rect();
                int baseline = getLineBounds(lastLineIndex, mRect);
                result = mRect.bottom - (baseline + layout.getPaint().getFontMetricsInt().bottom);
            }
        }
        return result;
    }
}
