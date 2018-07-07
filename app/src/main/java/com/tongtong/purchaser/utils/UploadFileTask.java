package com.tongtong.purchaser.utils;

import android.os.AsyncTask;

import com.tongtong.purchaser.application.MyApplication;

import java.io.File;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;


public class UploadFileTask extends AsyncTask<String, Integer, String> {


	private HttpTaskHandler taskHandler;
	
	private File uploadFile;
	
	private int code;
	
	public UploadFileTask(File uploadFile){
		this.code = 0;
		this.uploadFile = uploadFile;
	}
	

	@Override
	protected void onPreExecute() {
		if(taskHandler!=null){
			taskHandler.taskStart(code);
		}
	}

	@Override
	protected String doInBackground(String... params) {


		MultipartBody.Builder rb = new MultipartBody.Builder().setType(MultipartBody.FORM);
		if (params.length > 1 && params[1] != null) {
			rb.addFormDataPart("data", params[1]);
		}
		rb.addFormDataPart("file", "file.jpg", RequestBody.create(MediaType.parse("image/png"),uploadFile));
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
