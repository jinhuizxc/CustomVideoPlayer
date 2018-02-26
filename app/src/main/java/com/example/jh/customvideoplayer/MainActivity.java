package com.example.jh.customvideoplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.VideoView;

/**
 * 视频播放：videoView/surfaceview
 * 权限：读卡/联网
 * 方案1：自己制作？
 * 方案2：参考自https://github.com/Fessible/Android-Project/tree/master/VideoViewDemo
 */
public class MainActivity extends AppCompatActivity {

//    private VideoView videoView;
    private static String URL = "http://fairee.vicp.net:83/2016rm/0116/baishi160116.mp4";
    private static int UPDATE = 0;
    private static int UPDATE_Light = 1;
    private CustomVideoView videoView;
    private ImageView imgPlayControl; //暂停
    private SeekBar seekProgress; //进度条
    private SeekBar seekVolum;//音量条
    private ImageView imgScreen; //全屏键
    private TextView txtCurrTime;//当前时间
    private TextView txtTotalTime;//总时间
    private AudioManager audioManager;//音量控制
    private int screenWidth;
    private int screenHeight;
    private RelativeLayout videoLayout;
    private int currPosition;//当前视频播放位置
    private boolean isFullScreen;//是否全屏
    private boolean isLogical;//是否合法
    private int threshold = 53;//滑动的临界值
    private int maxVolum;
    private int curVolum;
    private float brightness;
    private float lastX = 0;
    private float lastY = 0;
    private String TAG = "MainActivity";
    private int touchRang;
    private ImageView imgControl;
    private SeekBar seekControl;
    private FrameLayout includeLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 方案1：
//        way1();
        // 方案2：
        way2();
    }


    /**
     * 重新进入时控制
     */
    @Override
    protected void onResume() {
        super.onResume();
        videoView.seekTo(currPosition);
        seekProgress.setProgress(currPosition);
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeMessages(UPDATE);
        videoView.getCurrentPosition();
        imgPlayControl.setImageResource(R.drawable.play_btn_style);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void way2() {
        initView();
        initData();
        setPlayEvent();
    }

    /**
     * 处理播放事件
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setPlayEvent() {
        /**
         * 控制视频的播放和暂停
         */
        imgPlayControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    imgPlayControl.setImageResource(R.drawable.play_btn_style);
                    videoView.pause();
                    handler.removeMessages(UPDATE);
                } else {
                    imgPlayControl.setImageResource(R.drawable.pause_btn_sylte);
                    videoView.start();
                    handler.sendEmptyMessage(UPDATE);
                }
            }
        });
        /**
         * 播放进度条控制
         */
        seekProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateFormatTime(txtCurrTime, progress);
                // 更新视频界面
                int progress1 = seekBar.getProgress();
                videoView.seekTo(progress1);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeMessages(UPDATE);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                int progress = seekBar.getProgress();
                videoView.seekTo(progress);
                handler.sendEmptyMessage(UPDATE);
            }
        });

        /**
         * 音量条控制
         */
        seekVolum.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, UPDATE);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        /**
         * 播放完毕监听
         */
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                imgPlayControl.setImageResource(R.drawable.play_btn_style);
            }
        });
        /**
         * 屏幕切换
         */
        imgScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isFullScreen) {//全屏
                    imgScreen.setImageResource(R.drawable.enlarge);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                } else {
                    imgScreen.setImageResource(R.drawable.shrink);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        });
        /**
         * 屏幕音量的控制
         */
        videoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN://手指按下

                        //1.按下记录值
                        lastY = event.getY();
                        lastX = event.getX();
                        curVolum = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                        touchRang = Math.min(screenHeight, screenWidth);//screenHeight
                        includeLayout.setVisibility(View.VISIBLE);

                        if (lastX < screenWidth / 2) {
                            imgControl.setImageResource(R.drawable.bright);
                            seekControl.setProgress(curVolum);
                        } else {
                            imgControl.setImageResource(R.drawable.volum);
                            seekControl.setProgress((int) (getWindow().getAttributes().screenBrightness*100));
                        }
                        break;
                    case MotionEvent.ACTION_MOVE://手指移动
                        //2.移动的记录相关值
                        float endY = event.getY();
                        float endX = event.getX();
                        float distanceY = lastY - endY;
                        float distanceX = lastX - endX;
                        float absY = Math.abs(distanceY);
                        float absX = Math.abs(distanceX);
                        if (absX > threshold && absY > threshold) {//斜向滑动
                            if (absX > absY) {
                                isLogical = false;
                            } else {
                                isLogical = true;
                            }
                        } else if (absX < threshold && absY > threshold) {
                            isLogical = true;
                        } else if (absX > threshold && absY < threshold) {
                            isLogical = false;
                        }
                        if (isLogical) {
                            if (endX < screenWidth / 2) {
                                imgControl.setImageResource(R.drawable.bright);
                                //左边屏幕-调节亮度
                                final double FLING_MIN_DISTANCE = 0.5;
                                final double FLING_MIN_VELOCITY = 0.5;
                                seekControl.setMax(100);//设置最大值为100
                                if (distanceY > FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                                    setBrightness(10);
                                }
                                if (distanceY < FLING_MIN_DISTANCE && Math.abs(distanceY) > FLING_MIN_VELOCITY) {
                                    setBrightness(-10);
                                }
                            } else {
                                imgControl.setImageResource(R.drawable.volum);
                                //右边屏幕-调节声音
                                //改变声音 = （滑动屏幕的距离： 总距离）*音量最大值
                                float delta = (distanceY / touchRang) * maxVolum;
                                //最终声音 = 原来的 + 改变声音；
                                int voice = (int) Math.min(Math.max(curVolum + delta, 0), maxVolum);
                                if (delta != 0) {
                                    seekControl.setMax(maxVolum);
                                    seekControl.setProgress(curVolum);
                                    updateVoice(voice);
                                }
                            }
                        }
                        break;
                    case MotionEvent.ACTION_UP://手指离开
                        includeLayout.setVisibility(View.GONE);
                        break;
                }
                return true;
            }
        });
    }

    /**
     * 更新声音
     * @param progress
     */
    private void updateVoice(int progress) {
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, progress, 0);
        seekVolum.setProgress(progress);
        curVolum = progress;
        seekControl.setProgress(progress);
    }


    /**
     *
     * 设置屏幕亮度 lp = 0 全暗 ，lp= -1,根据系统设置， lp = 1; 最亮
     * 屏幕最大亮度为255。
     * 屏幕最低亮度为0。
     * 屏幕亮度值范围必须位于：0～255。
     * @param brightness
     */
    public void setBrightness(float brightness) {

        WindowManager.LayoutParams lp = getWindow().getAttributes();

        lp.screenBrightness = lp.screenBrightness + brightness / 255.0f; //当前亮度+ 滑动值/255
        if (lp.screenBrightness > 1) {
            lp.screenBrightness = 1;
        } else if (lp.screenBrightness < 0.1) {
            lp.screenBrightness = (float) 0.1;
        }
        getWindow().setAttributes(lp);
        seekControl.setProgress((int) (lp.screenBrightness * 100));//以100为值计算
    }


    private void initData() {
        //播放控制条
//        videoView.setMediaController(new MediaController(this));
//        Uri uri = Uri.parse(URL);
////        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.baishi);
//        videoView.setVideoURI(uri);
//        videoView.start();

        //音量初始化
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolum = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        curVolum = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        seekVolum.setMax(maxVolum);
        seekVolum.setProgress(curVolum);

        //获取屏幕的宽度
        WindowManager manager = getWindowManager();
//        screenWidth = manager.getDefaultDisplay().getWidth();
//        screenHeight = manager.getDefaultDisplay().getHeight();
        screenWidth = getResources().getDisplayMetrics().widthPixels;
        screenHeight = getResources().getDisplayMetrics().heightPixels;

        //video初始化
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.baishi);
        videoView.setVideoURI(uri);
        videoView.start();
//        //获取视频当前的播放时间
//        currPosition = videoView.getCurrentPosition();
//        Log.e(TAG, "currPosition = " + currPosition);  // currPosition = 0;
//        //获取视频的总时间
//        int totalDuration = videoView.getDuration();
//        Log.e(TAG, "获取视频的总时间 = " + totalDuration); // 获取视频的总时间 = -1
        handler.sendEmptyMessage(UPDATE);
    }

    /**
     * 通过handler来控制时间
     */
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == UPDATE) {
                //获取视频当前的播放时间
                currPosition = videoView.getCurrentPosition();
                //获取视频的总时间
                int totalDuration = videoView.getDuration();
                //格式化
                updateFormatTime(txtCurrTime, currPosition);
                updateFormatTime(txtTotalTime, totalDuration);
                seekProgress.setMax(totalDuration);
                seekProgress.setProgress(currPosition);
                handler.sendEmptyMessageDelayed(UPDATE, 500);// 设置1000ms时间到14秒就停止了，将时间设置500ms
            }
        }
    };

    /**
     * 更新TextView的时间
     *
     * @param txtCurrTime    文本框
     * @param currPosition 时间
     */
    @SuppressLint("DefaultLocale")
    private void updateFormatTime(TextView txtCurrTime, int currPosition) {
        int second = currPosition / 1000;
        int hh = second / 3600;
        int mm = second % 3600 / 60;
        int ss = second % 60;
        String time = null;
        if (hh != 0) {
            time = String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            time = String.format("%02d:%02d", mm, ss);
        }
        txtCurrTime.setText(time);
    }

    private void initView() {
        videoView = findViewById(R.id.vedioview);
        imgPlayControl = findViewById(R.id.img_play_control);
        seekProgress = findViewById(R.id.seek_progress);
        seekVolum = findViewById(R.id.seek_volum);
        imgScreen = findViewById(R.id.img_screen);
        txtCurrTime = findViewById(R.id.txt_current_time);
        txtTotalTime = findViewById(R.id.txt_total_time);
        videoLayout = findViewById(R.id.vedioview_layout);
        imgControl = findViewById(R.id.img_control);
        seekControl = findViewById(R.id.seek_control);
        includeLayout = findViewById(R.id.include_layout);
    }

    /**
     * 控制屏幕显示的大小
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //横屏
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setVedioViewScale(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            isFullScreen = true;
            //移除半屏状态
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            //添加全屏状态
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            //竖屏
            setVedioViewScale(ViewGroup.LayoutParams.MATCH_PARENT, dp2px(250));
            isFullScreen = false;
            //移除全屏状态
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //添加半屏状态
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        }
    }

    /**
     * dp转化为px
     */
    public int dp2px(int value) {
        final float scale = getResources().getDisplayMetrics().density;
        return (int) (value * scale + 0.5f);
    }

    /**
     * 设置缩放比例
     */
    private void setVedioViewScale(int width, int height) {
        ViewGroup.LayoutParams layoutParams = videoView.getLayoutParams();
        layoutParams.width = width;
        layoutParams.height = height;
        videoView.setLayoutParams(layoutParams);
        // 同时需要设置外层布局
        ViewGroup.LayoutParams layoutParams1 = videoLayout.getLayoutParams();
        layoutParams1.width = width;
        layoutParams1.height = height;
        videoLayout.setLayoutParams(layoutParams1);
    }

    /**
     * 音量键控制
     *
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        int currVolum = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                currVolum = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                currVolum = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                break;
        }
        seekVolum.setProgress(currVolum);

        return super.onKeyDown(keyCode, event);
    }

    ////////////////////////////////////////////////////////////////////////////////

    private void way1() {
        //        videoView = findViewById(R.id.videoview);


        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/123.mp4";
        /**
         * 本地视频播放
         */
        videoView.setVideoPath(path);
        /**
         * 网络视频播放
         */
//        videoView.setVideoURI(Uri.parse(""));

        /**
         * 使用MediaController控制视频播放
         */
        MediaController controller = new MediaController(this);
        /**
         * 设置videoview与mediacontroller建立关联
         */
        videoView.setMediaController(controller);
        /**
         * 设置mediacontroller与videoview建立关联
         */
        controller.setMediaPlayer(videoView);

    }

}
