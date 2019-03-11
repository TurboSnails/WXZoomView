<h1>WXZoomView</h1>

![releasesvg] ![apisvg] [![license][licensesvg]][license]

<h2>关于</h2>

类似微信图片放大页的进出动画、图片预览支持图片手势缩放、拖拽等操作，基于ImageViewer微修改，修复了一处崩溃，一处体验


<h2>项目演示</h2>

![demo-simple]  ![demo-custom]  
![demo-land]  ![demo-port]

<h2 id="1">自定义属性</h2>  

| 属性名 | 描述 |    
| :---- | :---- |    
| ivr_show_index | 是否显示图片位置 |  
| ivr_do_enter | 是否开启进场动画 | 
| ivr_do_exit | 是否开启退场动画 |   
| ivr_duration | 进场与退场动画的执行时间 |    
| ivr_do_drag | 是否允许图片拖拽 |    
| ivr_drag_type | 拖拽模式（classic：今日头条效果 | wechat：微信朋友圈效果） |  
 
<h2 id="2">自定义方法</h2>    

| 方法名 | 描述 |
|:----|:----|
| setStartPosition(int position) | 设置开始展示的图片的位置 |
| setImageData(List list) | 设置图片资源 |
| setViewData(List<ViewData> list) | 设置目标 view 的相关数据 |
| setImageLoader(ImageLoader loader) | 设置图片加载类 |
| showIndex(boolean show) | 是否显示图片索引 |
| doDrag(boolean isDo) | 是否允许图片被拖拽 |
| setDragType(@ImageDraggerType int type) | 设置拖拽模式 |
| doEnterAnim(boolean isDo) | 是否开启进场动画 |
| doExitAnim(boolean isDo) | 是否开启退场动画 |
| setDuration(int duration) | 设置打开和关闭的动画执行时间 | 
| setOnImageChangedListener(OnImageChangedListener listener) | 设置图片切换监听 |
| setOnItemClickListener(OnItemClickListener listener) | 设置图片的单击击监听 |
| setOnItemLongClickListener(OnItemLongClickListener listener) | 设置图片的长按击监听 |
| setOnPreviewStatusListener(OnPreviewStatusListener listener) | 设置图片预览状态监听 |
| watch() | 开启图片预览 |
| close() | 关闭图片预览 |
| clear() | 清除所有数据 |
| getViewState() | 获取图片预览器的当前状态 |
| setImageScaleable(boolean scaleable) | 是否允许图片缩放 |  
| isImageScaleable() | 图片是否可缩放 |  
| getImageScale() | 获取图片当前的缩放等级 |  
| setImageMaxScale | 设置图片的最大缩放等级 |  
| getImageMaxScale() | 获取图片的最大缩放等级 |  
| setImageMinScale(float minScale) | 设置图片的最小缩放等级 | 
| getImageMinScale() | 获取图片的最小缩放等级 | 
| getCurrentView() | 获取当前 Item 的视图 |
| getCurrentPosition() | 获取当前图片的位置 |  
| onKeyDown(int keyCode, KeyEvent event) | 返回键监听（使用方法见demo） | 

<h2 id="3">简单示例</h2> 

#### XML 中添加 ImageViewer
```
  <com.liyi.viewer.widget.ImageViewer
        android:id="@+id/imagePreivew"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
```

#### 代码中设置 WXZoomView
```Java
  // 图片浏览的起始位置
  imageViewer.setStartPosition(position);
  // 图片的数据源
  imageViewer.setImageData(mImageList);
  // 目标 View 的位置以及尺寸等信息
  imageViewer.setViewData(mViewDatas);
  // 自定义图片的加载方式
  imageViewer.setImageLoader(new ImageLoader() {
        @Override
        public void displayImage(final int position, Object src, final ImageView view) {
               Glide.with(SimplePreviewActivity.this)
                    .load(src)
                    .into(new SimpleTarget<Drawable>() {
                          @Override
                          public void onLoadStarted(@Nullable Drawable placeholder) {
                                 super.onLoadStarted(placeholder);
                                 view.setImageDrawable(placeholder);
                          }

                          @Override
                          public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                 super.onLoadFailed(errorDrawable);
                                 view.setImageDrawable(errorDrawable);
                          }

                          @Override
                          public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                                 view.setImageDrawable(resource);
                                 mViewDatas.get(position).setImageWidth(resource.getIntrinsicWidth());
                                 mViewDatas.get(position).setImageHeight(resource.getIntrinsicHeight());
                          }
                     });
   }});
   // 开启图片浏览
   imageViewer.watch();
```
<h2 id="5">超巨图解决方案（进退场动画需重写，且不支持微信朋友圈拖拽，今日头条效果仍然支持）</h2>

1. 使用 [SubsamplingScaleImageView](SubsamplingScaleImageView) 代替 PhotoView（推荐）
2. 或者使用 [BigImageView](BigImageView) 代替 ScaleImageView


[releasesvg]: https://img.shields.io/badge/version-2.1.2-brightgreen.svg
[apisvg]: https://img.shields.io/badge/sdk-14+-brightgreen.svg
[licensesvg]: https://img.shields.io/badge/license-Apache--2.0-blue.svg
[license]:http://www.apache.org/licenses/LICENSE-2.0

[AutoGridView]:https://github.com/albert-lii/AutoGridView
[demo-simple]:https://github.com/albert-lii/ImageViewer/blob/new/snapshot/demo_simple.gif
[demo-custom]:https://github.com/albert-lii/ImageViewer/blob/new/snapshot/demo_custom.gif
[demo-land]:https://github.com/albert-lii/ImageViewer/blob/new/snapshot/demo_land.gif
[demo-port]:https://github.com/albert-lii/ImageViewer/blob/new/snapshot/demo_port.gif
[demo-apk]:https://github.com/albert-lii/ImageViewer/blob/new/apk/release/app-release.apk

[SubsamplingScaleImageView]:https://github.com/davemorrissey/subsampling-scale-image-view
[BigImageView]:https://github.com/Piasy/BigImageViewer



