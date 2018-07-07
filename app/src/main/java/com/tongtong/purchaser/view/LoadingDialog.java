package com.tongtong.purchaser.view;



import com.tongtong.purchaser.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class LoadingDialog extends Dialog {
	private Context context = null;
	

	public LoadingDialog(Context context) {
		super(context, R.style.LoadingDialog);
		this.context = context;

		setContentView(R.layout.loading_dialog);

		Window window = getWindow();
		WindowManager.LayoutParams lp = getWindow().getAttributes();
		window.setDimAmount(0);
		lp.gravity = Gravity.CENTER;
		window.setAttributes(lp);
		setCancelable(false);
	}

	public void onWindowFocusChanged(boolean hasFocus) {

		ImageView imageView = (ImageView) findViewById(R.id.loadingImageView);
		AnimationDrawable animationDrawable = (AnimationDrawable) imageView
				.getBackground();
		animationDrawable.start();
	}

	public void setMessage(String strMessage) {
		TextView tvMsg = (TextView) findViewById(R.id.id_tv_loadingmsg);

		if (tvMsg != null) {
			tvMsg.setText(strMessage);
		}

	}

	public void setMessage(int id) {
		String msg = context.getResources().getString(id);
		TextView tvMsg = (TextView) findViewById(R.id.id_tv_loadingmsg);

		if (tvMsg != null) {
			tvMsg.setText(msg);
		}

	}
}
