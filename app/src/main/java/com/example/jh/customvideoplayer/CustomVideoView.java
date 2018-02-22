package com.example.jh.customvideoplayer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.VideoView;

/**
 * Created by jinhui on 2018/2/22.
 * Email:1004260403@qq.com
 */

public class CustomVideoView extends VideoView {

    int defaultWidth = 1920;
    int defaultHeight = 1080;

    public CustomVideoView(Context context) {
        super(context);
    }

    public CustomVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int width = getDefaultSize(defaultWidth, widthMeasureSpec);
        int height = getDefaultSize(defaultHeight, heightMeasureSpec);
        Log.d("TAG", "width = " + width + "," + "height = " + height);
        setMeasuredDimension(width, height);

    }
}
