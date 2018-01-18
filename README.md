[![Release](https://jitpack.io/v/ashLikun/PhotoView.svg)](https://jitpack.io/#ashLikun/PhotoView)


PhotoView项目简介
    图片查看
## 使用方法

build.gradle文件中添加:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
并且:

```gradle
dependencies {
    compile 'com.github.ashLikun:PhotoView:{latest version}'
}
```

## 详细介绍

### PhotoView
    可缩放的ImageView，可浏览长图

### ScaleFinishView
    仿微信的下滑销毁activity
```java
 finishView.setOnSwipeListener(new ScaleFinishView.OnSwipeListener() {
                    @Override
                    public void onFinishSwipe() {
                        activity.finish();
                    }

                    @Override
                    public void onOverSwipe() {

                    }

                    @Override
                    public boolean onSwiping(float offsetY, float alpha) {
                        return false;
                    }
                });
```
### PhotoViewPager
    集成PhotoView与ScaleFinishView的ImageView

### 使用
    如果使用glide做为图片加载的引擎，那么只需 viewPager.setData(list, new DefaultPvViewHolderCreator(this));

    加载长图的代码

```java
RequestOptions options = new RequestOptions();
            options.override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
            GlideUtils.show(activity, new SimpleTarget<Drawable>() {
                @Override
                public void onResourceReady(Drawable resource, Transition transition) {
                    //图片的大小大于屏幕的大小
                    if (resource.getIntrinsicHeight() > displayMetrics.heightPixels) {
                        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    }
                    GlideUtils.show(imageView, data, options);
                }
            }, data, options);
```
