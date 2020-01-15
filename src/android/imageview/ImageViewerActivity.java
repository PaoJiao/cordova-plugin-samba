package net.cloudseat.smbova;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import android.app.Activity;
import android.os.Bundle;

import java.io.IOException;
import net.cloudseat.smbova.R;

public class ImageViewerActivity extends Activity {

    public static String smbPath;
    public static SambaAdapter samba;

    ///////////////////////////////////////////////////////
    // Activity override methods
    ///////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.image_viewer);

        ProgressBar loading = (ProgressBar) findViewById(R.id.loading);
        ImageView imageView = (ImageView) findViewById(R.id.image_view);
        loading.setVisibility(View.VISIBLE);

        try {
            byte[] bytes = samba.readAsByteArray(smbPath);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            imageView.setImageBitmap(bitmap);
        } catch (IOException e) {
            Toast.makeText(ImageViewerActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            loading.setVisibility(View.INVISIBLE);
        }
    }

}
