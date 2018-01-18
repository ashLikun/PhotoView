package com.ashlikun.photoview.simple;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ashlikun.glideutils.GlideUtils;

/**
 * 作者　　: 李坤
 * 创建时间: 2017/12/26 10:41
 * 邮箱　　：496546144@qq.com
 * <p>
 * 功能介绍：http://p1.pstatp.com/w439/4d8600040238b0064b38.webp
 * http://p9.pstatp.com/w640/4ea2000aca3f308d6583.webp
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlideUtils.setBaseUrl("");

        findViewById(R.id.photo_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PhotoActivity.class));
            }
        });
    }
}
