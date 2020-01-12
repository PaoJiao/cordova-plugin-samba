package net.cloudseat.cordova;

import android.app.Activity;
import android.media.MediaDataSource;
import android.media.MediaPlayer;

import android.os.Bundle;
import android.os.Handler;

import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import android.content.res.Configuration;
import android.content.res.Resources;

public class MediaPlayerActivity extends Activity implements SurfaceHolder.Callback {

    // Accepts external data source
    public static MediaDataSource dataSource;

    // Layout views
    private RelativeLayout mainLayout;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private SeekBar seekBar;
    private TextView position;
    private TextView duration;

    // Media player variables
    private MediaPlayer mediaPlayer;
    private boolean restartOnResume;

    // Update progress in new thread
    private Handler updateProgressHandler = new Handler();
    private Runnable updateProgress = new Runnable() {
        @Override
        public void run() {
            int currentPosition = mediaPlayer.getCurrentPosition();
            seekBar.setProgress(currentPosition);
            position.setText(formatDuration(currentPosition));
            updateProgressHandler.postDelayed(this, 100);
        }
    };

    ///////////////////////////////////////////////////////
    // Activity override methods
    ///////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        console("Activity onCreate");
        initLayoutView();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setScreenOnWhilePlaying(true);
        mediaPlayer.setDataSource(dataSource);
        mediaPlayer.prepareAsync();
        initMediaPlayerListener();
    }

    @Override
    protected void onPause() {
        super.onPause();
        console("Activity onPause");
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            restartOnResume = true;
        } else {
            restartOnResume = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        console("Activity onResume");
        if (restartOnResume) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        console("Activity onDestroy");
        updateProgressHandler.removeCallbacks(updateProgress);
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    /**
     * 转屏回调
     * 在 plugin.xml 中增加 android:configChanges="orientation|screenSize" 配置
     * 可以防止转屏时 activity 重建，改为调用当前方法。
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        String message = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
            ? "屏幕设置为：横屏" : "屏幕设置为：竖屏";
        console(message);
        adjustViewSize();
        // surfaceHolder.setSizeFromLayout();
    }

    ///////////////////////////////////////////////////////
    // SurfaceView override methods
    ///////////////////////////////////////////////////////

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        console("surfaceCreated");
        mediaPlayer.setDisplay(surfaceHolder);
    }

    /**
     * SurfaceView 尺寸变化时触发
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        console("surfaceChanged: " + format + "{" + width + "," + height + "}");
        adjustViewSize();
        // surfaceView.requestLayout();
    }

    /**
     * SurfaceView 销毁时触发
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        console("surfaceDestroyed");
        mediaPlayer.setDisplay(null);
    }

    ///////////////////////////////////////////////////////
    // Layout widget events
    ///////////////////////////////////////////////////////

    public void onPlayOrPause(View v) {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        } else {
            mediaPlayer.start();
        }
    }

    ///////////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////////

    private void initLayoutView() {
        Resources res = getApplication().getResources();
        String pkg = getApplication().getPackageName();
        setContentView(res.getIdentifier("media_layout", "layout", pkg));

        mainLayout = (RelativeLayout) findViewById(res.getIdentifier("main_layout", "id", pkg));
        surfaceView = (SurfaceView) findViewById(res.getIdentifier("video_view", "id", pkg));
        seekBar = (SeekBar) findViewById(res.getIdentifier("seek_bar", "id", pkg));
        position = (TextView) findViewById(res.getIdentifier("position", "id", pkg));
        duration = (TextView) findViewById(res.getIdentifier("duration", "id", pkg));

        surfaceHolder = surfaceView.getHolder();
        // surfaceHolder.setSizeFromLayout();
        surfaceHolder.addCallback(this);
    }

    private void initMediaPlayerListener() {
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                seekBar.setMax(mediaPlayer.getDuration());
                duration.setText(formatDuration(mediaPlayer.getDuration()));
                mediaPlayer.start();
                updateProgressHandler.post(updateProgress);
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {}
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {}
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                console("Media player error.");
                return false;
            }
        });
        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
                adjustViewSize();
            }
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int value = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                value = progress;
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mediaPlayer.seekTo(value);
                position.setText(formatDuration(value));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void adjustViewSize() {
        int viewWidth = mainLayout.getWidth();
        int viewHeight = mainLayout.getHeight();
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        double videoRatio = (float) videoWidth / videoHeight;
        double displayRatio = (float) viewWidth / viewHeight;

        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        if (videoRatio > displayRatio) {
            lp.width = viewWidth;
            lp.height = (int)((double)lp.width / videoRatio);
        } else {
            lp.height = viewHeight;
            lp.width = (int)((double)lp.height * videoRatio);
        }
        surfaceView.setLayoutParams(lp);
    }

    ///////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////

    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        String m = String.format("%02d", minutes %= 60);
        String s = String.format("%02d", seconds %= 60);
        if (hours > 0) {
            return hours + ":" + m + ":" + s;
        }
        return m + ":" + s;
    }

    private void console(String text) {
        Toast.makeText(MediaPlayerActivity.this, text, Toast.LENGTH_LONG).show();
    }

    ///////////////////////////////////////////////////////
    // Inner classes
    ///////////////////////////////////////////////////////

}
