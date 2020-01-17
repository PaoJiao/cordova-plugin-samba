package net.cloudseat.smbova;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.LruCache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.util.LinkedList;
import net.cloudseat.smbova.R;

public class ImageViewerActivity extends Activity {

    // 图像数据创建接口
    public static ImageCreator imageCreator;
    public interface ImageCreator {
        int getCurrentIndex();
        int getCount();
        String getPath(int index);
        byte[] getData(int index) throws IOException;
    }

    // 图像内存缓存
    private static LruCache<String, Bitmap> memoryCache;
    static {
        // 应用程序最大可用内存
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // 将最大可用内存的 1/8 作为图像缓存
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    };

    // PinchImageView 缓存
    private LinkedList<PinchImageView> imageViewCache = new LinkedList<PinchImageView>();

    // 布局控件
    private ProgressBar loading;
    private ViewPager viewPager;

    /**
     * 覆盖 ImageView 创建方法
     * @param undle savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);
        loading = (ProgressBar) findViewById(R.id.loading);
        loading.setVisibility(View.INVISIBLE);

        viewPager = (ViewPager) findViewById(R.id.image_pager);
        viewPager.setAdapter(new ImagePagerAdapter());
        viewPager.setCurrentItem(imageCreator.getCurrentIndex());
    }

    ///////////////////////////////////////////////////////
    // 私有方法
    ///////////////////////////////////////////////////////

    /**
     * 加载图像并设置到布局
     * @param ImageView view 目标控件
     */
    private void loadImageSource(ImageView imageView, int position) {
        Bitmap bitmap = memoryCache.get(imageCreator.getPath(position));
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            // 仅当前页面加载时显示loading
            if (viewPager.getCurrentItem() == position) {
                loading.setVisibility(View.VISIBLE);
            }
            new ImageCreatorTask(imageView).execute(position);
        }
    }

    ///////////////////////////////////////////////////////
    // 内部类
    ///////////////////////////////////////////////////////

    /**
     * 图片滑动翻页控件适配器
     */
    private class ImagePagerAdapter extends PagerAdapter {

        // 获取要滑动的控件（图片）数量
        @Override
        public int getCount() {
            return imageCreator.getCount();
        }

        // 官方建议直接返回两个参数相等
        @Override
        public boolean isViewFromObject(View v, Object o) {
            return v == o;
        }

        // 预加载三张要显示的图片进行布局初始化 （通常用于设置缩略图）
        // 需要将显示的 ImageView 加入到 ViewGroup 中，然后作为返回值返回
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PinchImageView piv;
            if (imageViewCache.size() > 0) {
                piv = imageViewCache.remove();
                piv.reset();
            } else {
                piv = new PinchImageView(ImageViewerActivity.this);
            }

            loadImageSource(piv, position);
            container.addView(piv);
            return piv;
        }

        // 由于 PagerAdapter 只缓存三张图片，滑动的图片超出缓存的范围会调用此方法将图片销毁
        // 需要将对应的 ImageView 从 ViewGroup 中移除
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            PinchImageView piv = (PinchImageView) object;
            container.removeView(piv);
            imageViewCache.add(piv);
        }
    }

    /**
     * Android 不允许在主线程（UI线程）请求网络，否则抛出 NetworkOnMainThreadException
     * 故开启新的任务线程获取图片和更新UI。
     */
    private class ImageCreatorTask extends AsyncTask<Integer, Integer, Bitmap> {

        // 当前任务要渲染的图像控件
        private ImageView imageView;
        public ImageCreatorTask(ImageView imageView) {
            this.imageView = imageView;
        }

        // 执行任务：在子线程执行，不允许更新UI
        @Override
        protected Bitmap doInBackground(Integer... params) {
            try {
                int position = params[0];
                byte[] bytes = imageCreator.getData(position);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                memoryCache.put(imageCreator.getPath(position), bitmap);
                return bitmap;
            } catch (IOException e) {
                return null;
            }
        }

        // 任务完成后回调：在主线程执行，允许更新UI
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
            loading.setVisibility(View.INVISIBLE);
        }
    }

}
