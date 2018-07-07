package com.tongtong.purchaser.utils;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;



import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

public class HttpImageLoadTask extends AsyncTask<String, Integer, Bitmap> {

	public static int SOTIMEOUT = 20 * 1000;
	
	
	private ImageView imgView;
	private int resId;

	public HttpImageLoadTask(ImageView imgView, int resId) {
		this.imgView = imgView;
		this.resId = resId;
	}

	@Override
	protected Bitmap doInBackground(String... params) {

		if (params[0] == null || params[0].equals("")) {
			return null;
		}
	
		Bitmap bitmap = null;
		String imgPath = imgView.getContext().getExternalCacheDir()+params[0];
		File myFile = new File(imgPath);
		if(myFile.exists()){
			bitmap = BitmapFactory.decodeFile(imgPath);
			if(bitmap!=null){
				return bitmap;
			}

		}
		URL myFileURL;
		try {
			myFileURL = new URL(UrlUtil.IMG_SERVER_URL+params[0]);

			HttpURLConnection conn = (HttpURLConnection) myFileURL
					.openConnection();
			conn.setConnectTimeout(SOTIMEOUT);

			conn.setDoInput(true);

			conn.setUseCaches(false);

			InputStream is = conn.getInputStream();

			bitmap = BitmapFactory.decodeStream(is);
			File dir = myFile.getParentFile();
			if(!dir.exists()){
				if(dir.mkdirs()){
					BitmapUtil.saveBitmap(bitmap, imgPath);
				}
			}
			else{
				BitmapUtil.saveBitmap(bitmap, imgPath);
			}
			is.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return bitmap;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if (result != null) {
			imgView.setImageBitmap(result);
		} else {
			imgView.setImageResource(resId);
		}

	}

}
