package net.cloudseat.smbova;

import android.app.Activity;
import android.content.res.Configuration;
import android.util.LruCache;
import android.view.View;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.os.AsyncTask;
import android.os.Bundle;

import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.IOException;
import net.cloudseat.smbova.R;

public class ImageViewerActivity extends Activity {

    /**
     * 图像数据创建接口
     */
    public static ImageCreator imageCreator;
    public interface ImageCreator {
        String getPath();
        byte[] getData() throws IOException;
    }

    /**
     * 图像内存缓存
     */
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

    // 布局控件变量
    private ImageView imageView;
    private ProgressBar loading;
    // 获取图像子线程
    private ImageCreatorTask task;

    /**
     * 覆盖 ImageView 创建方法
     * @param undle savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);
        imageView = (ImageView) findViewById(R.id.image_view);
        loading = (ProgressBar) findViewById(R.id.loading);

        Bitmap bitmap = memoryCache.get(imageCreator.getPath());
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
            loading.setVisibility(View.INVISIBLE);
        } else {
            task = new ImageCreatorTask();
            task.execute(imageCreator);
        }
    }

    /**
     * 覆盖 ImageView 销毁方法
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (task != null) {
            task.cancel(true);
        }
    }

    /**
     * Android 不允许在主线程（UI线程）请求网络，否则抛出 NetworkOnMainThreadException
     * 故开启新的任务线程获取图片和更新UI。
     */
    private class ImageCreatorTask extends AsyncTask<ImageCreator, Integer, Bitmap> {

        // 执行任务前回调：在主线程执行，允许更新UI
        @Override
        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
        }

        // 执行任务：在子线程执行，不允许更新UI
        @Override
        protected Bitmap doInBackground(ImageCreator... params) {
            try {
                ImageCreator imageCreator = params[0];
                byte[] bytes = imageCreator.getData();
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                memoryCache.put(imageCreator.getPath(), bitmap);
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
