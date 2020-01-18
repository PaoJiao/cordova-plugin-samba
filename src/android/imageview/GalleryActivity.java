package net.cloudseat.smbova;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import java.util.LinkedList;
import net.cloudseat.smbova.R;

public class GalleryActivity extends Activity {

    // 接受外部图像数据源传参
    public static ImageSource imageSource;

    // PinchImageView 缓存
    private LinkedList<PinchImageView> imageViewCache = new LinkedList<PinchImageView>();

    /**
     * 覆盖父类创建方法
     * @param Bundle savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);

        ViewPager viewPager = (ViewPager) findViewById(R.id.image_pager);
        viewPager.setAdapter(new GalleryAdapter());
        viewPager.setCurrentItem(imageSource.currentIndex());
    }

    ///////////////////////////////////////////////////////
    // 内部类
    ///////////////////////////////////////////////////////

    /**
     * 图片滑动翻页控件适配器
     */
    private class GalleryAdapter extends PagerAdapter {

        // 获取要滑动的控件（图片）数量
        @Override
        public int getCount() {
            return imageSource.size();
        }

        // 官方建议直接返回两个参数相等
        @Override
        public boolean isViewFromObject(View v, Object o) {
            return v == o;
        }

        // 预加载需要显示的图片（默认最多三张）进行布局初始化（通常用于设置缩略图）
        // 需要将显示的 ImageView 加入到 ViewGroup 中，然后返回该值
        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            // 创建 PinchImageView
            PinchImageView piv;
            if (imageViewCache.size() > 0) {
                piv = imageViewCache.remove();
                piv.setImageBitmap(null);
                piv.reset();
            } else {
                piv = new PinchImageView(GalleryActivity.this);
            }

            // 加载图像
            imageSource.load(position, new ImageSource.OnImageLoadedListener() {
                @Override
                public void onImageLoaded(Bitmap bitmap) {
                    piv.setImageBitmap(bitmap);
                }
            });

            // 加入到容器并返回
            container.addView(piv);
            return piv;
        }

        // 滑动的图片超出缓存范围（最多三张）会调用此方法将图片销毁
        // 需要将对应的 ImageView 从 ViewGroup 中移除
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            PinchImageView piv = (PinchImageView) object;
            container.removeView(piv);
            imageViewCache.add(piv);
        }
    }

}
