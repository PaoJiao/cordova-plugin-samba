package net.cloudseat.smbova;

import android.app.Activity;
import android.content.res.Configuration;
import android.media.MediaDataSource;
import android.media.MediaPlayer;

import android.os.Bundle;
import android.os.Handler;

import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.cloudseat.smbova.R;

public class MediaPlayerActivity extends Activity {

    // Accepts external data source
    public static MediaDataSource dataSource;
    public static final boolean DEBUG = false;

    // Layout views
    private RelativeLayout mainLayout;
    private ProgressBar loading;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private TableLayout controls;
    private ImageButton playBtn;
    private ImageButton fullscreenBtn;
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
            int pos = mediaPlayer.getCurrentPosition();
            seekBar.setProgress(pos);
            position.setText(formatDuration(pos));
            updateProgressHandler.postDelayed(this, 100);
        }
    };

    ///////////////////////////////////////////////////////
    // Activity override methods
    ///////////////////////////////////////////////////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        debug("Activity onCreate");
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
        debug("Activity onPause");
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
        debug("Activity onResume");
        if (restartOnResume) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        debug("Activity onDestroy");
        updateProgressHandler.removeCallbacks(updateProgress);
        mediaPlayer.stop();
        mediaPlayer.release();
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
        debug(message);
        adjustViewSize();
    }

    ///////////////////////////////////////////////////////
    // Private methods
    ///////////////////////////////////////////////////////

    /**
     * or: setContentView(getResources().getIdentifier("media_player", "layout", getPackageName()));
     * or: findViewById(getResources().getIdentifier("id_in_xml", "id", getPackageName()));
     */
    private void initLayoutView() {
        setContentView(R.layout.media_player);
        mainLayout = (RelativeLayout) findViewById(R.id.main_layout);
        loading = (ProgressBar) findViewById(R.id.loading);
        surfaceView = (SurfaceView) findViewById(R.id.video_view);
        controls = (TableLayout) findViewById(R.id.controls);
        playBtn = (ImageButton) findViewById(R.id.play_btn);
        fullscreenBtn = (ImageButton) findViewById(R.id.fullscreen_btn);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        position = (TextView) findViewById(R.id.position);
        duration = (TextView) findViewById(R.id.duration);

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                debug("surfaceCreated");
                mediaPlayer.setDisplay(surfaceHolder);
            }

            /**
             * SurfaceView 尺寸变化时触发
             */
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                debug("surfaceChanged: " + format + " | " + width + ", " + height);
            }

            /**
             * SurfaceView 销毁时触发
             */
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                debug("surfaceDestroyed");
                mediaPlayer.setDisplay(null);
            }
        });

        playBtn.setImageResource(R.drawable.play);
        playBtn.setVisibility(View.INVISIBLE);
        controls.setVisibility(View.INVISIBLE);
    }

    private void initMediaPlayerListener() {
        // MediaPlayer events
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                seekBar.setMax(mediaPlayer.getDuration());
                duration.setText(formatDuration(mediaPlayer.getDuration()));
                mediaPlayer.start();
                playBtn.setImageResource(R.drawable.pause);
                updateProgressHandler.post(updateProgress);
                loading.setVisibility(View.INVISIBLE);
            }
        });
        mediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mediaPlayer) {
                loading.setVisibility(View.INVISIBLE);
            }
        });
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                playBtn.setImageResource(R.drawable.play);
                playBtn.setVisibility(View.VISIBLE);
                controls.setVisibility(View.VISIBLE);
            }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
                console("Media player error.");
                mediaPlayer.reset();
                return false;
            }
        });
        mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
            @Override
            public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {
                debug("VideoSizeChanged: " + width + ", " + height);
                adjustViewSize();
            }
        });

        // SeekBar events
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {}
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int value = seekBar.getProgress();
                mediaPlayer.seekTo(value);
                position.setText(formatDuration(value));
                loading.setVisibility(View.VISIBLE);
                playBtn.setVisibility(View.INVISIBLE);
            }
        });

        // Toggle show/hide controls
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    if (controls.getVisibility() == View.INVISIBLE) {
                        playBtn.setVisibility(View.VISIBLE);
                        controls.setVisibility(View.VISIBLE);
                    } else {
                        playBtn.setVisibility(View.INVISIBLE);
                        controls.setVisibility(View.INVISIBLE);
                    }
                }
                return true;
            }
        });

        // Toggle play/pause event
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    playBtn.setImageResource(R.drawable.play);
                } else {
                    mediaPlayer.start();
                    playBtn.setImageResource(R.drawable.pause);
                }
            }
        });
    }

    private void adjustViewSize() {
        WindowManager wm = getWindowManager();
        int screenWidth = wm.getDefaultDisplay().getWidth();
        int screenHeight = wm.getDefaultDisplay().getHeight();
        int videoWidth = mediaPlayer.getVideoWidth();
        int videoHeight = mediaPlayer.getVideoHeight();
        double videoRatio = (float) videoWidth / videoHeight;
        double screenRatio = (float) screenWidth / screenHeight;

        ViewGroup.LayoutParams lp = surfaceView.getLayoutParams();
        if (videoRatio > screenRatio) {
            lp.width = screenWidth;
            lp.height = (int)((double)lp.width / videoRatio);
        } else {
            lp.height = screenHeight;
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

    private void debug(String text) {
        if (DEBUG) console(text);
    }

    private void console(String text) {
        Toast.makeText(MediaPlayerActivity.this, text, Toast.LENGTH_LONG).show();
    }

    ///////////////////////////////////////////////////////
    // Inner classes
    ///////////////////////////////////////////////////////

}
