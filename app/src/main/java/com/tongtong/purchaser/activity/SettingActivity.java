package com.tongtong.purchaser.activity;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.mobileim.IYWLoginService;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tongtong.purchaser.R;
import com.tongtong.purchaser.application.MyApplication;
import com.tongtong.purchaser.utils.CommonUtils;
import com.tongtong.purchaser.utils.HttpTask;
import com.tongtong.purchaser.utils.UrlUtil;
import com.tongtong.purchaser.utils.UserUtil;
import com.tongtong.purchaser.widget.AlertDialog;
import com.white.progressview.HorizontalProgressView;

import net.lucode.hackware.magicindicator.buildins.UIUtil;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by zxy on 2018/6/19.
 */

public class SettingActivity extends BaseActivity implements View.OnClickListener{
    private ImageView red_dot;
    private TextView version_tv;
    private JsonObject selectResultJson;
    private String download_url;
    private HorizontalProgressView progress;
    private DownloadManager downloadManager;
    private long downloadId;
    private Timer timer;
    private DownLoadBroadcast downLoadBroadcast;
    private File save_apk_file;
    private Dialog dialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        ((TextView)findViewById(R.id.title_text)).setText("设置");
        findViewById(R.id.back_bn).setOnClickListener(this);
        findViewById(R.id.kefu).setOnClickListener(this);
        findViewById(R.id.about_layout).setOnClickListener(this);
        findViewById(R.id.version).setOnClickListener(this);
        findViewById(R.id.login_bn).setOnClickListener(this);
        red_dot=(ImageView) findViewById(R.id.red_dot);
        version_tv=(TextView) findViewById(R.id.version_layout);
        checkVersion();
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.back_bn){
            onBackPressed();
        }else if(v.getId()==R.id.kefu){
            Intent intent=new Intent();
            intent.setClass(this,PlayWebViewActivity.class);
            intent.putExtra("url",UrlUtil.XIEYI_URL);
            startActivity(intent);
        }else if(v.getId()==R.id.about_layout){
            Intent intent=new Intent();
            intent.setClass(this,PlayWebViewActivity.class);
            intent.putExtra("url",UrlUtil.GUANYU_URL);
            startActivity(intent);
        }else if(v.getId()==R.id.version){
            if(red_dot.getVisibility()==View.VISIBLE){
                showAlertDialog();
            }
        }else if(v.getId()==R.id.login_bn){
           AlertDialog alertDialog=new AlertDialog(this).builder();
            alertDialog.setTitle("提示");
            alertDialog.setMsg("确定退出当前账号？");
            alertDialog.setNegativeButton("取消", new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            alertDialog.setPositiveButton("确定", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    IYWLoginService loginService = UserUtil.getIMKitInstance(SettingActivity.this).getLoginService();
                    loginService.logout(null);
                    UserUtil.clearUserModel(SettingActivity.this);
                    MyApplication myApplication = (MyApplication) getApplication();
                    Intent intent=new Intent();
                    intent.setClass(SettingActivity.this,LoginActivity.class);
                    myApplication.startActivity(intent);
                    myApplication.finshALLActivity();
                }
            });
            alertDialog.show();
        }
    }
    private void showAlertDialog(){
        final AlertDialog dialog=new AlertDialog(SettingActivity.this).builder();
        dialog.setCancelable(false);
        dialog.setTitle("新版本更新("+selectResultJson.get("version_name").getAsString()+")");
        dialog.setMsg(selectResultJson.get("desc").getAsString());
        dialog.setPositiveButton("更新", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initdownload();
                showDialog();
            }
        });
        dialog.setNegativeButton("忽略", new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        dialog.show();
    }
    private void initdownload(){
        downloadManager=(DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(download_url));
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setAllowedOverRoaming(true);
        request.setVisibleInDownloadsUi(true);
        save_apk_file=new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "ttsc_purchaser.apk");
        if(save_apk_file.exists()){
            save_apk_file.delete();
        }
        request.setDestinationUri(Uri.fromFile(save_apk_file));
        downloadId = downloadManager.enqueue(request);
    }
    private int[] getBytesAndStatus() {
        int[] bytesAndStatus = new int[]{
                -1, -1, 0
        };
        DownloadManager.Query query = new DownloadManager.Query().setFilterById(downloadId);
        Cursor cursor = null;
        try {
            cursor = downloadManager.query(query);
            if (cursor != null && cursor.moveToFirst()) {
                //已经下载文件大小
                bytesAndStatus[0] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                //下载文件的总大小
                bytesAndStatus[1] = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                //下载状态
                bytesAndStatus[2] = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return bytesAndStatus;
    }
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            progress.setProgress(msg.arg1);
            if(msg.arg2==8){
                timer.cancel();
                dialog.dismiss();
            }
        }
    };
    private void showDialog(){
        dialog=new Dialog(this, R.style.CustomDialog);
        dialog.setContentView(R.layout.dialog_loading_view);
        Window window=dialog.getWindow();
        WindowManager.LayoutParams lp=window.getAttributes();
        DisplayMetrics dm=new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        lp.width=dm.widthPixels- UIUtil.dip2px(this,40f);
        window.setAttributes(lp);
        progress=(HorizontalProgressView) dialog.findViewById(R.id.progress);
        dialog.show();
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                timer.cancel();
            }
        });
        registerBroadcast();
        timer=new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message msg=new Message();
                int[] data=getBytesAndStatus();
                if(data[0]!=-1&&data[1]!=-1&&data[2]!=0){
                    msg.arg1=Math.round((data[0]/(float)data[1])*100);
                    msg.arg2=data[2];
                }
                handler.sendMessage(msg);
            }
        },0,200);
    }
    /**
     * 注册广播
     */
    private void registerBroadcast() {
        /**注册service 广播 1.任务完成时 2.进行中的任务被点击*/
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        registerReceiver(downLoadBroadcast = new DownLoadBroadcast(), intentFilter);
    }
    private class DownLoadBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(intent.getAction())){
                try{
                    CommonUtils.installApk(SettingActivity.this,save_apk_file.getPath());
                }catch (Exception e){

                }
            }else if(DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(intent.getAction())){
                Intent i=new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(downLoadBroadcast!=null){
            unregisterReceiver(downLoadBroadcast);
        }
        if(timer!=null){
            timer.cancel();
        }
    }
    private void checkVersion(){
        HttpTask task=new HttpTask(this);
        task.setTaskHandler(new HttpTask.HttpTaskHandler() {
            @Override
            public void taskStart(int code) {

            }
            @Override
            public void taskSuccessful(String str, int code) {
                selectResultJson = new JsonParser().parse(str)
                        .getAsJsonObject();
                int selectResultCode = selectResultJson.get("code").getAsInt();
                if(verification(selectResultCode)){
                    int version=selectResultJson.get("version").getAsInt();
                    download_url=selectResultJson.get("download_url").getAsString();
                    if(version> CommonUtils.getVersionCode(SettingActivity.this)){
                        version_tv.setText("有新版本");
                        red_dot.setVisibility(View.VISIBLE);
                    }else{
                        version_tv.setText("已是最新版");
                        red_dot.setVisibility(View.GONE);
                    }
                }
            }
            @Override
            public void taskFailed(int code) {

            }
        });
        JsonObject data=new JsonObject();
        data.addProperty("client_type","purchaser");
        task.execute(UrlUtil.CHECK_VERSION,data.toString());
    }
}
