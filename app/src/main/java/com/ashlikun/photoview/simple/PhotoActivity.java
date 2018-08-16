package com.ashlikun.photoview.simple;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.ashlikun.photoview.simple.look.DefaultPViewHolderCreator;
import com.ashlikun.photoview.simple.look.PhotoViewPager;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/28　10:56
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：
 */

public class PhotoActivity extends AppCompatActivity {
    String lengthImage = "http://p1.pstatp.com/w439/4d8600040238b0064b38.webp";
    String meinvImage = "https://ss3.bdstatic.com/70cFv8Sh_Q1YnxGkpoWK1HF6hhy/it/u=3105304565,1509611228&fm=27&gp=0.jpg";
    PhotoViewPager viewPager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置全屏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_photo2);
        setWindowFullScreen();
        viewPager = findViewById(R.id.viewPager);
        List<String> list = new ArrayList<>();
        list.add(lengthImage);
        list.add(meinvImage);
        list.add(lengthImage);
        list.add(meinvImage);

        viewPager.setData(list, new DefaultPViewHolderCreator(this));
    }

    private void setWindowFullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //5.0以上
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            // 虚拟导航栏透明
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }
}

