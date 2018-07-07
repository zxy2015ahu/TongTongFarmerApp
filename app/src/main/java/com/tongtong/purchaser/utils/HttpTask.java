package com.tongtong.purchaser.utils;


		import android.content.Context;
import android.os.AsyncTask;

import com.tongtong.purchaser.application.MyApplication;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Response;


public class HttpTask extends AsyncTask<String, Integer, String> {

	public static int SOTIMEOUT = 60*1000;

	private HttpTaskHandler taskHandler;

	private int code;
	private Context context;

	public HttpTask(Context context){
		this.code = 0;
		this.context=context;
	}

	public HttpTask(int code,Context context){
		this.code = code;
		this.context=context;
	}

	@Override
	protected void onPreExecute() {
		if(taskHandler!=null){
			taskHandler.taskStart(code);
		}
	}

	@Override
	protected String doInBackground(String... params) {
		FormBody.Builder rb = new FormBody.Builder();
		if (params.length > 1 && params[1] != null) {
			rb.add("data", params[1]);
			rb.add("sign",AppData.encode(params[1]));
		}
		OkHttpClient client = MyApplication.instance.getClient();
		final okhttp3.Request request = new okhttp3.Request.Builder()
				.url(params[0])
				.post(rb.build())
				.build();
		Call call = client.newCall(request);

		try {
			Response res = call.execute();
			if (res.code() == 200) {
				return res.body().string();
			}
		} catch (Exception e) {

			return null;
		} finally {
		}
		return null;
	}
	@Override
	protected void onPostExecute(String result) {
		if(result!=null){
			if(taskHandler!=null){
				taskHandler.taskSuccessful(result,code);
			}
		}
		else{
			if(taskHandler!=null){
				taskHandler.taskFailed(code);
			}
		}

	}



	public void setTaskHandler(HttpTaskHandler taskHandler) {
		this.taskHandler = taskHandler;
	}

	public static interface HttpTaskHandler {
		void taskStart(int code);

		void taskSuccessful(String str, int code);

		void taskFailed(int code);
	}


}
