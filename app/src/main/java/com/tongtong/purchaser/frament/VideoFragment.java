package com.tongtong.purchaser.frament;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.githang.statusbar.StatusBarTools;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.NetUtil;
import com.tongtong.purchaser.widget.MyVideoPlayer;

import cn.jzvd.JZUserAction;
import cn.jzvd.JZUtils;
import cn.jzvd.JZVideoPlayer;
import cn.jzvd.JZVideoPlayerStandard;

/**
 * Created by Administrator on 2018-05-12.
 */

public class VideoFragment extends Fragment {
    private MyVideoPlayer player;
    private String video_url,video_thumb;
    private boolean is_loaded=false;
    private int position,index;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.video_fragment,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        video_url=getArguments().getString("video_url");
        video_thumb=getArguments().getString("video_thumb");
        position=getArguments().getInt("defaultIndex",0);
        index=getArguments().getInt("index");
        player=(MyVideoPlayer) view.findViewById(R.id.videoplayer);
//        player.audio_right.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(!location.getBoolean("is_open_volumn",false)){
//                    OpenVolume();
//                }else{
//                    closeVolume();
//                }
//            }
//        });
        player.setUp(video_url, JZVideoPlayerStandard.SCREEN_WINDOW_NORMAL,"");
        DisplayMetrics dm=new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        Glide.with(getActivity()).load(NetUtil.getFullUrl(video_thumb)).into(player.thumbImageView);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            ViewGroup.LayoutParams params=player.topContainer.getLayoutParams();
            if(params!=null){
                if(params instanceof ViewGroup.MarginLayoutParams){
                    ViewGroup.MarginLayoutParams marginLayoutParams=(ViewGroup.MarginLayoutParams) player.topContainer.getLayoutParams();
                    marginLayoutParams.topMargin= StatusBarTools.getStatusBarHeight(getActivity());
                }
            }
        }
        if(position==index) {
            startPlay();
            is_loaded=true;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(player==null){
            return;
        }
        if(isVisibleToUser&&!is_loaded&&position!=index){
            startPlay();
            is_loaded=true;
        }else if(is_loaded&&isVisibleToUser){
            if(player.currentState== JZVideoPlayer.CURRENT_STATE_PAUSE){
                player.goOnPlayOnResume();
            }
        }else{
            if(player.currentState==JZVideoPlayer.CURRENT_STATE_PLAYING){
                player.goOnPlayOnPause();
            }
        }
    }

    private void startPlay(){

        if (!JZUtils.isWifiConnected(getContext()) ) {
            player.showWifiDialog();
            return;
        }

        player.startVideo();
        player.onEvent(JZUserAction.ON_CLICK_START_ICON);
    }

//    private void OpenVolume(){
//        if(player.currentState==JZVideoPlayer.CURRENT_STATE_PLAYING||player.currentState==JZVideoPlayer.CURRENT_STATE_PAUSE) {
//            location.edit().putBoolean("is_open_volumn",true).commit();
//            player.audio_right.setImageResource(R.drawable.zf_fydy_icon_voice);
//            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
//                    AudioManager.ADJUST_SAME,
//                    AudioManager.FLAG_SHOW_UI);
//            jzMediaSystem.mediaPlayer.setVolume(1, 1);
//        }
//    }
//    private void closeVolume(){
//        if(player.currentState==JZVideoPlayer.CURRENT_STATE_PLAYING||player.currentState==JZVideoPlayer.CURRENT_STATE_PAUSE) {
//            location.edit().putBoolean("is_open_volumn",false).commit();
//            player.audio_right.setImageResource(R.drawable.zf_fydy_icon_mute);
//            jzMediaSystem.mediaPlayer.setVolume(0, 0);
//        }
//    }
}
