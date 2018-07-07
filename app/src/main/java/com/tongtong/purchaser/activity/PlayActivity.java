package com.tongtong.purchaser.activity;



import com.tongtong.purchaser.R;
import com.tongtong.purchaser.utils.VedioUtil;
import com.tongtong.purchaser.utils.VedioUtil.CacheHandler;
import com.tongtong.purchaser.view.ProgressDialog;

import android.R.integer;
import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

public class PlayActivity extends BaseActivity {
	private VideoView videoView;
	private String videoPath;
	private ProgressDialog progressDialog;
	private int mode;
	public static int HTTP_MODE = 1;
	public static int LOCAL_MODE = 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_play);
		progressDialog = new ProgressDialog(this);
		videoView = (VideoView) findViewById(R.id.video_view);
		videoView.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer arg0) {

				videoView.start();

			}
		});

		videoPath = getIntent().getStringExtra("videoPath");
		mode = getIntent().getIntExtra("mode", -1);
		if (mode == LOCAL_MODE) {
			videoView.setVideoPath(videoPath);
			videoView.start();
		} else if (mode == HTTP_MODE) {
			VedioUtil vedioUtil = new VedioUtil(this,new CacheHandler(){
				
				@Override
				public void sussce(String path) {
					videoView.setVideoPath(path);
					videoView.start();
					progressDialog.dismiss();
				}
				
				@Override
				public void start() {
					progressDialog.show();
					progressDialog.setMessage(PlayActivity.this.getResources().getString(R.string.vedio_loading)+"0%");
				}
				
				@Override
				public void fail() {
					progressDialog.dismiss();
					
				}
				
				@Override
				public void existsCache(String path) {
					videoView.setVideoPath(path);
					videoView.start();
					
				}

				@Override
				public void update(Integer values) {
					progressDialog.setMessage(PlayActivity.this.getResources().getString(R.string.vedio_loading)+values+"%");
					
				}
			});
			
			vedioUtil.cache(videoPath);
		}
		
	}

}
