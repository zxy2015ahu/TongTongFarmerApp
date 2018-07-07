package com.tongtong.purchaser.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class VedioUtil {
	private Context mContext;
	private final static int SUSSCE = 0;
	private final static int FAIL = 1;
	private CacheHandler cacheHandler;

	public VedioUtil(Context mContext, CacheHandler cacheHandler) {
		this.mContext = mContext;
		this.cacheHandler = cacheHandler;
	}

	public void cache(String urlName) {
		String path = mContext.getExternalCacheDir() + urlName;
		File file = new File(path);
		if (file.exists()) {
			if (cacheHandler != null) {
				cacheHandler.existsCache(path);
			}
		} else {
			CacheTask cacheTask = new CacheTask();

			cacheTask.execute(urlName);
		}
	}

	class CacheTask extends AsyncTask<String, Integer, Integer> {

		private String path;

		@Override
		protected void onPreExecute() {
			if (cacheHandler != null) {
				cacheHandler.start();
			}

			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (result == SUSSCE) {
				if (cacheHandler != null) {
					cacheHandler.sussce(path);
				}
			} else {
				if (cacheHandler != null) {
					cacheHandler.fail();
				}
			}
			super.onPostExecute(result);
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			if (cacheHandler != null) {
				cacheHandler.update(values[0]);
			}
			super.onProgressUpdate(values);
		}

		@Override
		protected Integer doInBackground(String... params) {
			int code = FAIL;
			String urlStr = UrlUtil.VEDIO_SERVER_URL + params[0];
			path = mContext.getExternalCacheDir() + params[0];
			File file = new File(path);
			File dir = file.getParentFile();
			OutputStream output = null;
			try {
				if (!dir.exists()) {
					dir.mkdirs();
				}
				if (!file.exists()) {
					file.createNewFile();
				}
				URL url = new URL(urlStr);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				long total = conn.getContentLength();
				int count = 0;
				int length = 0;
				InputStream input = conn.getInputStream();

				output = new FileOutputStream(file);
				// 读取大文件
				byte[] buffer = new byte[4 * 1024];
				while ((length = input.read(buffer)) != -1) {
					count = count + length;
					output.write(buffer,0,length);
					publishProgress((int) ((count / (float) total) * 100));
				}
				output.flush();
				code = SUSSCE;
			} catch (Exception e) {

				e.printStackTrace();
			}
			return code;

		}

	}

	public interface CacheHandler {
		void sussce(String path);

		void fail();

		void start();

		void existsCache(String path);

		void update(Integer values);
	}
}
