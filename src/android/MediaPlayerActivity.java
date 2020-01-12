package net.cloudseat.cordova;

import android.app.Activity;
import android.media.MediaDataSource;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MediaPlayerActivity extends Activity implements SurfaceHolder.Callback {

    public static MediaDataSource dataSource;
    private MediaPlayer mediaPlayer;
    private boolean isPaused;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(getApplication().getResources().getIdentifier("media_layout", "layout", getApplication().getPackageName()));
        SurfaceView surfaceView = (SurfaceView) findViewById(getApplication().getResources().getIdentifier("surface", "id", getApplication().getPackageName()));
        surfaceView.getHolder().addCallback(this);

        mediaPlayer = new MediaPlayer();
        isPaused = false;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer.setDisplay(holder);
        if (isPaused) {
            isPaused = false;
            mediaPlayer.start();
        } else {
            mediaPlayer.setDataSource(dataSource);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer.isPlaying() && !isPaused) {
            mediaPlayer.pause();
            isPaused = true;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

}
