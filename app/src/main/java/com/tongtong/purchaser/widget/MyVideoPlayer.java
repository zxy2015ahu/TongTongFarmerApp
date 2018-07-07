package com.tongtong.purchaser.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.tongtong.purchaser.R;

import cn.jzvd.JZVideoPlayerStandard;

/**
 * Created by Administrator on 2018-05-12.
 */

public class MyVideoPlayer extends JZVideoPlayerStandard {

    //public ImageView audio_right;
    public MyVideoPlayer(Context context){
        super(context);
    }
    public MyVideoPlayer(Context context, AttributeSet attrs){
        super(context,attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.myvideo_layout;
    }

    @Override
    public void init(Context context) {
        super.init(context);
        //audio_right=(ImageView) findViewById(R.id.right_audio);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
    }

    @Override
    public void setAllControlsVisiblity(int topCon, int bottomCon, int startBtn, int loadingPro, int thumbImg, int bottomPro, int retryLayout) {
        super.setAllControlsVisiblity(topCon, bottomCon, startBtn, loadingPro, thumbImg, bottomPro, retryLayout);
        bottomContainer.setVisibility(View.INVISIBLE);
        bottomProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void setUp(Object[] dataSourceObjects, int defaultUrlMapIndex, int screen, Object... objects) {
        super.setUp(dataSourceObjects, defaultUrlMapIndex, screen, objects);
        backButton.setVisibility(View.GONE);
        titleTextView.setVisibility(View.GONE);
        batteryTimeLayout.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
    }

    @Override
    public void changeStartButtonSize(int size) {

    }

    @Override
    public void onStatePlaying() {
        super.onStatePlaying();
        changeUiToPlayingShow();
        startDismissControlViewTimer();
    }

    @Override
    public void startWindowFullscreen() {
        super.startWindowFullscreen();
    }


    @Override
    public void onStateAutoComplete() {
        super.onStateAutoComplete();
    }

    @Override
    public void startVideo() {
        super.startVideo();
    }
}
