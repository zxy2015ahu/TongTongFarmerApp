package com.tongtong.purchaser.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.model.PurchaserModel;
import com.tongtong.purchaser.utils.BitmapUtil;
import com.tongtong.purchaser.utils.CodeUtil;
import com.tongtong.purchaser.utils.UploadFileTask;
import com.tongtong.purchaser.utils.UploadFileTask.HttpTaskHandler;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.view.ClipImageLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ClipImageActivity extends BaseActivity implements OnClickListener, HttpTaskHandler {
	public static final String RESULT_PATH = "crop_image";
	private static final String IMG_NAME = "headIcon.png";
	private static final String KEY = "path";
	private static final int MAX_WIDTH = 600;
	private static final int IMG_WIDTH = 120;

	private ClipImageLayout mClipImageLayout = null;
	private Button okBtn;

	public static void startActivity(Activity activity, String path, int code) {
		Intent intent = new Intent(activity, ClipImageActivity.class);
		intent.putExtra(KEY, path);

		activity.startActivityForResult(intent, code);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.crop_image_layout);
		mClipImageLayout = (ClipImageLayout) findViewById(R.id.clip_image_layout);
		String path = getIntent().getStringExtra(KEY);
		int degreee = 0;
		Bitmap bitmap = null;
		if (path == null) {
			if (getIntent().getData() != null)
				try {
					bitmap = MediaStore.Images.Media.getBitmap(
							this.getContentResolver(), getIntent().getData());
					if (bitmap.getWidth() > MAX_WIDTH) {
						bitmap = BitmapUtil.fitBitmap(bitmap, MAX_WIDTH);
					}
				} catch (FileNotFoundException e) {

					e.printStackTrace();
				} catch (IOException e) {

					e.printStackTrace();
				}
		} else {
			degreee = readBitmapDegree(path);
			bitmap = createBitmap(path);
			if (bitmap.getWidth() > MAX_WIDTH) {
				bitmap = BitmapUtil.fitBitmap(bitmap, MAX_WIDTH);
			}

		}

		if (bitmap != null) {
			if (degreee == 0) {
				mClipImageLayout.setImageBitmap(bitmap);
			} else {
				mClipImageLayout.setImageBitmap(rotateBitmap(degreee, bitmap));
			}
		} else {
			finish();
		}
		okBtn = (Button) findViewById(R.id.okBtn);
		okBtn.setOnClickListener(this);
		findViewById(R.id.cancleBtn).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {

		if (v.getId() == R.id.okBtn) {
			Bitmap bitmap = mClipImageLayout.clip();
			bitmap = BitmapUtil.fitBitmap(bitmap, IMG_WIDTH);
            String cacheImgPath = this.getExternalCacheDir().getPath() + "/" + IMG_NAME;
            File imgFile =  BitmapUtil.saveBitmap(bitmap,cacheImgPath);
            UploadFileTask uploadFileTask = new UploadFileTask(imgFile);
            uploadFileTask.setTaskHandler(this);
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("token", UserUtil.getUserModel(this).getToken());
            uploadFileTask.execute(UrlUtil.UPDATE_HEAD_ICON_URL,requestJson.toString());
           
		} else if (v.getId() == R.id.cancleBtn) {
			finish();
		}

	}

	private Bitmap createBitmap(String path) {
		if (path == null) {
			return null;
		}

		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inSampleSize = 1;
		opts.inJustDecodeBounds = false;
		opts.inPurgeable = true;
		opts.inInputShareable = true;
		opts.inDither = false;
		opts.inPurgeable = true;
		FileInputStream is = null;
		Bitmap bitmap = null;
		try {
			is = new FileInputStream(path);
			bitmap = BitmapFactory.decodeFileDescriptor(is.getFD(), null, opts);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
					is = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return bitmap;
	}

	private int readBitmapDegree(String path) {
		int degree = 0;
		try {
			ExifInterface exifInterface = new ExifInterface(path);
			int orientation = exifInterface.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree = 270;
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return degree;
	}

	private Bitmap rotateBitmap(int angle, Bitmap bitmap) {

		Matrix matrix = new Matrix();
		matrix.postRotate(angle);

		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
				bitmap.getWidth(), bitmap.getHeight(), matrix, false);
		return resizedBitmap;
	}

	@Override
	public void taskStart(int code) {
		showLoading("提交中");
	}

	@Override
	public void taskSuccessful(String str, int code) {
		JsonObject resultJson = new JsonParser().parse(str).getAsJsonObject();
		int resultCode = resultJson.get("code").getAsInt();
		if(verification(resultCode)){
			if(resultCode == CodeUtil.SUCCESS_CODE){
				PurchaserModel purchaser = UserUtil.getUserModel(this);
				String headUrl = resultJson.get("data").getAsJsonObject().get("headUrl").getAsString();
				purchaser.setHeadUrl(headUrl);
				UserUtil.setUserModel(this, purchaser);
				Intent data=new Intent();
				setResult(RESULT_OK,data);
				finish();
			}
			else{
				showToast(R.string.submit_fail);
			}
			
		}
		dismissLoading();
	}

	@Override
	public void taskFailed(int code) {
		dismissLoading();
		showToast(R.string.net_err);
	}

}
