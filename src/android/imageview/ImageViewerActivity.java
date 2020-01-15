package net.cloudseat.smbova;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import android.os.AsyncTask;
import android.os.Bundle;

import android.app.Activity;
import java.io.IOException;
import net.cloudseat.smbova.R;

public class ImageViewerActivity extends Activity {

    public static ImageCreator imageCreator;

    private ImageView imageView;
    private ProgressBar loading;

    public interface ImageCreator {
        byte[] getByteArray() throws IOException;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);

        imageView = (ImageView) findViewById(R.id.image_view);
        loading = (ProgressBar) findViewById(R.id.loading);
        new BitmapCreateTask().execute(imageCreator);
    }

    /**
     * Android 不允许在主线程请求网络，否则抛出 NetworkOnMainThreadException
     * 故开启新的任务线程获取图片和更新UI
     */
    private class BitmapCreateTask extends AsyncTask<ImageCreator, Integer, Bitmap> {
        @Override
        protected void onPreExecute() {
            loading.setVisibility(View.VISIBLE);
        }

        @Override
        protected Bitmap doInBackground(ImageCreator... params) {
            try {
                ImageCreator imageCreator = params[0];
                byte[] bytes = imageCreator.getByteArray();
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            imageView.setImageBitmap(bitmap);
            loading.setVisibility(View.INVISIBLE);
        }
    }

}
